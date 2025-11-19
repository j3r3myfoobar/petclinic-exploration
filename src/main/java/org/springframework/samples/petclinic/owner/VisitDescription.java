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
 * Value Object representing a visit description. Encapsulates the description text with
 * validation for length constraints and provides domain behavior for text summarization.
 *
 * @author Wick Dynex
 */
public record VisitDescription(@Column(name = "description") String value) implements ValueObject {

    public static final int MAX_LENGTH = 500;

    public VisitDescription {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Visit description must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Visit description exceeds maximum length of " + MAX_LENGTH + " characters");
        }
    }

    /**
     * Create a VisitDescription from a nullable string.
     *
     * @param value the description text
     * @return a VisitDescription, or null if value is null
     */
    public static @Nullable VisitDescription of(@Nullable String value) {
        return value != null ? new VisitDescription(value) : null;
    }

    /**
     * Get a summarized version of the description, truncated to the specified maximum
     * characters with ellipsis if needed.
     *
     * @param maxChars the maximum number of characters in the summary
     * @return the summarized description
     */
    public String getSummary(int maxChars) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars must be positive");
        }
        if (value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars - 3) + "...";
    }

    /**
     * Check if this description is valid (not blank and within length limit).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return value != null && !value.isBlank() && value.length() <= MAX_LENGTH;
    }

    @Override
    public String toString() {
        return value;
    }

}
