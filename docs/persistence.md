## ❗️❗️❗️After sbp version 18, sbp will only supports Spring Boot 3.x. And Spring Boot stops providing builtin JTA supports from 3.0.x. So the SBP will stop supporting  JTA

**sbp** supports each plugin run as individual application, of
course, with individual persistence layer setting. You could choose shared
a single database for app and all of its plugins, or setup separate database
for plugin.

### Approach 1: Single shared dataSource / Same persistence middle-ware  
Choose any persistence middle-ware as you like, like JdbcTemplate/Jooq/
Mybatis, etc. With **sbp** bean management, 
you could shared data source from app to plugin with local
transaction support. This is recommended since it's the simplest way.

![](persistence_1.png?raw=true)

See [SharedDataSourceSpringBootstrap](../sbp-core/src/main/java/org/laxture/sbp/spring/boot/SharedDataSourceSpringBootstrap.java).

Unfortunately, JPA/Hibernate is not one of these options. Next section will explain
the reason.

### Approach 2: Single shared dataSource / Multiple persistence middle-ware
It's possible to mix JPA and other simple RMDB mapping tools like Jooq/Mybatis,
however, it's much more complicated case than approach 1, since Spring Data JPA(Hibernate)
involves very complex dependencies stack. The biggest challenge is **Transaction
Management**.

##### without JTA/XA
JPA uses `JpaTransactionManager` and other data source middle-wares use `DataSourceTransactionManager` generally.
According to Spring explanation:
```
JpaTransactionManager does not support 
running within DataSourceTransactionManager if told to manage the DataSource itself.
It is recommended to use a single JpaTransactionManager for all transactions
on a single DataSource, no matter whether JPA or JDBC access.
```
However, if we choose to use `DataSourceTransactionManager` in app 
(JPA is good for Domain-Driven-Design, but it lost advance control for database) 
and JPA in plugin, it's important to instantiate `transactionManager` appropriately
and inject to the right `applicationContext`. It works but, transactions couldn't be joined
anymore across different `transactionManager`s. In other words, we partially lost 
[ACID](https://en.wikipedia.org/wiki/ACID).

![](persistence_2.png?raw=true)

Event more, if you are using JPA in app and wants to share the same data source to plugins, the mentioned
limitation will prevent this to happen. Jpa natively prevents sharing active transaction information
out side of its control, so plugins will never be able to fire workable transactions. It's also 
impossible to share `EntityManager` between app and plugins, since it does provide any API to 
manage its internal state.

![](persistence_5.png?raw=true)

We could introduce JTA/XA to solve this problem.

##### with JTA/XA
We setup different `XADataSource` for JPA and other persistence middle-ware,
which already has build-in support for JTA/XA. Then we inject `JtaTransactionManager`
to every `applicationContext` of plugins. `JtaTransactionManager` will then
coordinate transactions with `@Transactional` transparently.
![](persistence_3.png?raw=true)

##### Example Configuration - All use JPA
In this example, we assume both App and plugin use JPA. 
1. Introduce dependencies to app and plugin, see [app build.gradle](/demo-jpa/build.gradle).
```
    // these two are required 
    implementation "org.springframework.boot:spring-boot-starter-data-jpa"
    implementation "org.springframework.boot:spring-boot-starter-jta-atomikos"
```
  You could use a shared lib project to provide the same dependencies.
2. Setup Jpa and Jta configuration on plugin side, below is the example:
```yml
spring:
  datasource:
    url: "jdbc:postgresql://localhost/sbp"
    username: postgres
    driver-class-name: "org.postgresql.Driver"
  sbp:
    # plugin-properties will apply these properties for all plugins
    plugin-properties:
      spring:
        jpa:
          properties:
            hibernate:
              temp:
                use_jdbc_metadata_defaults: false
          database-platform: org.hibernate.dialect.PostgreSQL9Dialect
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        temp:
          use_jdbc_metadata_defaults: false
    database-platform: org.hibernate.dialect.PostgreSQL9Dialect
  jta:
    atomikos:
      datasource:
        max-pool-size: 20
        min-pool-size: 5
        borrow-connection-timeout: 60
```
2. Setup Jpa and Jta configuration on plugin side, below is the example:
```yml
spring:
  datasource:
    url: "jdbc:postgresql://localhost/sbp"
    username: postgres
    driver-class-name: "org.postgresql.Driver"
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL95Dialect
        hbm2ddl:
          create_namespaces: true
    show-sql: true
    open-in-view: false
  jta:
    atomikos:
      datasource:
        beanName: dataSource-library # this line is necessary, could be any name except "dataSource" 
```
3. Use [SharedJtaSpringBootstrap](../sbp-core/src/main/java/org/laxture/sbp/spring/boot/SharedJtaSpringBootstrap.java)
   to start your plugin.
4. Close `AtomikosDataSourceBean` when plugin stops, see [LibraryPlugin.java](/plugins/demo-plugin-library/src/main/java/demo/sbp/library/LibraryPlugin.java)

With this configuration, app and plugins will have their own `EntityManager`, and Atomikos will 
join the transaction across different JPA sessions.

##### Example Configuration - Only plugin use JPA
In this example, we assume app doesn't use JPA, it also doesn't provide JPA dependencies, but plugins use JPA.
1. Setup Jpa and Jta configuration on plugin side, see [plugin build.gradle](plugins/demo-plugin-library/build.gradle)
```
# define specific gradle configuration, we need to use to copy dependencies as libs for deployment purpose.
configurations {
  jpa
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-jta-atomikos"
    implementation "org.springframework.boot:spring-boot-starter-data-jpa"
    jpa "org.springframework.boot:spring-boot-starter-data-jpa"
}

task clearDependencies(type: Delete) {
    delete files("${project.projectDir}/libs")
}

# Since app doesn't provide dependencies, we need to copy dependent jars
# to its classpath additinally.
task copyDependencies(type: Copy) {
    group 'build'
    dependsOn clearDependencies
    from configurations.jpa.files
    into 'libs'
}
```
2. Setup Jpa and Jta configuration on plugin side, same as above, but additionally, 
   we need to tell the plugin, loads these classes from plugin's classpath first to avoid `ClassCastException`.
   Spring places all `AutoConfiguration` in an all-in-one spring-boot-\<version\>.jar, which
   will be conflicted with the one in `libs`.
```yml
sbp-plugin:
  plugin-first-classes:
    - org.springframework.boot.autoconfigure.data.*
    - org.springframework.boot.autoconfigure.orm.*
    - org.springframework.boot.orm.jpa.*
```
3. Use [SharedJtaSpringBootstrap](../sbp-core/src/main/java/org/laxture/sbp/spring/boot/SharedJtaSpringBootstrap.java)
   to start your plugin.
4. Close `AtomikosDataSourceBean` when plugin stops, see [LibraryPlugin.java](/plugins/demo-plugin-library/src/main/java/demo/sbp/library/LibraryPlugin.java)

### Approach 3: Multiple dataSource
Similar to approach 2, we could use JTA/XA to provide distributed transaction
management across multiple database.
![](persistence_4.png?raw=true)

### NoSQL (MongoDB / Redis)
TBD. So far we haven't test **sbp** with NoSQL persistence
tools like MongoDB, redis. Compare to RMDBs, NoSQL tools are generally
much more straight forward to integrate.