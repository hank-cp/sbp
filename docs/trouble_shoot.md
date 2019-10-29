## Trouble Shoot

##### I get `ClassNotFoundException`
* Solution: Locate the missing class library, and add dependency in the main app project.
* It mainly because SpringBoot instantiate bean by `new`, in this case
the class is load by main app classloader that our of our control.

```java
// org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport
@Override
public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
        BeanNameGenerator importBeanNameGenerator) {
    RepositoryConfigurationDelegate delegate = new RepositoryConfigurationDelegate(
            getConfigurationSource(registry, importBeanNameGenerator), this.resourceLoader, this.environment);
    delegate.registerRepositoriesIn(registry, getRepositoryConfigurationExtension());
}
```

##### I get `ClassCastException`
* Solution: specify class in `plugin.pluginFirstClasses` of `application.yml`
* It's because the class is loaded by mismatch class loader. `spring-boot-autoconfigure`
package referred a lot 3rd party classed directly, which should be loaded
by our `PluginClassloader` in our case. 

##### Plugin modification is not compiled.

## Misc.
* Package name cannot start with `org.pf4j`, `java` or `javax`. 
`PluginClassLoader` will treat them differently. 
* Stay in same framework stack as long as you can. Introducing library with complex 
dependencies (link Spring Data JPA) into plugin only, will cause many unexpected
problems. Because plugin and app have different classloader, it's a little
bit hard to mark sure all referred dependent classes are loaded by the same
classloader.