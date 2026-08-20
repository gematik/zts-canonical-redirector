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

package de.gematik.zts.redirector.controller;

import de.gematik.zts.redirector.exceptions.ServiceUnavailableException;
import de.gematik.zts.redirector.model.ApplicationConfig;
import de.gematik.zts.redirector.model.HealthResponse;
import de.gematik.zts.redirector.services.DownloadSchedulerService;


import de.gematik.zts.redirector.tools.FileProcessHelper;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.io.IOException;
import java.util.Optional;


import static org.mockito.Mockito.*;

class ResolveControllerTest {


    @Mock
    private ApplicationConfig applicationConfig;


    @Mock
    private DownloadSchedulerService schedulerService;

    @InjectMocks
    private ResolveController resolveController;

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // Erstellen eines gemockten ApplicationConfig-Objekts

        when(applicationConfig.getResourcesList()).thenReturn(mockWebServer.url("/resources_list.json").toString());
        when(applicationConfig.getAccessToken()).thenReturn(null);
        when(applicationConfig.getPathToResourcesFile()).thenReturn("src/test/resources/resources_list.json");


    }
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    @Test
    void resolve_ValidUrlAndVersion() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        String url = "https://terminologien.bfarm.de/fhir/CodeSystem/ucum-common-units-translation-de-de";
        String version = "1.5.0";
        Mono<ResponseEntity<Object>> result = resolveController.resolve(url, version);
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.FOUND)
                .verifyComplete();

    }



    @Test
    void resolve_ValidUrlNoVersion() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        String url = "http://fhir.de/ValueSet/bfarm/ops";
        Mono<ResponseEntity<Object>> result = resolveController.resolve(url, null);
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.FOUND)
                .verifyComplete();
    }

    @Test
    void resolve_InvalidUrl() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> response = resolveController.resolve(":/fhir/CodeSystem/ucum-common-units-translation-de-de", null);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Ungültiges URL-Format"))
                .verify();
    }

    @Test
    void resolve_InvalidVersion() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        String url = "https://terminologien.bfarm.de/fhir/CodeSystem/ucum-common-units-translation-de-de";
        String version = "01.5.0";
        Mono<ResponseEntity<Object>> response = resolveController.resolve(url, version);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Ungültiges Format der Version"))
                .verify();
    }

    @Test
    void resolve_InvalidURLAndVersion() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        String url = "//terminologien.bfarm.de/fhir/CodeSystem/ucum-common-units-translation-de-de";
        String version = "01.5.0";
        Mono<ResponseEntity<Object>> response = resolveController.resolve(url, version);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Ungültiges URL-Format"))
                .verify();
    }

    @Test
    void serviceRedirect_InvalidResourceType() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> response = resolveController.serviceRedirect("Patient", "icd10gm");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Ungültiger Ressourcentyp"))
                .verify();
    }

    @Test
    void serviceRedirect_InvalidResourceId() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> response = resolveController.serviceRedirect("CodeSystem", "123 %&@d");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Ungültiges Format des Identifiers der Ressource"))
                .verify();
    }

    @Test
    void resolve_ValidUrlResourceNotFound() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> response = resolveController.resolve("https://terminologien.bfarm.de/fhir/CodeSystem/ucum-units-translation-de-de", null);
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof NoResourceFoundException &&
                        throwable.getMessage().contains("Die angefragte FHIR-Ressource konnte nicht gefunden werden"))
                .verify();

    }

    @Test
    void resolve_ValidUrlAndVersionResourceNotFound() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        try (MockedStatic<FileProcessHelper> fileProcessMock = mockStatic(FileProcessHelper.class)) {
            fileProcessMock.when(() -> FileProcessHelper.findHighestVersionResource("src/test/resources/files/output_resources_list.json", "https://terminologien.bfarm.de/fhir/CodeSystem/ucum-units-translation-de-de", null))
                    .thenReturn(Optional.empty());
            Mono<ResponseEntity<Object>> response = resolveController.resolve("https://terminologien.bfarm.de/fhir/CodeSystem/ucum-units-translation-de-de", null);
            StepVerifier.create(response)
                    .expectErrorMatches(throwable -> throwable instanceof NoResourceFoundException &&
                            throwable.getMessage().contains("Die angefragte FHIR-Ressource konnte nicht gefunden werden"))
                    .verify();
        }
    }

    @Test
    void healthCheck_ServiceUnavailable() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(false);

        Mono<ResponseEntity<Object>> result = resolveController.healthCheck();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServiceUnavailableException &&
                        throwable.getMessage().contains("Der Dienst wurde nicht korrekt initialisiert"))
                .verify();
    }

    @Test
    void healthCheck_ServiceAvailable() {

        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> result = resolveController.healthCheck();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK &&
                        ((HealthResponse) response.getBody()).getStatus().equals("UP"))
                .verifyComplete();
    }


    @Test
    void resolve_ThrowError() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(false);
        String url = "https://terminologien.bfarm.de/fhir/CodeSystem/ucum-common-units-translation-de-de";
        String version = "1.5.0";
        Mono<ResponseEntity<Object>> result = resolveController.resolve(url, version);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServiceUnavailableException &&
                        throwable.getMessage().contains("Der Dienst wurde nicht korrekt initialisiert. Bitte versuchen Sie es später erneut."))
                .verify();

    }

    @Test
    void serviceRedirect_ThrowError() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(false);
        Mono<ResponseEntity<Object>> result = resolveController.serviceRedirect("CodeSystem", "ops-zusatzk");
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ServiceUnavailableException &&
                        throwable.getMessage().contains("Der Dienst wurde nicht korrekt initialisiert. Bitte versuchen Sie es später erneut."))
                .verify();

    }


    @Test
    void serviceRedirect_ValidResource()  {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);

        Mono<ResponseEntity<Object>> result = resolveController.serviceRedirect("CodeSystem", "ops-zusatzk");

        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    assert response.getStatusCode() == HttpStatus.FOUND;
                    assert response.getHeaders().getLocation().toString().equals(applicationConfig.getContentBaseURL()+"/CodeSystem-ops-zusatzk-2025.html");
                    return true;
                })
                .verifyComplete();

    }

    @Test
    void serviceRedirect_ResourceNotFound() {
        when(schedulerService.isOnceDownloadSuccessful()).thenReturn(true);
        Mono<ResponseEntity<Object>> response = resolveController.serviceRedirect("CodeSystem", "ops-zusat");
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof NoResourceFoundException &&
                        throwable.getMessage().contains("Die angefragte FHIR-Ressource konnte nicht gefunden werden"))
                .verify();
    }


}
