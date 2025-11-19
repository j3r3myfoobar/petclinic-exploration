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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for BirthDate value object.
 *
 * @author Wick Dynex
 */
class BirthDateTest {

    @Test
    void shouldCreateValidBirthDate() {
        LocalDate date = LocalDate.of(2020, 1, 15);
        BirthDate birthDate = new BirthDate(date);

        assertThat(birthDate.date()).isEqualTo(date);
    }

    @Test
    void shouldFailWithNullDate() {
        assertThatThrownBy(() -> new BirthDate(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Birth date must not be null");
    }

    @Test
    void shouldFailWithFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> new BirthDate(futureDate)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Birth date cannot be in the future");
    }

    @Test
    void shouldAllowToday() {
        LocalDate today = LocalDate.now();
        BirthDate birthDate = new BirthDate(today);

        assertThat(birthDate.date()).isEqualTo(today);
    }

    @Test
    void shouldCalculateAgeInYears() {
        LocalDate fiveYearsAgo = LocalDate.now().minusYears(5);
        BirthDate birthDate = new BirthDate(fiveYearsAgo);

        assertThat(birthDate.getAgeInYears()).isEqualTo(5);
    }

    @Test
    void shouldCalculateAgeInMonths() {
        LocalDate date = LocalDate.now().minusYears(2).minusMonths(3);
        BirthDate birthDate = new BirthDate(date);

        assertThat(birthDate.getAgeInMonths()).isEqualTo(27); // 2 years * 12 + 3 months
    }

    @Test
    void shouldIdentifyElderly() {
        LocalDate eightYearsAgo = LocalDate.now().minusYears(8);
        BirthDate birthDate = new BirthDate(eightYearsAgo);

        assertThat(birthDate.isElderly()).isTrue();
        assertThat(birthDate.isElderly(7)).isTrue();
        assertThat(birthDate.isElderly(10)).isFalse();
    }

    @Test
    void shouldIdentifyNotElderly() {
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);
        BirthDate birthDate = new BirthDate(threeYearsAgo);

        assertThat(birthDate.isElderly()).isFalse();
    }

    @Test
    void shouldIdentifyPuppy() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        BirthDate birthDate = new BirthDate(sixMonthsAgo);

        assertThat(birthDate.isPuppy()).isTrue();
    }

    @Test
    void shouldIdentifyNotPuppy() {
        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);
        BirthDate birthDate = new BirthDate(twoYearsAgo);

        assertThat(birthDate.isPuppy()).isFalse();
    }

    @Test
    void shouldFormatAgeDescriptionForYearsOnly() {
        LocalDate threeYearsAgo = LocalDate.now().minusYears(3);
        BirthDate birthDate = new BirthDate(threeYearsAgo);

        assertThat(birthDate.getAgeDescription()).isEqualTo("3 years");
    }

    @Test
    void shouldFormatAgeDescriptionForMonthsOnly() {
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        BirthDate birthDate = new BirthDate(sixMonthsAgo);

        assertThat(birthDate.getAgeDescription()).isEqualTo("6 months");
    }

    @Test
    void shouldFormatAgeDescriptionForYearsAndMonths() {
        LocalDate date = LocalDate.now().minusYears(2).minusMonths(5);
        BirthDate birthDate = new BirthDate(date);

        assertThat(birthDate.getAgeDescription()).isEqualTo("2 years 5 months");
    }

    @Test
    void shouldFormatAgeDescriptionForOneYear() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        BirthDate birthDate = new BirthDate(oneYearAgo);

        assertThat(birthDate.getAgeDescription()).isEqualTo("1 year");
    }

    @Test
    void shouldFormatAgeDescriptionForOneMonth() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        BirthDate birthDate = new BirthDate(oneMonthAgo);

        assertThat(birthDate.getAgeDescription()).isEqualTo("1 month");
    }

    @Test
    void shouldReturnNullFromFactoryWithNull() {
        BirthDate birthDate = BirthDate.of(null);

        assertThat(birthDate).isNull();
    }

    @Test
    void shouldCreateFromFactoryWithValidDate() {
        LocalDate date = LocalDate.of(2018, 6, 15);
        BirthDate birthDate = BirthDate.of(date);

        assertThat(birthDate).isNotNull();
        assertThat(birthDate.date()).isEqualTo(date);
    }

    @Test
    void shouldSupportValueEquality() {
        LocalDate date = LocalDate.of(2020, 3, 10);
        BirthDate bd1 = new BirthDate(date);
        BirthDate bd2 = new BirthDate(date);
        BirthDate bd3 = new BirthDate(LocalDate.of(2019, 3, 10));

        assertThat(bd1).isEqualTo(bd2);
        assertThat(bd1).isNotEqualTo(bd3);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        LocalDate date = LocalDate.of(2020, 3, 10);
        BirthDate bd1 = new BirthDate(date);
        BirthDate bd2 = new BirthDate(date);

        assertThat(bd1.hashCode()).isEqualTo(bd2.hashCode());
    }

    @Test
    void toStringShouldIncludeAgeDescription() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        BirthDate birthDate = new BirthDate(date);

        String toString = birthDate.toString();

        assertThat(toString).contains("2020-01-01");
        assertThat(toString).contains("years");
    }

}
