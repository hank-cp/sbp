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
package org.laxture.sbp.spring.boot;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.SpringBootPluginManager;
import org.laxture.sbp.internal.PluginListableBeanFactory;
import org.laxture.sbp.internal.SpringBootPluginClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base plugin {@link ApplicationContext} bootstrap class like {@link SpringApplication}
 * to initialize environment in spring-boot style.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringBootstrap extends SpringApplication {

    private final static Logger log = LoggerFactory.getLogger(SpringBootstrap.class);

    public final static String BEAN_PLUGIN = "pf4j.plugin";
    public final static String BEAN_IMPORTED_BEAN_NAMES = "sharedBeanNames";

    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    public static final String[] DEFAULT_EXCLUDE_CONFIGURATIONS = {
            "org.laxture.sbp.spring.boot.SbpAutoConfiguration",
            // Spring Web MVC
            "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration",
            "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration",
            // Actuator/JMX
            "org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.audit.AuditEventsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cassandra.CassandraReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cloudfoundry.servlet.CloudFoundryActuatorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.cloudfoundry.reactive.ReactiveCloudFoundryActuatorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.context.properties.ConfigurationPropertiesReportEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.context.ShutdownEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.couchbase.CouchbaseReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchClientHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchJestHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.elasticsearch.ElasticSearchRestHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.jmx.JmxEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.flyway.FlywayEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.influx.InfluxDbHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.info.InfoContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.integration.IntegrationGraphEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jms.JmsHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.jolokia.JolokiaEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.ldap.LdapHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.liquibase.LiquibaseEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.logging.LogFileWebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.management.ThreadDumpEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.Log4J2MetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.amqp.RabbitMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.cache.CacheMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.appoptics.AppOpticsMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.atlas.AtlasMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.datadog.DatadogMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.dynatrace.DynatraceMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.elastic.ElasticMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.ganglia.GangliaMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.graphite.GraphiteMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.humio.HumioMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.influx.InfluxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.jmx.JmxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.kairos.KairosMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.newrelic.NewRelicMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.signalfx.SignalFxMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.statsd.StatsdMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.export.wavefront.WavefrontMetricsExportAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.jdbc.DataSourcePoolMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.jersey.JerseyServerMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.orm.jpa.HibernateMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.client.HttpClientMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.jetty.JettyMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.reactive.WebFluxMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mongo.MongoHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.mongo.MongoReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.neo4j.Neo4jHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.redis.RedisHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.security.reactive.ReactiveManagementWebSecurityAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.session.SessionsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.solr.SolrHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.system.DiskSpaceHealthContributorAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.mappings.MappingsEndpointAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.web.servlet.ServletManagementContextAutoConfiguration",
            "org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration",
            "org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration",
            // Spring Security
            "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration",
            "org.springframework.boot.autoconfigure.sendgrid.SendGridAutoConfiguration",
            "org.springframework.boot.autoconfigure.session.SessionAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
            // Spring Cloud
            "org.springframework.cloud.netflix.archaius.ArchaiusAutoConfiguration",
            "org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration",
            "org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration",
            "org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration",
            "org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
            "org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration",
            "org.springframework.cloud.autoconfigure.WritableEnvironmentEndpointAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration",
            "org.springframework.cloud.loadbalancer.config.LoadBalancerCacheAutoConfiguration",
            "org.springframework.cloud.consul.discovery.RibbonConsulAutoConfiguration",
            "org.springframework.cloud.consul.discovery.configclient.ConsulConfigServerAutoConfiguration",
            "org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration",
            "org.springframework.cloud.consul.serviceregistry.ConsulServiceRegistryAutoConfiguration",
            "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration",
            "org.springframework.cloud.consul.discovery.reactive.ConsulReactiveDiscoveryClientConfiguration",
            "org.springframework.cloud.consul.discovery.ConsulCatalogWatchAutoConfiguration",
            "org.springframework.cloud.consul.support.ConsulHeartbeatAutoConfiguration",
    };

    public static final String[] DEFAULT_EXCLUDE_APPLICATION_LISTENERS = {
            "org.springframework.cloud.bootstrap.BootstrapApplicationListener",
            "org.springframework.cloud.bootstrap.LoggingSystemShutdownListener",
            "org.springframework.cloud.context.restart.RestartListener",
    };

    private final SpringBootPlugin plugin;

    private final ApplicationContext mainApplicationContext;

    private final ClassLoader pluginClassLoader;

    private final HashSet<String> sharedBeanNames = new HashSet<>();

    private final HashSet<String> importedBeanNames = new HashSet<>();

    private final Map<String, Object> presetProperties = new HashMap<>();

    private List<String> pluginFirstClasses;

    private List<String> pluginOnlyResources;

    /**
     * Constructor should be the only thing need to take care for this Class.
     * Generally new an instance and {@link #run(String...)} it
     * in {@link SpringBootPlugin#createSpringBootstrap()} method.
     *
     * @param primarySources {@link SpringApplication} that annotated with @SpringBootApplication
     */
    @SuppressWarnings("JavadocReference")
    public SpringBootstrap(SpringBootPlugin plugin,
                           Class<?>... primarySources) {
        super(new DefaultResourceLoader(plugin.getWrapper().getPluginClassLoader()), primarySources);
        this.plugin = plugin;
        this.mainApplicationContext = plugin.getMainApplicationContext();
        this.pluginClassLoader = plugin.getWrapper().getPluginClassLoader();
        Map<String, Object> presetProperties = ((SpringBootPluginManager)
                plugin.getWrapper().getPluginManager()).getPresetProperties();
        if (presetProperties != null) this.presetProperties.putAll(presetProperties);
        this.presetProperties.put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,
                getExcludeConfigurations());
    }

    /**
     * Beans that wanted to be shared from main {@link ApplicationContext}.
     * Note that this method only takes effect before {@link #run(String...)} method.
     */
    public SpringBootstrap addSharedBeanName(String beanName) {
        this.sharedBeanNames.add(beanName);
        return this;
    }

    /**
     * Properties that need to be set when this app is started as a plugin.
     * Note that this method only takes effect before {@link #run(String...)} method.
     */
    public SpringBootstrap addPresetProperty(String name, Object value) {
        this.presetProperties.put(name, value);
        return this;
    }

    @Override
    protected void configurePropertySources(ConfigurableEnvironment environment,
                                            String[] args) {
        super.configurePropertySources(environment, args);
        String[] profiles = ((SpringBootPluginManager)
                plugin.getWrapper().getPluginManager()).getProfiles();
        environment.setActiveProfiles(profiles);
        environment.getPropertySources().addLast(new ExcludeConfigurations());
    }

    @Override
    protected void bindToSpringApplication(ConfigurableEnvironment environment) {
        super.bindToSpringApplication(environment);

        pluginFirstClasses = new ArrayList<>();
        String pluginFirstClassesProp = null;
        int i = 0;
        do {
            pluginFirstClassesProp = getProperties(environment, "plugin.pluginFirstClasses", i++);
            if (pluginFirstClassesProp != null) {
                pluginFirstClasses.add(pluginFirstClassesProp);
            }
        } while (pluginFirstClassesProp != null);

        pluginOnlyResources = new ArrayList<>();
        String pluginOnlyResourcesProp = null;
        i = 0;
        do {
            pluginOnlyResourcesProp = getProperties(environment, "plugin.pluginOnlyResources", i++);
            if (pluginOnlyResourcesProp != null) {
                pluginOnlyResources.add(pluginOnlyResourcesProp);
            }
        } while (pluginOnlyResourcesProp != null);
    }

    /** Override this methods to customize excluded spring boot configuration */
    protected String[] getExcludeConfigurations() {
        return DEFAULT_EXCLUDE_CONFIGURATIONS;
    }

    protected String[] getExcludeApplicationListeners() {
        return DEFAULT_EXCLUDE_APPLICATION_LISTENERS;
    }

    @Override
    public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
        super.setListeners(listeners
                .stream()
                .filter(listener -> !ArrayUtils.contains(
                        getExcludeApplicationListeners(), listener.getClass().getName()))
                .collect(Collectors.toList()));
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        setWebApplicationType(WebApplicationType.NONE);
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
//        applicationContext.setParent(mainApplicationContext);
        hackBeanFactory(applicationContext);
        applicationContext.setClassLoader(pluginClassLoader);

        applicationContext.getBeanFactory().registerSingleton(BEAN_PLUGIN, plugin);
        applicationContext.getBeanFactory().autowireBean(plugin);

        if (!CollectionUtils.isEmpty(sharedBeanNames)) {
            for (String beanName : sharedBeanNames) {
                registerBeanFromMainContext(applicationContext, beanName);
            }
        }

        return applicationContext;
    }

    @Override
    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
        context.getBeanFactory().registerSingleton(BEAN_IMPORTED_BEAN_NAMES, importedBeanNames);
    }

    private void hackBeanFactory(ApplicationContext applicationContext) {
        if (pluginClassLoader instanceof SpringBootPluginClassLoader) {
            if (pluginFirstClasses != null) {
                ((SpringBootPluginClassLoader) pluginClassLoader).setPluginFirstClasses(pluginFirstClasses);
            }
            if (pluginOnlyResources != null) {
                ((SpringBootPluginClassLoader) pluginClassLoader).setPluginOnlyResources(pluginOnlyResources);
            }
        }

        BeanFactory beanFactory = new PluginListableBeanFactory(pluginClassLoader);
        Field beanFactoryField = ReflectionUtils.findField(
                applicationContext.getClass(), "beanFactory");
        if (beanFactoryField != null) {
            beanFactoryField.setAccessible(true);
            ReflectionUtils.setField(beanFactoryField, applicationContext, beanFactory);
        }
    }

    protected void registerBeanFromMainContext(AbstractApplicationContext applicationContext,
                                               String beanName) {
        try {
            Object bean = mainApplicationContext.getBean(beanName);
            applicationContext.getBeanFactory().registerSingleton(beanName, bean);
            applicationContext.getBeanFactory().autowireBean(bean);
            importedBeanNames.add(beanName);
            log.info("Bean {} is registered from main ApplicationContext", beanName);

        } catch (NoSuchBeanDefinitionException ex) {
            log.warn("Bean {} is not found in main ApplicationContext", beanName);
        }
    }

    protected void registerBeanFromMainContext(AbstractApplicationContext applicationContext,
                                               Class<?> beanClass) {
        try {
            Map<String, ?> beans = mainApplicationContext.getBeansOfType(beanClass);
            for (String beanName : beans.keySet()) {
                if (applicationContext.containsBean(beanName)) continue;
                Object bean = beans.get(beanName);
                applicationContext.getBeanFactory().registerSingleton(beanName, bean);
                importedBeanNames.add(beanName);
                applicationContext.getBeanFactory().autowireBean(bean);
            }
            log.info("Bean {} is registered from main ApplicationContext", beanClass.getSimpleName());
        } catch (NoSuchBeanDefinitionException ex) {
            log.warn("Bean {} is not found in main ApplicationContext", beanClass.getSimpleName());
        }
    }

    private String getProperties(Environment env, String propName, int index) {
        String prop = env.getProperty(String.format("%s[%s]", propName, index));
        if (prop == null) prop = env.getProperty(String.format("%s.%s", propName, index));
        if (prop == null) prop = env.getProperty(String.format("%s[%s]",
                String.join("-", StringUtils.splitByCharacterTypeCamelCase(propName)).toLowerCase(), index));
        if (prop == null) prop = env.getProperty(String.format("%s.%s",
                String.join("-", StringUtils.splitByCharacterTypeCamelCase(propName)).toLowerCase(), index));
        return prop;
    }

    public class ExcludeConfigurations extends MapPropertySource {
        ExcludeConfigurations() {
            super("Exclude Configurations", presetProperties);
        }
    }
}
