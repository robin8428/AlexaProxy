package proxy.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class ExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler({ HomeServerException.class })
	public ResponseEntity<String> handleHomeServerException(HomeServerException e) {
		return ResponseEntity
				.status(e.getApiError().getHttpStatus())
				.header("Content-Type", "application/json")
				.body(e.toString());
	}
}
