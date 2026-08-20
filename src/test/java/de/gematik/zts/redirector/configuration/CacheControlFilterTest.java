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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CacheControlFilterTest {

    @Mock
    private CacheConfiguration cacheConfiguration;

    @Mock
    private WebFilterChain chain;

    private CacheControlFilter cacheControlFilter;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        cacheControlFilter = new CacheControlFilter(cacheConfiguration);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_PublicCacheControlHeader() {
        when(cacheConfiguration.isCpublic()).thenReturn(true);
        when(cacheConfiguration.isNoCache()).thenReturn(false);
        when(cacheConfiguration.isNoStore()).thenReturn(false);
        when(cacheConfiguration.getMaxAge()).thenReturn(0);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("public", exchange.getResponse().getHeaders().getCacheControl());
    }

    @Test
    void filter_PrivateCacheControlHeader() {
        when(cacheConfiguration.isCpublic()).thenReturn(false);
        when(cacheConfiguration.isNoCache()).thenReturn(false);
        when(cacheConfiguration.isNoStore()).thenReturn(false);
        when(cacheConfiguration.getMaxAge()).thenReturn(0);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("", exchange.getResponse().getHeaders().getCacheControl());
    }

    @Test
    void filter_NoCacheHeader() {
        when(cacheConfiguration.isCpublic()).thenReturn(false);
        when(cacheConfiguration.isNoCache()).thenReturn(true);
        when(cacheConfiguration.isNoStore()).thenReturn(false);
        when(cacheConfiguration.getMaxAge()).thenReturn(0);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("no-cache", exchange.getResponse().getHeaders().getCacheControl());
    }

    @Test
    void filter_NoStoreHeader() {
        when(cacheConfiguration.isCpublic()).thenReturn(false);
        when(cacheConfiguration.isNoCache()).thenReturn(false);
        when(cacheConfiguration.isNoStore()).thenReturn(true);
        when(cacheConfiguration.getMaxAge()).thenReturn(0);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("no-store", exchange.getResponse().getHeaders().getCacheControl());
    }

    @Test
    void filter_MaxAgeHeader() {
        when(cacheConfiguration.isCpublic()).thenReturn(false);
        when(cacheConfiguration.isNoCache()).thenReturn(false);
        when(cacheConfiguration.isNoStore()).thenReturn(false);
        when(cacheConfiguration.getMaxAge()).thenReturn(3600);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("max-age=3600", exchange.getResponse().getHeaders().getCacheControl());
    }

    @Test
    void filter_CombinedHeaders() {
        when(cacheConfiguration.isCpublic()).thenReturn(true);
        when(cacheConfiguration.isNoCache()).thenReturn(true);
        when(cacheConfiguration.isNoStore()).thenReturn(true);
        when(cacheConfiguration.getMaxAge()).thenReturn(600);

        cacheControlFilter.filter(exchange, chain).block();

        assertEquals("public, no-cache, no-store, max-age=600", exchange.getResponse().getHeaders().getCacheControl());
    }
}
