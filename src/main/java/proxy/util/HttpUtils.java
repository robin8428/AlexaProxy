package proxy.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class HttpUtils {

	private static final Logger LOG = LogManager.getLogger();

	public static void tryCallback(String url, String auth) throws IOException {
		LOG.debug("Invoking callback '{}'", url);
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader("Authorization", auth);

		HttpResponse response = httpclient.execute(httppost);
		int statusCode = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		String entityContent = entity != null
				? EntityUtils.toString(entity, StandardCharsets.UTF_8)
				: "";

		LOG.debug("Received response code: '{}' body: '{}'", statusCode, entityContent);
		if (statusCode < 200 || statusCode > 299) {
			throw new NokResponseException(statusCode, entityContent);
		}
	}

	@SuppressWarnings("serial")
	private static class NokResponseException extends RuntimeException {

		public NokResponseException(int statusCode, String body) {
			super("Received nok response code: " + statusCode + " body: " + body);
		}
	}
}
