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

import de.gematik.zts.redirector.model.ApplicationConfig;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadSchedulerServiceTest {

    private DownloadSchedulerService downloadSchedulerService;

    private static final String OUTPUT_RESOURCES_LIST_JSON = "target/test/files/output_resources_list.json";

    @Mock
    private ApplicationConfig applicationConfig;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // Erstellen eines gemockten ApplicationConfig-Objekts
        applicationConfig = mock(ApplicationConfig.class);
//        // Initialisieren des Dienstes mit dem gemockten ApplicationConfig

        downloadSchedulerService = new DownloadSchedulerService(applicationConfig);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
        File outputFile = new File(OUTPUT_RESOURCES_LIST_JSON);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }


    @Test
    void downloadResourcesList_ExecutesSuccessfully() throws IOException {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        String content = Files.readString(Paths.get("src/test/resources/resources_list.json"), StandardCharsets.UTF_8);
        // Erfolgreiche HTTP-Antwort simulieren (200 OK)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(content));

        // Methode aufrufen
        downloadSchedulerService.downloadResourcesList();

        // Überprüfen, ob die Datei erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertTrue(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));

    }

    @Test
    void downloadResourcesList_ExecutesSuccessfullyWhenAccessToken() throws IOException{
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn("accessToken");
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        String content = Files.readString(Paths.get("src/test/resources/resources_list.json"),StandardCharsets.UTF_8);
        // Erfolgreiche HTTP-Antwort simulieren (200 OK)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(content));

        // Methode aufrufen
        downloadSchedulerService.downloadResourcesList();
        // Überprüfen, ob die Datei erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertTrue(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));

    }

    @Test
    void downloadResourcesList_FailsWithHttpError() {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        // Fehlerhafte HTTP-Antwort simulieren (500 Internal Server Error)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        downloadSchedulerService.downloadResourcesList();
        // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }



    @Test
    void initializeDownload_SuccessfulDownload() throws IOException {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);

        String content = Files.readString(Paths.get("src/test/resources/resources_list.json"),StandardCharsets.UTF_8);
        // Erfolgreiche HTTP-Antwort simulieren (200 OK)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(content));

        // Methode über Reflexion aufrufen, da sie `@PostConstruct` ist
        downloadSchedulerService.initializeDownload();
        // Überprüfen, ob die Erfolgsvariable auf true gesetzt wurde
        assertTrue(downloadSchedulerService.isOnceDownloadSuccessful());
        // Überprüfen, ob die Datei erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertTrue(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }


    @Test
    void initializeDownload_Failure() {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        downloadSchedulerService.initializeDownload();
        assertFalse(downloadSchedulerService.isOnceDownloadSuccessful());
        // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }

    @Test
    void downloadResourcesList_FailsToWriteFile() throws IOException {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        File tempFile = File.createTempFile("tmpFile", ".txt");
        tempFile.setReadOnly();
        try {
            when(applicationConfig.getPathToResourcesFile()).thenReturn(tempFile.getAbsolutePath());
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));
            downloadSchedulerService.downloadResourcesList();
            // Überprüfen, ob die Datei nicht erfolgreich in die Datei geschrieben wurde
            assertTrue(Files.readString(Paths.get(tempFile.getAbsolutePath()), StandardCharsets.UTF_8).isEmpty());
        } finally {
            tempFile.delete();
        }
        }

    @Test
    void initializeDownload_FailsToWriteFile() throws IOException {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        File tempFile = File.createTempFile("tmpFile", ".txt");
        tempFile.setReadOnly();
        try {
            when(applicationConfig.getPathToResourcesFile()).thenReturn(tempFile.getAbsolutePath());
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));
            downloadSchedulerService.initializeDownload();
            // Überprüfen, ob die Datei nicht erfolgreich in die Datei geschrieben wurde
            assertTrue(Files.readString(Paths.get(tempFile.getAbsolutePath()), StandardCharsets.UTF_8).isEmpty());
        } finally {
            tempFile.delete();
        }
    }


    @Test
    void downloadResourcesList_InvalidJsonResponse() {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{invalidJson}"));
        downloadSchedulerService.downloadResourcesList();
        // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }

    @Test
    void downloadResourcesList_InvalidJsonResponse_ResourcesFieldNotArray() {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"resources\": \"not-an-array\"}"));
        downloadSchedulerService.downloadResourcesList();
    // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }

    @Test
    void validJsonResponse_ResourcesFieldIsArray() {
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"resources\": []}"));
        downloadSchedulerService.downloadResourcesList();

        // Überprüfen, ob die Datei erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertTrue(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));

    }
    @Test
    void downloadResourcesList_UriSyntaxException() {
        when(applicationConfig.getResourcesList()).thenReturn("ht@tp://invalid-url");
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        downloadSchedulerService.downloadResourcesList();
        // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }

    @Test
    void initializeDownload_UriSyntaxException() {
        when(applicationConfig.getResourcesList()).thenReturn("ht@tp://invalid-url");
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn(OUTPUT_RESOURCES_LIST_JSON);
        downloadSchedulerService.initializeDownload();
        // Überprüfen, ob die Datei nicht erfolgreich heruntergeladen wurde und in die Datei geschrieben wurde
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));
    }

    @Test
    void downloadResourcesList_ShouldHandleInterruptedException(){
        // Mock ApplicationConfig
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn("target/test/resources_list.json");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBodyDelay(100, java.util.concurrent.TimeUnit.MILLISECONDS)); // Verzögerung führt zum Interrupt

        // Simuliere das Unterbrechen des Threads
        Thread.currentThread().interrupt();
        downloadSchedulerService.downloadResourcesList();

        // Überprüfe, dass der Thread weiterhin unterbrochen ist
        assertTrue(Thread.interrupted());
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));

    }

    @Test
    void initializeDownload_ShouldHandleInterruptedException(){
        // Mock ApplicationConfig
        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn("target/test/resources_list.json");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBodyDelay(100, java.util.concurrent.TimeUnit.MILLISECONDS)); // Verzögerung führt zum Interrupt

        // Simuliere das Unterbrechen des Threads
        Thread.currentThread().interrupt();
        downloadSchedulerService.initializeDownload();

        // Überprüfe, dass der Thread weiterhin unterbrochen ist
        assertTrue(Thread.interrupted());
        assertFalse(Files.exists(Paths.get(OUTPUT_RESOURCES_LIST_JSON)));

    }


}
