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

import org.jmolecules.ddd.types.ValueObject;

import jakarta.persistence.Column;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing a telephone number. Encapsulates validation rules and ensures
 * immutability. Uses value-based equality.
 *
 * @author Wick Dynex
 */
public record Telephone(@Column(name = "telephone") String number) implements ValueObject {

	/**
	 * Compact constructor with validation.
	 * @throws IllegalArgumentException if telephone format is invalid
	 */
	public Telephone {
		if (number != null && !number.matches("\\d{10}")) {
			throw new IllegalArgumentException("Telephone must be exactly 10 digits, got: " + number);
		}
	}

	/**
	 * Create a Telephone from a string, or null if the input is null.
	 * @param number the telephone number string
	 * @return Telephone value object or null
	 */
	public static @Nullable Telephone of(@Nullable String number) {
		return number != null ? new Telephone(number) : null;
	}

	/**
	 * Check if this telephone number is valid (non-null).
	 * @return true if valid
	 */
	public boolean isValid() {
		return number != null;
	}

	@Override
	public String toString() {
		return number != null ? number : "";
	}

}
