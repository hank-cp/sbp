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

import com.logaritex.mcp.annotation.McpResource;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginResourceService {

	@McpResource(uri = "plugin://{username}", name = "Plugin Hello world", description = "For test plugin support for mcp")
	public McpSchema.ReadResourceResult getUserProfile(McpSchema.ReadResourceRequest request, String username) {
		return new McpSchema.ReadResourceResult(List.of(
			new McpSchema.TextResourceContents(request.uri(), "text/plain",
				"Hi %s, I am plugin resources.".formatted(username))));
	}

}