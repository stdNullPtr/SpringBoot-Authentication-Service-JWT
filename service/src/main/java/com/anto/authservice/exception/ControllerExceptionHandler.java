package com.anto.authservice.exception;

import com.anto.authservice.model.payload.response.ErrorResponse;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {

    @ExceptionHandler({ApiException.class, IllegalArgumentException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleServiceException(RuntimeException ex, WebRequest request) {
        return new ErrorResponse(ex.getMessage(), Date.from(Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        Gson gson = new Gson();
        List<String> fieldErrors = result.getFieldErrors().stream()
                .map(f -> gson.toJson(new CustomFieldError(f.getField(), Optional.ofNullable(f.getDefaultMessage()).orElse(""))))
                .toList();

        return new ErrorResponse("Bad request: " + fieldErrors, Date.from(Instant.now()));
    }

    @AllArgsConstructor
    @Getter
    private static class CustomFieldError {
        private final String fieldName;
        private final String defaultMessage;
    }
}