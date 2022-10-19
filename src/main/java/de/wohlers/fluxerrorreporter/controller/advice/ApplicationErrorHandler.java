package de.wohlers.fluxerrorreporter.controller.advice;

import de.wohlers.fluxerrorreporter.model.dtos.ProblemDTO;
import de.wohlers.fluxerrorreporter.model.exceptions.EntryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ProblemDTO> handleInternalError(Throwable e, ServerHttpRequest webRequest) {
        log.error("An Internal Server Error occurred", e);
        ProblemDTO problem = new ProblemDTO();
        problem.setTitle("An Internal Server Error occurred");
        problem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setType(URI.create("problem/internal_server_error"));

        // if used in publicly available service, consider exposing less detailed info
        problem.setDetail(String.format("Exception: %s, Message: %s", e.getClass(), e.getMessage()));
        problem.setInstance(webRequest.getURI());

        return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // You can use this pattern to implement handlers for specific exceptions
    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<ProblemDTO> handleNotFound(EntryNotFoundException e, ServerHttpRequest webRequest) {
        // a "not found" should be logged as warn. A client probably expected a result, where actually is no data
        log.warn("An entry could not be found", e);
        ProblemDTO problem = new ProblemDTO();
        problem.setTitle("An entry could not be found");
        problem.setStatus(HttpStatus.NOT_FOUND.value());
        problem.setType(URI.create("problem/not_found"));

        // if used in publicly available service, consider exposing less detailed info
        problem.setDetail(String.format("There is no entry with the given id: %s", "some id"));
        problem.setInstance(webRequest.getURI());

        return new ResponseEntity<>(problem, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDTO> handleInvalidParameter(ConstraintViolationException e, ServerHttpRequest webRequest) {
        // you could argue, that client errors can be logged as info instead, as there was no error in this service, but the client did something wrong
        // on the other hand: something went wrong and somebody has to raise an error
        log.error("The request was invalid", e);
        ProblemDTO problem = new ProblemDTO();
        problem.setTitle("The request was invalid");
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setType(URI.create("problem/bad_request/invalid_parameters"));

        // ProblemDTO may be extended with additional attributes, e.g. Map<String, String> validationErrors as "parameter name" -> "description of violation"
        problem.setDetail(e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")));
        problem.setInstance(webRequest.getURI());

        return new ResponseEntity<>(problem, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDTO> handleParameterTypeMismatch(ServerWebInputException e, ServerHttpRequest webRequest) {
        // you could argue, that client errors can be logged as info instead, as there was no error in this service, but the client did something wrong
        // on the other hand: something went wrong and somebody has to raise an error
        log.error("The request was invalid", e);

        ProblemDTO problem = new ProblemDTO();
        problem.setTitle("The request was invalid");
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setType(URI.create("problem/bad_request/unexpected_value_type"));

        if(e.getCause() instanceof TypeMismatchException tme) {
            // I didn't find a way to get the name of the parameter where the TypeMismatchException was risen
            Object value = tme.getValue();
            String valueClass = value == null ? "null" : value.getClass().getName();
            problem.setDetail(String.format("Value \"%s\" is of type %s, but should be of type %s.", value, valueClass, tme.getRequiredType()));
        } else {
            problem.setDetail("Some parameters were of an unexpected type. Please check the API specifications.");
        }

        problem.setInstance(webRequest.getURI());

        return new ResponseEntity<>(problem, HttpStatus.BAD_REQUEST);
    }

}
