package proxy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


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

	@Configuration
	@ConfigurationProperties("alexa")
	public static class AlexaProperties {

		private String skillId;
		private String intentName;
		private String intentRoomSlot;
		private String intentActionSlot;

		public void setSkillId(String skillId) {
			this.skillId = skillId;
		}


		public String getSkillId() {
			return skillId;
		}


		public String getSkillUUID() {
			return skillId.split(Pattern.quote("."))[3];
		}


		public void setIntentName(String intentName) {
			this.intentName = intentName;
		}


		public String getIntentName() {
			return intentName;
		}


		public void setIntentRoomSlot(String intentRoomSlot) {
			this.intentRoomSlot = intentRoomSlot;
		}


		public String getIntentRoomSlot() {
			return intentRoomSlot;
		}


		public void setIntentActionSlot(String intentActionSlot) {
			this.intentActionSlot = intentActionSlot;
		}


		public String getIntentActionSlot() {
			return intentActionSlot;
		}
	}

	@Configuration
	@ConfigurationProperties("proxy")
	public static class ProxyProperties {

		private List<RoomProperties> rooms;
		private String fallbackNokSentence;

		public void setRooms(List<RoomProperties> rooms) {
			this.rooms = rooms;
		}


		public List<RoomProperties> getRooms() {
			return rooms;
		}


		public void setFallbackNokSentence(String fallbackNokSentence) {
			this.fallbackNokSentence = fallbackNokSentence;
		}


		public String getFallbackNokSentence() {
			return fallbackNokSentence;
		}

		public static class RoomProperties {

			private String roomName;
			private List<RoomAction> roomActions;

			public void setRoomName(String roomName) {
				this.roomName = roomName;
			}


			public String getRoomName() {
				return roomName;
			}


			public void setRoomActions(List<RoomAction> roomActions) {
				this.roomActions = roomActions;
			}


			public List<RoomAction> getRoomActions() {
				return new ArrayList<>(roomActions);
			}

			public static class RoomAction {

				private String actionName;
				private ActionCallback actionCallback;

				public void setActionName(String actionName) {
					this.actionName = actionName;
				}


				public String getActionName() {
					return actionName;
				}


				public void setActionCallback(ActionCallback actionCallback) {
					this.actionCallback = actionCallback;
				}


				public ActionCallback getActionCallback() {
					return actionCallback;
				}

				public static class ActionCallback {

					private String callbackUrl;
					private String callbackAuthentication;
					private String callbackSuccessSentence;

					public void setCallbackUrl(String callbackUrl) {
						this.callbackUrl = callbackUrl;
					}


					public String getCallbackUrl() {
						return callbackUrl;
					}


					public void setCallbackAuthentication(String callbackAuthentication) {
						this.callbackAuthentication = callbackAuthentication;
					}


					public String getCallbackAuthentication() {
						return callbackAuthentication;
					}


					public void setCallbackSuccessSentence(String callbackSuccessSentence) {
						this.callbackSuccessSentence = callbackSuccessSentence;
					}


					public String getCallbackSuccessSentence() {
						return callbackSuccessSentence;
					}
				}
			}
		}
	}
}
