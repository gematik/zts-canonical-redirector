<img align="right" width="250" height="47" src="https://raw.githubusercontent.com/gematik/gematik.github.io/master/Gematik_Logo_Flag_With_Background.png" /> <br />

# canonical-redirector

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
       <ul>
        <li><a href="#release-notes">Release Notes</a></li>
        <li><a href="#contributions-and-acknowledgements">Contributions and Acknowledgements</a></li>
      </ul>
	</li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#endpoints">Endpoints</a></li>
        <li><a href="#build project">Build Project</a></li>
      </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#additional-notes">Additional Notes and Disclaimer from gematik GmbH</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

## About The Project
The **ZTS Canonical Redirector API** is a service that **resolves FHIR resources** based on their **canonical URLs** or **FHIR resource type & identifier** and redirects clients to the appropriate **ZTS (Zentraler Terminologieserver)** page.

### Release Notes

See [ReleaseNotes.md](./ReleaseNotes.md) for all information regarding the (newest) releases.

### Contributions and Acknowledgements

This open source project was developed in cooperation with the German Federal Institute for Drugs and Medical Devices (BfArM) on the basis of Section 355 (12-14) of the German Social Code Book V (SGB V).
As part of the projects implementation, the fbeta GmbH and Fraunhofer FOKUS were commissioned to provide software development services.

We would like to thank all parties involved for their constructive and trusted collaboration.

## Getting Started

### Endpoints

#### 1. Resolve Endpoint

 - **Endpoint**: `GET /resolve`  
 - **Description**: Resolves a **FHIR resource** using its **canonical URL** and **version** (optional), then redirects to the correct **ZTS page**.

##### Request Parameters

| Parameter | Required | Type | Description | Example |
|-----------|----|------|-------------|---------|
| `url`  | yes | `string` | The canonical URL of the FHIR resource | `http://fhir.de/ValueSet/bfarm/icd-10-gm` |
| `version` | no | `string` | (Optional) The version of the FHIR resource | `2025` |

##### Response

| HTTP Status | Description | Headers |
|------------|-------------|---------|
| `302 Found` | Redirects to the resolved resource in ZTS | `Location` → `https://terminologien.bfarm.de/ValueSet-icd10gm-2025.html` |

##### Example Request

```http
GET https://terminologien.bfarm.de/resolve?url=http://fhir.de/ValueSet/bfarm/icd-10-gm&version=2025
```
**Example Redirected URL**:
```
https://terminologien.bfarm.de/ValueSet-icd10gm-2025.html
```

---

#### 2. Resolve a FHIR Resource by FHIR Resource Type & Identifier

 - **Endpoint**: `GET /fhir/{resourceType}/{resourceId}`  
 - **Description**: Resolves a **FHIR resource** based on its **type** (`CodeSystem`, `ValueSet`, or `ConceptMap`) and **resource identifier**, then redirects to the correct **ZTS page**.

##### Request Parameters

| Parameter | Required | Type | Description | Example |
|-----------|-----|------|-------------|---------|
| `resourceType` | Yes | `string` | Type of the FHIR resource (`CodeSystem`, `ValueSet`, `ConceptMap`) | `CodeSystem` |
| `resourceId`  | Yes | `string` | Identifier of the FHIR resource | `icd10gm` |

##### Response

| HTTP Status | Description | Headers |
|------------|-------------|---------|
| `302 Found` | Redirects to the resolved FHIR resource | `Location` → `https://terminologien.bfarm.de/CodeSystem-icd10gm-2025.html` |
| `404 Not Found` | FHIR resource could not be found | — |


##### Example Request

```http
GET https://terminologien.bfarm.de/fhir/CodeSystem/icd10gm
```
🔹 **Example Redirected URL**:
```
https://terminologien.bfarm.de/CodeSystem-icd10gm-2025.html
```

---

#### 3. Health Check Endpoint

 - **Endpoint**: `GET /resolve/health`  
 - **Description**: Checks the health of the service.
 - **Response OK**: `{"status":"UP","reason":"healthy"}`

#### 4. OpenAPI Document Endpoint

 - **Endpoint**: `GET /docs/v3/api-docs`  
 - **Description**: Returns the OpenAPI document for the service in JSON and YAML formats.

### Build Project

