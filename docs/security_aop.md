pf4j-spring-boot Support introduce Spring Security based configuration
from app to plugin, allowing the whole application runs under unify 
security control. It also support AOP programming for scenario like 
permission checking.

However, security configuration could be very different from case to case.
pf4j-spring-boot doesn't mean to provide general security integration. You
could take the [demo-security](../demo-security) project for example to
implement your own, it should cover all things you need to think about.

Some important hints:
* `org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration` 
and `org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration` 
should never be applied to plugin(this has been done in `SpringBootstrap`), 
as well as your [security configuration](../demo-security/src/main/java/demo/pf4j/security/SecurityConfig.java)
(This part should be managed by yourself). 
The main app servlet context will handle security stuff by filter chain.
* Make sure AOP configuration is always loaded by plugin `ApplicationContext`, 
so that `@Controller` and `@RestController` beans could be weaved appropriately.