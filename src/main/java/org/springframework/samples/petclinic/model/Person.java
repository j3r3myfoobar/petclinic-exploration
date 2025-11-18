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

import java.io.Serializable;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing an person. Does not extend BaseEntity to
 * avoid ID conflicts with jMolecules entities using type-safe IDs.
 *
 * Uses PersonName value object internally for cohesive name representation.
 *
 * @author Ken Krebs
 */
@MappedSuperclass
public class Person implements Serializable {

	@Embedded
	private @Nullable PersonName name;

	/**
	 * Get the PersonName value object (domain use).
	 * @return the person name value object
	 */
	public @Nullable PersonName getPersonNameValue() {
		return this.name;
	}

	/**
	 * Set the PersonName value object (domain use).
	 * @param name the person name value object
	 */
	public void setPersonNameValue(@Nullable PersonName name) {
		this.name = name;
	}

	/**
	 * Get first name as string (for form binding).
	 * @return first name or null
	 */
	@NotBlank
	public @Nullable String getFirstName() {
		return this.name != null ? this.name.firstName() : null;
	}

	/**
	 * Set first name from string (for form binding). Updates the PersonName value object,
	 * preserving last name if it exists.
	 * @param firstName the first name
	 */
	public void setFirstName(@Nullable String firstName) {
		String lastName = this.name != null ? this.name.lastName() : null;
		if (firstName != null && lastName != null) {
			this.name = new PersonName(firstName, lastName);
		}
		else if (firstName != null || lastName != null) {
			// Partial name - store what we have (will fail validation on save if incomplete)
			this.name = PersonName.of(firstName, lastName);
		}
		else {
			this.name = null;
		}
	}

	/**
	 * Get last name as string (for form binding).
	 * @return last name or null
	 */
	@NotBlank
	public @Nullable String getLastName() {
		return this.name != null ? this.name.lastName() : null;
	}

	/**
	 * Set last name from string (for form binding). Updates the PersonName value object,
	 * preserving first name if it exists.
	 * @param lastName the last name
	 */
	public void setLastName(@Nullable String lastName) {
		String firstName = this.name != null ? this.name.firstName() : null;
		if (firstName != null && lastName != null) {
			this.name = new PersonName(firstName, lastName);
		}
		else if (firstName != null || lastName != null) {
			// Partial name - store what we have (will fail validation on save if incomplete)
			this.name = PersonName.of(firstName, lastName);
		}
		else {
			this.name = null;
		}
	}

	/**
	 * Get the full name formatted as "FirstName LastName".
	 * @return the full name, or null if name is not set
	 */
	public @Nullable String getFullName() {
		return this.name != null ? this.name.getFullName() : null;
	}

}
