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

import java.util.Objects;
import java.util.UUID;

import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.owner.domain.ports.OwnerRepositoryPort;
import org.springframework.samples.petclinic.owner.domain.ports.PetTypeRepositoryPort;
import org.springframework.samples.petclinic.owner.events.PetAdoptedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.Nullable;

/**
 * Application service for Pet management operations.
 * <p>
 * Encapsulates business logic and orchestrates domain operations following DDD
 * principles. Controllers delegate to this service, keeping them thin and focused
 * on HTTP concerns.
 * </p>
 * <p>
 * This service coordinates operations across aggregates (Owner and PetType) while
 * maintaining aggregate boundaries. Business rules are validated here before
 * delegating to domain entities.
 * </p>
 *
 * @author Wick Dynex
 */
@ApplicationLayer
@Service
@Transactional
public class PetApplicationService {

	private final OwnerRepositoryPort ownerRepository;

	private final PetTypeRepositoryPort petTypeRepository;

	private final ApplicationEventPublisher events;

	public PetApplicationService(OwnerRepositoryPort ownerRepository, PetTypeRepositoryPort petTypeRepository,
			ApplicationEventPublisher events) {
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
		this.events = events;
	}

	/**
	 * Add a new pet to an owner.
	 * @param ownerId the owner's identifier
	 * @param formData validated form data from the web layer
	 * @return the newly created pet
	 * @throws IllegalArgumentException if owner not found or pet type invalid
	 * @throws DuplicatePetNameException if owner already has a pet with this name
	 */
	public Pet addPet(UUID ownerId, PetFormData formData) {
		Owner owner = loadOwner(ownerId);

		// Business rule: duplicate pet name validation
		validateNoDuplicatePetName(owner, formData.name(), null);

		// Convert DTO to domain object
		PetType type = findPetType(formData.typeName());
		Pet pet = formData.toDomainObject(type);

		// Delegate to aggregate root
		owner.addPet(pet);
		this.ownerRepository.save(owner);

		// Publish domain event after successful persistence
		this.events.publishEvent(PetAdoptedEvent.of(pet.getId(), type.getId(), owner.getId()));

		return pet;
	}

	/**
	 * Update an existing pet.
	 * @param ownerId the owner's identifier
	 * @param petId the pet's identifier
	 * @param formData validated form data from the web layer
	 * @return the updated pet
	 * @throws IllegalArgumentException if owner or pet not found, or pet type invalid
	 * @throws DuplicatePetNameException if owner has another pet with this name
	 */
	public Pet updatePet(UUID ownerId, UUID petId, PetFormData formData) {
		Owner owner = loadOwner(ownerId);
		Pet pet = owner.getPet(new PetId(petId));

		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		// Business rule: duplicate pet name validation (excluding current pet)
		validateNoDuplicatePetName(owner, formData.name(), petId);

		// Update domain object from DTO
		PetType type = findPetType(formData.typeName());
		formData.updateDomainObject(pet, type);

		this.ownerRepository.save(owner);

		return pet;
	}

	/**
	 * Prepare form data for creating a new pet.
	 * @return empty form data
	 */
	@Transactional(readOnly = true)
	public PetFormData prepareNewPetForm() {
		return PetFormData.empty();
	}

	/**
	 * Prepare form data for editing an existing pet.
	 * @param ownerId the owner's identifier
	 * @param petId the pet's identifier
	 * @return form data populated from the existing pet
	 * @throws IllegalArgumentException if owner or pet not found
	 */
	@Transactional(readOnly = true)
	public PetFormData prepareEditPetForm(UUID ownerId, UUID petId) {
		Owner owner = loadOwner(ownerId);
		Pet pet = owner.getPet(new PetId(petId));

		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		return PetFormData.fromDomainObject(pet, this.petTypeRepository);
	}

	/**
	 * Business rule: Validate that owner doesn't already have a pet with the given name.
	 * @param owner the owner
	 * @param petName the pet name to check
	 * @param excludePetId optional pet ID to exclude from check (for updates)
	 * @throws DuplicatePetNameException if duplicate found
	 */
	private void validateNoDuplicatePetName(Owner owner, @Nullable String petName, @Nullable UUID excludePetId) {
		if (petName == null) {
			return; // Bean validation will catch this
		}

		Pet existingPet = owner.getPet(petName, excludePetId == null);

		if (existingPet != null) {
			// For updates, check if it's a different pet
			if (excludePetId == null || !Objects.equals(existingPet.getId(), new PetId(excludePetId))) {
				throw new DuplicatePetNameException(
						"A pet named '" + petName + "' already exists for this owner");
			}
		}
	}

	/**
	 * Load an owner by UUID.
	 * @param ownerId the owner's UUID
	 * @return the owner
	 * @throws IllegalArgumentException if not found
	 */
	private Owner loadOwner(UUID ownerId) {
		return this.ownerRepository.findById(new OwnerId(ownerId))
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));
	}

	/**
	 * Find a pet type by name.
	 * @param typeName the type name
	 * @return the pet type
	 * @throws IllegalArgumentException if not found
	 */
	private PetType findPetType(@Nullable String typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException("Pet type name cannot be null");
		}
		return PetFormData.findTypeByName(typeName, this.petTypeRepository.findPetTypes());
	}

	/**
	 * Exception thrown when attempting to add/update a pet with a duplicate name.
	 */
	public static class DuplicatePetNameException extends RuntimeException {

		public DuplicatePetNameException(String message) {
			super(message);
		}

	}

}
