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
 * Unit tests for Telephone value object.
 *
 * @author Wick Dynex
 */
class TelephoneTest {

    @Test
    void shouldCreateValidTelephone() {
        Telephone telephone = Telephone.of("1234567890");

        assertThat(telephone).isNotNull();
        assertThat(telephone.number()).isEqualTo("1234567890");
        assertThat(telephone.isValid()).isTrue();
    }

    @Test
    void shouldFailWithInvalidLength() {
        assertThatThrownBy(() -> new Telephone("12345")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telephone must be exactly 10 digits");
    }

    @Test
    void shouldFailWithNonNumericCharacters() {
        assertThatThrownBy(() -> new Telephone("123456789a")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telephone must be exactly 10 digits");
    }

    @Test
    void shouldFailWithSpaces() {
        assertThatThrownBy(() -> new Telephone("123 456 7890")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Telephone must be exactly 10 digits");
    }

    @Test
    void shouldAllowNullViaFactory() {
        Telephone telephone = Telephone.of(null);

        assertThat(telephone).isNull();
    }

    @Test
    void shouldSupportValueEquality() {
        Telephone tel1 = Telephone.of("1234567890");
        Telephone tel2 = Telephone.of("1234567890");
        Telephone tel3 = Telephone.of("0987654321");

        assertThat(tel1).isEqualTo(tel2);
        assertThat(tel1).isNotEqualTo(tel3);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        Telephone tel1 = Telephone.of("1234567890");
        Telephone tel2 = Telephone.of("1234567890");

        assertThat(tel1.hashCode()).isEqualTo(tel2.hashCode());
    }

    @Test
    void toStringShouldReturnNumber() {
        Telephone telephone = Telephone.of("1234567890");

        assertThat(telephone.toString()).isEqualTo("1234567890");
    }

}
