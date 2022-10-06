package proxy.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;

import proxy.util.Properties.ProxyProperties.EntityProperties.EntityAction.ActionCallback;


@Configuration
public class Properties {

	@Value("${server.port}")
	private int port;
	@Autowired
	private AlexaProperties alexaProperties;
	@Autowired
	private ProxyProperties proxyProperties;

	public AlexaProperties getAlexaProperties() {
		return alexaProperties;
	}


	public ProxyProperties getProxyProperties() {
		return proxyProperties;
	}

	@Validated
	@Configuration
	@ConfigurationProperties("alexa")
	public static class AlexaProperties {

		@NotBlank
		private String skillId;
		@NotNull
		private IntentProperties intent;
		boolean disableTimestampVerification = false;
		boolean disableSignatureVerification = false;
		private List<String> authorizedUserIds = Collections.emptyList();
		private List<String> authorizedDeviceIds = Collections.emptyList();

		public void setSkillId(String skillId) {
			this.skillId = skillId;
		}


		public String getSkillId() {
			return skillId;
		}


		public String getSkillUUID() {
			return skillId.split(Pattern.quote("."))[3];
		}


		public void setIntent(IntentProperties intent) {
			this.intent = intent;
		}


		public IntentProperties getIntent() {
			return intent;
		}


		public void setDisableTimestampVerification(boolean disableTimestampVerification) {
			this.disableTimestampVerification = disableTimestampVerification;
		}


		public boolean getDisableTimestampVerification() {
			return disableTimestampVerification;
		}


		public void setDisableSignatureVerification(boolean disableSignatureVerification) {
			this.disableSignatureVerification = disableSignatureVerification;
		}


		public boolean getDisableSignatureVerification() {
			return disableSignatureVerification;
		}


		public void setAuthorizedUserIds(List<String> authorizedUserIds) {
			this.authorizedUserIds = authorizedUserIds;
		}


		public List<String> getAuthorizedUserIds() {
			return authorizedUserIds;
		}


		public void setAuthorizedDeviceIds(List<String> authorizedDeviceIds) {
			this.authorizedDeviceIds = authorizedDeviceIds;
		}


		public List<String> getAuthorizedDeviceIds() {
			return authorizedDeviceIds;
		}

		@Validated
		public static class IntentProperties {

			@NotBlank
			private String name;
			@NotBlank
			private String entitySlot;
			@NotBlank
			private String actionSlot;

			public void setName(String name) {
				this.name = name;
			}


			public String getName() {
				return name;
			}


			public void setEntitySlot(String entitySlot) {
				this.entitySlot = entitySlot;
			}


			public String getEntitySlot() {
				return entitySlot;
			}


			public void setActionSlot(String actionSlot) {
				this.actionSlot = actionSlot;
			}


			public String getActionSlot() {
				return actionSlot;
			}
		}
	}

	@Validated
	@Configuration
	@ConfigurationProperties("proxy")
	public static class ProxyProperties implements Validator {

		private List<EntityProperties> entities;
		@NotBlank
		private String fallbackNokSentence;

		public void setEntities(List<EntityProperties> entities) {
			this.entities = entities;
		}


		public List<EntityProperties> getEntities() {
			return entities;
		}


		public void setFallbackNokSentence(String fallbackNokSentence) {
			this.fallbackNokSentence = fallbackNokSentence;
		}


		public String getFallbackNokSentence() {
			return fallbackNokSentence;
		}

		@Validated
		public static class EntityProperties {

			@NotBlank
			private String name;
			private List<EntityAction> actions;

			public void setName(String name) {
				this.name = name;
			}


			public String getName() {
				return name;
			}


			public void setActions(List<EntityAction> actions) {
				this.actions = actions;
			}


			public List<EntityAction> getActions() {
				return actions;
			}

			@Validated
			public static class EntityAction {

				@NotBlank
				private String name;
				@NotBlank
				private String successSentence;
				private List<ActionCallback> callbacks;

				public void setName(String name) {
					this.name = name;
				}


				public String getName() {
					return name;
				}


				public void setSuccessSentence(String successSentence) {
					this.successSentence = successSentence;
				}


				public String getSuccessSentence() {
					return successSentence;
				}


				public void setCallbacks(List<ActionCallback> callbacks) {
					this.callbacks = callbacks;
				}


				public List<ActionCallback> getCallbacks() {
					return callbacks;
				}

				@Validated
				public static class ActionCallback {

					private String httpUrl;
					private String httpAuthentication;

					private String[] execCmd = new String[0];
					private String execSynchronizationId;

					public void setHttpUrl(String httpUrl) {
						this.httpUrl = httpUrl;
					}


					public String getHttpUrl() {
						return httpUrl;
					}


					public void setHttpAuthentication(String httpAuthentication) {
						this.httpAuthentication = httpAuthentication;
					}


					public String getHttpAuthentication() {
						return httpAuthentication;
					}


					public void setExecCmd(String[] execCmd) {
						this.execCmd = execCmd;
					}


					public String[] getExecCmd() {
						return execCmd;
					}


					public void setExecSynchronizationId(String execSynchronizationId) {
						this.execSynchronizationId = execSynchronizationId;
					}


					public String getExecSynchronizationId() {
						return execSynchronizationId;
					}


					public boolean execute() throws IOException {
						if (StringUtils.isNotBlank(httpUrl)) {
							HttpUtils.tryCallback(httpUrl, httpAuthentication);
						}
						if (execCmd.length > 0) {
							ExecUtils.exec(execCmd, execSynchronizationId);
						}
						return true;
					}
				}
			}
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return ActionCallback.class.isAssignableFrom(clazz);
		}


		@Override
		public void validate(Object target, Errors errors) {
			ActionCallback actionCallback = (ActionCallback) target;

			if (StringUtils.isNotBlank(actionCallback.httpAuthentication)) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "httpUrl", "field.required", "must not be empty if httpAuthentication is provided");
			}
			if (StringUtils.isNotBlank(actionCallback.execSynchronizationId) && actionCallback.execCmd.length == 0) {
				errors.rejectValue("execCmd", "field.required", "must not be empty if execSynchronizationId is provided");
			}
			if (StringUtils.isBlank(actionCallback.httpUrl) && actionCallback.execCmd.length == 0) {
				errors.reject("field.required", "at least one httpUrl or execCmd must be provided");
			}
		}
	}
}
