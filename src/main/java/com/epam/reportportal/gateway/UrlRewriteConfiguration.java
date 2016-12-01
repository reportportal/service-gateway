/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-gateway
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package com.epam.reportportal.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * URL rewriting configuration
 * ZUUL+Spring has performance issues with Multipart requests.
 * Multipart request should be router directly to ZUUL servlet, so this filter routes some specific request directly to ZUUL
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class UrlRewriteConfiguration {

	@Autowired
	private ZuulProperties zuulProperties;

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new ZuulMiltipartRewriteFilter(zuulProperties.getServletPath()));
		registrationBean.addUrlPatterns("/*");

		/* should be the first one in the filter chain */
		registrationBean.setOrder(Integer.MIN_VALUE);
		return registrationBean;
	}

	private static class ZuulMiltipartRewriteFilter implements Filter {

		private final String zuulServlet;

		ZuulMiltipartRewriteFilter(String zuulServlet) {
			this.zuulServlet = zuulServlet;
		}

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {

		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			HttpServletRequest rq = (HttpServletRequest) request;

			Optional<MediaType> contentType = Optional
					.ofNullable(rq.getHeader(HttpHeaders.CONTENT_TYPE))
					.map(MediaType::parseMediaType);

            /* if request doesn't go to zuul and has multipart content type*/
			//noinspection OptionalGetWithoutIsPresent
			if (!rq.getRequestURI().startsWith(zuulServlet) && contentType.isPresent() && contentType.get()
					.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)) {
				rq.getRequestDispatcher(zuulServlet + rq.getRequestURI()).forward(rq, response);

			} else {
				chain.doFilter(request, response);
			}

		}

		@Override
		public void destroy() {

		}
	}

}

