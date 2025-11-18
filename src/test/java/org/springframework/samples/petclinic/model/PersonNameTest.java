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
package org.springframework.samples.petclinic.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PersonName value object.
 *
 * @author Wick Dynex
 */
class PersonNameTest {

	@Test
	void shouldCreateValidPersonName() {
		PersonName name = new PersonName("John", "Doe");

		assertThat(name.firstName()).isEqualTo("John");
		assertThat(name.lastName()).isEqualTo("Doe");
	}

	@Test
	void shouldFormatFullName() {
		PersonName name = new PersonName("Jane", "Smith");

		assertThat(name.getFullName()).isEqualTo("Jane Smith");
	}

	@Test
	void shouldFormatLastNameFirst() {
		PersonName name = new PersonName("Bob", "Johnson");

		assertThat(name.getLastNameFirst()).isEqualTo("Johnson, Bob");
	}

	@Test
	void shouldFailWithBlankFirstName() {
		assertThatThrownBy(() -> new PersonName("", "Doe")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First name must not be blank");
	}

	@Test
	void shouldFailWithNullFirstName() {
		assertThatThrownBy(() -> new PersonName(null, "Doe")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First name must not be blank");
	}

	@Test
	void shouldFailWithBlankLastName() {
		assertThatThrownBy(() -> new PersonName("John", "")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Last name must not be blank");
	}

	@Test
	void shouldFailWithNullLastName() {
		assertThatThrownBy(() -> new PersonName("John", null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Last name must not be blank");
	}

	@Test
	void shouldReturnNullFromFactoryWithNullFirstName() {
		PersonName name = PersonName.of(null, "Doe");

		assertThat(name).isNull();
	}

	@Test
	void shouldReturnNullFromFactoryWithNullLastName() {
		PersonName name = PersonName.of("John", null);

		assertThat(name).isNull();
	}

	@Test
	void shouldCreateFromFactoryWithValidNames() {
		PersonName name = PersonName.of("Alice", "Wonder");

		assertThat(name).isNotNull();
		assertThat(name.firstName()).isEqualTo("Alice");
		assertThat(name.lastName()).isEqualTo("Wonder");
	}

	@Test
	void shouldSupportValueEquality() {
		PersonName name1 = new PersonName("John", "Doe");
		PersonName name2 = new PersonName("John", "Doe");
		PersonName name3 = new PersonName("Jane", "Doe");

		assertThat(name1).isEqualTo(name2);
		assertThat(name1).isNotEqualTo(name3);
	}

	@Test
	void shouldHaveConsistentHashCode() {
		PersonName name1 = new PersonName("John", "Doe");
		PersonName name2 = new PersonName("John", "Doe");

		assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
	}

	@Test
	void toStringShouldReturnFullName() {
		PersonName name = new PersonName("George", "Franklin");

		assertThat(name.toString()).isEqualTo("George Franklin");
	}

}
