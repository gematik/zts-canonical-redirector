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

class CanonicalRedirectorExceptionTest {

    @Test
    void canonicalRedirectorException_ReturnsCorrectMessage() {
        CanonicalRedirectorException exception = new CanonicalRedirectorException("Test message");
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void canonicalRedirectorException_WithNullMessage() {
        CanonicalRedirectorException exception = new CanonicalRedirectorException(null);
        assertNull(exception.getMessage());
    }

    @Test
    void canonicalRedirectorException_WithEmptyMessage() {
        CanonicalRedirectorException exception = new CanonicalRedirectorException("");
        assertEquals("", exception.getMessage());
    }
}