To quickly check your build environment do in project root:

In application.yml following variables for addition context path:

set server url

- CONTENT_BASE_URL: "https://{{ $.Values.domainName }}"

set url to get resources list

- RESOURCES_LIST: "https://{{ $.Values.domainName }}/terminologies/resourceslist.json"

set time interval in milliseconds for fetching resources list

- TIME_INTERVAL: "600000"

set file path to store fetched resources list
- PATH_TO_RESOURCES_FILE: "/app/files/resources_list.json"

set server port

- SERVER_PORT: "8080"

save logs to file
- SPRING_PROFILES_ACTIVE: "file-logging"

set logging root level

- LOGGING_ROOT_LEVEL: "INFO"

set logging levels for springboot

- LOGGING_SPRINGBOOT_LEVEL: "WARN"

set logging levels for web reactive

- LOGGING_WEB_REACTIVE_LEVEL: "INFO"

set logging levels for webflux

- LOGGING_WEBFLUX_LEVEL: "INFO"

set logging levels for redirector service

- LOGGING_REDIRECTOR_LEVEL: "DEBUG"

cache control setting for http request header 'no-cache, no-store'

- CACHE_IS_PUBLIC: "false"
- CACHE_NO_CACHE: "true"
- CACHE_NO_STORE: "true"
- CACHE_MAX_AGE: "0"

enable springdoc-openapi
- SPRINGDOC_API-DOCS_ENABLED: "true"

disable swagger-ui

- SPRINGDOC_SWAGGER-UI_ENABLED: "false"

 path to the api-docs

 - SPRINGDOC_API-DOCS_PATH: "/docs/v3/api-docs"

excluded path(s) - comma separated

- SPRINGDOC_PATHS-TO-EXCLUDE: "/api/health"

open api version

- SPRINGDOC_API-DOCS_VERSION: "openapi_3_0"

disable auto-tagging

- SPRINGDOC_AUTO-TAG-CLASSES: "true"

server-url(s) for generated api-docs - comma separated

- APPLICATION_API-DOCS_SERVERURLS: "https://{{ $.Values.domainName }}"

title and version for generated api-docs

- APPLICATION_API-DOCS_TITLE: "ZTS Canonical Redirector API"
- APPLICATION_API-DOCS_VERSION: "1.0.0"

Building: <br>
`mvn clean package`

#### Start SonarQube Locally
`mvn clean verify sonar:sonar -Dsonar.projectKey=$Your_ProjectKey -Dsonar.projectName=$Your_ProjectName -Dsonar.host.url=http://localhost:9000 -Dsonar.token=$Your_Token -Pcoverage`

## Contributing
If you want to contribute, please check our [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

Copyright 2026 gematik GmbH

Apache License, Version 2.0

See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License

## Additional Notes and Disclaimer from gematik GmbH

1. Copyright notice: Each published work result is accompanied by an explicit statement of the license conditions for use. These are regularly typical conditions in connection with open source or free software. Programs described/provided/linked here are free software, unless otherwise stated.
2. Permission notice: Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
1. The copyright notice (Item 1) and the permission notice (Item 2) shall be included in all copies or substantial portions of the Software.
2. The software is provided "as is" without warranty of any kind, either express or implied, including, but not limited to, the warranties of fitness for a particular purpose, merchantability, and/or non-infringement. The authors or copyright holders shall not be liable in any manner whatsoever for any damages or other claims arising from, out of or in connection with the software or the use or other dealings with the software, whether in an action of contract, tort, or otherwise.
3. We take open source license compliance very seriously. We are always striving to achieve compliance at all times and to improve our processes. If you find any issues or have any suggestions or comments, or if you see any other ways in which we can improve, please reach out to: ospo@gematik.de
3. Parts of this software and - in isolated cases - content such as text or images may have been developed using the support of AI tools. They are subject to the same reviews, tests, and security checks as any other contribution. The functionality of the software itself is not based on AI decisions.

## Contact
We take open source license compliance very seriously. We are always striving to achieve compliance at all times and to improve our processes.
This software is currently being tested to ensure its technical quality and legal compliance. Your feedback is highly valued.
If you find any issues or have any suggestions or comments, or if you see any other ways in which we can improve, please reach out to: OSPO@gematk.de.