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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

import static de.gematik.zts.redirector.RedirectorConstants.*;

import de.gematik.zts.redirector.tools.FileProcessHelper;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Order(1)
@Tag(name = "Canonical-Redirector-API", description = "API for redirect Canonical URLs to ZTS")
/**
 * Controller für die Endpunkte /resolve, /resolve/resourcelists /fhir/** und /health
 */
public class ResolveController {

    private static final String VERSION_REGEX = "^(0|[1-9]\\d*)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?$";
    private static final String URL_REGEX = "^(https?://)([a-zA-Z0-9.-]+)\\.([a-zA-Z]{2,6})(/[^\s]*)?$";
    private static final String CANONICAL_IDENTIFIER_PATTERN = "^[A-Za-z0-9._-]+$";

    private static final String CANONICAL_TYPE_PATTERN = "^(CodeSystem|ValueSet|ConceptMap)$";
    private static final String MESSAGE_INITIAL_DOWNLOAD_FAILED = "Der Dienst wurde nicht korrekt initialisiert. Bitte versuchen Sie es später erneut.";

    private ApplicationConfig applicationConfig;
    private DownloadSchedulerService schedulerService;

    // Simulierter Cache für Scheduler-Fehlerstatus
    public ResolveController(ApplicationConfig applicationConfig, DownloadSchedulerService schedulerService) {
        this.applicationConfig = applicationConfig;
        this.schedulerService = schedulerService;
    }

    /**
     * Endpunkt /resolve zum Auflösen einer Ressource
     *
     * @param url     URL der Ressource
     * @param version Versionsnummer der Ressource
     * @return
     */
    @Operation(
            summary = "Resolve a resource and redirect to the resolved resource page in ZTS",
            description = "Resolves a resource using the provided Canonical URL and optional version and then redirect to the resolved resource page in ZTS."
    )

            @ApiResponse(responseCode = "302", description = "Redirect to the resolved resource",
                    headers = @Header(
                            name = "Location",
                            description = "URL of the resolved resource, e.g. https://terminologien.bfarm.de/ValueSet-icd10gm-2025.html",
                            schema = @Schema(type = "string", format = "uri"),
                            example = "https://terminologien.bfarm.de/ValueSet-icd10gm-2025.html"
                    ))
            @ApiResponse(responseCode = "404", description = "FHIR Ressource not found (NOT_FOUND)")

            @ApiResponse(responseCode = "503", description = "Service unavailable (SERVICE_UNAVAILABLE)")

    @GetMapping(value = RESOLVE_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> resolve(
            @RequestParam
            @Parameter(description = "Canonical url of FHIR resource", example = "http://fhir.de/ValueSet/bfarm/icd-10-gm", required = true)
            String url,
            @RequestParam(required = false)
            @Parameter(description = "Version of the FHIR resource", example = "2025", required = false)
            String version) {
        String notFoundMessage = "Die angefragte FHIR-Ressource konnte nicht gefunden werden: ";
        String foundMessage = "Die angefragte FHIR-Ressource wurde im ZTS gefunden. Weiterleitung zur {}";
        // URL-Format validieren
        if (!Pattern.matches(URL_REGEX, url)) {
            return Mono.error(new IllegalArgumentException("Ungültiges URL-Format! Bitte geben Sie ein gültiges URL-Format an."));
        }

        //  Versions-Format validieren (optional)
        if (version != null && !Pattern.matches(VERSION_REGEX, version)) {

            return Mono.error(new IllegalArgumentException("Ungültiges Format der Version! Bitte geben Sie ein gültiges Format an."));
        }
        if (!schedulerService.isOnceDownloadSuccessful()) {
            return Mono.error(new ServiceUnavailableException(MESSAGE_INITIAL_DOWNLOAD_FAILED));
        }

        // Resourcenliste aus Datei laden
        //  URL und Version in der Resourcenliste prüfen


        Optional<JSONObject> matchedResource = FileProcessHelper.findHighestVersionResource(applicationConfig.getPathToResourcesFile(), url, version);
        if (matchedResource.isPresent()) {
            JSONObject resource = matchedResource.get();
            String newUrl = applicationConfig.getContentBaseURL() + "/" + resource.getString(FHIR_ATTRIBUTE_RESOURCE_TYPE) + "-" + resource.getString(FHIR_ATTRIBUTE_RESOURCE_ID) + ".html";
            log.debug(foundMessage, newUrl);
            return Mono.just(ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, newUrl).build());
        } else {
            return Mono.error(new NoResourceFoundException(URI.create(url), notFoundMessage));
        }


    }



