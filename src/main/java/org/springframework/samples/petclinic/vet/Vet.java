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
package org.springframework.samples.petclinic.vet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.springframework.samples.petclinic.model.NamedEntity;
import org.springframework.samples.petclinic.model.Person;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlElement;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * Uses jMolecules AggregateRoot type with type-safe VetId. ByteBuddy will automatically
 * add @Entity annotation.
 *
 * Specialties are stored as cross-aggregate references using IDs only, following DDD
 * aggregate boundary principles.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 */
@Table(name = "vets")
public class Vet extends Person implements AggregateRoot<Vet, VetId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private VetId id = new VetId();

	// Store only specialty IDs as cross-aggregate references (DDD pattern)
	@ElementCollection
	@CollectionTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"))
	@Column(name = "specialty_id")
	private Set<SpecialtyId> specialtyIds = new HashSet<>();

	/**
	 * Get the type-safe VetId. Required by AggregateRoot interface.
	 * @return the vet's identifier
	 */
	public VetId getId() {
		return this.id;
	}

	/**
	 * Set the vet's identifier using type-safe VetId.
	 * @param id the vet's identifier
	 */
	public void setId(VetId id) {
		this.id = id;
	}

	/**
	 * Get the specialty IDs (internal use).
	 * @return the set of specialty IDs
	 */
	protected Set<SpecialtyId> getSpecialtyIdsInternal() {
		return this.specialtyIds;
	}

	/**
	 * Get specialty IDs as Association objects.
	 * @return set of specialty associations
	 */
	public Set<Association<Specialty, SpecialtyId>> getSpecialtyAssociations() {
		return specialtyIds.stream().map(Association::forId).collect(Collectors.toSet());
	}

	/**
	 * Get just the specialty IDs.
	 * @return set of specialty IDs
	 */
	public Set<SpecialtyId> getSpecialtyIds() {
		return Set.copyOf(specialtyIds);
	}

	/**
	 * Resolve specialty associations to get actual Specialty aggregates. Used by DTOs and
	 * view layer.
	 * @param resolver the specialty repository/resolver
	 * @return list of resolved specialties, sorted by name
	 */
	public List<Specialty> resolveSpecialties(AssociationResolver<Specialty, SpecialtyId> resolver) {
		return specialtyIds.stream()
			.map(Association::forId)
			.map(resolver::resolve)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.sorted(Comparator.comparing(NamedEntity::getName))
			.collect(Collectors.toList());
	}

	/**
	 * Legacy method for XML serialization. Note: This will return an empty list. Use
	 * resolveSpecialties() with a resolver instead.
	 * @return empty list (specialties must be resolved via repository)
	 * @deprecated Use {@link #resolveSpecialties(AssociationResolver)} instead
	 */
	@Deprecated
	@XmlElement
	public List<Specialty> getSpecialties() {
		return List.of(); // Return empty - XML serialization should use DTO
	}

	/**
	 * Get the number of specialties.
	 * @return number of specialties
	 */
	public int getNrOfSpecialties() {
		return specialtyIds.size();
	}

	/**
	 * Add a specialty by ID.
	 * @param specialtyId the specialty identifier
	 */
	public void addSpecialtyId(SpecialtyId specialtyId) {
		this.specialtyIds.add(specialtyId);
	}

	/**
	 * Add a specialty from the aggregate. Extracts the ID and stores it.
	 * @param specialty the specialty aggregate
	 */
	public void addSpecialty(Specialty specialty) {
		this.specialtyIds.add(specialty.getId());
	}

	/**
	 * Remove a specialty by ID.
	 * @param specialtyId the specialty identifier
	 */
	public void removeSpecialtyId(SpecialtyId specialtyId) {
		this.specialtyIds.remove(specialtyId);
	}

}
