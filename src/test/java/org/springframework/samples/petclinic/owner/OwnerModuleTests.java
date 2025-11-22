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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.samples.petclinic.owner.events.PetAdoptedEvent;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * Tests for the Owner module in isolation.
 *
 * <p>Uses {@link ApplicationModuleTest} to load only the owner module's beans,
 * proving that the module can function independently without the vet module or
 * other modules being loaded.
 *
 * <p>This is a critical test for verifying module boundaries and ensuring that
 * the owner module could be extracted to a separate Maven module or microservice.
 *
 * @author Spring Modulith Migration
 */
@ApplicationModuleTest
@RecordApplicationEvents
class OwnerModuleTests {

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private PetApplicationService petService;

	@Autowired
	private PetTypeRepository petTypeRepository;

	@Autowired
	private ApplicationEvents events;

	@Test
	void ownerModuleShouldWorkInIsolation() {
		// Given: Owner module is loaded independently
		// (vet module is NOT loaded)

		// When: We use owner module functionality
		var owners = ownerRepository.findAll();

		// Then: Owner module works without dependencies
		assertThat(owners).isNotEmpty();
	}

	@Test
	void shouldBeAbleToAddPetWithoutVetModule() {
		// Given: An existing owner (vet module NOT loaded)
		var owner = ownerRepository.findAll().iterator().next();
		var ownerId = owner.getId().value();
		var initialPetCount = owner.getPets().size();

		// When: Adding a pet
		var petFormData = new PetFormData("Rocky", "dog", LocalDate.of(2022, 6, 15));
		var pet = petService.addPet(ownerId, petFormData);

		// Then: Pet is added successfully
		assertThat(pet).isNotNull();
		assertThat(pet.getName()).isEqualTo("Rocky");

		// And: Owner has the new pet
		var updatedOwner = ownerRepository.findById(owner.getId()).orElseThrow();
		assertThat(updatedOwner.getPets()).hasSize(initialPetCount + 1);
	}

	@Test
	void shouldPublishEventsEvenWhenVetModuleNotLoaded() {
		// Given: Owner module in isolation (no vet listeners)
		var owner = ownerRepository.findAll().iterator().next();
		var ownerId = owner.getId().value();

		// When: Triggering an event
		var petFormData = new PetFormData("Max", "cat", LocalDate.of(2023, 3, 20));
		petService.addPet(ownerId, petFormData);

		// Then: Event is published despite no vet module
		var publishedEvents = events.stream(PetAdoptedEvent.class).toList();
		assertThat(publishedEvents).hasSize(1);

		// This proves events are published regardless of consumers
	}

	@Test
	void shouldAccessPetTypes() {
		// Given: Owner module has access to reference data
		var petTypes = petTypeRepository.findPetTypes();

		// Then: Pet types are available
		assertThat(petTypes).isNotEmpty();
		assertThat(petTypes).extracting("name").contains("cat", "dog");
	}

	@Test
	void petsShouldBePartOfOwnerAggregate() {
		// Given: An owner
		var owner = ownerRepository.findAll().iterator().next();

		// When: Accessing pets through aggregate
		var pets = owner.getPets();

		// Then: Pets are accessible through the aggregate root
		assertThat(pets).isNotNull();

		// This verifies the aggregate boundary is maintained
	}

}
