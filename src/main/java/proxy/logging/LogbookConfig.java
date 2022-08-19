package proxy.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.Logbook;


@Configuration
public class LogbookConfig {

	@Bean
	public Logbook logbook() {
		return Logbook.builder()
				.sink(new DefaultSink(
						new DefaultHttpLogFormatter(),
						new DefaultHttpLogWriter()))
				.build();
	}
}
