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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

class OpenAPIDocConfigTest {
    @Test
    void testGetterSetter() {
        OpenAPIDocConfig config = new OpenAPIDocConfig();

        config.setTitle("API Dokumentation");
        config.setVersion("1.0");
        List<String> urls = Arrays.asList("https://api.example.com", "https://dev.example.com");
        config.setServerUrls(urls);

        assertThat(config.getTitle()).isEqualTo("API Dokumentation");
        assertThat(config.getVersion()).isEqualTo("1.0");
        assertThat(config.getServerUrls()).containsExactly("https://api.example.com", "https://dev.example.com");
    }


}
