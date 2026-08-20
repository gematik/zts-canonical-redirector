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

package de.gematik.zts.redirector.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.fieldnames.LogstashFieldNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import tools.jackson.core.JsonGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomLogLevelJsonProviderTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private ILoggingEvent loggingEvent;

    private CustomLogLevelJsonProvider customLogLevelJsonProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customLogLevelJsonProvider = new CustomLogLevelJsonProvider();
    }

    @Test
    void writeTo_MapsTraceLevelToDebug() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.TRACE);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "DEBUG");
    }

    @Test
    void writeTo_MapsDebugLevelToDebug() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.DEBUG);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "DEBUG");
    }

    @Test
    void writeTo_MapsInfoLevelToInfo() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.INFO);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "INFO");
    }

    @Test
    void writeTo_MapsWarnLevelToWarning() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.WARN);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "WARNING");
    }

    @Test
    void writeTo_MapsErrorLevelToError() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.ERROR);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "ERROR");
    }

    @Test
    void writeTo_MapsUnknownLevelToDefault() throws IOException {
        when(loggingEvent.getLevel()).thenReturn(Level.OFF);
        customLogLevelJsonProvider.writeTo(jsonGenerator, loggingEvent);
        verify(jsonGenerator).writeStringProperty("severity", "DEFAULT");
    }

    @Test
    void setFieldNames_SetsFieldNameToSeverity() {
        LogstashFieldNames fieldNames = new LogstashFieldNames();
        customLogLevelJsonProvider.setFieldNames(fieldNames);
        assertEquals("severity", customLogLevelJsonProvider.getFieldName());
    }
}

