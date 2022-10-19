package de.wohlers.fluxerrorreporter.controller;

import de.wohlers.fluxerrorreporter.model.exceptions.EntryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@RestController
@Validated
public class ExampleController {

    @GetMapping("/200")
    public Mono<ResponseEntity<String>> successfulRequest() {
        return Mono.just(new ResponseEntity<>("Ok", HttpStatus.OK));
    }

    @GetMapping("/400")
    public Mono<ResponseEntity<String>> badRequest(@RequestParam(value = "number", required = true) @NotNull @Min(2) Integer number) {
        // ConstraintViolationExceptions is thrown by spring bean validation when this is called as /400?number=0
        return Mono.just(new ResponseEntity<>("Recieved number: " + number, HttpStatus.OK));
    }

    @GetMapping("/404")
    public Mono<ResponseEntity<String>> entryNotFound() {
        return Mono.error(new EntryNotFoundException("Entry was not found"));
    }

    @GetMapping("/500")
    public Mono<ResponseEntity<String>> internalServerError() {
        return Mono.error(new RuntimeException("something went wrong"));
    }

}
