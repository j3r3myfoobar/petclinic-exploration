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

import java.util.Objects;
import java.util.UUID;

import org.jmolecules.architecture.hexagonal.Application;
import org.jspecify.annotations.Nullable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetFormData;
import org.springframework.samples.petclinic.owner.PetId;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.domain.ports.EventPublisherPort;
import org.springframework.samples.petclinic.owner.domain.ports.OwnerRepositoryPort;
import org.springframework.samples.petclinic.owner.domain.ports.PetTypeRepositoryPort;
import org.springframework.samples.petclinic.owner.events.PetAdoptedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for Pet management operations.
 * <p>
 * This service implements the {@link PetManagementUseCase} primary port and
 * coordinates operations across aggregates (Owner and PetType) using domain
 * ports (secondary ports) for infrastructure access.
 * </p>
 * <p>
 * In hexagonal architecture, this is the application layer that:
 * <ul>
 * <li>Implements use cases (primary ports)</li>
 * <li>Depends on domain ports (secondary ports)</li>
 * <li>Contains no infrastructure code</li>
 * </ul>
 * </p>
 *
 * @author Wick Dynex
 */
@Application
@Service
@Transactional
public class PetManagementService implements PetManagementUseCase {

	private final OwnerRepositoryPort ownerRepository;

	private final PetTypeRepositoryPort petTypeRepository;

	private final EventPublisherPort eventPublisher;

	public PetManagementService(OwnerRepositoryPort ownerRepository, PetTypeRepositoryPort petTypeRepository,
			EventPublisherPort eventPublisher) {
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
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
		this.eventPublisher.publish(PetAdoptedEvent.of(pet.getId(), type.getId(), owner.getId()));

		return pet;
	}

	@Override
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

	@Override
	@Transactional(readOnly = true)
	public PetFormData prepareNewPetForm() {
		return PetFormData.empty();
	}

	@Override
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
	 * Business rule: Validate that owner doesn't already have a pet with the given
	 * name.
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
				throw new DuplicatePetNameException("A pet named '" + petName + "' already exists for this owner");
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
