package proxy.alexa;

import javax.servlet.http.HttpServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazon.ask.Skills;
import com.amazon.ask.servlet.SkillServlet;

import proxy.alexa.handler.IntentRequestHandler;
import proxy.alexa.handler.LaunchRequestHandler;
import proxy.util.Properties;


@Configuration
public class AlexaConfig {

	private final Properties config;
	private final IntentRequestHandler intentHandler;
	private final LaunchRequestHandler launchHandler;

	@Autowired
	AlexaConfig(Properties config, IntentRequestHandler intentHandler, LaunchRequestHandler launchHandler) {
		this.config = config;
		this.launchHandler = launchHandler;
		this.intentHandler = intentHandler;
	}


	@Bean
	public ServletRegistrationBean<HttpServlet> alexaServlet() {
		ServletRegistrationBean<HttpServlet> registrationBean = new ServletRegistrationBean<>(createAlexaServlet(), "/alexa/*");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}


	@SuppressWarnings("unchecked")
	private SkillServlet createAlexaServlet() {
		return new SkillServlet(Skills.standard()
				.addRequestHandlers(
						launchHandler,
						intentHandler)
				.withSkillId(config.getAlexaProperties().getSkillId())
				.build());
	}
}
