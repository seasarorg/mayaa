package org.seasar.mayaa.test.boot;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.seasar.mayaa.impl.MayaaServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class WebConfig {

	@Bean
	public ServletRegistrationBean<?> mayaaServlet() {
		MayaaServlet servlet = new MayaaServlet();
		ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(servlet, "*.html");
		bean.setName("MayaaServlet");
		bean.setLoadOnStartup(1);
		bean.setInitParameters(Map.of(
			"enableBackwardOrderLoading", "true"
		));
		return bean;
	}

	@Bean
	public FilterRegistrationBean<OncePerRequestFilter> trailingSlashIndexFilter() {
		FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				String method = request.getMethod();
				if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
					filterChain.doFilter(request, response);
					return;
				}
				String requestUri = request.getRequestURI();
				String contextPath = request.getContextPath();
				String path = requestUri.substring(contextPath.length());
				if (path.endsWith("/") && !path.endsWith("/index.html")) {
					String forwardPath = path + "index.html";
					request.getRequestDispatcher(forwardPath).forward(request, response);
					return;
				}
				filterChain.doFilter(request, response);
			}
		});
		bean.setOrder(0);
		return bean;
	}
}
