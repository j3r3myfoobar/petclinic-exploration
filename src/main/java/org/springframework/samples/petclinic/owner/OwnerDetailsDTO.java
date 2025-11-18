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

import java.util.List;
import java.util.stream.Collectors;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jspecify.annotations.Nullable;

/**
 * Data Transfer Object for Owner details view with resolved pet associations. This DTO is
 * used in the view layer to display owner and pet information with resolved PetType names
 * while maintaining DDD aggregate boundaries in the domain model.
 *
 * @author Wick Dynex
 */
public class OwnerDetailsDTO {

	private final Owner owner;

	private final List<PetDTO> pets;

	private OwnerDetailsDTO(Owner owner, List<PetDTO> pets) {
		this.owner = owner;
		this.pets = pets;
	}

	/**
	 * Create an OwnerDetailsDTO from an Owner entity, resolving all PetType associations.
	 * @param owner the owner entity
	 * @param petTypeResolver the PetType repository/resolver
	 * @return a new OwnerDetailsDTO with resolved pet type names
	 */
	public static OwnerDetailsDTO from(Owner owner, AssociationResolver<PetType, PetTypeId> petTypeResolver) {
		List<PetDTO> pets = owner.getPets()
			.stream()
			.map(pet -> PetDTO.from(pet, petTypeResolver))
			.collect(Collectors.toList());
		return new OwnerDetailsDTO(owner, pets);
	}

	public OwnerId getId() {
		return this.owner.getId();
	}

	public @Nullable String getFirstName() {
		return this.owner.getFirstName();
	}

	public @Nullable String getLastName() {
		return this.owner.getLastName();
	}

	public @Nullable String getAddress() {
		Address address = this.owner.getAddressValue();
		return address != null ? address.street() : null;
	}

	public @Nullable String getCity() {
		Address address = this.owner.getAddressValue();
		return address != null ? address.city() : null;
	}

	public @Nullable String getTelephone() {
		Telephone telephone = this.owner.getTelephoneValue();
		return telephone != null ? telephone.number() : null;
	}

	public List<PetDTO> getPets() {
		return this.pets;
	}

	/**
	 * Get the underlying Owner entity. Useful for operations that need the actual
	 * aggregate root.
	 * @return the owner entity
	 */
	public Owner getOwner() {
		return this.owner;
	}

}
