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

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * Simple JavaBean domain object representing an person. Does not extend BaseEntity to
 * avoid ID conflicts with jMolecules entities using type-safe IDs.
 * <p>
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
     *
     * @return the person name value object
     */
    public @Nullable PersonName getPersonNameValue() {
        return this.name;
    }

    /**
     * Set the PersonName value object (domain use).
     *
     * @param name the person name value object
     */
    public void setPersonNameValue(@Nullable PersonName name) {
        this.name = name;
    }

    /**
     * Get first name as string (for form binding).
     *
     * @return first name or null
     */
    @NotBlank
    public @Nullable String getFirstName() {
        if (this.name == null) {
            return null;
        }
        String firstName = this.name.firstName();
        // Don't return placeholder values
        return "___PLACEHOLDER___".equals(firstName) ? null : firstName;
    }

    /**
     * Set first name from string (for form binding). Updates the PersonName value object,
     * preserving last name if it exists.
     *
     * @param firstName the first name
     */
    public void setFirstName(@Nullable String firstName) {
        String lastName = this.name != null ? this.name.lastName() : null;

        if (firstName == null || firstName.isBlank()) {
            if (lastName == null || lastName.isBlank()) {
                this.name = null;
            }
            // If only lastName exists, keep current name unchanged
            return;
        }

        // firstName is non-blank, create or update PersonName
        if (lastName != null && !lastName.isBlank()) {
            this.name = new PersonName(firstName, lastName);
        } else {
            // Use placeholder for missing lastName to allow sequential setter calls
            this.name = new PersonName(firstName, "___PLACEHOLDER___");
        }
    }

    /**
     * Get last name as string (for form binding).
     *
     * @return last name or null
     */
    @NotBlank
    public @Nullable String getLastName() {
        if (this.name == null) {
            return null;
        }
        String lastName = this.name.lastName();
        // Don't return placeholder values
        return "___PLACEHOLDER___".equals(lastName) ? null : lastName;
    }

    /**
     * Set last name from string (for form binding). Updates the PersonName value object,
     * preserving first name if it exists.
     *
     * @param lastName the last name
     */
    public void setLastName(@Nullable String lastName) {
        String firstName = this.name != null ? this.name.firstName() : null;

        if (lastName == null || lastName.isBlank()) {
            if (firstName == null || firstName.isBlank()) {
                this.name = null;
            }
            // If only firstName exists, keep current name unchanged
            return;
        }

        // lastName is non-blank, create or update PersonName
        if (firstName != null && !firstName.isBlank() && !"___PLACEHOLDER___".equals(firstName)) {
            this.name = new PersonName(firstName, lastName);
        } else {
            // Use placeholder for missing firstName to allow sequential setter calls
            this.name = new PersonName("___PLACEHOLDER___", lastName);
        }
    }

    /**
     * Get the full name formatted as "FirstName LastName".
     *
     * @return the full name, or null if name is not set
     */
    public @Nullable String getFullName() {
        return this.name != null ? this.name.getFullName() : null;
    }

}
