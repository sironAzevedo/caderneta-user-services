package br.com.user.handler;

import com.br.azevedo.exception.*;
import com.br.azevedo.model.vo.StandardError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ExceptionHandler(value = { EmptyResultDataAccessException.class, NotFoundException.class, UserException.class })
	public com.br.azevedo.model.vo.StandardError tNotFound(RuntimeException e, HttpServletRequest request) {
		return com.br.azevedo.model.vo.StandardError.builder(HttpStatus.NOT_FOUND.value(), e.getMessage(), new Date());
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(value = { InternalErrorException.class })
	public com.br.azevedo.model.vo.StandardError internalError(RuntimeException e, HttpServletRequest request) {
		return com.br.azevedo.model.vo.StandardError.builder(HttpStatus.NOT_FOUND.value(), e.getMessage(), new Date());
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(value = { AuthenticationException.class })
	public com.br.azevedo.model.vo.StandardError handleAuthenticationException(AuthenticationException authenticationException) {
		return com.br.azevedo.model.vo.StandardError.builder(HttpStatus.UNAUTHORIZED.value(), authenticationException.getMessage(), new Date());
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler(value = { AuthorizationException.class })
	public com.br.azevedo.model.vo.StandardError handleAuthorizationException(AuthorizationException authorizationException) {
		return com.br.azevedo.model.vo.StandardError.builder(HttpStatus.FORBIDDEN.value(), authorizationException.getMessage(), new Date());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
																  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		com.br.azevedo.model.vo.StandardError error = com.br.azevedo.model.vo.StandardError.builder(HttpStatus.BAD_REQUEST.value(), "Error de validação", new Date(),
				ex.getBindingResult().getFieldErrors());

		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
															 HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		return new ResponseEntity<>(StandardError.builder(statusCode.value(), ex.getLocalizedMessage(), new Date()),
				headers, statusCode);
	}
}
