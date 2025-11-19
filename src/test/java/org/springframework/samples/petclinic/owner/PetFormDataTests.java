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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PetFormData} DTO validation.
 *
 * @author Wick Dynex
 */
class PetFormDataTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void testValidPetFormData() {
		PetFormData formData = new PetFormData("Fluffy", "cat", LocalDate.of(2020, 1, 1));

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).isEmpty();
	}

	@Test
	void testBlankNameIsInvalid() {
		PetFormData formData = new PetFormData("", "cat", LocalDate.of(2020, 1, 1));

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
		assertThat(violations.iterator().next().getMessage()).contains("Pet name is required");
	}

	@Test
	void testNullNameIsInvalid() {
		PetFormData formData = new PetFormData(null, "cat", LocalDate.of(2020, 1, 1));

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
	}

	@Test
	void testNullTypeNameIsInvalid() {
		PetFormData formData = new PetFormData("Fluffy", null, LocalDate.of(2020, 1, 1));

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("typeName");
		assertThat(violations.iterator().next().getMessage()).contains("Pet type is required");
	}

	@Test
	void testNullBirthDateIsInvalid() {
		PetFormData formData = new PetFormData("Fluffy", "cat", null);

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("birthDate");
		assertThat(violations.iterator().next().getMessage()).contains("Birth date is required");
	}

	@Test
	void testFutureBirthDateIsInvalid() {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		PetFormData formData = new PetFormData("Fluffy", "cat", futureDate);

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("birthDate");
		assertThat(violations.iterator().next().getMessage()).contains("cannot be in the future");
	}

	@Test
	void testTodayBirthDateIsValid() {
		LocalDate today = LocalDate.now();
		PetFormData formData = new PetFormData("Fluffy", "cat", today);

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).isEmpty();
	}

	@Test
	void testPastBirthDateIsValid() {
		LocalDate pastDate = LocalDate.now().minusYears(5);
		PetFormData formData = new PetFormData("Fluffy", "cat", pastDate);

		Set<ConstraintViolation<PetFormData>> violations = validator.validate(formData);

		assertThat(violations).isEmpty();
	}

}
