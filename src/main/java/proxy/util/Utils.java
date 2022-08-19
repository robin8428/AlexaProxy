package proxy.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import proxy.exception.HomeServerException;


public class Utils {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.setSerializationInclusion(Include.NON_NULL);

	private static final Logger LOG = LogManager.getLogger();

	public static String hashPassword(String rawPassword) {
		try {
			return hash(rawPassword, "SHA-256");
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.toString());
		}
		throw HomeServerException.invalidRequest("auth header invalid");
	}


	public static String hash(String content, String alg) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(alg);
		byte[] encodedhash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encodedhash);
	}


	public static String humanReadable(byte[] bytes) {
		return humanReadable(new String(bytes, StandardCharsets.UTF_8));
	}


	public static String humanReadable(String string) {
		return Arrays.stream(string.split("\\R"))
				.map(s -> s.replaceAll("\\p{C}", " ")
						.replaceAll("\\s+", " ")
						.trim())
				.collect(Collectors.joining("\n"))
				.replaceFirst("\\n$", "");
	}


	public static synchronized String exec(String... command) {
		return execAsync(command);
	}


	public static String execAsync(String... command) {
		return humanReadable(execAsyncRaw(command));
	}


	public static byte[] execAsyncRaw(String... command) {
		LOG.trace("exec '{}'", String.join(" ", command));
		long callTime = System.currentTimeMillis();

		Process process = null;
		try {
			if (command.length == 1) {
				process = Runtime.getRuntime().exec(command[0]);
			} else {
				process = Runtime.getRuntime().exec(command);
			}
		} catch (IOException e) {
			throw HomeServerException.error(e);
		}

		final Process createdProcess = process;

		new Thread() {

			@Override
			public void run() {
				String errorString = readStream(() -> createdProcess.getErrorStream());
				if (!errorString.matches("(?s)\\s*"))
					LOG.error(humanReadable(errorString));
			}
		}.start();

		byte[] result = readStreamRaw(() -> createdProcess.getInputStream());
		LOG.trace("exec returned {} bytes in {}ms", result.length, System.currentTimeMillis() - callTime);
		return result;
	}


	public static boolean ipReachable(String ip) {
		Process process = null;
		try {
			if (System.getProperty("os.name").length() >= 7 && System.getProperty("os.name").substring(0, 7).equals("Windows"))
				process = Runtime.getRuntime().exec("ping -n 1 " + ip);
			else
				process = Runtime.getRuntime().exec("ping -c 1 " + ip);
		} catch (Exception e) {
			LOG.error(e.toString());
			return false;
		}
		final Process createdProcess = process;
		return readStream(() -> createdProcess.getInputStream()).contains("ms");
	}


	public static String readStream(Supplier<InputStream> supplier) {
		String result = "";

		try (InputStream is = supplier.get()) {

			result = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
			is.close();

		} catch (IOException e) {
			throw HomeServerException.error(e);
		}

		return result;
	}


	public static byte[] readStreamRaw(Supplier<InputStream> supplier) {
		byte[] result = new byte[0];

		try (InputStream is = supplier.get()) {

			result = StreamUtils.copyToByteArray(is);
			is.close();

		} catch (IOException e) {
			throw HomeServerException.error(e);
		}

		return result;
	}
}
