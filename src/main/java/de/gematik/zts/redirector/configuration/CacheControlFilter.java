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

package de.gematik.zts.redirector.configuration;

import de.gematik.zts.redirector.model.CacheConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CacheControlFilter implements WebFilter {

    private final CacheConfiguration cacheConfiguration;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        StringBuilder cacheControl = new StringBuilder();
        if (cacheConfiguration.isCpublic()) {
            cacheControl.append("public, ");
        }


        if (cacheConfiguration.isNoCache()) {
            cacheControl.append("no-cache, ");
        }

        if (cacheConfiguration.isNoStore()) {
            cacheControl.append("no-store, ");
        }

        if (cacheConfiguration.getMaxAge() > 0) {
            cacheControl.append("max-age=").append(cacheConfiguration.getMaxAge()).append(", ");
        }

        // Entfernt das letzte ", "
        String cacheHeader = cacheControl.toString().replaceAll(", $", "");

        // Setzt den Cache-Control-Header in der HTTP-Response
        exchange.getResponse().getHeaders().setCacheControl(cacheHeader);

        // Weiterleitung der Anfrage durch die Filter-Kette
        return chain.filter(exchange);
    }
}
