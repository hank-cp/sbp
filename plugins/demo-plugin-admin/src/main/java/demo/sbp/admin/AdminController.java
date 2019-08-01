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
package demo.sbp.admin;

import demo.sbp.security.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/user")
    public @ResponseBody String user() {
        return "Hello User!";
    }

    @RequestMapping(value = "/admin")
    @RequirePermission("ADMIN")
    public @ResponseBody String admin() {
        return "Hello Admin!";
    }

    @RequestMapping(value = "/profile")
    public @ResponseBody String profile() {
        return applicationContext.getEnvironment().getProperty("spring.plugin");
    }

}