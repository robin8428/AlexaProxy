package proxy.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StreamUtils;


public class ExecUtils {

	private static final Logger LOG = LogManager.getLogger();
	private static final Map<String, Object> LOCK_OBJECTS = new HashMap<>();

	public static String exec(String[] cmd, String syncId) throws IOException {

		if (StringUtils.isNotBlank(syncId)) {
			Object lock = LOCK_OBJECTS.computeIfAbsent(syncId, k -> new Object());
			synchronized (lock) {
				return exec(cmd);
			}
		}

		return exec(cmd);
	}


	private static String exec(String... command) throws IOException {
		return humanReadable(execRaw(command));
	}


	private static byte[] execRaw(String... command) throws IOException {
		LOG.debug("exec '{}'", String.join(" ", command));
		long callTime = System.currentTimeMillis();

		final Process process;
		if (command.length == 1) {
			process = Runtime.getRuntime().exec(command[0]);
		} else {
			process = Runtime.getRuntime().exec(command);
		}

		new Thread() {

			@Override
			public void run() {
				String errorString = readStream(() -> process.getErrorStream());
				if (!errorString.matches("(?s)\\s*"))
					LOG.error(humanReadable(errorString));
			}
		}.start();

		byte[] result = readStreamRaw(() -> process.getInputStream());
		LOG.debug("exec returned {} bytes in {}ms", result.length, System.currentTimeMillis() - callTime);
		return result;
	}


	private static String readStream(Supplier<InputStream> supplier) {
		String result = "";

		try (InputStream is = supplier.get()) {

			result = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
			is.close();

		} catch (IOException e) {
			ExceptionUtils.rethrow(e);
		}

		return result;
	}


	private static byte[] readStreamRaw(Supplier<InputStream> supplier) {
		byte[] result = new byte[0];

		try (InputStream is = supplier.get()) {

			result = StreamUtils.copyToByteArray(is);
			is.close();

		} catch (IOException e) {
			ExceptionUtils.rethrow(e);
		}

		return result;
	}


	private static String humanReadable(byte[] bytes) {
		return humanReadable(new String(bytes, StandardCharsets.UTF_8));
	}


	private static String humanReadable(String string) {
		return Arrays.stream(string.split("\\R"))
				.map(s -> s.replaceAll("\\p{C}", " ")
						.replaceAll("\\s+", " ")
						.trim())
				.collect(Collectors.joining("\n"))
				.replaceFirst("\\n$", "");
	}
}
