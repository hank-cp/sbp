Demo project is used for **sbp** project integration test and
demonstrate its feature. You could also use it as your own projects 
starting skeleton.

### Run the demo project

##### Prerequisite
* JDK 8+
* PostgreSQL 9.6+
    * Modify `postgresql.conf` file, set `max_prepared_connections` to non-zero value.
    * Create database: `psql -c 'create database "sbp";' -U postgres`
    * Create database: `psql -c 'create database "sbp-test";' -U postgres`

##### Setup on local
* `git clone git@github.com:hank-cp/sbp.git`
* run build command in sequence:
    ```
    > ./gradlew install
    > ./gradlew doMigration
    > ./gradlew buildApp
    > ./gradlew copyDependencies
    > ./gradlew test
    ```
    
##### Start DemoApp
* Make sure jooq mapping code if generated
    ```
    > ./gradlew doMigration
    > ./gradlew clean generateSbpJooqSchemaSource
    > ./gradlew copyDependencies
    ```
* Optionally, import test data with
    ```
    > ./gradlew doDataMigration
    ```
* If you using IDEA, you will need to refresh Gradle setting to include 
generated Jooq code.
* There are two Spring Boot app you could choose to start. `DemoApp`
omit security configuration. `DemoSecurityApp` includes security 
configuration with two user:

|  Username  | Password | Request Authentication Header |
| :---: | :---: | :--- |
| admin | admin | Basic YWRtaW46YWRtaW4=
| user | user | Basic dXNlcjp1c2Vy

##### Run test in IDE
* You need to set the `workingDir` of Run configuration to project root,
otherwise `PluginManager` couldn't find the `plugin` folder.
 ![](work_dir.png?raw=true)

### Project Details
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