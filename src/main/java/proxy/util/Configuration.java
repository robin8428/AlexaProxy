package proxy.util;

import org.springframework.beans.factory.annotation.Value;


@org.springframework.context.annotation.Configuration
public class Configuration {

	@Value("${server.port}")
	private int port;

}
