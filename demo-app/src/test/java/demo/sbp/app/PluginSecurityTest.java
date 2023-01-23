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

import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.extern.java.Log;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.SpringBootPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoTestApp.class)
@TestPropertySource(properties = "sbp-demo.security.app-enabled=true")
@AutoConfigureMockMvc
@Rollback
@Log
public class PluginSecurityTest {

    @Autowired
    private MockMvc mvc;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpringBootPluginManager pluginManager;

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
    public void testAppUnauthorized() throws Exception {
        mvc.perform(get("/book/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPluginUnauthorized() throws Exception {
        mvc.perform(get("/author/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAppAuthorized() throws Exception {
        mvc.perform(get("/book/list")
                .header("authorization", "Basic dXNlcjp1c2Vy") // login as user
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testPluginAuthorized() throws Exception {
        mvc.perform(get("/author/list")
                .header("authorization", "Basic dXNlcjp1c2Vy") // login as user
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testAppPermissionCheckByAop() throws Exception {
        mvc.perform(get("/plugin/list")
                .header("authorization", "Basic dXNlcjp1c2Vy") // login as user
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/plugin/list")
                .header("authorization", "Basic YWRtaW46YWRtaW4=") // login as user
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testPluginPermissionCheckByAop() throws Exception {
        mvc.perform(get("/admin/admin")
                .header("authorization", "Basic dXNlcjp1c2Vy") // login as user
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/admin/admin")
                .header("authorization", "Basic YWRtaW46YWRtaW4=")  // login as admin
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
