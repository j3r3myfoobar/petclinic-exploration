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

import org.jmolecules.ddd.types.Entity;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * Uses jMolecules Entity type with type-safe VisitId. ByteBuddy will automatically
 * add @Entity annotation.
 *
 * @author Ken Krebs
 * @author Dave Syer
 */
@Table(name = "visits")
public class Visit implements org.jmolecules.ddd.types.Entity<Owner, VisitId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private VisitId id = new VisitId();

	@Column(name = "visit_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private @Nullable LocalDate date;

	@NotBlank
	private @Nullable String description;

	/**
	 * Creates a new instance of Visit for the current date
	 */
	public Visit() {
		this.date = LocalDate.now();
	}

	/**
	 * Get the type-safe VisitId. Required by Entity interface.
	 * @return the visit's identifier
	 */
	public VisitId getId() {
		return this.id;
	}

	/**
	 * Set the visit's identifier using type-safe VisitId.
	 * @param id the visit's identifier
	 */
	public void setId(VisitId id) {
		this.id = id;
	}

	public @Nullable LocalDate getDate() {
		return this.date;
	}

	public void setDate(@Nullable LocalDate date) {
		this.date = date;
	}

	public @Nullable String getDescription() {
		return this.description;
	}

	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	/**
	 * Check if this is a new visit (not yet persisted).
	 * @return true if the visit has never been persisted
	 */
	public boolean isNew() {
		return this.id == null || this.id.value() == null;
	}

}
