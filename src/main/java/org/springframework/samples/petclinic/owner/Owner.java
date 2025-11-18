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
import java.util.Objects;

import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.util.Assert;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
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

	@Column(name = "address")
	@NotBlank
	private @Nullable String address;

	@Column(name = "city")
	@NotBlank
	private @Nullable String city;

	@Column(name = "telephone")
	@NotBlank
	@Pattern(regexp = "\\d{10}", message = "{telephone.invalid}")
	private @Nullable String telephone;

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

	public @Nullable String getAddress() {
		return this.address;
	}

	public void setAddress(@Nullable String address) {
		this.address = address;
	}

	public @Nullable String getCity() {
		return this.city;
	}

	public void setCity(@Nullable String city) {
		this.city = city;
	}

	public @Nullable String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(@Nullable String telephone) {
		this.telephone = telephone;
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
			.append("city", this.city)
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

}
