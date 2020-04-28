### SbpProperties

Configurations for main app (not plugin) to control PluginManager behavior.

* `spring.sbp.enabled`: set to true to enable sbp. Default false.
* `spring.sbp.autoStartPlugin`: auto start plugin when main app is ready. Default true
* `spring.sbp.disabledPlugins`: Plugins disabled by default 
* `spring.sbp.enabledPlugins`: Plugins enabled by default, prior to `disabledPlugins`
* `spring.sbp.exactVersionAllowed`: Set to true to allow requires expression to be exactly x.y.z. The default is
false, meaning that using an exact version x.y.z will implicitly mean the
same as >=x.y.z. Default false.
private boolean  = false;
* `spring.sbp.classesDirectories`: where to load plugin classes, relative to plugin folder.
    * for IDEA
    ```
    spring:
      sbp:
        classes-directories:
        - "out/production/classes"
        - "out/production/resources"
    ```
* `spring.sbp.libDirectories`: where to load jar libs, relative to plugin folder.
* `spring.sbp.runtimeMode`: with two options
    * DEPLOYMENT: load plugin in jar/zip format.
    * DEVELOPMENT: load plugin from build folder with Java class file.
* `spring.sbp.pluginsRoot`: plugins home folder, relative to project working DIR. 
Default `plusins`
* `spring.sbp.plugins`: absolute path of plugins.
* `spring.sbp.systemVersion`: The system version used for comparisons to the plugin requires attribute.
* `spring.sbp.controller.base-path`: If this property is set, `PluginController` will be registered to 
help manage plugin via REST api. 

Check [Demo](../demo-app/src/main/resources/application.yml) for example. 

### SbpPluginProperties

Configurations to control how plugins are loaded by PluginManager.

* `sbp-plugin.properties`: properties define under this property will be passed to
plugin `ApplicationContext` environment. You could use it to passed global plugin
properties. Check [Demo](../demo-app/src/main/resources/application.yml) for example.
* `sbp-plugin.pluginFirstClasses`: If a class file existed in app classpath and 
plugin classpath at the same time, `PluginClassLoader` will try to load it
from plugin classpath first. This will cause `ClassCastException` when classes from
different classloader working together. Use this setting to load classes from 
plugin classpath first to make sure classes come from same classloader. 
e.g. Spring Boot AutoConfiguration used in plugin only. See
[JPA example](../plugins/demo-plugin-library/src/main/resources/application.yml).
* `sbp-plugin.pluginOnlyResources`: If a resources file (including resource) existed 
in app classpath and plugin classpath at the same time, `PluginClassLoader` scan all of
them out when performing `ClassLoader.loadResources()`. This will potentially cause
unexpected errors, e.g. loading wrong configuration file from the wrong place. Use this 
setting making sure resources are only loaded from plugin classpath.

