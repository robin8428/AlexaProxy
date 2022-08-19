package proxy;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class AlexaProxy {

	private static final Logger LOG = LogManager.getLogger();
	private static AlexaProxy instance;

	private ApplicationContext ctx;

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(AlexaProxy.class).build().run(args);
		init(ctx);
	}


	public static AlexaProxy getInstance() {
		if (instance == null) {
			instance = new AlexaProxy();
		}
		return instance;
	}


	public static void init(ApplicationContext ctx) {
		AlexaProxy.getInstance().ctx = ctx;

		LOG.info("AlexyProxy startup successful.");
	}


	public <T> T getBean(Class<T> clazz) {
		return ctx.getBean(clazz);
	}


	@PreDestroy
	private void shutdown() {
		LOG.trace("shutting down server...");

		LOG.trace("server shut down successfully");
		System.exit(0);
	}
}
