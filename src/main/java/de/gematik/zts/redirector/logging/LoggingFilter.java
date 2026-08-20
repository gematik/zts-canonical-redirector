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

import de.gematik.zts.redirector.model.HttpRequestLogEntry;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.LogstashMarker;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static de.gematik.zts.redirector.RedirectorConstants.ATTRIBUTE_LOG_MESSAGE;
import static de.gematik.zts.redirector.RedirectorConstants.ATTRIBUTE_USER;
import static de.gematik.zts.redirector.logging.ResponseSizeWebFilter.RESPONSE_SIZE;
import static net.logstash.logback.marker.Markers.append;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class LoggingFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).doOnTerminate(() -> logRequest(exchange));
    }

    private void logRequest(ServerWebExchange exchange) {
        String clientIp = getClientIpAddress(exchange);
        String user = getUser(exchange);
        String logMessage = getLogMessage(exchange);
        String requestSize = getRequestSize(exchange);
        String responseSize = getResponseSize(exchange);
        String latency = getLatency(exchange);

        HttpRequestLogEntry entry = buildLogEntry(exchange, clientIp, requestSize, responseSize, latency);
        LogstashMarker marker = append("user", user).and(append("httpRequest", entry));


        if (Objects.requireNonNull(exchange.getResponse().getStatusCode()).isError()) {
            log.warn(marker, "{}", logMessage);
        } else {
            log.info(marker, "{}", logMessage);
        }
    }

    private String getUser(ServerWebExchange exchange) {
        return exchange.getAttributes().getOrDefault(ATTRIBUTE_USER, "invalid/not-needed").toString();
    }

    private String getLogMessage(ServerWebExchange exchange) {
        return exchange.getAttributes().getOrDefault(ATTRIBUTE_LOG_MESSAGE, "HTTP Request processed").toString();
    }

    private String getRequestSize(ServerWebExchange exchange) {
        return exchange.getAttributes().getOrDefault(RequestSizeWebFilter.REQUEST_SIZE, "0").toString();
    }

    private String getResponseSize(ServerWebExchange exchange) {
        if (exchange.getResponse().getHeaders().getContentLength() != -1) {
            return String.valueOf(exchange.getResponse().getHeaders().getContentLength());
        }
        return exchange.getAttributes().getOrDefault(RESPONSE_SIZE, "0").toString();
    }

    private String getLatency(ServerWebExchange exchange) {
        return exchange.getAttributes().getOrDefault(LatencyMeasurementFilter.LATENCY, "0s").toString();
    }

    private HttpRequestLogEntry buildLogEntry(ServerWebExchange exchange, String clientIp, String requestSize, String responseSize, String latency) {
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        int statusCode;
        if (status != null) {
           statusCode = status.value();
        }
        else statusCode=0;
        return HttpRequestLogEntry.builder()
                .remoteIp(clientIp)
                .requestUrl(exchange.getRequest().getURI().toString())
                .requestMethod(exchange.getRequest().getMethod().name())
                .status(statusCode)
                .protocol(exchange.getRequest().getURI().getScheme())
                .requestSize(requestSize)
                .responseSize(responseSize)
                .userAgent(exchange.getRequest().getHeaders().getFirst("User-Agent"))
                .latency(latency)
                .build();
    }


    private String getClientIpAddress(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String xForwardedForHeader = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.trim();
        }
        var remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }
}
