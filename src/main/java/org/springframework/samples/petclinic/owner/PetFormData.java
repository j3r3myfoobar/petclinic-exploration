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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Data Transfer Object for Pet form submissions.
 * <p>
 * Handles web-layer validation and form binding, keeping framework-specific
 * validation annotations out of the domain model. This follows the layered
 * validation pattern recommended for DDD applications.
 * </p>
 *
 * @author Wick Dynex
 */
public record PetFormData(
		@NotBlank(message = "Pet name is required") String name,

		@NotNull(message = "Pet type is required") String typeName,

		@NotNull(message = "Birth date is required") @PastOrPresent(
				message = "Birth date cannot be in the future") @DateTimeFormat(
						pattern = "yyyy-MM-dd") LocalDate birthDate) {

	/**
	 * Converts this DTO to a domain Pet entity.
	 * @param type the PetType to associate with the pet
	 * @return a new Pet instance with values from this DTO
	 */
	public Pet toDomainObject(PetType type) {
		Pet pet = new Pet();
		pet.setName(this.name);
		pet.setType(type);
		pet.setBirthDate(this.birthDate);
		return pet;
	}

	/**
	 * Updates an existing Pet entity with values from this DTO.
	 * @param pet the existing pet to update
	 * @param type the PetType to associate with the pet
	 */
	public void updateDomainObject(Pet pet, PetType type) {
		pet.setName(this.name);
		pet.setType(type);
		pet.setBirthDate(this.birthDate);
	}

	/**
	 * Creates a DTO from an existing Pet entity.
	 * @param pet the domain pet to convert
	 * @param types the repository to resolve the pet type name
	 * @return a new PetFormData instance populated from the domain object
	 */
	public static PetFormData fromDomainObject(Pet pet, PetTypeRepository types) {
		String typeName = resolvePetTypeName(pet, types);
		return new PetFormData(pet.getName(), typeName, pet.getBirthDate());
	}

	/**
	 * Creates an empty DTO for new pet forms.
	 * @return a new PetFormData with null values
	 */
	public static PetFormData empty() {
		return new PetFormData(null, null, null);
	}

	/**
	 * Resolves the PetType name from a Pet entity.
	 * @param pet the pet entity
	 * @param types the type repository
	 * @return the type name, or null if not found
	 */
	private static @Nullable String resolvePetTypeName(Pet pet, PetTypeRepository types) {
		if (pet.getType() == null) {
			return null;
		}

		return types.findPetTypes()
			.stream()
			.filter(type -> type.getId().equals(pet.getType().getId()))
			.findFirst()
			.map(PetType::getName)
			.orElse(null);
	}

	/**
	 * Finds a PetType by name from the available types.
	 * @param typeName the name to search for
	 * @param availableTypes the collection of available types
	 * @return the matching PetType
	 * @throws IllegalArgumentException if type name is not found
	 */
	public static PetType findTypeByName(String typeName, Collection<PetType> availableTypes) {
		return availableTypes.stream()
			.filter(type -> type.getName().equals(typeName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Invalid pet type: " + typeName));
	}

}
