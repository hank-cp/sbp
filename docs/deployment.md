### Gradle
[revision](https://github.com/hank-cp/sbp/commit/9107e87656026d30c7e0461c45254aad303a5bbf) demonstrates how to build plugin JAR in development mode. 

#### Breakdown
* Add configuration of plugin libs, so later then could be packed in to the jar.
```
  configurations {
      localLibs
      compile.extendsFrom(localLibs)
  }

  dependencies {
      localLibs fileTree(dir: 'libs', include: '**')
      ......
  }
```
* Add a task to build the jar. The most important thing is to set the 
manifest of the jar could it could be recognized as a plugin.
```
  manifest.attributes(
          "Plugin-Id": pluginProp.get("plugin.id"),
          "Plugin-Class": pluginProp.get("plugin.class"),
          "Plugin-Version": pluginProp.get("plugin.version"),
          "Plugin-Provider": pluginProp.get("plugin.provider"),
          "Plugin-Dependencies": pluginProp.get("plugin.dependencies"))
```


### Maven
**NEED YOUR HELP!**