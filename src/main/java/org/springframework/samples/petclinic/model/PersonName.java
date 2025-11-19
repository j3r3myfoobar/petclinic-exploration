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

import jakarta.persistence.Column;
import org.jmolecules.ddd.types.ValueObject;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * Value Object representing a person's name as a cohesive concept. Encapsulates first
 * and last name together with domain behavior for formatting and validation.
 *
 * @author Wick Dynex
 */
public record PersonName(@Column(name = "first_name") String firstName,
                         @Column(name = "last_name") String lastName) implements ValueObject, Serializable {

    public PersonName {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
    }

    /**
     * Create a PersonName from nullable strings.
     *
     * @param firstName the first name
     * @param lastName  the last name
     * @return a PersonName, or null if either parameter is null
     */
    public static @Nullable PersonName of(@Nullable String firstName, @Nullable String lastName) {
        if (firstName == null || lastName == null) {
            return null;
        }
        return new PersonName(firstName, lastName);
    }

    /**
     * Get the full name formatted as "FirstName LastName".
     *
     * @return the full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get the name formatted as "LastName, FirstName".
     *
     * @return the name in last-first format
     */
    public String getLastNameFirst() {
        return lastName + ", " + firstName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

}
