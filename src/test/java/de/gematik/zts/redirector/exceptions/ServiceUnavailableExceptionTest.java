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

package de.gematik.zts.redirector.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUnavailableExceptionTest {

    @Test
    void serviceUnavailableException_ReturnsCorrectMessage() {
        ServiceUnavailableException exception = new ServiceUnavailableException("Service is down");
        assertEquals("503 SERVICE_UNAVAILABLE \"Service is down\"", exception.getMessage());
    }

    @Test
    void serviceUnavailableException_WithNullMessage() {
        ServiceUnavailableException exception = new ServiceUnavailableException(null);
        assertEquals("503 SERVICE_UNAVAILABLE", exception.getMessage());
    }

    @Test
    void serviceUnavailableException_WithEmptyMessage() {
        ServiceUnavailableException exception = new ServiceUnavailableException("");
        assertEquals("503 SERVICE_UNAVAILABLE \"\"", exception.getMessage());
    }
}
