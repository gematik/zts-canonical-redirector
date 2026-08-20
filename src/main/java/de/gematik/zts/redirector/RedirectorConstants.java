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

package de.gematik.zts.redirector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;




@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedirectorConstants {
    public static final String RESOLVE_ENDPOINT = "/resolve";
    public static final String HEALH_ENDPOINT = "/api/health";
    public static final String RESOURCES_LIST_ENDPOINT = "/resolve/resource-list";

    public static final String FHIR_RESOURCE_ENDPOINT = "/fhir" ;

    public static final String CANONICAL_BASE = "https://terminologien.bfarm.de";

    public static final String FHIR_ATTRIBUTE_RESOURCE_TYPE = "resourceType";

    public static final String FHIR_ATTRIBUTE_RESOURCE_ID = "id";

    public static final String FHIR_ATTRIBUTE_RESOURCE_VERSION = "version";
    public static final String FHIR_ATTRIBUTE_RESOURCES = "resources";

    public static final String FHIR_ATTRIBUTE_URL = "url";

    public static final String FHIR_ATTRIBUTE_IDENTIFIER = "identifier";

    public static final String PROBLEMDETAILS_TITLE_BAD_REQUEST =
            "Fehler bei der Verarbeitung der Anfrage (BAD_REQUEST)";

    public static final String PROBLEMDETAILS_TITLE_INTERNAL_SERVER_ERROR =
            "Interner Serverfehler (INTERNAL_SERVER_ERROR)";
    public static final String PROBLEMDETAILS_TITLE_NOT_FOUND =
            "Ressource nicht gefunden (NOT_FOUND)";

    public static final String PROBLEMDETAILS_TITLE_SERVICE_UNAVAILABLE =
            "Der Service ist derzeit nicht verfügbar (SERVICE_UNAVAILABLE)";

    public static final String PROBLEMDETAILS_PROPERTY_TIMESTAMP = "timestamp";

    public static final String ATTRIBUTE_USER = "user";
    public static final String ATTRIBUTE_LOG_MESSAGE = "logMessage";

    public static final String SEVERITY_LEVEL = "severity";

}
