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

package de.gematik.zts.redirector.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.zts.redirector.model.ApplicationConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;


import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DownloadSchedulerService {
    public static final String ERROR_MSG_RESOURCELIST_DOWNLOAD = "Fehler beim Download der Resourceslist";

    @Getter
    @Setter
    private boolean onceDownloadSuccessful = false;

    private final ApplicationConfig applicationConfig;

    public DownloadSchedulerService(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }


    /**
     * Download der Ressourcenliste
     */
    @Scheduled(initialDelayString = "60000", fixedRateString = "#{@applicationConfig.getTimeInterval()}")
    public void downloadResourcesList() {
        log.debug("Scheduler startet. URL der Ressourceslist {}", applicationConfig.getResourcesList());
        try {
            if (downloadResourcesList(applicationConfig.getResourcesList(), applicationConfig.getAccessToken(), applicationConfig.getPathToResourcesFile())) {
                log.debug("Resourceslist erfolgreich heruntergeladen!");
            } else {
                log.error(ERROR_MSG_RESOURCELIST_DOWNLOAD);
            }
         } catch (Exception e) {
            log.error(ERROR_MSG_RESOURCELIST_DOWNLOAD, e);
        }
    }

    /**
     * Initialisierung des Downloads nach Applikationsstart
     */
    @PostConstruct
    public void initializeDownload() {
        log.debug("Initialdownload startet. URL der Ressourceslist {}", applicationConfig.getResourcesList());
        try {
            if (downloadResourcesList(applicationConfig.getResourcesList(), applicationConfig.getAccessToken(), applicationConfig.getPathToResourcesFile())) {
                onceDownloadSuccessful = true;
                log.debug("Initialdownload Resourceslist erfolgreich heruntergeladen!");
            } else {
                log.error(ERROR_MSG_RESOURCELIST_DOWNLOAD);
            }
        } catch (Exception e) {
            log.error(ERROR_MSG_RESOURCELIST_DOWNLOAD, e);
        }
    }

    /**
     * Download der Ressourcenliste.
     *
     * @param resourcesListUrl URL der Ressourcenliste
     * @param accessToken      Access-Token
     * @param outputFilePath   Ausgabepfad
     * @return
     */

    private boolean downloadResourcesList(String resourcesListUrl, String accessToken, String outputFilePath) {

        try {
            Path outputPath = Path.of(outputFilePath);
            Files.createDirectories(outputPath.getParent()); // Verzeichnis sicherstellen

            // Download der Resourceslist
            log.debug("Download Resourceslist.json gestartet...");

            HttpRequest request;

            //Header Param "PRIVATE-TOKEN" wird nur gesetzt, wenn die Resourceslist im GitLab-Repo abgelegt ist und dafür ein Access-Token benutzt wird
            if (accessToken != null && !accessToken.isEmpty()) {
                request = HttpRequest.newBuilder()
                        .uri(new URI(resourcesListUrl))
                        .header("PRIVATE-TOKEN", accessToken)
                        .GET()
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(new URI(resourcesListUrl))
                        .GET()
                        .build();
            }
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpStatus.OK.value()) {
                if (!isValidJsonResponse(response.body())) {
                    log.error("Fehler beim Download, ungültiges JSON-Objekt: {}", response.body());
                    return false;
                }
                Files.write(outputPath, response.body().getBytes(StandardCharsets.UTF_8));
                onceDownloadSuccessful = true;
                log.debug("Download erfolgreich: {}", outputPath);
                return true;
            } else {
                log.error("Fehler beim Download, HTTP Status: {}, HTTP Response: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (IOException e) {
            log.error("Fehler beim Schreiben der Resourceslist in die Datei" ,  e);
            return false;
        } catch (URISyntaxException e) {
            log.error("Syntaxfehler beim Parsen der URL zum Download der Resourceslist",  e);
            return false;
        } catch (InterruptedException e) {
            log.error(ERROR_MSG_RESOURCELIST_DOWNLOAD, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean isValidJsonResponse(String jsonResponse) throws JsonProcessingException {

        // Prüfen wenn die Antwort ein gültiges JSON-Objekt ist
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Prüfen wenn "resources" existiert und ist ein Array
        return rootNode.has("resources") && rootNode.get("resources").isArray();

    }

}
