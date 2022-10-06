package proxy.alexa;

import com.amazon.ask.model.Context;
import com.amazon.ask.servlet.verifiers.AlexaHttpRequest;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;

import proxy.util.Properties.AlexaProperties;


public class SkillRequestAuthorizationVerifier implements SkillServletVerifier {

	private final AlexaProperties properties;

	public SkillRequestAuthorizationVerifier(AlexaProperties properties) {
		this.properties = properties;
	}


	@Override
	public void verify(AlexaHttpRequest alexaHttpRequest) throws SecurityException {
		final Context requestContext = alexaHttpRequest.getDeserializedRequestEnvelope().getContext();
		final String userId = requestContext.getSystem().getUser().getUserId();
		final String deviceId = requestContext.getSystem().getDevice().getDeviceId();

		boolean userVerified = properties.getAuthorizedUserIds().stream()
				.map(userId::equals)
				.reduce(Boolean::logicalOr)
				.orElse(true);
		boolean deviceVerified = properties.getAuthorizedDeviceIds().stream()
				.map(deviceId::equals)
				.reduce(Boolean::logicalOr)
				.orElse(true);

		if (!userVerified || !deviceVerified) {
			throw new SecurityException("Failed to verify the user/device for the provided skill request");
		}
	}
}
