package io.illuminates.communications.server.config;

import io.illuminates.communications.server.models.Registry;
import org.apache.camel.component.atmosphere.websocket.CamelWebSocketServlet;
import org.atmosphere.cpr.ContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.*;

@Configuration
@CrossOrigin(origins = {"*"})
public class SpringRegistrationBeanServlet {

    private static Logger logger = LoggerFactory.getLogger(SpringRegistrationBeanServlet.class);

    @Bean
    public EmbeddedAtmosphereInitializer atmosphereInitializer() {
        return new EmbeddedAtmosphereInitializer();
    }

    @Bean
    public ServletRegistrationBean camelWebSocketServlet() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new CamelWebSocketServlet(),"/ws/*");

        //!!! Remember always set name "CamelWsServlet" - nothing else should be used.
        // If you use another name your consumer will not be registered and nothing will work.
        bean.setName("CamelWsServlet");
        bean.setLoadOnStartup(0);
        // Need to occur before the EmbeddedAtmosphereInitializer
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // Enables events notification feature
        Map<String,String> params = new HashMap<>();
        params.put("events","true");
        bean.setInitParameters(params);
        return bean;
    }

    private static class EmbeddedAtmosphereInitializer extends ContainerInitializer
            implements ServletContextInitializer {

        @Override
        public void onStartup(ServletContext servletContext) throws ServletException {
            onStartup(Collections.<Class<?>> emptySet(), servletContext);
        }
    }

}
