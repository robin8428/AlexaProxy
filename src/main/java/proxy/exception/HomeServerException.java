package proxy.exception;

import java.time.LocalDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

import proxy.util.Utils;


public class HomeServerException extends RuntimeException {

	private static final long serialVersionUID = -2904510181145471822L;

	public static HomeServerException invalidRequest(String message) {
		return new HomeServerException(new ApiError(HttpStatus.BAD_REQUEST, message));
	}


	public static HomeServerException unauthorized() {
		return new HomeServerException(new ApiError(HttpStatus.UNAUTHORIZED, null));
	}


	public static HomeServerException error(Exception e) {
		return new HomeServerException(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, e.toString()));
	}

	private final ApiError apiError;

	public HomeServerException(ApiError apiError) {
		this.apiError = apiError;
	}


	public ApiError getApiError() {
		return apiError;
	}


	@Override
	public String toString() {
		try {
			return Utils.OBJECT_MAPPER.writeValueAsString(apiError);
		} catch (JsonProcessingException e) {
			ExceptionUtils.rethrow(e);
		}
		return null;
	}

	public static class ApiError {

		private final HttpStatus status;
		private final String message;
		private final String error;
		private final LocalDateTime timestamp;

		public ApiError(HttpStatus status, String message) {
			this.status = status;
			this.message = message;
			this.error = status.getReasonPhrase();
			this.timestamp = LocalDateTime.now();
		}


		@JsonIgnore
		public HttpStatus getHttpStatus() {
			return status;
		}


		@JsonGetter("status")
		public int getStatus() {
			return status.value();
		}


		@JsonGetter("message")
		public String getMessage() {
			return message;
		}


		@JsonGetter("error")
		public String getError() {
			return error;
		}


		@JsonGetter("timestamp")
		public String getTimestamp() {
			return Utils.DATE_FORMATTER.format(timestamp);
		}
	}
}