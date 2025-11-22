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
package org.springframework.samples.petclinic.owner.application;

import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetFormData;

import java.util.UUID;

/**
 * Primary port (use case interface) for pet management operations.
 * <p>
 * This interface defines the use cases that can be performed on pets
 * from the perspective of the external world (controllers, CLI, etc.).
 * </p>
 *
 * @author Wick Dynex
 */
@PrimaryPort
public interface PetManagementUseCase {

	/**
	 * Add a new pet to an owner.
	 * @param ownerId the owner's identifier
	 * @param formData validated form data from the web layer
	 * @return the newly created pet
	 * @throws IllegalArgumentException if owner not found or pet type invalid
	 */
	Pet addPet(UUID ownerId, PetFormData formData);

	/**
	 * Update an existing pet.
	 * @param ownerId the owner's identifier
	 * @param petId the pet's identifier
	 * @param formData validated form data from the web layer
	 * @return the updated pet
	 * @throws IllegalArgumentException if owner or pet not found, or pet type invalid
	 */
	Pet updatePet(UUID ownerId, UUID petId, PetFormData formData);

	/**
	 * Prepare form data for creating a new pet.
	 * @return empty form data
	 */
	PetFormData prepareNewPetForm();

	/**
	 * Prepare form data for editing an existing pet.
	 * @param ownerId the owner's identifier
	 * @param petId the pet's identifier
	 * @return form data populated from the existing pet
	 * @throws IllegalArgumentException if owner or pet not found
	 */
	PetFormData prepareEditPetForm(UUID ownerId, UUID petId);

}
