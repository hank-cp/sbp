[![GitHub release](https://img.shields.io/github/release/hank-cp/sbp.svg)](https://github.com/hank-cp/sbp/releases)
![Maven Central](https://img.shields.io/maven-central/v/org.laxture/sbp-core)
![Test](https://github.com/hank-cp/sbp/workflows/CI%20Test/badge.svg)
![GitHub](https://img.shields.io/github/license/hank-cp/sbp.svg)
![GitHub last commit](https://img.shields.io/github/last-commit/hank-cp/sbp.svg)

sbp为Spring Boot带来面向插件开发设计的能力. 它受到开源项目[Pf4j](https://pf4j.org/)的启发, 并构建在其基础之上. 

### 为什么我们需要插件化的Spring Boot?
* Spring Boot很棒棒，但它是单体架构。这意味着每次代码修改您必须交付整个应用程序.
* 我们需要一个更具灵活性和可扩展性的现代框架，可以快速为复杂的业务场景提供灵活解决方案. 
* 并非所有项目都需要在开始阶段需要考虑线性扩展问题。
* 使用sbp，我们在仅使用Spring Boot的前提下以微服务架构的方式来考虑问题，
但不必过多花费心思在“云原生”问题上，如服务发现，流量控制等.
* 它是一个介于单体Spring Boot应用和分布式Spring Cloud架构之间的中间过渡方案.

### Feature / Benefits
* 使单体Spring Boot应用支持**插件化**.
* 一个开发部署更加**轻量简单**的螺旋扩展架构.
* **在线** 安装/升级/启动/停止插件.
* 由Spring Boot提供**全栈**Web/Rest后台服务, 包括:
  * Controller/RestController
  * RouterFunction
  * WebFlux
  * 静态资源加载, 也包括Freemarker/Velocity/Thymeleaf这类模板引擎
  * 各种持久化中间件 (Spring Data/JPA/Jooq/MybatisPlain SQL)
  * Spring Security
  * AOP
* 在**独立可执行的**Spring Boot工程中开发调试测试插件.
* 只需熟悉Spring Boot, **无需要学习**其它框架知识. 
* 支持Spring Boot从`2.0`到`2.7`各版本
* **NO XML**

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
