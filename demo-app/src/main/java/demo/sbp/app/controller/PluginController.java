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
package demo.sbp.app.controller;

import demo.sbp.api.extension.PluginRegister;
import demo.sbp.security.annotation.RequirePermission;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/plugin")
public class PluginController {

    @Autowired(required = false)
    private PluginManager pluginManager;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/list")
    @RequirePermission("ADMIN")
    public List<String> list() {
        return pluginManager.getResolvedPlugins().stream()
                .map(PluginWrapper::getPluginId).collect(Collectors.toList());
    }

    @RequestMapping(value = "/start/{pluginId}")
    public int start(@PathVariable String pluginId) {
        pluginManager.startPlugin(pluginId);
        return 0;
    }

    @RequestMapping(value = "/stop/{pluginId}")
    public int stop(@PathVariable String pluginId) {
        pluginManager.stopPlugin(pluginId);
        return 0;
    }

    @RequestMapping(value = "/extensions/list")
    public List<String> listExtensions() {
        List<PluginRegister> registers = pluginManager.getExtensions(PluginRegister.class);
        return registers.stream().map(PluginRegister::name).collect(Collectors.toList());
    }

}
