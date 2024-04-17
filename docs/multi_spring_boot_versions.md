## Sbp version
* To match Spring Boot versions, sbp will use the major part and minor part of Spring Boot
versions, and use micro part for sbp version.
* Before sbp version 18, Every sbp release will have wwo versions: `2.4.X` and `2.7.X`.
* ❗️After sbp [version 18](https://github.com/hank-cp/sbp/releases/tag/18), sbp will only supports Spring Boot 3.x.
* Table below shows the corresponding sbp version to different Spring Boot version.

| Spring Boot Version | sbp Version | Jvm | Gradle                                                                                                                                                                                                                |
|---------------------|-------------|-----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <= 2.4.x            | 2.4.17      | 8+  | implementation "org.springframework.boot:spring-boot-starter-web:2.4.13"<br/>implementation "org.springframework.boot:spring-boot-starter-aop:2.4.13"<br/>implementation 'org.laxture:sbp-spring-boot-starter:2.4.17' |
| >= 2.5.x, < 3.x     | 2.7.17      | 8+  | implementation "org.springframework.boot:spring-boot-starter-web:2.7.8"<br/>implementation "org.springframework.boot:spring-boot-starter-aop:2.7.8"<br/>implementation 'org.laxture:sbp-spring-boot-starter:2.7.17'   |
| >= 3.x              | 3.2.24      | 17+ | implementation "org.springframework.boot:spring-boot-starter-web:3.0.6"<br/>implementation "org.springframework.boot:spring-boot-starter-aop:3.0.6"<br/>implementation 'org.laxture:sbp-spring-boot-starter:3.0.18'   |
| >= 3.x              | -SNAPSHOT   | 17+ | 'org.laxture:sbp-spring-boot-starter:-SNAPSHOT'                                                                                                                                                                       |
