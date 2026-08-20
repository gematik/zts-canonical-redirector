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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MissingRequestValueException;
import reactor.core.publisher.Mono;

import static de.gematik.zts.redirector.RedirectorConstants.*;


import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
/**
 * Exception Handler für die Behandlung von Exceptions
 */
public class RedirectorExceptionHandler {
    /**
     * Behandlung von Exception vom Typ CanonicalRedirectorException
     * @param exc Exception
     * @return  ResponseEntity mit ProblemDetail
     */
    @ExceptionHandler(CanonicalRedirectorException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleRedirectorException(final CanonicalRedirectorException exc) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle(PROBLEMDETAILS_TITLE_INTERNAL_SERVER_ERROR);
        problemDetail.setDetail(exc.getMessage());
        problemDetail.setProperties(Map.of(PROBLEMDETAILS_PROPERTY_TIMESTAMP, System.currentTimeMillis()));
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(getHeader()).body(problemDetail));
    }


   /**
     * Behandlung von Exception vom Typ handleMissingRequestValueException
     * @param exc Exception
     * @return ResponseEntity mit ProblemDetail
     */
    @ExceptionHandler(MissingRequestValueException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleMissingRequestValueException(
        MissingRequestValueException exc) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(PROBLEMDETAILS_TITLE_BAD_REQUEST);
        problemDetail.setDetail(exc.getMessage());
        problemDetail.setProperties(Map.of(PROBLEMDETAILS_PROPERTY_TIMESTAMP, System.currentTimeMillis()));
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(getHeader()).body(problemDetail));
    }

    /**
     * Behandlung von Exception vom Typ NoResourceFoundException
     * @param exc Exception
     * @return ResponseEntity mit ProblemDetail
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleNoResourceFoundException(
            NoResourceFoundException exc) {

        ProblemDetail problemDetail = ProblemDetail.forStatus(exc.getStatusCode());
        problemDetail.setTitle(PROBLEMDETAILS_TITLE_NOT_FOUND);
        problemDetail.setDetail(exc.getReason());
        problemDetail.setProperties(Map.of(PROBLEMDETAILS_PROPERTY_TIMESTAMP, System.currentTimeMillis()));

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).headers(getHeader()).body(problemDetail));
    }
    /**
     * Behandlung von Exception vom Typ ServiceUnavailableException
     * @param exc Exception
     * @return ResponseEntity mit ProblemDetail
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleServiceUnavailableException(
            ServiceUnavailableException exc) {

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(exc.getStatusCode());
        problemDetail.setTitle(PROBLEMDETAILS_TITLE_SERVICE_UNAVAILABLE);
        problemDetail.setDetail(exc.getReason());
        problemDetail.setProperties(Map.of(PROBLEMDETAILS_PROPERTY_TIMESTAMP, System.currentTimeMillis()));

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).headers(getHeader()).body(problemDetail));
    }



    /**
     * Behandlung von Exception vom Typ IllegalArgumentException.
     * Tritt auf, wenn die Validierung der Anfrageparameter fehlschlägt oder Anfrageparameter fehlt.
     * @param exc
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleConstraintValidationException(
            IllegalArgumentException exc) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(PROBLEMDETAILS_TITLE_BAD_REQUEST);
        problemDetail.setDetail(exc.getMessage());
        problemDetail.setProperties(Map.of(PROBLEMDETAILS_PROPERTY_TIMESTAMP, System.currentTimeMillis()));

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(getHeader()).body(problemDetail));
    }



    /**
     *  Erzeugt HttpHeaders für die Response
     * @return HttpHeaders
     */
    private HttpHeaders getHeader() {
        final HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
        responseHeaders.remove(HttpHeaders.CACHE_CONTROL);
        responseHeaders.remove(HttpHeaders.PRAGMA);
        return responseHeaders;
    }

}

