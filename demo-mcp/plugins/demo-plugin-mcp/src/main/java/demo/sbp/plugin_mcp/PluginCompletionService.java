/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package demo.sbp.plugin_mcp;

import com.logaritex.mcp.annotation.McpComplete;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginCompletionService {

	@McpComplete(uri = "plugin://{username}")
	public List<String> pluginCompletion(String usernamePrefix) {
		return List.of(usernamePrefix + "_plugin_1", usernamePrefix + "_plugin_2");
	}

}