* Plain SQL / Jooq / Mybatis 
* Spring Data (JPA / Hibernate)
    * copyDependencies
    * plugin.pluginFirstClasses 
        * all in one spring-boot-2.1.6.RELEASE.jar
    * javax.transaction:javax.transaction-api
    * Hibernate / JTA
    * JpaTransactionManager does not support 
    running within DataSourceTransactionManager if told to manage the DataSource itself.
    It is recommended to use a single JpaTransactionManager for all transactions
    on a single DataSource, no matter whether JPA or JDBC access.
* JTA Support
    * max_prepared_connections
* NoSQL (Mongo / Redis)