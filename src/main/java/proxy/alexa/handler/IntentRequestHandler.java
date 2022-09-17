package proxy.alexa.handler;

import java.util.List;
import java.util.Optional;

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

import proxy.service.ActionExectuorService;
import proxy.util.Properties;


@Component
public class IntentRequestHandler implements com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler {

	private static final Logger LOG = LogManager.getLogger();
	private final Properties properties;
	private final ActionExectuorService exectuorService;

	@Autowired
	IntentRequestHandler(Properties properties, ActionExectuorService exectuorService) {
		this.properties = properties;
		this.exectuorService = exectuorService;
	}


	@Override
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
		return intentRequest.getIntent().getName().equals(properties.getAlexaProperties().getIntent().getName());
	}


	@Override
	public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
		final String entity = getIdFromSlot(intentRequest.getIntent().getSlots().get(properties.getAlexaProperties().getIntent().getEntitySlot()));
		final String action = getIdFromSlot(intentRequest.getIntent().getSlots().get(properties.getAlexaProperties().getIntent().getActionSlot()));
		LOG.info("Requesting action '{}' for room '{}'", action, entity);

		try {
			final Optional<String> successSentence = exectuorService.execute(entity, action);

			if (successSentence.isPresent()) {
				return HandlerUtils.buildSuccessResponse(input, entity, successSentence.get());
			}

			LOG.warn("Could not find callback for action '{}' and room '{}'", action, entity);
			return Optional.empty();

		} catch (Exception e) {
			LOG.error("Exception ocurred trying to invoke callback", e);
			return HandlerUtils.buildExceptionResponse(input, entity, properties.getProxyProperties().getFallbackNokSentence(), e);
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
}