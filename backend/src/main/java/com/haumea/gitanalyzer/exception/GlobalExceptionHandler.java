// adapted from: https://github.com/RameshMF/springboot-crud-restful-webservices
// adapted from: https://github.com/brunocleite/spring-boot-exception-handling

package com.haumea.gitanalyzer.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // handling specific exception
    // @Valid @RequestBody throws exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> methodArgumentNotValidExceptionHandling(MethodArgumentNotValidException exception,
                                                                          WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Validation error",request, HttpStatus.BAD_REQUEST));

    }

    // validations on @RequestParam throws exception
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationExceptionHandling(ConstraintViolationException exception,
                                                                          WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Validation error", request, HttpStatus.BAD_REQUEST));

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> httpMessageNotReadableExceptionHandling(HttpMessageNotReadableException exception,
                                                                       WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Malformed JSON request",request, HttpStatus.BAD_REQUEST));

    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> responseStatusExceptionHandling(ResponseStatusException exception,
                                                                  WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Error",request, exception.getStatus()));

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> resourceNotFoundHandling(ResourceNotFoundException exception, WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Requested resource not found", request,
                HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ResourceAlredyExistException.class)
    public ResponseEntity<Object> resourceAlredyExistExceptionHandling(ResourceAlredyExistException exception,
                                                                       WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "Resource already exists",request,
                HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(GitLabRuntimeException.class)
    public ResponseEntity<Object> gitLabRuntimeExceptionHandling(GitLabRuntimeException exception, WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());

        return buildResponseEntity(buildStandardError(exception, "GitLab server error",request,
                HttpStatus.BAD_REQUEST));
    }

    // handling global exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> globalExceptionHandling(Exception exception, WebRequest request){

        log.info(new Exception().getStackTrace()[0].getMethodName());
        log.info(exception.toString());

        return buildResponseEntity(buildStandardError(exception, "Unexpected error",request,
                HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // helper methods
    private ResponseEntity<Object> buildResponseEntity(ErrorDetails error){
        return new ResponseEntity<>(error, error.getStatus());
    }

    private ErrorDetails buildStandardError(Throwable exception, String message, WebRequest request, HttpStatus status){
        return new ErrorDetails(
                status,
                message,
                request.getDescription(false),
                exception);
    }
}
