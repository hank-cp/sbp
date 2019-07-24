### Pf4jProperties

Configurations for main app (not plugin) to control PluginManager behavior.

* `spring.pf4j.enabled`: set to true to enable Pf4j. Default false.
* `spring.pf4j.autoStartPlugin`: auto start plugin when main app is ready. Default true
* `spring.pf4j.exactVersionAllowed`: Set to true to allow requires expression to be exactly x.y.z. The default is
false, meaning that using an exact version x.y.z will implicitly mean the
same as >=x.y.z. Default false.
private boolean  = false;
* `spring.pf4j.classesDirectories`: where to load plugin classes, relative to plugin folder.
    * for IDEA
    ```
    spring:
      pf4j:
        classes-directories:
        - "out/production/classes"
        - "out/production/resources"
    ```
* `spring.pf4j.libDirectories`: where to load jar libs, relative to plugin folder.
* `spring.pf4j.runtimeMode`: with two options
    * DEPLOYMENT: load plugin in jar/zip format.
    * DEVELOPMENT: load plugin from build folder with Java class file.
* `spring.pf4j.pluginsRoot`: plugins home folder, relative to project working DIR. 
Default `plusins`
* `spring.pf4j.plugins`: absolute path of plugins.
* `spring.pf4j.systemVersion`: The system version used for comparisons to the plugin requires attribute.

Check [Demo](../demo-app/src/main/resources/application.yml) for example. 

### Pf4jPluginProperties

Configurations to control how plugins are loaded by PluginManager.

* `plugin.properties`: properties define under this property will be passed to
plugin `ApplicationContext` environment. You could use it to passed global plugin
properties. Check [Demo](../demo-app/src/main/resources/application.yml) for example.
* `plugin.pluginFirstClasses`: If a class file existed in app classpath and 
plugin classpath at the same time, `PluginClassLoader` will try to load it
from plugin classpath first. This will cause `ClassCastException` when classes from
different classloader working together. Use this setting to load classes from 
plugin classpath first to make sure classes come from same classloader. 
e.g. Spring Boot AutoConfiguration used in plugin only. Check 
[JPA example](../plugins/demo-plugin-library/src/main/resources/application.yml).