    /**
     * Endpunkt /health zum Überprüfen des Gesundheitszustands des Services
     *
     * @return Gesundheitszustand des Services
     */
    @GetMapping(value = HEALH_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> healthCheck() {

        boolean downloadSuccessful = schedulerService.isOnceDownloadSuccessful();
        if (!downloadSuccessful) {
            return Mono.error(
                    new ServiceUnavailableException(
                            MESSAGE_INITIAL_DOWNLOAD_FAILED));
        } else {
            return Mono.just(
                    ResponseEntity.ok().body(new HealthResponse("UP", "healthy")));

        }

    }

    /**
     * Endpunkt /fhir/{resourceType}/{resourceId} zum Auflösen einer Ressource
     *
     * @param resourceType Typ der Ressource
     * @param resourceId   ID der Ressource
     * @return
     */
    @Operation(
            summary = "Resolve a FHIR resource and redirect to the resolved resource page in ZTS",
            description = "Resolves a FHIR resource based on the resource type and resource identifier and redirect to the resolved resource page in ZTS."
    )

    @ApiResponse(responseCode = "302", description = "Redirect to the resolved FHIR resource",
            headers = @Header(
                    name = "Location",
                    description = "URL of the resolved resource, e.g. https://terminologien.bfarm.de/CodeSystem-icd10gm-2025.html",
                    schema = @Schema(type = "string", format = "uri"),
                    example = "https://terminologien.bfarm.de/CodeSystem-icd10gm-2025.html"
            ))
    @ApiResponse(responseCode = "404", description = "FHIR resource not found (NOT_FOUND)")
    @ApiResponse(responseCode = "503", description = "Service unavailable (SERVICE_UNAVAILABLE)")

    @GetMapping(FHIR_RESOURCE_ENDPOINT + "/{resourceType}/{resourceId}")
    public Mono<ResponseEntity<Object>> serviceRedirect(
            @PathVariable
            @Parameter(description = "Resource type of a FHIR resource", example = "CodeSystem", schema = @Schema(type = "string",allowableValues = {"CodeSystem", "ValueSet", "ConceptMap"}))
            String resourceType,
            @PathVariable
            @Parameter(description = "Resource identifier of a FHIR resource", example = "icd10gm")
            String resourceId) {
        // Ressourcetyp validieren
        if (!Pattern.matches(CANONICAL_TYPE_PATTERN, resourceType)) {
            return Mono.error(new IllegalArgumentException("Ungültiger Ressourcentyp! Bitte geben Sie ein gültiger Ressourcentyp an."));
        }

        //  Identifier der Ressourcen validieren
        if (!Pattern.matches(CANONICAL_IDENTIFIER_PATTERN, resourceId)) {

            return Mono.error(new IllegalArgumentException("Ungültiges Format des Identifiers der Ressource! Bitte geben Sie ein gültiges Format an."));
        }

        log.info("HTTP-Request: GET " + FHIR_RESOURCE_ENDPOINT + "/" + resourceType + "/" + resourceId);
        if (!schedulerService.isOnceDownloadSuccessful()) {
            return Mono.error(new ServiceUnavailableException(MESSAGE_INITIAL_DOWNLOAD_FAILED));
        }
        String canonical = CANONICAL_BASE + FHIR_RESOURCE_ENDPOINT + "/" + resourceType + "/" + resourceId;
        Optional<JSONObject> matchedResource = FileProcessHelper.findMatchedResource(applicationConfig.getPathToResourcesFile(), canonical);


        if (matchedResource.isPresent()) {
            JSONObject resource = matchedResource.get();
            String newUrl = String.format("%s/%s-%s.html", applicationConfig.getContentBaseURL(), resource.getString(FHIR_ATTRIBUTE_RESOURCE_TYPE), resource.getString(FHIR_ATTRIBUTE_RESOURCE_ID));
            log.debug("Die angefragte FHIR-Ressource wurde im ZTS gefunden. Weiterleitung zur {}", newUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", newUrl);
            return Mono.just(ResponseEntity.status(HttpStatus.FOUND).headers(headers).build());

        } else {
            return Mono.error(new NoResourceFoundException(URI.create(canonical), "Die angefragte FHIR-Ressource konnte nicht gefunden werden"));
        }

    }


}
