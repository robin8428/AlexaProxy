package proxy.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import com.amazon.ask.servlet.verifiers.SkillRequestSignatureVerifier;
import com.amazon.ask.servlet.verifiers.SkillRequestTimestampVerifier;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;

import proxy.alexa.AlexaController;


@TestComponent
public class TestUtils {

	@Autowired
	private AlexaController alexaController;
	private static final Object VALIDATION_OVERRIDE_SYNC = new Object();

	@SuppressWarnings("unchecked")
	public void runWithDisabledTimestampValidation(Runnable runnable) {
		synchronized (VALIDATION_OVERRIDE_SYNC) {
			List<SkillServletVerifier> verifiers = (List<SkillServletVerifier>) getPrivateValue(alexaController, "verifiers");

			Optional<SkillServletVerifier> verifier = verifiers.stream()
					.filter(v -> v instanceof SkillRequestTimestampVerifier)
					.findFirst();
			verifier.ifPresent(verifiers::remove);

			runnable.run();

			verifier.ifPresent(verifiers::add);
		}
	}


	@SuppressWarnings("unchecked")
	public void runWithEnabledSignatureValidation(Runnable runnable) {
		synchronized (VALIDATION_OVERRIDE_SYNC) {
			List<SkillServletVerifier> verifiers = (List<SkillServletVerifier>) getPrivateValue(alexaController, "verifiers");

			SkillServletVerifier verifier = new SkillRequestSignatureVerifier();
			verifiers.add(verifier);

			runnable.run();

			verifiers.remove(verifier);
		}
	}


	@SuppressWarnings("unchecked")
	public void runWithEnabledSignatureValidationAndDisabledTimestampValidation(Runnable runnable) {
		synchronized (VALIDATION_OVERRIDE_SYNC) {
			List<SkillServletVerifier> verifiers = (List<SkillServletVerifier>) getPrivateValue(alexaController, "verifiers");

			SkillServletVerifier signatureVerifier = new SkillRequestSignatureVerifier();
			verifiers.add(signatureVerifier);

			Optional<SkillServletVerifier> timestampVerifier = verifiers.stream()
					.filter(v -> v instanceof SkillRequestTimestampVerifier)
					.findFirst();
			timestampVerifier.ifPresent(verifiers::remove);

			runnable.run();

			timestampVerifier.ifPresent(verifiers::add);

			verifiers.remove(signatureVerifier);
		}
	}


	private static Object getPrivateValue(Object parent, String fieldName) {
		try {
			Field field = parent.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(parent);
		} catch (Exception e) {
			ExceptionUtils.rethrow(e);
		}
		return null;
	}
}
