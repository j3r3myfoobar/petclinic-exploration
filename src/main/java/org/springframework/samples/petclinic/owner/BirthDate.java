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

import java.time.LocalDate;
import java.time.Period;

import org.jmolecules.ddd.types.ValueObject;
import org.jspecify.annotations.Nullable;

import jakarta.persistence.Column;

/**
 * Value Object representing a birth date with domain behavior for age calculation and
 * life stage determination. Encapsulates birth date validation and age-related logic.
 *
 * @author Wick Dynex
 */
public record BirthDate(@Column(name = "birth_date") LocalDate date) implements ValueObject {

	/**
	 * Typical threshold (in years) to consider a pet as elderly/senior. This varies by
	 * species and breed, but 7 years is a common general threshold.
	 */
	public static final int DEFAULT_ELDERLY_THRESHOLD = 7;

	public BirthDate {
		if (date == null) {
			throw new IllegalArgumentException("Birth date must not be null");
		}
		if (date.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Birth date cannot be in the future");
		}
	}

	/**
	 * Create a BirthDate from a nullable LocalDate.
	 * @param date the birth date
	 * @return a BirthDate, or null if date is null
	 */
	public static @Nullable BirthDate of(@Nullable LocalDate date) {
		return date != null ? new BirthDate(date) : null;
	}

	/**
	 * Calculate the age in years based on the current date.
	 * @return the age in years
	 */
	public int getAgeInYears() {
		return Period.between(date, LocalDate.now()).getYears();
	}

	/**
	 * Calculate the age in months based on the current date.
	 * @return the age in months
	 */
	public int getAgeInMonths() {
		Period period = Period.between(date, LocalDate.now());
		return period.getYears() * 12 + period.getMonths();
	}

	/**
	 * Check if the pet is considered elderly/senior based on the default threshold.
	 * @return true if the pet is elderly (7+ years old)
	 */
	public boolean isElderly() {
		return isElderly(DEFAULT_ELDERLY_THRESHOLD);
	}

	/**
	 * Check if the pet is considered elderly/senior based on a custom threshold.
	 * @param thresholdYears the threshold age in years
	 * @return true if the pet's age meets or exceeds the threshold
	 */
	public boolean isElderly(int thresholdYears) {
		return getAgeInYears() >= thresholdYears;
	}

	/**
	 * Check if the pet is a puppy/kitten (less than 1 year old).
	 * @return true if less than 1 year old
	 */
	public boolean isPuppy() {
		return getAgeInYears() < 1;
	}

	/**
	 * Get a human-readable age description.
	 * @return age description (e.g., "2 years", "6 months", "3 years 4 months")
	 */
	public String getAgeDescription() {
		Period period = Period.between(date, LocalDate.now());
		int years = period.getYears();
		int months = period.getMonths();

		if (years == 0) {
			return months == 1 ? "1 month" : months + " months";
		}
		else if (months == 0) {
			return years == 1 ? "1 year" : years + " years";
		}
		else {
			String yearsPart = years == 1 ? "1 year" : years + " years";
			String monthsPart = months == 1 ? "1 month" : months + " months";
			return yearsPart + " " + monthsPart;
		}
	}

	@Override
	public String toString() {
		return date.toString() + " (" + getAgeDescription() + ")";
	}

}
