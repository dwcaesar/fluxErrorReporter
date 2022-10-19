package de.wohlers.fluxerrorreporter.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ExampleController.class)
class ExampleControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void test_invoke200_successfulResponse() {
        webTestClient.get().uri("/200")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke400withValidParameter_successfulResponse() {
        webTestClient.get().uri("/400?number=5")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke400withInvalidParameter_badRequest() {
        webTestClient.get().uri("/400?number=0")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke400withInvalidParameterType_badRequest() {
        webTestClient.get().uri("/400?number=string")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke400withoutParameter_badRequest() {
        webTestClient.get().uri("/400")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke404_notFound() {
        webTestClient.get().uri("/404")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody().consumeWith(this::logBody);
    }

    @Test
    void test_invoke500_internalServerError() {
        webTestClient.get().uri("/500")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody().consumeWith(this::logBody);
    }

    private void logBody(EntityExchangeResult<byte[]> entityExchangeResult) {
        byte[] responseBody = entityExchangeResult.getResponseBody();
        if(responseBody == null) {
            log.warn("response body was null");
        } else {
            log.info(new String(responseBody, StandardCharsets.UTF_8));
        }
    }

}