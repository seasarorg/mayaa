package org.seasar.mayaa.test.boot;

import org.seasar.mayaa.impl.MayaaServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestWarApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestWarApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean<?> mayaaServlet() {
        MayaaServlet servlet = new MayaaServlet();
        ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(servlet, "*.html");
        bean.setName("MayaaServlet");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
