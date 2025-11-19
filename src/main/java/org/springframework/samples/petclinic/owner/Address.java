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

import jakarta.persistence.Column;
import org.jmolecules.ddd.types.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing a physical address. Encapsulates street and city as a cohesive
 * concept. Immutable and uses value-based equality.
 *
 * @author Wick Dynex
 */
public record Address(@Column(name = "address") @Nullable String street,
                      @Column(name = "city") @Nullable String city) implements ValueObject {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if both street and city are null/blank
     */
    public Address {
        if ((street == null || street.isBlank()) && (city == null || city.isBlank())) {
            throw new IllegalArgumentException("Address must have at least street or city");
        }
    }

    /**
     * Create an Address from street and city.
     *
     * @param street the street address
     * @param city   the city
     * @return Address value object
     */
    public static Address of(@Nullable String street, @Nullable String city) {
        return new Address(street, city);
    }

    /**
     * Get full address as single string.
     *
     * @return formatted address
     */
    public String getFullAddress() {
        if (street != null && city != null) {
            return street + ", " + city;
        }
        return street != null ? street : (city != null ? city : "");
    }

    @Override
    public String toString() {
        return getFullAddress();
    }

}
