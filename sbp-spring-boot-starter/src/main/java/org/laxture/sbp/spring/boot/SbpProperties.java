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
package org.laxture.sbp.spring.boot;

import lombok.Data;
import org.pf4j.RuntimeMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties for Sbp main app
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see SbpAutoConfiguration
 */
@ConfigurationProperties(prefix = SbpProperties.PREFIX)
@Data
public class SbpProperties {

	public static final String PREFIX = "spring.sbp";

	/**
	 * Enable Sbp
	 */
	private boolean enabled = false;
	/**
	 * Auto start plugin when main app is ready
	 */
	private boolean autoStartPlugin = true;
	/**
	 * Plugins disabled by default
	 */
	private String[] disabledPlugins;
	/**
	 * Plugins enabled by default, prior to `disabledPlugins`
	 */
	private String[] enabledPlugins;
	/**
	 * Profile for plugin Spring {@link ApplicationContext}
	 */
	private String[] profiles = new String[] {"plugin"};
	/**
	 * Set to true to allow requires expression to be exactly x.y.z. The default is
	 * false, meaning that using an exact version x.y.z will implicitly mean the
	 * same as >=x.y.z
	 */
	private boolean exactVersionAllowed = false;
	/**
	 * Extended Plugin Class Directory
	 */
	private List<String> classesDirectories = new ArrayList<>();
	/**
	 * Extended Plugin Jar Directory
	 */
	private List<String> libDirectories = new ArrayList<>();
	/**
	 * Runtime Mode：development/deployment
	 */
	private RuntimeMode runtimeMode = RuntimeMode.DEPLOYMENT;
	/**
	 * Plugin root directory: default “plugins”; when non-jar mode plugin, the value
	 * should be an absolute directory address
	 */
	private String pluginsRoot = "plugins";
	/**
	 * Plugin address: absolute address
	 */
	private List<String> plugins = new ArrayList<>();
	/**
	 * The system version used for comparisons to the plugin requires attribute.
	 */
	private String systemVersion = "0.0.0";

}
