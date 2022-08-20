package proxy.alexa.handler;

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;


class HandlerUtils {

	static Optional<Response> buildSuccessResponse(HandlerInput handlerInput, String text) {
		return handlerInput.getResponseBuilder()
				.withSpeech(text)
				.build();
	}


	static Optional<Response> buildExceptionResponse(HandlerInput handlerInput, String text, Exception e) {
		if (text.endsWith(".")) {
			text = text.substring(0, text.length() - 1) + ":";
		}
		return handlerInput.getResponseBuilder()
				.withSpeech(text + " " + e.getClass().getSimpleName())
				.withSimpleCard("Exception", e.getMessage())
				.build();
	}
}
