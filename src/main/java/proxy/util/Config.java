package proxy.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Config {

	@Value("${server.port}")
	private int port;
	@Value("${alexa.skill-id}")
	private String skillId;

	public String getSkillId() {
		return skillId;
	}
}
