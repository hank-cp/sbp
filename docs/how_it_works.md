### Plugin loading
different from original pf4j implementation, pf4j-spring-boot 
change the class loading mechanism a little bit.
![ClassLoader](classloader.png?raw=true)

### Get involved with Spring `ApplicationContext` initialization process.
pf4j-spring-boot extends `SpringApplication` from Spring Boot, which is 
`SpringBootstrap` to work with pf4j together. 
![ApplicationContext](applicationContext.png?raw=true)
