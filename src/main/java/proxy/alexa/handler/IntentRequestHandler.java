package proxy.alexa.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;

import proxy.util.Properties;
import proxy.util.Properties.ProxyProperties.RoomProperties;
import proxy.util.Properties.ProxyProperties.RoomProperties.RoomAction;
import proxy.util.Properties.ProxyProperties.RoomProperties.RoomAction.ActionCallback;


@Component
public class IntentRequestHandler implements com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler {

	private static final Logger LOG = LogManager.getLogger();
	private final Properties properties;

	@Autowired
	IntentRequestHandler(Properties properties) {
		this.properties = properties;
	}


	@Override
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
		return intentRequest.getIntent().getName().equals(properties.getAlexaProperties().getIntentName());
	}


	@Override
	public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
		final String room = getIdFromSlot(intentRequest.getIntent().getSlots().get(properties.getAlexaProperties().getIntentRoomSlot()));
		final String action = getIdFromSlot(intentRequest.getIntent().getSlots().get(properties.getAlexaProperties().getIntentActionSlot()));
		LOG.info("Requesting action '{}' for room '{}'", action, room);

		Optional<ActionCallback> optionalCallback = properties.getProxyProperties().getRooms().stream()
				.filter(r -> r.getRoomName().equals(room))
				.map(RoomProperties::getRoomActions)
				.flatMap(List::stream)
				.filter(a -> a.getActionName().equals(action))
				.map(RoomAction::getActionCallback)
				.findFirst();

		if (optionalCallback.isEmpty()) {
			LOG.warn("Could not find callback for action '{}' and room '{}'", action, room);
			return Optional.empty();
		}

		ActionCallback callback = optionalCallback.get();

		try {
			tryCallback(callback.getCallbackUrl(), callback.getCallbackAuthentication());
			return HandlerUtils.buildSuccessResponse(input, room, callback.getCallbackSuccessSentence());
		} catch (Exception e) {
			LOG.error("Exception ocurred trying to invoke callback", e);
			return HandlerUtils.buildExceptionResponse(input, room, properties.getProxyProperties().getFallbackNokSentence(), e);
		}
	}


	private String getIdFromSlot(Slot slot) {
		return slot.getResolutions().getResolutionsPerAuthority().stream()
				.filter(r -> r.getAuthority().contains(properties.getAlexaProperties().getSkillUUID()))
				.map(Resolution::getValues)
				.flatMap(List::stream)
				.map(ValueWrapper::getValue)
				.map(Value::getName)
				.findFirst()
				.orElseThrow();
	}


	private void tryCallback(String url, String auth) throws IOException {
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