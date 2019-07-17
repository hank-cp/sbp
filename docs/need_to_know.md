* Package name cannot start with `org.pf4j`, `java` or `javax`. 
`PluginClassLoader` will treat them differently. 
* Stay in same framework stack as long as you can. Introducing library with complex 
dependencies (link Spring Data JPA) into plugin only, will cause many unexpected
problems. Because plugin and app have different classloader, it's a little
bit hard to mark sure all referred dependent classes are loaded by the same
classloader.