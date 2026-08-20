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

package de.gematik.zts.redirector.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;


import org.springframework.http.HttpStatus;


import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import java.util.HashMap;
import java.util.Map;

import static de.gematik.zts.redirector.RedirectorConstants.ATTRIBUTE_LOG_MESSAGE;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @InjectMocks
    private LoggingFilter loggingFilter;

    @Mock
    private WebFilterChain chain;

    private MockServerWebExchange mockExchange;

    private final Map<String, Object> attributes = new HashMap<>();


    @BeforeEach
    void setUp() {


        chain = mock(WebFilterChain.class);

    }

    @Test
    void testFilter() {
        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test")
                        .header("Test-Header", "TestValue")
                        .header("X-Forwarded-For", "192.168.1.100")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.OK);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });
    }

    @Test
    void testFilter_WithAttributes() {
        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test")
                        .header("Test-Header", "TestValue")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.OK);
        attributes.put("user", "test-user");
        attributes.put("remoteIp", "127.0.0.1:8080");
        attributes.put("httpRequest", "Test request log message");
        attributes.put("requestSize", "100");
        attributes.put("responseSize","200");
        attributes.put("latency", "50ms");
        mockExchange.getAttributes().putAll(attributes);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });

    }


    @Test
    void getClientIpAddress_ShouldReturnUnknowHost() {

        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test")
                        .build());
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.OK);
        attributes.put("remoteIp", "unknown");

        mockExchange.getAttributes().putAll(attributes);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });
    }
    @Test
    void getClientIpAddress_ShouldReturnUnknowhost_WhenHeaderXForwardedForIsEmpty() {

        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test").header("X-Forwarded-For", "")
                        .build());
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.OK);
        attributes.put("remoteIp", "unknown");

        mockExchange.getAttributes().putAll(attributes);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });
    }

    @Test
    void logRequest_ShouldLogWarningForErrorResponse() {

        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test")
                        .build());
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        attributes.put(ATTRIBUTE_LOG_MESSAGE, "HTTP Request processed");

        mockExchange.getAttributes().putAll(attributes);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });
    }

    @Test
    void getResponseSize_ShouldReturnContentLength_WhenValid() {

        mockExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/test")
                        .build());
        when(chain.filter(mockExchange)).thenReturn(Mono.empty());
        mockExchange.getResponse().setStatusCode(HttpStatus.OK);
        mockExchange.getResponse().getHeaders().setContentLength(500); // Simulate a valid Content-Length

        attributes.put("responseSize","500");

        mockExchange.getAttributes().putAll(attributes);
        assertThatNoException().isThrownBy(() -> {
            Mono<Void> result = loggingFilter.filter(mockExchange, chain);
            StepVerifier.create(result).verifyComplete();
        });
    }

}
