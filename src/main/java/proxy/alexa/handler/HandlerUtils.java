package proxy.alexa.handler;

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;


class HandlerUtils {

	static Optional<Response> buildResponse(HandlerInput handlerInput, String title, String text) {
		return handlerInput.getResponseBuilder()
				.withSpeech(text)
				.withSimpleCard(title, text)
				.withReprompt(text)
				.build();
	}
}
