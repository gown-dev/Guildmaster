package exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.ErrorResponse;

@ControllerAdvice
public class AuthExceptionHandler {
	
	@ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleResponseStatus(AuthException ex, HttpServletRequest request, HttpServletResponse response) {
        ErrorResponse error = new ErrorResponse(ex.getError());
		return ResponseEntity.status(ex.getHttpStatus()).contentType(MediaType.APPLICATION_JSON).body(error);
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request, HttpServletResponse response) {
    	ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
	
}