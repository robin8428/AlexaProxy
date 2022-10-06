package proxy.alexa;

import static com.amazon.ask.servlet.ServletConstants.DEFAULT_TOLERANCE_MILLIS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import com.amazon.ask.servlet.util.ServletUtils;
import com.amazon.ask.servlet.verifiers.AlexaHttpRequest;
import com.amazon.ask.servlet.verifiers.ServletRequest;
import com.amazon.ask.servlet.verifiers.SkillRequestSignatureVerifier;
import com.amazon.ask.servlet.verifiers.SkillRequestTimestampVerifier;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;

import proxy.alexa.handler.IntentRequestHandler;
import proxy.util.Properties;


@RestController
public class AlexaController {

	private final List<SkillServletVerifier> verifiers;
	private final Skill skill;

	@Autowired
	public AlexaController(Properties config, IntentRequestHandler intentHandler) {
		this.skill = Skills.standard()
				.addRequestHandler(intentHandler)
				.withSkillId(config.getAlexaProperties().getSkillId())
				.build();

		List<SkillServletVerifier> defaultVerifiers = new ArrayList<>();
		if (!ServletUtils.isRequestSignatureCheckSystemPropertyDisabled() && !config.getAlexaProperties().getDisableSignatureVerification()) {
			defaultVerifiers.add(new SkillRequestSignatureVerifier());
		}
		if (!config.getAlexaProperties().getDisableTimestampVerification()) {
			Long timestampToleranceProperty = ServletUtils.getTimeStampToleranceSystemProperty();
			defaultVerifiers.add(new SkillRequestTimestampVerifier(timestampToleranceProperty != null
					? timestampToleranceProperty
					: DEFAULT_TOLERANCE_MILLIS));
		}
		defaultVerifiers.add(new SkillRequestAuthorizationVerifier(config.getAlexaProperties()));
		this.verifiers = defaultVerifiers;
	}


	@RequestMapping(path = { "/", "/alexa" })
	public ResponseEnvelope customSkill(@RequestBody RequestEnvelope envelope, ContentCachingRequestWrapper servletRequest) throws IOException {
		byte[] serializedEnvelope = servletRequest.getContentAsByteArray();
		final AlexaHttpRequest alexaHttpRequest = new ServletRequest(servletRequest, serializedEnvelope, envelope);

		for (SkillServletVerifier verifier : verifiers) {
			verifier.verify(alexaHttpRequest);
		}

		return skill.invoke(envelope);
	}
}
