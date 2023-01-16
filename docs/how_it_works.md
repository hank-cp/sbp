### Plugin loading
![ClassLoader](classloader.png?raw=true)

**NOTICE**: If you have ever used [pf4j](https://pf4j.org/), please note that 
although **sbp** is based on pf4j, but **sbp** rewrites classloading 
behavior that will be quite different from original pf4j does.

### Get involved with Spring `ApplicationContext` initialization process.
**sbp** extends `SpringApplication` as `SpringBootstrap` to bootstrap a plugin `ApplicationContext`. 
![ApplicationContext](applicationContext.png?raw=true)

### Extending `SpringBootstrap`
* You could always extend `SpringBootstrap` to satisfy your own project configuration.
  * exclude more Spring boot autoConfiguration.
  * inject more beans from App's `ApplicationContext` to plugin's 'ApplicationContext'.
* There are two ready to use extended `SpringBootstrap`
  * `SharedDataSourceSpringBootstrap`
  * `SharedJtaSpringBootstrap`