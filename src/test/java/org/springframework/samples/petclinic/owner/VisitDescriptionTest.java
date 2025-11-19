/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for VisitDescription value object.
 *
 * @author Wick Dynex
 */
class VisitDescriptionTest {

    @Test
    void shouldCreateValidDescription() {
        VisitDescription description = new VisitDescription("Annual checkup");

        assertThat(description.value()).isEqualTo("Annual checkup");
        assertThat(description.isValid()).isTrue();
    }

    @Test
    void shouldFailWithBlankDescription() {
        assertThatThrownBy(() -> new VisitDescription("")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Visit description must not be blank");
    }

    @Test
    void shouldFailWithNullDescription() {
        assertThatThrownBy(() -> new VisitDescription(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Visit description must not be blank");
    }

    @Test
    void shouldFailWithTooLongDescription() {
        String tooLong = "a".repeat(VisitDescription.MAX_LENGTH + 1);

        assertThatThrownBy(() -> new VisitDescription(tooLong)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Visit description exceeds maximum length");
    }

    @Test
    void shouldAllowMaxLengthDescription() {
        String maxLength = "a".repeat(VisitDescription.MAX_LENGTH);

        VisitDescription description = new VisitDescription(maxLength);

        assertThat(description.value()).hasSize(VisitDescription.MAX_LENGTH);
        assertThat(description.isValid()).isTrue();
    }

    @Test
    void shouldReturnFullTextForShortSummary() {
        VisitDescription description = new VisitDescription("Short text");

        String summary = description.getSummary(100);

        assertThat(summary).isEqualTo("Short text");
    }

    @Test
    void shouldTruncateForLongSummary() {
        VisitDescription description = new VisitDescription("This is a very long description that needs truncation");

        String summary = description.getSummary(20);

        assertThat(summary).isEqualTo("This is a very lo...");
        assertThat(summary).hasSize(20);
    }

    @Test
    void shouldFailWithInvalidSummaryLength() {
        VisitDescription description = new VisitDescription("Some text");

        assertThatThrownBy(() -> description.getSummary(0)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxChars must be positive");
    }

    @Test
    void shouldReturnNullFromFactoryWithNull() {
        VisitDescription description = VisitDescription.of(null);

        assertThat(description).isNull();
    }

    @Test
    void shouldCreateFromFactoryWithValidText() {
        VisitDescription description = VisitDescription.of("Vaccination");

        assertThat(description).isNotNull();
        assertThat(description.value()).isEqualTo("Vaccination");
    }

    @Test
    void shouldSupportValueEquality() {
        VisitDescription desc1 = new VisitDescription("Same description");
        VisitDescription desc2 = new VisitDescription("Same description");
        VisitDescription desc3 = new VisitDescription("Different description");

        assertThat(desc1).isEqualTo(desc2);
        assertThat(desc1).isNotEqualTo(desc3);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        VisitDescription desc1 = new VisitDescription("Annual checkup");
        VisitDescription desc2 = new VisitDescription("Annual checkup");

        assertThat(desc1.hashCode()).isEqualTo(desc2.hashCode());
    }

    @Test
    void toStringShouldReturnValue() {
        VisitDescription description = new VisitDescription("Rabies vaccination");

        assertThat(description.toString()).isEqualTo("Rabies vaccination");
    }

}
