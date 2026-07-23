package com.tiendev.task_management_api.helper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.tiendev.task_management_api.exception.ResourceAlreadyExistsException;
import com.tiendev.task_management_api.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandle {
	// bat exception ma chua thiet lap
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleAllException(Exception ex) {
		System.out.println(ex);
		return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler({ ResourceAlreadyExistsException.class, ResourceNotFoundException.class })
	public ResponseEntity<?> handleNotFounf(Exception ex) {
		return ApiResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	// loi truyen url
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String errorMessage = String.format("The parameter '%s' has a value '%s' that is incorrectly formatted.",
				ex.getName(), ex.getValue());
		return ApiResponse.error(HttpStatus.BAD_REQUEST, errorMessage);
	}

	// validate input
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		List<String> errorList = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList());
		String errors = String.join("; ", errorList);

		ApiResponse<Object> response = new ApiResponse<>(HttpStatus.BAD_REQUEST, errors, null, "VALIDATION_ERROR");
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
}
