package org.springframework.boot.autoconfigure.web.reactive;

import lombok.extern.slf4j.Slf4j;
import org.laxture.sbp.SpringBootPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class PluginLoadingLockServletFilter implements WebFilter {

    @Autowired
    private SpringBootPluginManager pluginManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        while (true) {
            if (!pluginManager.isLoading()) break;
            log.debug("Plugin loading, waiting...");
        }
        return chain.filter(exchange);
    }
}