package org.laxture.sbp.spring.boot;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.laxture.sbp.SpringBootPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration @Slf4j
public class PluginLoadingLockServletFilter implements Filter {

    @Autowired
    private SpringBootPluginManager pluginManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        while (true) {
            if (!pluginManager.isLoading()) break;
            log.debug("Plugin loading, waiting...");
        }
        chain.doFilter(request, response);
    }
}