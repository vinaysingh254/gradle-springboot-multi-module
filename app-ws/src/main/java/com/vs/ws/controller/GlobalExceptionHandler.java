package com.vs.ws.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception e,
                                            WebRequest request) {
        List<String> errors = new ArrayList<>();
        errors.add(e.getLocalizedMessage());
        return buildErrorMsg(errors, "Error Occurred!", BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<String> errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getObjectName() + " : " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        return buildErrorMsg(errorMsg, "Validation Errors!", BAD_REQUEST);
    }

    private ResponseEntity<Object> buildErrorMsg(List<String> details,
                                                 String msg,
                                                 HttpStatus badRequest) {
        return ResponseEntityBuilder.build(ApiError.builder()
                                                   .errors(details)
                                                   .message(msg)
                                                   .status(badRequest)
                                                   .timestamp(LocalDateTime.now()).build());
    }
}
