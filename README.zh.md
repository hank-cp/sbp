pf4j-spring-boot为Spring Boot带来面向插件开发设计的能力. 它受到开源项目[Pf4j](https://pf4j.org/)的启发, 并构建在其基础之上. 

### 为什么我们需要插件化的Spring Boot?
* Spring boot很好，但它是单体架构。这意味着每次代码修改您必须交付整个应用程序.
* 我们需要一个具有灵活性和可扩展性的现代框架，可以快速为复杂的业务场景提供灵活解决方案. 
* 并非所有项目都需要在开始阶段考虑线性扩展问题，就像Spring Cloud一样。
* 使用pf4j-spring-boot，我们在仅使用Spring Boot的前提下以微服务架构的方式来考虑问题，
但不必过多花费心思在“云原生”问题上，如服务发现，流量控制等.
* 它是一个介于单体Spring Boot应用和分布式Spring Cloud架构之间的中间过渡选择方案.

### Feature / Benefits
* 使单体Spring Boot应用支持**插件化**.
* 一个开发部署更加**轻量简单**的螺旋扩展架构.
* **在线** 安装/升级/启动/停止插件.
* 由Spring Boot提供**全栈**Web/Rest后台服务, 包括:
    * 处理请求的Controller
    * 各种持久化工具 (Spring Data/JPA/Jooq/Plain SQL)
    * Spring Security
    * AOP
    * 静态资源加载
* 在**独立可执行的**Spring Boot工程中开发调试测试插件.
* 只需熟悉Spring Boot, **无需要学习**其它框架知识. 
* **NO XML**

##### vs OSGi Based Server ([Eclipse Virgo](https://www.eclipse.org/virgo/) \ [Apache Karaf](http://karaf.apache.org/))
* OSGi based server need a server container to deploy, which is not _cloud friendly_.
* OSGi configuration is complex and strict, the learning curve is steep.
* Bundle cannot be run standalone, which is hard for debug and test.
* ~~Spring dm server is dead, and its successor Virgo is now still struggle to live.~~ 

##### vs Spring Cloud
* Spring Cloud does not aim to solve similar problem as pf4j-spring-boot, but so far it maybe the 
best choice to solve application extension problem.
* With pf4j-spring-boot, you will no need to consider too much about computer resource arrangement, 
everything stick tight within one process.
* Again, pf4j-spring-boot is good for medium level applications, which have problem to handle business
change rapidly. For large scale application, Spring Cloud is still your best choice.
* pf4j-spring-boot should be able to live with Spring Cloud. After all, it is still a Spring Boot
application, just like any single service provider node in Spring Cloud network.

### Getting Start

##### Create app project
1. Create a Spring Boot project with multi sub project structure.
    1. For Gradle, it means [multiple projects](https://docs.gradle.org/current/userguide/intro_multi_project_builds.html)
    2. For Maven, it means [multiple modules](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
    3. Take the demo projects for reference.
2. Introduce `pf4j-spring-boot-starter` to dependencies.
    * Maven
        ```
        TBD
        ```
    * Gradle
        ```
        TBD
        ```
4. Add belows to `application.properties`.
    ```
    spring.pf4j.runtimeMode = development
    spring.pf4j.enabled = true
    # remember to add this line in case you are using IDEA
    spring.pf4j.classes-directories = "out/production/classes, out/production/resources"
    ``` 
4. Add anything you want in this project like `Controller`, `Service`, `Repository`, `Model`, etc.
3. Create an empty folder named `plugins`.

##### Create plugin project
1. Create a plain Spring Boot project in the `plugins` folder. 
2. Add `plugin.properties` file to the plugin project.
    ```properties
    plugin.id=<>
    plugin.class=demo.pf4j.admin.AdminPlugin
    plugin.version=0.0.1
    plugin.provider=Hank CP
    plugin.dependencies=
    ```
3. Add Plugin class
    ```java
    public class DemoPlugin extends SpringBootPlugin {
    
        public DemoPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }
    
        @Override
        protected SpringBootstrap createSpringBootstrap() {
            return new SpringBootstrap(this, AdminPluginStarter.class);
        }
    }
    ```
4. Add anything you want in the plugin project like `Controller`, `Service`, `Repository`, `Model`, etc.
 
Everything is done and now you could start the app project to test the plugin. 

##### Checkout the demo projects for more details
* demo-shared: Shared code for app and plugin projects.
* demo-security: Security configuration demonstrate how to introduce Spring Security and secure your 
API via AOP.
* demo-apis: Class shared between app\<-\>plugins and plugins\<-\>plugins need to be declared as API.
* demo-app: The entry point and master project. It provides two `SpringApplication`
    * `DemoApp` does not have Spring Security configured.
    * `DomeSecureApp` include Spring Security, so you need to provide authentication information
    when access its rest API.
* plugins
    * demo-plugin-admin
        * Demonstrate Spring Security integration to plugin projects
        * Demonstrate [Spring Boot profile](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html) 
        feature support.
    * demo-plugin-author: Demonstrate share resource (like DataSource, TransactionManager) between
    api/plugins.
    * demo-plugin-library: Demonstrate using Spring Data/JPA.
    * demo-plugin-shelf: Demonstrate api expose and invocation between plugins.
* Every single projects with `SpringApplication` could be run standalone.
* It is basically a skeleton project that you could starts your own project. It almost contains 
everything we need in the real project. 

### Documentation
* [Plugin driven design]()
* [How it works]()
    * Plugin loading by pf4j
    * Get involved with Spring `ApplicationContext` initialization process.
    * Constraint
* [Configuration]()
* [Request Mapping]()
* [Persistence]()
    * Plain SQL / Jooq
    * Spring Data (JPA / Hibernate)
    * NoSQL (Mongo / Redis)
* [Security / AOP]()
* [Deployment]()
* [Weaknesses]()
* [Trouble Shoot]()
* [TODO]()

### Credit & Contribution

### TODO
* Cache (n+1 problem)
* Distributed Transaction Support
* More elegant way to register plugin controller
* Actuator support
* Management console
* Front End modularization
* Continuous Deployment

### Note
* Package name cannot start with `org.pf4j`
* Stay in same framework stack, don't introduce library with complex dependencies into plugin, e.g Spring data JPA

### License 

```
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
```