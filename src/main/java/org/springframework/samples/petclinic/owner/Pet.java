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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Entity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.NamedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

/**
 * Simple business object representing a pet.
 *
 * Uses jMolecules Entity type with type-safe PetId. ByteBuddy will automatically add
 *
 * @Entity annotation.
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Wick Dynex
 */
@Table(name = "pets")
public class Pet extends NamedEntity implements Entity<Owner, PetId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private PetId id = new PetId();

	@Embedded
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private @Nullable BirthDate birthDateValue;

	// Transient field to hold raw date for form binding/validation
	// This allows Spring's @PastOrPresent validation to work properly
	@jakarta.persistence.Transient
	private @Nullable LocalDate rawBirthDate;

	// Store only the PetType ID as per DDD - Association holds the ID reference
	// ByteBuddy will add @Convert(converter=PetTypeAssociationConverter) automatically
	@Column(name = "type_id")
	private @Nullable Association<PetType, PetTypeId> type;

	// ByteBuddy adds @OneToMany(cascade=ALL, orphanRemoval=true) with LAZY fetch automatically
	@JoinColumn(name = "pet_id")
	@OrderBy("date ASC")
	private final Set<Visit> visits = new LinkedHashSet<>();

	/**
	 * Get the type-safe PetId. Required by Entity interface.
	 * @return the pet's identifier
	 */
	public PetId getId() {
		return this.id;
	}

	/**
	 * Set the pet's identifier using type-safe PetId.
	 * @param id the pet's identifier
	 */
	public void setId(PetId id) {
		this.id = id;
	}

	/**
	 * Get the BirthDate value object (domain use).
	 * @return the birth date value object
	 */
	public @Nullable BirthDate getBirthDateValue() {
		return this.birthDateValue;
	}

	/**
	 * Set the BirthDate value object (domain use).
	 * @param birthDate the birth date value object
	 */
	public void setBirthDateValue(@Nullable BirthDate birthDate) {
		this.birthDateValue = birthDate;
	}

	/**
	 * Get birth date as LocalDate (for form binding). Returns the raw date if set during
	 * form binding, otherwise extracts from BirthDate value object.
	 * @return birth date or null
	 */
	@jakarta.validation.constraints.PastOrPresent
	public @Nullable LocalDate getBirthDate() {
		// Return raw date if set (for form binding/validation)
		if (this.rawBirthDate != null) {
			return this.rawBirthDate;
		}
		// Otherwise extract from value object
		return this.birthDateValue != null ? this.birthDateValue.date() : null;
	}

	/**
	 * Set birth date from LocalDate (for form binding). Stores the raw date temporarily
	 * for validation, and creates BirthDate value object if valid.
	 * @param birthDate the birth date
	 */
	public void setBirthDate(@Nullable LocalDate birthDate) {
		// Try to create value object if date is valid
		try {
			this.birthDateValue = BirthDate.of(birthDate);
			// Successfully created value object, clear raw date
			this.rawBirthDate = null;
		}
		catch (IllegalArgumentException e) {
			// Invalid date - store raw for validation, don't create value object
			this.rawBirthDate = birthDate;
			this.birthDateValue = null;
		}
	}

	/**
	 * Get the pet's age in years.
	 * @return age in years, or null if no birth date is set
	 */
	public @Nullable Integer getAgeInYears() {
		return this.birthDateValue != null ? this.birthDateValue.getAgeInYears() : null;
	}

	/**
	 * Check if the pet is considered elderly (7+ years old).
	 * @return true if elderly, false otherwise
	 */
	public boolean isElderly() {
		return this.birthDateValue != null && this.birthDateValue.isElderly();
	}

	/**
	 * Check if the pet is a puppy/kitten (less than 1 year old).
	 * @return true if puppy, false otherwise
	 */
	public boolean isPuppy() {
		return this.birthDateValue != null && this.birthDateValue.isPuppy();
	}

	public @Nullable Association<PetType, PetTypeId> getType() {
		return this.type;
	}

	public void setType(@Nullable PetType type) {
		this.type = type != null ? Association.forAggregate(type) : null;
	}

	public void setTypeAssociation(@Nullable Association<PetType, PetTypeId> type) {
		this.type = type;
	}

	/**
	 * Get the PetType ID.
	 * @return the pet type ID, or null if no type is set
	 */
	public @Nullable PetTypeId getTypeId() {
		return this.type != null ? this.type.getId() : null;
	}

	/**
	 * Resolve the PetType association to get the actual PetType aggregate.
	 * @param resolver the PetType repository/resolver
	 * @return the resolved PetType, or null if no type is set or not found
	 */
	public @Nullable PetType resolveType(AssociationResolver<PetType, PetTypeId> resolver) {
		if (this.type == null) {
			return null;
		}
		return resolver.resolve(this.type).orElse(null);
	}

	/**
	 * Resolve the PetType name by resolving the association.
	 * @param resolver the PetType repository/resolver
	 * @return the pet type name, or null if no type is set or not found
	 */
	public @Nullable String resolveTypeName(AssociationResolver<PetType, PetTypeId> resolver) {
		if (this.type == null) {
			return null;
		}
		return resolver.resolve(this.type).map(PetType::getName).orElse(null);
	}

	public Collection<Visit> getVisits() {
		return this.visits;
	}

	public void addVisit(Visit visit) {
		getVisits().add(visit);
	}

	/**
	 * Check if this is a new pet (not yet persisted).
	 * @return true if the pet has never been persisted
	 */
	public boolean isNew() {
		return this.id == null || this.id.value() == null;
	}

}
