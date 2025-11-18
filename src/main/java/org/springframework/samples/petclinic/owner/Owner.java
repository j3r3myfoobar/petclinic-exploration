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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.util.Assert;

import jakarta.persistence.Embedded;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * Uses jMolecules AggregateRoot type with type-safe OwnerId. ByteBuddy will automatically
 * add @Entity annotation.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Oliver Drotbohm
 * @author Wick Dynex
 */
@Table(name = "owners")
public class Owner extends Person implements AggregateRoot<Owner, OwnerId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private OwnerId id = new OwnerId();

	@Embedded
	private @Nullable Address address;

	@Embedded
	private @Nullable Telephone telephone;

	// ByteBuddy adds @OneToMany(cascade=ALL, orphanRemoval=true) with LAZY fetch
	// automatically
	@JoinColumn(name = "owner_id")
	@OrderBy("name")
	private final List<Pet> pets = new ArrayList<>();

	/**
	 * Get the type-safe OwnerId. Required by AggregateRoot interface.
	 * @return the owner's identifier
	 */
	public OwnerId getId() {
		return this.id;
	}

	/**
	 * Set the owner's identifier using type-safe OwnerId.
	 * @param id the owner's identifier
	 */
	public void setId(OwnerId id) {
		this.id = id;
	}

	/**
	 * Get the Address value object (domain use).
	 * @return the address value object
	 */
	public @Nullable Address getAddressValue() {
		return this.address;
	}

	/**
	 * Set the Address value object (domain use).
	 * @param address the address value object
	 */
	public void setAddressValue(@Nullable Address address) {
		this.address = address;
	}

	/**
	 * Get street address as string (for form binding - field name "address").
	 * @return street address or null
	 */
	@NotBlank
	public @Nullable String getAddress() {
		return this.address != null ? this.address.street() : null;
	}

	/**
	 * Set street address from string (for form binding - field name "address"). Updates the
	 * Address value object, preserving city if it exists.
	 * @param street the street address
	 */
	public void setAddress(@Nullable String street) {
		String city = this.address != null ? this.address.city() : null;
		if (street != null || city != null) {
			this.address = Address.of(street, city);
		}
		else {
			this.address = null;
		}
	}

	/**
	 * Get city as string (for form binding).
	 * @return city or null
	 */
	@NotBlank
	public @Nullable String getCity() {
		return this.address != null ? this.address.city() : null;
	}

	/**
	 * Set city from string (for form binding). Updates the Address value object, preserving
	 * street if it exists.
	 * @param city the city
	 */
	public void setCity(@Nullable String city) {
		String street = this.address != null ? this.address.street() : null;
		if (street != null || city != null) {
			this.address = Address.of(street, city);
		}
		else {
			this.address = null;
		}
	}

	/**
	 * Get the Telephone value object (domain use).
	 * @return the telephone value object
	 */
	public @Nullable Telephone getTelephoneValue() {
		return this.telephone;
	}

	/**
	 * Set the Telephone value object (domain use).
	 * @param telephone the telephone value object
	 */
	public void setTelephoneValue(@Nullable Telephone telephone) {
		this.telephone = telephone;
	}

	/**
	 * Get telephone number as string (for form binding - field name "telephone").
	 * @return telephone number or null
	 */
	@NotBlank
	@Pattern(regexp = "\\d{10}", message = "{telephone.invalid}")
	public @Nullable String getTelephone() {
		return this.telephone != null ? this.telephone.number() : null;
	}

	/**
	 * Set telephone number from string (for form binding - field name "telephone").
	 * @param number the telephone number
	 */
	public void setTelephone(@Nullable String number) {
		this.telephone = Telephone.of(number);
	}

	public List<Pet> getPets() {
		return this.pets;
	}

	public void addPet(Pet pet) {
		getPets().add(pet);
	}

	/**
	 * Return the Pet with the given name, or null if none found for this Owner.
	 * @param name to test
	 * @return the Pet with the given name, or null if no such Pet exists for this Owner
	 */
	public @Nullable Pet getPet(String name) {
		return getPet(name, false);
	}

	/**
	 * Return the Pet with the given PetId, or null if none found for this Owner.
	 * @param petId to test
	 * @return the Pet with the given PetId, or null if no such Pet exists for this Owner
	 */
	public @Nullable Pet getPet(PetId petId) {
		for (Pet pet : getPets()) {
			if (Objects.equals(pet.getId(), petId)) {
				return pet;
			}
		}
		return null;
	}

	/**
	 * Return the Pet with the given name, or null if none found for this Owner.
	 * @param name to test
	 * @param ignoreNew whether to ignore new pets (pets that are not saved yet)
	 * @return the Pet with the given name, or null if no such Pet exists for this Owner
	 */
	public @Nullable Pet getPet(String name, boolean ignoreNew) {
		for (Pet pet : getPets()) {
			String compName = pet.getName();
			if (compName != null && compName.equalsIgnoreCase(name)) {
				return pet;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("address", this.address)
			.append("telephone", this.telephone)
			.toString();
	}

	/**
	 * Adds the given {@link Visit} to the {@link Pet} with the given identifier.
	 * @param petId the identifier of the {@link Pet}, must not be {@literal null}.
	 * @param visit the visit to add, must not be {@literal null}.
	 */
	public void addVisit(PetId petId, Visit visit) {

		Assert.notNull(petId, "Pet identifier must not be null!");
		Assert.notNull(visit, "Visit must not be null!");

		Pet pet = getPet(petId);

		Assert.notNull(pet, "Invalid Pet identifier!");

		pet.addVisit(visit);
	}

	/**
	 * Resolve all unique PetTypes for this owner's pets.
	 * @param resolver the PetType repository/resolver
	 * @return set of resolved PetTypes (may be empty if owner has no pets)
	 */
	public Set<PetType> resolvePetTypes(AssociationResolver<PetType, PetTypeId> resolver) {
		return this.pets.stream()
			.map(pet -> pet.resolveType(resolver))
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	/**
	 * Get all visits across all pets for this owner.
	 * @return list of all visits, may be empty
	 */
	public List<Visit> getAllVisits() {
		return this.pets.stream()
			.flatMap(pet -> pet.getVisits().stream())
			.collect(Collectors.toList());
	}

	/**
	 * Count pets grouped by their type ID.
	 * @return map of PetTypeId to count
	 */
	public Map<PetTypeId, Long> countPetsByType() {
		return this.pets.stream()
			.map(Pet::getTypeId)
			.filter(Objects::nonNull)
			.collect(Collectors.groupingBy(typeId -> typeId, Collectors.counting()));
	}

	/**
	 * Check if this owner has any pets of the specified type.
	 * @param petTypeId the pet type identifier
	 * @return true if the owner has at least one pet of this type
	 */
	public boolean hasPetOfType(PetTypeId petTypeId) {
		return this.pets.stream().anyMatch(pet -> petTypeId.equals(pet.getTypeId()));
	}

}
