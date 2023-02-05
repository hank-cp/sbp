/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.sbp.app;

import com.atomikos.datasource.RecoverableResource;
import com.atomikos.icatch.config.Configuration;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import demo.sbp.api.service.BookService;
import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.SpringBootPluginManager;
import org.laxture.sbp.internal.SpringBootPluginClassLoader;
import org.pf4j.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoTestApp.class)
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration")
@AutoConfigureMockMvc
@Rollback
@ActiveProfiles("no_security")
@Log
public class PluginIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private BookService bookService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpringBootPluginManager pluginManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @After
    public void afterTest() {
        ((AtomikosDataSourceBean)dataSource).close();
        SpringBootPlugin plugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-library").getPlugin();
        plugin.releaseAdditionalResources();
    }

    @Test
    public void testApp() throws Exception {
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testPlugin() throws Exception {
        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/shelf/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/plugin/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "demo-plugin-author", "demo-plugin-shelf",
                        "demo-plugin-admin", "demo-plugin-library")));

        mvc.perform(get("/plugin/extensions/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]",
                        containsInAnyOrder("author", "shelf", "admin", "library")));
    }

    @Test
    public void testPluginStartStop() throws Exception {
        pluginManager.stopPlugin("demo-plugin-shelf");

        mvc.perform(get("/shelf/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(get("/plugin/extensions/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]",
                        containsInAnyOrder("author", "admin", "library")));

        pluginManager.startPlugin("demo-plugin-shelf");

        mvc.perform(get("/shelf/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                // verify dependent service is injected back
                .andExpect(jsonPath("$[0].author.name", equalTo("George Orwell")));

        mvc.perform(get("/plugin/extensions/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*]",
                        containsInAnyOrder("author", "shelf", "admin", "library")));
    }

    @Test
    public void testPluginStartStopReleaseDataSource() throws Exception {
        // first access dataSource to trigger xaDataSource init
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        mvc.perform(get("/library/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        List<RecoverableResource> dataSource = new ArrayList<>();
        Enumeration<RecoverableResource> enumeration = Configuration.getResources();
        while (enumeration.hasMoreElements()) {
            dataSource.add(enumeration.nextElement());
        }
        assertThat(dataSource, hasSize(2));

        RecoverableResource jpaDataSource = Configuration.getResource("dataSource-library");
        assertThat(jpaDataSource, notNullValue());
        assertThat(jpaDataSource.isClosed(), is(false));

        pluginManager.stopPlugin("demo-plugin-library");

        jpaDataSource = Configuration.getResource("dataSource-library");
        assertThat(jpaDataSource, nullValue());

        pluginManager.startPlugin("demo-plugin-library");

        jpaDataSource = Configuration.getResource("dataSource-library");
        assertThat(jpaDataSource, notNullValue());
        assertThat(jpaDataSource.isClosed(), is(false));
    }

    @Test
    public void loadClassFromParentFirst() throws Exception {
        // class should be loaded from app and parent plugin first
        SpringBootPlugin authorPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-author").getPlugin();
        assertThat(authorPlugin, notNullValue());

        SpringBootPlugin shelfPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-shelf").getPlugin();
        assertThat(shelfPlugin, notNullValue());

        // Shelf class should be loaded from plugin itself
        Class shelf = shelfPlugin.getWrapper().getPluginClassLoader().loadClass(
                "demo.sbp.shelf.model.Shelf");
        assertThat(shelf.getClassLoader(), equalTo(shelfPlugin.getWrapper().getPluginClassLoader()));

        // Author class should be loaded
        Class author = shelfPlugin.getWrapper().getPluginClassLoader().loadClass(
                "demo.sbp.api.model.Author");
        assertThat(author.getClassLoader(), equalTo(authorPlugin.getWrapper().getPluginClassLoader()));

        Class book = shelfPlugin.getWrapper().getPluginClassLoader().loadClass(
                "demo.sbp.api.model.Book");
        assertThat(book.getClassLoader(), equalTo(this.getClass().getClassLoader()));
    }

    @Test
    public void loadResourceFromPluginFirst() throws IOException {
        // resource should be loaded from plugin first
        // class should be loaded from app and parent plugin first
        SpringBootPlugin authorPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-author").getPlugin();
        assertThat(authorPlugin, notNullValue());

        SpringBootPlugin shelfPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-shelf").getPlugin();
        assertThat(shelfPlugin, notNullValue());

        URL authorRes = authorPlugin.getWrapper().getPluginClassLoader().getResource("res.txt");
        assertThat(authorRes, notNullValue());
        assertThat(authorRes.getPath(), stringContainsInOrder(Arrays.asList("plugins", "demo-plugin-author")));
        List<String> contents = FileUtils.readLines(FileSystems.getDefault().getPath(authorRes.getPath()), true);
        assertThat(contents, hasItem("author"));

        URL shelfRes = shelfPlugin.getWrapper().getPluginClassLoader().getResource("res.txt");
        assertThat(shelfRes, notNullValue());
        contents = FileUtils.readLines(FileSystems.getDefault().getPath(shelfRes.getPath()), true);
        assertThat(contents, hasItem("app"));
    }

    @Test
    public void loadResourceFromPluginOnly() throws IOException {
        // resource should be loaded from plugin first
        // class should be loaded from app and parent plugin first
        SpringBootPlugin authorPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-author").getPlugin();
        assertThat(authorPlugin, notNullValue());

        SpringBootPlugin shelfPlugin = (SpringBootPlugin)
                pluginManager.getPlugin("demo-plugin-shelf").getPlugin();
        assertThat(shelfPlugin, notNullValue());

        List<URL> urls = Collections.list(
                authorPlugin.getWrapper().getPluginClassLoader().getResources("plugin_only"));
        log.info(urls.toString());
        if (urls.size() > 1) {
            // there might be two build folder, IDEA and gradle
            assertThat(urls, hasSize(2));
            assertThat(urls.stream().map(URL::toString).collect(Collectors.toList()), contains(
                containsString("/plugins/demo-plugin-author/out/production/resources/plugin_only"),
                containsString("/plugins/demo-plugin-author/build/resources/main/plugin_only")));
        } else {
            assertThat(urls, hasSize(1)); // resource form plugin only
        }

        urls = Collections.list(
                shelfPlugin.getWrapper().getPluginClassLoader().getResources("plugin_only"));
        log.info(urls.toString());
        assertThat(urls, hasSize(1)); // resources from app
    }

    @Test
    public void loadLoadClassesFromPlugin() throws Exception {
        SpringBootPlugin authorPlugin = (SpringBootPlugin)
            pluginManager.getPlugin("demo-plugin-author").getPlugin();
        assertThat(authorPlugin, notNullValue());
        SpringBootPluginClassLoader authorClassLoader = (SpringBootPluginClassLoader)
            authorPlugin.getWrapper().getPluginClassLoader();

        SpringBootPlugin shelfPlugin = (SpringBootPlugin)
            pluginManager.getPlugin("demo-plugin-shelf").getPlugin();
        SpringBootPluginClassLoader shelfClassLoader = (SpringBootPluginClassLoader)
            shelfPlugin.getWrapper().getPluginClassLoader();
        assertThat(authorClassLoader, notNullValue());
        assertThat(shelfClassLoader, notNullValue());
        assertThat(shelfClassLoader.loadClass("demo.sbp.api.model.Author"),
            sameInstance(authorClassLoader.loadClass("demo.sbp.api.model.Author")));
        assertThat(exceptionOf(() -> shelfClassLoader.loadClass("demo.sbp.author.DontLoadMe")),
            instanceOf(ClassNotFoundException.class));
    }

    @Test
    @Transactional
    public void testAppService() throws Exception {
        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        JsonNode response = objectMapper.readTree(mvc.perform(get("/author/new")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(response, notNullValue());
        assertThat(response.get("id").asLong(), not(0L));

        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // test api insert two new books cascade
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mvc.perform(get("/author/delete/"+response.get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(0)));

        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testAppServiceFailedAndRollback() throws Exception {
        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        try {
            mvc.perform(get("/author/failed")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        } catch (Exception ignored) {}

        // authors should be rollback to 2
        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // books cascade saving should be rollback to 3
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testAppServiceFailedAndRollbackWithJpa() throws Exception {
        mvc.perform(get("/library/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        try {
            mvc.perform(get("/library/failed")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        } catch (Exception ignored) {}

        // libraries should be rollback to 0
        mvc.perform(get("/library/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // books cascade saving should be rollback to 3
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @Transactional
    public void testPluginService() throws Exception {
        mvc.perform(get("/shelf/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                // books should be loaded by bookService
                .andExpect(jsonPath("$[0].books", hasSize(2)))
                // book.name should be loaded by bookService
                .andExpect(jsonPath("$[0].books[0].name", equalTo("1984")))
                // author should be loaded by authorService
                .andExpect(jsonPath("$[0].author", notNullValue()))
                .andExpect(jsonPath("$[0].author.name", equalTo("George Orwell")));

        mvc.perform(get("/shelf/test-plugin-service")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", equalTo("plugin")));
    }

    @Test
//    @Transactional
    public void testJpaCompatibility() throws Exception {
        /*
        JpaTransactionManager does not support
        running within DataSourceTransactionManager if told to manage the DataSource itself.
        It is recommended to use a single JpaTransactionManager for all transactions
        on a single DataSource, no matter whether JPA or JDBC access.

        If @Transactional is annotated, Spring Test will start a transaction by
        DataSourceTransactionManager to rollback test data, which is conflict with
        JPATransactionManager. So in this test case we have to rollback test data manually.
         */

        // no data initially
        mvc.perform(get("/library/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // insert data
        JsonNode response = objectMapper.readTree(mvc.perform(get("/library/new")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray());
        assertThat(response, notNullValue());
        assertThat(response.get("id").asLong(), not(0L));

        mvc.perform(get("/library/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(get("/library/books/"+response.get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // new book insert cascade
        ArrayNode books = (ArrayNode) objectMapper.readTree(
                mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andReturn().getResponse().getContentAsByteArray());

        // rollback test data since @Transactional couldn't be applied to this test
        mvc.perform(get("/library/delete/"+response.get("id").asLong())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        bookService.deleteBook(books.get(books.size()-1).get("id").asLong());
    }

    @Test
    public void testPluginProfile() throws Exception {
        mvc.perform(get("/admin/user")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/admin/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", equalTo(true)));
    }

    @Test
    public void testResourceHandler() throws Exception {
        mvc.perform(get("/public/foo.html") // served by demo-app
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andDo(result -> {
                    assertThat(result.getResponse().getContentAsString(), equalTo("<body>\nHello foo!\n</body>"));
                });

        mvc.perform(get("/public/bar.html") // served by demo-plugin-admin
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andDo(result -> {
                    assertThat(result.getResponse().getContentAsString(), equalTo("<body>\nHello bar!\n</body>"));
                });

        // stop demo-plugin-admin
        pluginManager.stopPlugin("demo-plugin-admin");

        // bar.html should be not found after plugin stop.
        mvc.perform(get("/public/bar.html")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound());

        // start demo-plugin-admin again
        pluginManager.startPlugin("demo-plugin-admin");

        // bar.html should come back to live again.
        mvc.perform(get("/public/bar.html")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andDo(result -> {
                    assertThat(result.getResponse().getContentAsString(), equalTo("<body>\nHello bar!\n</body>"));
                });
    }

    public interface Runnable {
        public abstract void run() throws Exception;
    }

    private static Throwable exceptionOf(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }
}
