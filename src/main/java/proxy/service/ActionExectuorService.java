package proxy.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.fauxpas.FauxPas;

import proxy.util.Properties;
import proxy.util.Properties.ProxyProperties.EntityProperties;
import proxy.util.Properties.ProxyProperties.EntityProperties.EntityAction;
import proxy.util.Properties.ProxyProperties.EntityProperties.EntityAction.ActionCallback;


@Service
public class ActionExectuorService {

	private static final Logger LOG = LogManager.getLogger();
	private final Properties properties;

	@Autowired
	public ActionExectuorService(Properties properties) {
		this.properties = properties;
	}


	public Optional<String> execute(String entity, String action) throws IOException {
		final AtomicReference<EntityAction> chosenAction = new AtomicReference<>();

		final boolean success = properties.getProxyProperties().getEntities().stream()
				.filter(r -> r.getName().equals(entity))
				.map(EntityProperties::getActions)
				.flatMap(List::stream)
				.filter(a -> a.getName().equals(action))
				.peek(chosenAction::set)
				.map(EntityAction::getCallbacks)
				.flatMap(List::stream)
				.map(FauxPas.throwingFunction(ActionCallback::execute))
				.reduce(Boolean::logicalAnd)
				.orElse(false);

		if (success) {
			return Optional.of(chosenAction.get().getSuccessSentence());
		}

		LOG.warn("Could not find callback for action '{}' and room '{}'", action, entity);
		return Optional.empty();
	}
}
