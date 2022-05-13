package com.neo;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.neo.branding.NeoBanner;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		application.application().setBanner(new NeoBanner());
		return application.sources(ServletInitializer.class);
	}

}
