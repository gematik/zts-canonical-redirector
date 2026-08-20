/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ******
 *
 * For additional notes and disclaimer from gematik and in case of changes
 * by gematik, find details in the "Readme" file.
 */

package de.gematik.zts.redirector.exceptions.handler;

import de.gematik.zts.redirector.exceptions.CanonicalRedirectorException;
import de.gematik.zts.redirector.exceptions.ServiceUnavailableException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MissingRequestValueException;

import static de.gematik.zts.redirector.RedirectorConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectorExceptionHandlerTest {

    @InjectMocks
    private RedirectorExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleRedirectorException_ReturnsInternalServerError() {
        CanonicalRedirectorException exception = new CanonicalRedirectorException("Internal server error");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleRedirectorException(exception).block();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(PROBLEMDETAILS_TITLE_INTERNAL_SERVER_ERROR, response.getBody().getTitle());
        assertEquals("Internal server error", response.getBody().getDetail());
        assertEquals("application/json; charset=utf-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }


    @Test
    void handleMissingRequestValueException_ReturnsBadRequest() {
        MissingRequestValueException exception = new MissingRequestValueException("Missing request parameter", String.class, "url", null);
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleMissingRequestValueException(exception).block();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(PROBLEMDETAILS_TITLE_BAD_REQUEST, response.getBody().getTitle());
        assertEquals("400 BAD_REQUEST \"Required url 'Missing request parameter' is not present.\"", response.getBody().getDetail());
        assertEquals("application/json; charset=utf-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handleNoResourceFoundException_ReturnsNotFound() {
        NoResourceFoundException exception = new NoResourceFoundException(URI.create("handleNoResourceFoundException.uri"), "Resource not found");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleNoResourceFoundException(exception).block();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(PROBLEMDETAILS_TITLE_NOT_FOUND, response.getBody().getTitle());
        assertEquals("No static resource Resource not found for request 'handleNoResourceFoundException.uri'.", response.getBody().getDetail());
        assertEquals("application/json; charset=utf-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handleServiceUnavailableException_ReturnsServiceUnavailable() {
        ServiceUnavailableException exception = new ServiceUnavailableException("Service unavailable");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleServiceUnavailableException(exception).block();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(PROBLEMDETAILS_TITLE_SERVICE_UNAVAILABLE, response.getBody().getTitle());
        assertEquals("Service unavailable", response.getBody().getDetail());
        assertEquals("application/json; charset=utf-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handleIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleConstraintValidationException(exception).block();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(PROBLEMDETAILS_TITLE_BAD_REQUEST, response.getBody().getTitle());
        assertEquals("Invalid argument", response.getBody().getDetail());
        assertEquals("application/json; charset=utf-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
