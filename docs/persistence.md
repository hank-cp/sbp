pf4j-spring-boot supports each plugin run as individual application, of
course, with individual persistence layer setting. You could choose shared
a single database for app and all of its plugins, or setup separate database
for plugin.

### Approach 1: Single shared dataSource / Same persistence middle-ware  
Choose any persistence middle-ware as you like, like JDBC template/Jooq/
Mybatis/Spring Data JPA(Hibernate). With pf4j-spring-boot bean management, 
you could shared database connection resource from app to plugin with local
transaction support. This is recommended since it's the simplest way.

![](persistence_1.png?raw=true)

See [SharedDataSourceSpringBootstrap](../pf4j-spring-boot-support/src/main/java/org/pf4j/spring/boot/SharedDataSourceSpringBootstrap.java).

### Approach 2: Single shared dataSource / Multiple persistence middle-ware
It's possible to mix JPA and other simple RMDB mapping tools like Jooq/Mybatis,
however, it's much more complicated case than approach 1, since Spring Data JPA(Hibernate)
involves very complex dependencies stack. The biggest challenge is **Transaction
Management**.

##### without JTA/XA
JPA uses `JpaTransactionManager` and others use `DataSourceTransactionManager` generally.
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
and inject to the right `applicationContext`. It works but, transactions could be joined
any more across different `transactionManager`s. In other words, we partially lost 
[ACID](https://en.wikipedia.org/wiki/ACID).

![](persistence_2.png?raw=true)

We could introduce JTA/XA to solve this problem.

##### with JTA/XA
We setup different `XADataSource` for JPA and other persistence middle-ware,
which already has build-in support for JTA/XA. Then we inject `JtaTransactionManager`
to every `applicationContext` of plugins. `JtaTransactionManager` will then
coordinate transactions with `@Transactional` transparently.
![](persistence_3.png?raw=true)

##### Example Configuration
In this example, we explain how to introduce JPA only in plugin:
1. Copy Spring Data JPA and all it's dependencies to plugin libs folder.
See `copyDependencies` task in [build.gradle](../plugins/demo-plugin-library/build.gradle) for example.
2. Configure `plugin.pluginFirstClasses` property to tell `PluginClassLoader`
load `@Configuration` classes from main app's `ClassLoader` to avoid `ClassCastException`.
    * Spring place almost `AutoConfiguration` in an all-in-one spring-boot-\<version\>.jar, which
    is conflicted with the one in Spring Data JPA dependencies.
    * See [demo-plugin-library](../plugins/demo-plugin-library/src/main/resources/application.yml) for example.
3. Add dependencies to app project"
    ```
    dependencies {
        ......
        implementation 'javax.transaction:javax.transaction-api'
        ......
    }
    ```
4. JTA register XADataSource with beanName by default, which is `dataSource`,
so you need to tell JTA use a different name. Otherwise an error 
`javax.naming.NamingException: Another resource already exists with name dataSource - pick a different name`
will be thrown.
   ```
   spring.jta.atomikos.datasource.beanName: dataSource-[plugin]
   ``` 
5. Use [SharedJtaSpringBootstrap](../pf4j-spring-boot-support/src/main/java/org/pf4j/spring/boot/SharedJtaSpringBootstrap.java)
to start your plugin.
6. **!!! IMPORTANT !!!** Atomikos `JtaTransactionManager` manage distribution transaction with static reference 
and file system. So you will have to release `XADataSrouce` on plugin stopping. 
See [LibraryPlugin](../plugins/demo-plugin-library/src/main/java/demo/pf4j/library/LibraryPlugin.java).  

See [demo-plugin-library](../plugins/demo-plugin-library) project for more example.

### Approach 3: Multiple dataSource
Similar to approach 2, we could use JTA/XA to provide distributed transaction
management across multiple database.
![](persistence_4.png?raw=true)

### NoSQL (MongoDB / Redis)
TBD. So far we haven't test _pf4j-spring-boot_ with NoSQL persistence
tools like MongoDB, redis. Compare to RMDBs, NoSQL tools are generally
much more straight forward to integrate.