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

import static org.junit.jupiter.api.Assertions.*;


class CacheConfigurationTest {

    @Test
    void isPublic_ReturnsTrue() {
        CacheConfiguration config = new CacheConfiguration(true, false, false, 3600);
        assertTrue(config.isCpublic());
    }

    @Test
    void isPublic_ReturnsFalse() {
        CacheConfiguration config = new CacheConfiguration(false, false, false, 3600);
        assertFalse(config.isCpublic());
    }

    @Test
    void isNoCache_ReturnsTrue() {
        CacheConfiguration config = new CacheConfiguration(false, true, false, 3600);
        assertTrue(config.isNoCache());
    }

    @Test
    void isNoCache_ReturnsFalse() {
        CacheConfiguration config = new CacheConfiguration(false, false, false, 3600);
        assertFalse(config.isNoCache());
    }

    @Test
    void isNoStore_ReturnsTrue() {
        CacheConfiguration config = new CacheConfiguration(false, false, true, 3600);
        assertTrue(config.isNoStore());
    }

    @Test
    void isNoStore_ReturnsFalse() {
        CacheConfiguration config = new CacheConfiguration(false, false, false, 3600);
        assertFalse(config.isNoStore());
    }

    @Test
    void getMaxAge_ReturnsCorrectValue() {
        CacheConfiguration config = new CacheConfiguration(false, false, false, 3600);
        assertEquals(3600, config.getMaxAge());
    }

    @Test
    void setPublic_SetsCorrectValue() {
        CacheConfiguration config = new CacheConfiguration();
        config.setCpublic(true);
        assertTrue(config.isCpublic());
    }

    @Test
    void setNoCache_SetsCorrectValue() {
        CacheConfiguration config = new CacheConfiguration();
        config.setNoCache(true);
        assertTrue(config.isNoCache());
    }

    @Test
    void setNoStore_SetsCorrectValue() {
        CacheConfiguration config = new CacheConfiguration();
        config.setNoStore(true);
        assertTrue(config.isNoStore());
    }

    @Test
    void setMaxAge_SetsCorrectValue() {
        CacheConfiguration config = new CacheConfiguration();
        config.setMaxAge(3600);
        assertEquals(3600, config.getMaxAge());
    }

    @Test
    void builder_CreatesCorrectObject() {
        CacheConfiguration config = CacheConfiguration.builder()
                .cpublic(true)
                .noCache(false)
                .noStore(false)
                .maxAge(3600)
                .build();
        assertTrue(config.isCpublic());
        assertFalse(config.isNoCache());
        assertFalse(config.isNoStore());
        assertEquals(3600, config.getMaxAge());
    }
}
