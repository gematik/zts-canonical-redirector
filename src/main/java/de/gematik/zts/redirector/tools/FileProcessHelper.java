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

import io.swagger.v3.core.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import static de.gematik.zts.redirector.RedirectorConstants.*;

/**
 * Helper-Klasse für die Verarbeitung von Dateien
 */
@Component
@Slf4j
public class FileProcessHelper {

    private FileProcessHelper() {
        // private constructor to hide the implicit public one
    }


    /**
     * Lese den Inhalt der Ressourcenliste aus der Datei
     * @param outputFilePath Ausgabepfad
     * @return JSONObject
     */
    public static JSONObject getAvailableResources(String outputFilePath) {
        return readContentfromFile(outputFilePath);
    }

    /**
     * Suche nach der höchsten Version einer Ressource
     * @param outputFilePath Ausgabepfad
     * @param url Canonical-URL
     * @param version Verison
     * @return  Optional<JSONObject>
     */
    public static Optional<JSONObject> findHighestVersionResource(String outputFilePath, String url, String version) {

        List<JSONObject> resources = findResourceByElement(readContentfromFile(outputFilePath), url);
        List<JSONObject> matchedResources = new ArrayList<>();
        for (JSONObject resource : resources) {
            if (version == null || resource.getString(FHIR_ATTRIBUTE_RESOURCE_VERSION).equals(version)) {
                matchedResources.add(resource);
            }
        }
        return matchedResources.stream().max(Comparator.comparing(r -> r.getString(FHIR_ATTRIBUTE_RESOURCE_VERSION)));
    }

    /**
     * Suche nach einer Ressource
     * @param outputFilePath Ausgabepfad
     * @param url Canonical-URL
     * @return Optional<JSONObject>
     */
    public static Optional<JSONObject> findMatchedResource(String outputFilePath, String url) {

        List<JSONObject> resources = findResourceByElement(readContentfromFile(outputFilePath), url);
        return resources.stream().max(Comparator.comparing(r -> r.getString(FHIR_ATTRIBUTE_RESOURCE_VERSION)));
    }

    /**
     * Suche nach Ressourcen anhand eines Elements
     * @param jsonData JSON-Objekt als suchbare Ressource
     * @param url zu suchender url
     * @return List<JSONObject>
     */
    private static List<JSONObject> findResourceByElement(JSONObject jsonData, String url) {

        JSONArray resourcesArray = jsonData.getJSONArray(FHIR_ATTRIBUTE_RESOURCES);
        List<JSONObject> resources = new ArrayList<>();
        for (int i = 0; i < resourcesArray.length(); i++) {
            JSONObject resource = resourcesArray.getJSONObject(i);
            JSONArray identifiers = resource.optJSONArray(FHIR_ATTRIBUTE_IDENTIFIER);
            if (identifiers!=null) {
                for (int j = 0; j < identifiers.length(); j++) {
                    JSONObject identifier = identifiers.getJSONObject(j);
                    if (identifier.getString("value").equals(url)) {
                        resources.add(resource);
                        break;
                    }
                }
            }
            if (resource.getString(FHIR_ATTRIBUTE_URL).equals(url)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Lese den Inhalt aus der Datei
     * @param outputFilePath Ausgabepfad
     * @return  JSONObject
     */
    private static JSONObject readContentfromFile (String outputFilePath) {
        String content;
        try {
            Path path = Paths.get(outputFilePath);
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Fehler während der Verarbeitung von Resourceslist", e);
            throw new CanonicalRedirectorException("Interner Serverfehler. Bitte versuchen Sie es später erneut.");
        }
        return new JSONObject(content);
    }


}
