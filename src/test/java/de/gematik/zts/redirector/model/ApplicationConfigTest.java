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

class ApplicationConfigTest {

    @Test
    void getTitle_ReturnsTitle() {
        ApplicationConfig config = new ApplicationConfig();
        config.setTitle("Test Title");
        assertEquals("Test Title", config.getTitle());
    }

    @Test
    void getVersion_ReturnsVersion() {
        ApplicationConfig config = new ApplicationConfig();
        config.setVersion("1.0.0");
        assertEquals("1.0.0", config.getVersion());
    }

    @Test
    void getAccessToken_ReturnsAccessToken() {
        ApplicationConfig config = new ApplicationConfig();
        config.setAccessToken("token123");
        assertEquals("token123", config.getAccessToken());
    }

    @Test
    void getResourcesList_ReturnsResourcesList() {
        ApplicationConfig config = new ApplicationConfig();
        config.setResourcesList("resourcesList");
        assertEquals("resourcesList", config.getResourcesList());
    }

    @Test
    void getPathToResourcesFile_ReturnsPathToResourcesFile() {
        ApplicationConfig config = new ApplicationConfig();
        config.setPathToResourcesFile("path/to/resources");
        assertEquals("path/to/resources", config.getPathToResourcesFile());
    }

    @Test
    void getTimeInterval_ReturnsTimeInterval() {
        ApplicationConfig config = new ApplicationConfig();
        config.setTimeInterval("30m");
        assertEquals("30m", config.getTimeInterval());
    }

    @Test
    void getContentBaseURL_ReturnsContentBaseURL() {
        ApplicationConfig config = new ApplicationConfig();
        config.setContentBaseURL("http://base.url");
        assertEquals("http://base.url", config.getContentBaseURL());
    }

    @Test
    void getLogLevel_ReturnsLogLevel() {
        ApplicationConfig config = new ApplicationConfig();
        config.setLogLevel("DEBUG");
        assertEquals("DEBUG", config.getLogLevel());
    }

    @Test
    void setTitle_SetsTitle() {
        ApplicationConfig config = new ApplicationConfig();
        config.setTitle("New Title");
        assertEquals("New Title", config.getTitle());
    }

    @Test
    void setVersion_SetsVersion() {
        ApplicationConfig config = new ApplicationConfig();
        config.setVersion("2.0.0");
        assertEquals("2.0.0", config.getVersion());
    }

    @Test
    void setAccessToken_SetsAccessToken() {
        ApplicationConfig config = new ApplicationConfig();
        config.setAccessToken("newToken");
        assertEquals("newToken", config.getAccessToken());
    }

    @Test
    void setResourcesList_SetsResourcesList() {
        ApplicationConfig config = new ApplicationConfig();
        config.setResourcesList("newResourcesList");
        assertEquals("newResourcesList", config.getResourcesList());
    }

    @Test
    void setPathToResourcesFile_SetsPathToResourcesFile() {
        ApplicationConfig config = new ApplicationConfig();
        config.setPathToResourcesFile("new/path/to/resources");
        assertEquals("new/path/to/resources", config.getPathToResourcesFile());
    }

    @Test
    void setTimeInterval_SetsTimeInterval() {
        ApplicationConfig config = new ApplicationConfig();
        config.setTimeInterval("60m");
        assertEquals("60m", config.getTimeInterval());
    }

    @Test
    void setContentBaseURL_SetsContentBaseURL() {
        ApplicationConfig config = new ApplicationConfig();
        config.setContentBaseURL("http://new.base.url");
        assertEquals("http://new.base.url", config.getContentBaseURL());
    }

    @Test
    void setLogLevel_SetsLogLevel() {
        ApplicationConfig config = new ApplicationConfig();
        config.setLogLevel("INFO");
        assertEquals("INFO", config.getLogLevel());
    }

    @Test
    void builder_CreatesApplicationConfig() {
        ApplicationConfig config = ApplicationConfig.builder()
                .title("Builder Title")
                .version("1.0.0")
                .accessToken("builderToken")
                .resourcesList("builderResourcesList")
                .pathToResourcesFile("builder/path/to/resources")
                .timeInterval("15m")
                .contentBaseURL("http://builder.base.url")
                .logLevel("WARN")
                .build();

        assertEquals("Builder Title", config.getTitle());
        assertEquals("1.0.0", config.getVersion());
        assertEquals("builderToken", config.getAccessToken());
        assertEquals("builderResourcesList", config.getResourcesList());
        assertEquals("builder/path/to/resources", config.getPathToResourcesFile());
        assertEquals("15m", config.getTimeInterval());
        assertEquals("http://builder.base.url", config.getContentBaseURL());
        assertEquals("WARN", config.getLogLevel());
    }}
