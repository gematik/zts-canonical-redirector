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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.WebFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class WebConfigTest {

    @Mock
    private CacheControlFilter cacheControlFilter;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webConfig = new WebConfig(cacheControlFilter);
    }

    @Test
    void cacheControlWebFilter_ReturnsCacheControlFilter() {
        WebFilter result = webConfig.cacheControlWebFilter();
        assertNotNull(result);
    }

    @Test
    void cacheControlWebFilter_CorrectBeanType() {
        WebFilter result = webConfig.cacheControlWebFilter();
        assertEquals(CacheControlFilter.class, result.getClass());
    }
}
