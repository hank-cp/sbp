### Principle
* If plugin doesn't rely on any 3rd-party libs, normal jar is enough.
  * You still need to sign jar manifest as below example
* Fat-jar format (jar in jar) is not supported at the moment. There are two walkaround solutions:
  * Flat lib jar, then pack all classes and resources into on jar file. (as the below gradle example)
  * Use [zip format](https://pf4j.org/doc/packaging.html), put all 3rd-parth libs jar under `/lib`, then pack into zip file with `/classes`.

### Gradle
This [revision](https://github.com/hank-cp/sbp/commit/9107e87656026d30c7e0461c45254aad303a5bbf) demonstrates how to build plugin JAR for production mode. 

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
    task buildPlugin(type: Jar) {
        ......
        manifest.attributes(
                "Plugin-Id": pluginProp.get("plugin.id"),
                "Plugin-Class": pluginProp.get("plugin.class"),
                "Plugin-Version": pluginProp.get("plugin.version"),
                "Plugin-Provider": pluginProp.get("plugin.provider"),
                "Plugin-Dependencies": pluginProp.get("plugin.dependencies"))
        ......
    }
```
* flat all classes of dependent jars and repack them to a new jar. The default
`JarPluginLoader` is not able to nested jars in side of jar, e.g. [Spring executable jar](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-executable-jar-format.html).
```
    task buildPlugin(type: Jar) {
        ......
        from configurations.localLibs.asFileTree.files.collect { zipTree(it) }
        ......
    }
```

### Maven
**NEED YOUR HELP!**
