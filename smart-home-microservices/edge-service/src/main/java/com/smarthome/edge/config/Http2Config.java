package com.smarthome.edge.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Http2Config {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> http2Customizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.addUpgradeProtocol(new org.apache.coyote.http2.Http2Protocol());
            });
        };
    }

    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(8084);
        return connector;
    }
}