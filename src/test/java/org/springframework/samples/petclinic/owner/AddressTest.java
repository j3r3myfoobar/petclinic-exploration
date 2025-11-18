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
 * Unit tests for Address value object.
 *
 * @author Wick Dynex
 */
class AddressTest {

	@Test
	void shouldCreateAddressWithBothFields() {
		Address address = Address.of("123 Main St", "Springfield");

		assertThat(address.street()).isEqualTo("123 Main St");
		assertThat(address.city()).isEqualTo("Springfield");
		assertThat(address.getFullAddress()).isEqualTo("123 Main St, Springfield");
	}

	@Test
	void shouldCreateAddressWithStreetOnly() {
		Address address = Address.of("123 Main St", null);

		assertThat(address.street()).isEqualTo("123 Main St");
		assertThat(address.city()).isNull();
		assertThat(address.getFullAddress()).isEqualTo("123 Main St");
	}

	@Test
	void shouldCreateAddressWithCityOnly() {
		Address address = Address.of(null, "Springfield");

		assertThat(address.street()).isNull();
		assertThat(address.city()).isEqualTo("Springfield");
		assertThat(address.getFullAddress()).isEqualTo("Springfield");
	}

	@Test
	void shouldFailWhenBothFieldsAreNull() {
		assertThatThrownBy(() -> Address.of(null, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Address must have at least street or city");
	}

	@Test
	void shouldFailWhenBothFieldsAreBlank() {
		assertThatThrownBy(() -> Address.of("", "")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Address must have at least street or city");
	}

	@Test
	void shouldSupportValueEquality() {
		Address address1 = Address.of("123 Main St", "Springfield");
		Address address2 = Address.of("123 Main St", "Springfield");
		Address address3 = Address.of("456 Elm St", "Springfield");

		assertThat(address1).isEqualTo(address2);
		assertThat(address1).isNotEqualTo(address3);
	}

	@Test
	void shouldHaveConsistentHashCode() {
		Address address1 = Address.of("123 Main St", "Springfield");
		Address address2 = Address.of("123 Main St", "Springfield");

		assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
	}

	@Test
	void toStringShouldReturnFullAddress() {
		Address address = Address.of("123 Main St", "Springfield");

		assertThat(address.toString()).isEqualTo("123 Main St, Springfield");
	}

}
