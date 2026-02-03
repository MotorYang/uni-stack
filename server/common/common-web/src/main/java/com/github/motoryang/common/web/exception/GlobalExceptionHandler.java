package com.github.motoryang.common.web.exception;

import com.github.motoryang.common.core.exception.BusinessException;
import com.github.motoryang.common.core.result.RestResult;
import com.github.motoryang.common.core.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Global exception handler for Web MVC applications
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle business exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public RestResult<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return RestResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * Handle validation exceptions (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return RestResult.fail(ResultCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * Handle validation exceptions (@RequestParam, @PathVariable)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", message);
        return RestResult.fail(ResultCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * Handle binding exceptions
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Binding failed: {}", message);
        return RestResult.fail(ResultCode.PARAM_VALID_ERROR.getCode(), message);
    }

    /**
     * Handle missing request parameter
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing request parameter: {}", e.getParameterName());
        return RestResult.fail(ResultCode.PARAM_MISSING.getCode(), "Missing parameter: " + e.getParameterName());
    }

    /**
     * Handle request body parse failure
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestResult<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Request body parse failed: {}", e.getMessage());
        return RestResult.fail(ResultCode.BAD_REQUEST);
    }

    /**
     * Handle unsupported request method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public RestResult<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Request method not supported: {}", e.getMethod());
        return RestResult.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /**
     * Handle resource not found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RestResult<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        return RestResult.fail(ResultCode.NOT_FOUND);
    }

    /**
     * Handle other unknown exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult<Void> handleException(Exception e) {
        log.error("System exception", e);
        return RestResult.fail(ResultCode.INTERNAL_ERROR);
    }
}
