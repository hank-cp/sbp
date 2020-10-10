### SbpProperties

Configurations for main application's PluginManager.

##### `spring.sbp.enabled`
set to true to enable sbp. Default false.

##### `spring.sbp.auto-start-plugin`
auto start plugin when main app is ready. Default true

##### `spring.sbp.disabled-plugins`
Plugins disabled by default 

##### `spring.sbp.enabled-plugins`
Plugins enabled by default, prior to `disabled-plugins`

##### `spring.sbp.classes-directories`
where to load plugin classes, relative to plugin folder.
    * for IDEA
    ```
    spring:
      sbp:
        classes-directories:
        - "out/production/classes"
        - "out/production/resources"
    ```
##### `spring.sbp.lib-directories`
where to load jar libs, relative to plugin folder.

##### `spring.sbp.runtime-mode`
with two options
    * DEPLOYMENT: load plugin in jar/zip format.
    * DEVELOPMENT: load plugin from build folder with Java class file.

##### `spring.sbp.plugins-root`
plugins home folder, relative to project working DIR. 
Default `plugins`

##### `spring.sbp.custom-plugin-loaders`
Allows to provide custom plugin loaders. Provided custom `PluginLoader` should have
a constructor with one argument that accepts `PluginManager`.

##### `spring.sbp.plugin-profiles`: 
Specify Spring profiles to be used when creating plugins' `ApplicationContext` environment. With profile `plugin` by default. 
You could use it to config plugin globally.
Check [Demo](../demo-app/src/main/resources/application.yml) for example.

##### `spring.sbp.plugin-properties`
Specify properties to be used when creating plugins' `ApplicationContext` environment. 
You could use it to config plugin globally.
Check [Demo](../demo-app/src/main/resources/application.yml) for example.

##### `spring.sbp.system-version`
The system version used for comparisons to the plugin requires attribute.

##### `spring.sbp.controller.base-path`
If this property is set, `PluginController` will be registered to 
help manage plugin via REST api. 

Check [Demo](../demo-app/src/main/resources/application.yml) for example. 

### SbpPluginProperties

Configurations for individual plugins.

##### `sbp-plugin.plugin-first-classes`
If a class file existed in app classpath and 
plugin classpath at the same time, `PluginClassLoader` will try to load it
from plugin classpath first. This will cause `ClassCastException` when classes from
different classloader working together. Use this setting to load classes from 
plugin classpath first to make sure classes come from same classloader. 
e.g. Spring Boot AutoConfiguration used in plugin only. See
[JPA example](../plugins/demo-plugin-library/src/main/resources/application.yml).

##### `sbp-plugin.plugin-only-resources`
If a resource file (including resource) existed 
in app classpath and plugin classpath at the same time, `PluginClassLoader` scan all of
them out when performing `ClassLoader.loadResources()`. This will potentially cause
unexpected errors, e.g. loading wrong configuration file from the wrong place. Use this 
setting making sure resources are only loaded from plugin classpath.

