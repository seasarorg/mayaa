package org.seasar.mayaa.test.boot;

import org.seasar.mayaa.impl.MayaaServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public ServletRegistrationBean<?> mayaaServlet() {
        MayaaServlet servlet = new MayaaServlet();
        ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(servlet, "*.html");
        bean.setName("MayaaServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
