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

package de.gematik.zts.redirector.tools;

import de.gematik.zts.redirector.exceptions.CanonicalRedirectorException;
import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class FileProcessHelperTest {

    @Test
    void getAvailableResources_ReturnsCorrectJSONObject() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        JSONObject jsonObject = FileProcessHelper.getAvailableResources(resourcesListPath);
        JSONArray resourcesArray = jsonObject.getJSONArray("resources");
        assertEquals(159,resourcesArray.length());
    }

    @Test
    void findHighestVersionResource_ReturnsCorrectResource() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "http://www.orpha.net";

            Optional<JSONObject> result = FileProcessHelper.findHighestVersionResource(resourcesListPath, canonicalUrl, null);
            assertTrue(result.isPresent());
            assertEquals("2024", result.get().getString("version"));
    }

    @Test
    void findHighestVersionResource_ReturnsEmptyWhenNoMatch() {
        // Act
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "https://notfound.com/resource";
        Optional<JSONObject> result = FileProcessHelper.findHighestVersionResource(
                resourcesListPath, canonicalUrl, null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findMatchedResource_ReturnsCorrectResource() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "http://www.orpha.net";

            Optional<JSONObject> result = FileProcessHelper.findMatchedResource(resourcesListPath, canonicalUrl);
            assertTrue(result.isPresent());
            assertEquals(canonicalUrl, result.get().getString("url"));

    }

    @Test
    void findMatchedResource_NotMatchesResource() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "https://example.com/resource";

        Optional<JSONObject> result = FileProcessHelper.findMatchedResource(resourcesListPath, canonicalUrl);

        assertTrue(result.isEmpty());

    }

    @Test
    void readContentFromFile_ThrowsExceptionForInvalidPath() {
        // Act & Assert

        String resourcesListPath = "invalid/path/to/file.json";
        CanonicalRedirectorException exception = assertThrows(CanonicalRedirectorException.class, () ->
                FileProcessHelper.getAvailableResources(resourcesListPath));

        assertTrue(exception.getMessage().contains("Interner Serverfehler. Bitte versuchen Sie es später erneut"));
    }

    @Test
    void findMatchedResource_FindsByIdentifierAndBreaksLoop() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "https://terminologien.bfarm.de/fhir/CodeSystem/ops";

        Optional<JSONObject> result = FileProcessHelper.findMatchedResource(resourcesListPath, canonicalUrl);
        assertTrue(result.isPresent());
        assertEquals("http://fhir.de/CodeSystem/bfarm/ops", result.get().getString("url"));


    }

    @Test
    void findHighestVersionResource_FindsByIdentifierAndBreaksLoop() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "http://fhir.de/CodeSystem/dimdi/icd-10-gm";

        Optional<JSONObject> result = FileProcessHelper.findHighestVersionResource(resourcesListPath, canonicalUrl, null);
        assertTrue(result.isPresent());
        assertEquals("http://fhir.de/CodeSystem/bfarm/icd-10-gm", result.get().getString("url"));


    }

    @Test
    void findHighestVersionResource_FindsExactVersionMatch() {
        String resourcesListPath = "src/test/resources/resources_list.json";
        String canonicalUrl = "http://www.orpha.net";

        Optional<JSONObject> result = FileProcessHelper.findHighestVersionResource(resourcesListPath, canonicalUrl, "2024");
        assertTrue(result.isPresent());
        assertEquals("2024", result.get().getString("version"));

    }
}
