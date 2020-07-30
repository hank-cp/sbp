***sbp*** serve static resource inside of plugins by extending Spring Boot's 
`WebMvcAutoConfiguration` resource handling. The same 
[configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc-static-content) 
as Spring Boot will work.

```
spring:
  mvc:
    static-path-pattern: /public/**
  resources:
    add-mappings: true
    cache:
      period: 3600
```
