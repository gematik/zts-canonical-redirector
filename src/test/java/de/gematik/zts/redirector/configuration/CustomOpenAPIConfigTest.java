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

package de.gematik.zts.redirector.configuration;

import de.gematik.zts.redirector.model.OpenAPIDocConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
class CustomOpenAPIConfigTest {


    @Mock
    private OpenAPIDocConfig openAPIDocConfig;

    @InjectMocks
    private CustomOpenAPIConfig customOpenAPIConfig;

    @BeforeEach
    void setUp() {
        lenient().when(openAPIDocConfig.getServerUrls()).thenReturn(List.of("http://localhost:8086"));
        lenient().when(openAPIDocConfig.getTitle()).thenReturn("ZTS - Canonical Redirector API");
        lenient().when(openAPIDocConfig.getVersion()).thenReturn("1.0.0");

    }

    @Test
    void testCustomizeOpenApi_SortsTagsCorrectly() {
        OpenApiCustomizer customizer = customOpenAPIConfig.customizeOpenApi();
        OpenAPI openAPI = new OpenAPI();

        // Creating unordered tags
        List<Tag> tags = Arrays.asList(new Tag().name("Test-API-Tag"), new Tag().name("Canonical-Redirector-API"), new Tag().name("OtherTag"));
        openAPI.setTags(tags);

        // Applying the customizer
        customizer.customise(openAPI);

        // Verifying sorting order
        assertEquals("Canonical-Redirector-API", openAPI.getTags().get(0).getName());
        assertEquals("OtherTag", openAPI.getTags().get(1).getName());
        assertEquals("Test-API-Tag", openAPI.getTags().get(2).getName());

    }

    @Test
    void testCustomizeOpenApi_SetsServerUrlsAndInfo() {

        OpenApiCustomizer customizer = customOpenAPIConfig.customizeOpenApi();
        OpenAPI openAPI = new OpenAPI();

        // Applying the customizer
        customizer.customise(openAPI);

        // Checking server URLs
        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
        assertEquals("http://localhost:8086", openAPI.getServers().get(0).getUrl());

        // Checking API Info
        assertNotNull(openAPI.getInfo());
        assertEquals("ZTS - Canonical Redirector API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void testCustomizeOpenApi_RemovesOperationIds() {
        OpenApiCustomizer customizer = customOpenAPIConfig.customizeOpenApi();
        OpenAPI openAPI = new OpenAPI();

        // Mocking paths with operations that have operation IDs
        Paths paths = new Paths();
        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setOperationId("testOperationId");

        pathItem.setGet(operation);
        paths.addPathItem("/test", pathItem);
        openAPI.setPaths(paths);

        // Applying the customizer
        customizer.customise(openAPI);

        // Checking that the operationId was removed
        assertNull(openAPI.getPaths().get("/test").getGet().getOperationId());
    }

    @Test
    void testRedirectOpenApi_CreatesGroupedOpenApiBean() {
        GroupedOpenApi groupedOpenApi = customOpenAPIConfig.redirectOpenApi();

        assertNotNull(groupedOpenApi);
        assertEquals("redirect", groupedOpenApi.getGroup());
        assertArrayEquals(new String[]{"/resolve/**", "/fhir/**"}, groupedOpenApi.getPathsToMatch().toArray());
    }
}
