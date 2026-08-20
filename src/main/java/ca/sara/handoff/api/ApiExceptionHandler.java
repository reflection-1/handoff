package ca.sara.handoff.api;

import ca.sara.handoff.service.HandoffNotFoundException;
import ca.sara.handoff.service.InvalidStatusTransitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HandoffNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(HandoffNotFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(error.getMessage(), Map.of(), Instant.now()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    ResponseEntity<ApiError> handleConflict(InvalidStatusTransitionException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(error.getMessage(), Map.of(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException error) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError("please check the highlighted fields", fieldErrors, Instant.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException error) {
        return ResponseEntity.badRequest().body(new ApiError(
                "check that shift, priority, and status use supported values",
                Map.of(),
                Instant.now()
        ));
    }

    record ApiError(String message, Map<String, String> fieldErrors, Instant timestamp) {
    }
}
