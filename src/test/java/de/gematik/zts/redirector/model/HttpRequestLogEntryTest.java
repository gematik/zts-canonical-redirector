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

package de.gematik.zts.redirector.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpRequestLogEntryTest {

    @Test
    void builder_SetsAllFieldsCorrectly() {
        HttpRequestLogEntry entry = HttpRequestLogEntry.builder()
                .requestMethod("GET")
                .requestUrl("http://example.com")
                .requestSize("1234")
                .status(200)
                .responseSize("5678")
                .userAgent("Mozilla/5.0")
                .remoteIp("192.168.1.1")
                .serverIp("192.168.1.2")
                .referer("http://referer.com")
                .latency("0.123s")
                .cacheLookup(true)
                .cacheHit(false)
                .cacheValidatedWithOriginServer(true)
                .cacheFillBytes("1024")
                .protocol("HTTP/1.1")
                .build();

        assertEquals("GET", entry.getRequestMethod());
        assertEquals("http://example.com", entry.getRequestUrl());
        assertEquals("1234", entry.getRequestSize());
        assertEquals(200, entry.getStatus());
        assertEquals("5678", entry.getResponseSize());
        assertEquals("Mozilla/5.0", entry.getUserAgent());
        assertEquals("192.168.1.1", entry.getRemoteIp());
        assertEquals("192.168.1.2", entry.getServerIp());
        assertEquals("http://referer.com", entry.getReferer());
        assertEquals("0.123s", entry.getLatency());
        assertEquals(true, entry.getCacheLookup());
        assertEquals(false, entry.getCacheHit());
        assertEquals(true, entry.getCacheValidatedWithOriginServer());
        assertEquals("1024", entry.getCacheFillBytes());
        assertEquals("HTTP/1.1", entry.getProtocol());
    }

    @Test
    void builder_SetsNullFieldsCorrectly() {
        HttpRequestLogEntry entry = HttpRequestLogEntry.builder().build();

        assertNull(entry.getRequestMethod());
        assertNull(entry.getRequestUrl());
        assertNull(entry.getRequestSize());
        assertEquals(0, entry.getStatus());
        assertNull(entry.getResponseSize());
        assertNull(entry.getUserAgent());
        assertNull(entry.getRemoteIp());
        assertNull(entry.getServerIp());
        assertNull(entry.getReferer());
        assertNull(entry.getLatency());
        assertNull(entry.getCacheLookup());
        assertNull(entry.getCacheHit());
        assertNull(entry.getCacheValidatedWithOriginServer());
        assertNull(entry.getCacheFillBytes());
        assertNull(entry.getProtocol());
    }
}
