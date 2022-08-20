package proxy.alexa.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;


@Component
public class LaunchRequestHandler implements RequestHandler {

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(Predicates.requestType(LaunchRequest.class));
	}


	@Override
	public Optional<Response> handle(HandlerInput input) {
		String speechText = "Welcome to the Alexa Skills Kit, you can say hello";
		return HandlerUtils.buildResponse(input, "Hello World", speechText);
	}

}
