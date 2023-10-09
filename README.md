[![GitHub release](https://img.shields.io/github/release/hank-cp/sbp.svg)](https://github.com/hank-cp/sbp/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.laxture/sbp-core)
![Test](https://github.com/hank-cp/sbp/workflows/CI%20Test/badge.svg)
![GitHub](https://img.shields.io/github/license/hank-cp/sbp.svg)
![GitHub last commit](https://img.shields.io/github/last-commit/hank-cp/sbp.svg)

sbp introduce plugin oriented programming to Spring Boot. It is inspired and builds 
on top of [Pf4j](https://pf4j.org/) project.

❗️❗️❗️sbp supports Spring Boot 3.x ONLY from [version 18](https://github.com/hank-cp/sbp/releases/tag/18). If you are still using Spring Boot 2.x, please
stay with the [old version](docs/multi_spring_boot_versions.md) of sbp.

[中文](README.zh-cn.md)

### Why we need plugin for Spring Boot?
* Spring boot is good, but it is monolithic. It means you have to delivery the whole application
for every single code change every time.
* We need a modern framework with flexibility and extensibility to rapidly deliver solution 
for complex business scenario. 
* Not all projects need to consider scaling at the beginning stage, like Spring Cloud does.
* With **sbp**, we could think in micro service architecture only with Spring Boot,
but no need to worried too much about "cloud native" things, like service discovery, traffic control, etc.
* It's a medium level preference between monolithic Spring Boot application and distributed 
Spring Cloud application.

### Feature / Benefits
* Turn monolithic Spring Boot application to be **MODULAR**.
* Much more **LIGHTWEIGHT** development and deployment for spiral extensible architecture.
* Install/update/start/stop plugins **ON THE FLY**.
* **FULL STACK** Web/Rest server-side features powered by Spring Boot, including:
  * Controller/RestController
  * RouterFunction
  * WebFlux
  * static resource mapping, including template engine like Freemarker/Velocity/Thymeleaf
  * persistence (Spring Data/JPA/Jooq/Mybatis/Plain SQL)
  * Spring Security
  * AOP

* Code and test plugin project as **STANDALONE** Spring Boot project.
* Supports Spring Boot from `2.x` to `3.1.x`
* **NO** extra knowledge **NEED TO LEARN** as long as you are familiar with Spring Boot.
* **NO XML**

##### vs OSGi Based Server ([Eclipse Virgo](https://www.eclipse.org/virgo/) \ [Apache Karaf](http://karaf.apache.org/))
* OSGi based server need a server container to deploy, which is not _cloud friendly_.
* OSGi configuration is complex and strict, the learning curve is steep.
* Bundle cannot be run standalone, which is hard for debug and test.
* ~~Spring dm server has been dead, and its successor Virgo is now still struggling to live.~~ 

##### vs Spring Cloud
* Spring Cloud does not aim to solve similar problem as `sbp`, but so far it maybe the 
best choice to solve application extension problem.
* With **sbp**, you will no need to consider too much about computer resource arrangement, 
everything stick tight within one process.
* Again, **sbp** is good for medium level applications, which have problem to handle business
change rapidly. For large scale application, Spring Cloud is still your best choice.
* **sbp** should be able to live with Spring Cloud. After all, it is still a Spring Boot
application, just like any single service provider node in Spring Cloud network.

### Getting Start

##### Create app project
1. Create a Spring Boot project with multi sub project structure.
    1. For Gradle, it means [multiple projects](https://docs.gradle.org/current/userguide/intro_multi_project_builds.html)
    2. For Maven, it means [multiple modules](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
    3. Take the demo projects for reference.
2. Introduce `sbp-spring-boot-starter` to dependencies.
    * Maven
        ```
        <dependency>
            <groupId>org.laxture</groupId>
            <artifactId>sbp-spring-boot-starter</artifactId>
            <version>3.1.22</version>
        </dependency>
        ```
    * Gradle
        ```
        dependencies {
            implementation "org.springframework.boot:spring-boot-starter-web"
            implementation "org.springframework.boot:spring-boot-starter-aop"
            implementation 'org.laxture:sbp-spring-boot-starter:3.1.22'
        }
        ```
    * Latest master code is always available with version `-SNAPSHOT`
3. Add belows to `application.properties`.
    ```
    spring.sbp.runtimeMode = development
    spring.sbp.enabled = true
    # remember to add this line in case you are using IDEA
    spring.sbp.classes-directories = "out/production/classes, out/production/resources"
    ``` 
4. Add anything you want in this project like `Controller`, `Service`, `Repository`, `Model`, etc.
5. Create an empty folder named `plugins`.

##### Create plugin project
1. Create a plain Spring Boot project in the `plugins` folder. 
2. Add `plugin.properties` file to the plugin project.
    ```properties
    plugin.id=<>
    plugin.class=demo.sbp.admin.DemoPlugin
    plugin.version=0.0.1
    plugin.provider=Your Name
    plugin.dependencies=
    ```
3. Introduce `sbp-core` to dependencies.
    * Maven
        ```
        <dependency>
            <groupId>org.laxture</groupId>
            <artifactId>sbp-core</artifactId>
            <version>3.1.22</version>
        </dependency>
        ```
    * Gradle
        ```
        dependencies {
            implementation 'org.laxture:sbp-core:3.1.22'
        }
        ```
4. Add Plugin class
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

### Documentation
* [How it works](docs/how_it_works.md)
* [Configuration](docs/configuration.md)
* [Serve Static Content](docs/resource_handling.md)
* [Extensible Integration](docs/extensible_integration.md)
  * ~~[Persistence](docs/persistence.md)~~
* [Security / AOP](docs/security_aop.md)
* [Deployment](docs/deployment.md)
* [Support Different Spring Boot Versions](docs/multi_spring_boot_versions)
* [Trouble Shoot & Misc](docs/trouble_shoot.md)
* [How to run](docs/demo_project.md)
* [Road map](docs/roadmap.md)

<!--
### Credit & Contribution
-->

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
