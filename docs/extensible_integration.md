As a powerful full-function framework, Spring Boot integrates many outstanding third party frameworks and libraries, 
such as JPA/Hibernate, Quartz, etc. To use these frameworks/libraries under plugin environment, we need to make them become
dynamically registerable/unregisterable. It could be done by extending `IPluginConfigurer`. It will be invoked during plugin
`ApplicationContext` bootstrap process, so you could register/unregister plugin resources to the main `ApplicationContext`.

### Usage
```java
public class LibraryPlugin extends SpringBootPlugin {

    public LibraryPlugin(PluginWrapper wrapper) {
        super(wrapper, 
          // SbpWebConfigurer is mandatory, so it will be config automatically.        
          new SbpJpaConfigurer(new String[] {"demo.sbp.library.model"}),
          new SbpSpringDocConfigurer()
        );
    }
}
```

----

## Integrations
sbp provides below integrations. You could implement your own `IPluginConfigurer` to integrate other frameworks/libraries, 
or raise an [issue](https://github.com/hank-cp/sbp/issues/new) to request a new integration.

### WebMvc/WebFlux
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-starter:3.1.22'`
* by `SbpWebConfigurer`
* it's mandatory, and it will always be first one in the processing queue.
* Plugin webmvc/webflux controller and router function will be registered to main `ApplicationContext` when plugin is started.
* Unregister when plugin is stopped.

### DataSource
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-starter:3.1.22'`
* by `SbpDataSourceConfigurer`
* Share data source from main `ApplicationContext` to plugin.e
### ~~Jta Transaction(deprecated)~~
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-starter:3.1.22'`
* by `SbpJtaConfigurer`
* Share Jta transaction manager from main `ApplicationContext` to plugin.

### Jpa/Hibernate
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-jpa-tarter:3.1.22'`
* by `SbpJpaConfigurer`
* Plugin JPA entity will be registered to main `EntityManagerFactory` when plugin is started.
* Unregister when plugin is stopped.

### [SpringDoc-OpenApi](https://springdoc.org/v2/)
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-springdoc-tarter:3.1.22'`
* by `SbpSpringDocConfigurer`
* Register plugin api doc when plugin is started.
* Unregister when plugin is stopped.

#### Usage by Annotation (@Controller/@RestController)
* Use [GroupedOpenApi](https://springdoc.org/v2/#how-can-i-define-multiple-openapi-definitions-in-one-spring-boot-project) to register app/plugin api doc by scan package.
* example
```
    // main app
    @Bean
    public GroupedOpenApi mainApiDoc() {
        return GroupedOpenApi.builder().group("main")
            .packagesToScan("demo.sbp.app")
            .build();
    }
    
    // plugin
    @Bean
    public GroupedOpenApi adminApiDoc() {
        return GroupedOpenApi.builder().group("admin")
            .packagesToScan("demo.sbp.webflux.admin")
            .build();
    }
```

#### Usage for RouterFunction
* You will need to use [SpringDoc's api](https://springdoc.org/v2/#spring-webfluxwebmvc-fn-with-functional-endpoints) to build the RouterFunction bean, here is the
[official example](https://github.com/springdoc/springdoc-openapi/blob/master/springdoc-openapi-webflux-core/src/test/java/test/org/springdoc/api/app90/HelloRouter.java). 
* [`@RouterOperation\@RouterOperations`](https://springdoc.org/v2/#spring-cloud-function-web-support) doesn't work for sbp.

### Flyway
* Gradle dependencies: `implementation 'org.laxture:sbp-spring-boot-springdoc-tarter:3.0.18'`
* It will be automatically applied when `spring.flyway.enabled` is true.