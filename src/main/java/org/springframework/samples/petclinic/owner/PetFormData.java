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
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Data Transfer Object for Pet form submissions.
 * <p>
 * Handles web-layer validation and form binding, keeping framework-specific
 * validation annotations out of the domain model. This follows the layered
 * validation pattern recommended for DDD applications.
 * </p>
 * <p>
 * Fields are marked @Nullable because they can be null during form binding
 * (when form is first displayed or has validation errors). Bean Validation
 * annotations (@NotBlank, @NotNull) enforce non-null requirements during
 * form submission validation.
 * </p>
 *
 * @author Wick Dynex
 */
public record PetFormData(
        @NotBlank(message = "Pet name is required") @Nullable String name,

        @NotNull(message = "Pet type is required") @Nullable String typeName,

        @NotNull(message = "Birth date is required") @PastOrPresent(
                message = "Birth date cannot be in the future") @DateTimeFormat(
                pattern = "yyyy-MM-dd") @Nullable LocalDate birthDate) {

    /**
     * Converts this DTO to a domain Pet entity.
     * <p>
     * This method should only be called after successful validation, which ensures
     * all fields are non-null via Bean Validation annotations.
     * </p>
     *
     * @param type the PetType to associate with the pet
     * @return a new Pet instance with values from this DTO
     * @throws IllegalStateException if any required field is null (validation failure)
     */
    public Pet toDomainObject(PetType type) {
        if (this.name == null) {
            throw new IllegalStateException("name should not be null after validation");
        }
        if (this.birthDate == null) {
            throw new IllegalStateException("birthDate should not be null after validation");
        }

        Pet pet = new Pet();
        pet.setName(this.name);
        pet.setType(type);
        pet.setBirthDate(this.birthDate);
        return pet;
    }

    /**
     * Updates an existing Pet entity with values from this DTO.
     * <p>
     * This method should only be called after successful validation, which ensures
     * all fields are non-null via Bean Validation annotations.
     * </p>
     *
     * @param pet  the existing pet to update
     * @param type the PetType to associate with the pet
     * @throws IllegalStateException if any required field is null (validation failure)
     */
    public void updateDomainObject(Pet pet, PetType type) {
        if (this.name == null) {
            throw new IllegalStateException("name should not be null after validation");
        }
        if (this.birthDate == null) {
            throw new IllegalStateException("birthDate should not be null after validation");
        }

        pet.setName(this.name);
        pet.setType(type);
        pet.setBirthDate(this.birthDate);
    }

    /**
     * Creates a DTO from an existing Pet entity.
     *
     * @param pet   the domain pet to convert
     * @param types the repository to resolve the pet type name
     * @return a new PetFormData instance populated from the domain object
     * @throws IllegalStateException if pet has null name, birthDate, or type (data integrity issue)
     */
    public static PetFormData fromDomainObject(Pet pet, PetTypeRepository types) {
        String name = pet.getName();
        if (name == null) {
            throw new IllegalStateException("Pet name cannot be null for existing pet with ID: " + pet.getId());
        }

        LocalDate birthDate = pet.getBirthDate();
        if (birthDate == null) {
            throw new IllegalStateException("Pet birthDate cannot be null for existing pet with ID: " + pet.getId());
        }

        String typeName = resolvePetTypeName(pet, types);
        if (typeName == null) {
            throw new IllegalStateException("Pet type cannot be null for existing pet with ID: " + pet.getId());
        }

        return new PetFormData(name, typeName, birthDate);
    }

    /**
     * Creates an empty DTO for new pet forms.
     *
     * @return a new PetFormData with null values
     */
    public static PetFormData empty() {
        return new PetFormData(null, null, null);
    }

    /**
     * Resolves the PetType name from a Pet entity.
     *
     * @param pet   the pet entity
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
     *
     * @param typeName       the name to search for
     * @param availableTypes the collection of available types
     * @return the matching PetType
     * @throws IllegalArgumentException if type name is not found
     */
    public static PetType findTypeByName(String typeName, Collection<PetType> availableTypes) {
        return availableTypes.stream()
                .filter(type -> {
                    String name = type.getName();
                    return name != null && name.equals(typeName);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid pet type: " + typeName));
    }

}
