sbp supports Spring Boot versions from `2.0` to `2.7`. Introduce sbp to your 
production project will NOT bind Spring Boot dependencies. You will have to declare
Spring Boot dependencies explicitly. For example:
```
  implementation "org.springframework.boot:spring-boot-starter-web:2.4.13"
  implementation "org.springframework.boot:spring-boot-starter-aop:2.4.13"
  implementation 'org.laxture:sbp-spring-boot-starter:2.4.14'
```
```
  implementation "org.springframework.boot:spring-boot-starter-web:2.7.8"
  implementation "org.springframework.boot:spring-boot-starter-aop:2.7.8"
  implementation 'org.laxture:sbp-spring-boot-starter:2.7.14'
```

To match Spring Boot versions, sbp will use the major part and minor part of Spring Boot
versions, and use micro part for sbp version. Every sbp release will have 
two versions: `2.4.X` and `2.7.X`. `X` is the major version of sbp. 
  * `2.4.X` supports Spring Boot `2.0` to `2.4` 
  * `2.7.x` supports Spring Boot `2.5` to `2.7`
Please choose the correct sbp version to match you Spring Boot version.

`-SNAPSHOT` version only supports Spring Boot `2.5` to `2.7`.