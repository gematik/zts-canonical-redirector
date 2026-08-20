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
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class CustomOpenAPIConfig {

    private static final List<String> openApiTags = List.of("Canonical-Redirector-API");


    private final OpenAPIDocConfig openAPIDocConfig;

    // To sort tags in the order defined above
    private static int getTagPriority(Tag tag) {
        for (int i = 0; i < openApiTags.size(); i++) {
            var frag = openApiTags.get(i);
            if (Strings.CS.contains(tag.getName(), frag)) return i;
        }
        // All the others will go in lexicographical order
        return Integer.MAX_VALUE;
    }


    @Bean
    public OpenApiCustomizer customizeOpenApi() {

        return openApi -> {
            // sort the tags in the desired order
            List<Tag> tags = openApi.getTags();
            if (tags != null) {
                tags.sort(
                        Comparator.comparingInt(CustomOpenAPIConfig::getTagPriority)
                                // Last resort for tags of least/same priority: lexicographically
                                .thenComparing(Tag::getName, Comparator.naturalOrder()));
                openApi.setTags(tags);
            }
            // remove the operationId from the generated OpenAPI
            var paths = openApi.getPaths();
            if (paths != null) {
                paths
                        .values()
                        .forEach(
                                pathItem ->
                                        pathItem.readOperations().forEach(operation -> operation.setOperationId(null)));
            }

            var appVersion =
                    getClass().getPackage().getImplementationVersion() != null
                            ? getClass().getPackage().getImplementationVersion()
                            : openAPIDocConfig.getVersion();

            // set customized server URLs and API info
            openApi
                    .servers(openAPIDocConfig.getServerUrls().stream().map(serverUrl -> new Server().url(serverUrl)).toList())
                    .info(new Info().title(openAPIDocConfig.getTitle()).version(appVersion));
        };
    }


    @Bean
    public GroupedOpenApi redirectOpenApi() {
        String[] paths = {"/resolve/**", "/fhir/**"};
        return GroupedOpenApi.builder()
                .group("redirect")
                .pathsToMatch(paths)
                .addOpenApiCustomizer(customizeOpenApi())
                .build();
    }
}



