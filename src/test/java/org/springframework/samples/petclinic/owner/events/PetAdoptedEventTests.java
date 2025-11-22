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
package org.springframework.samples.petclinic.owner.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.PetApplicationService;
import org.springframework.samples.petclinic.owner.PetFormData;
import org.springframework.samples.petclinic.owner.PetId;
import org.springframework.samples.petclinic.owner.PetTypeId;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * Tests for {@link PetAdoptedEvent} publishing and handling.
 *
 * <p>Verifies that domain events are properly published when pets are adopted
 * and that the event contract is maintained.
 *
 * @author Spring Modulith Migration
 */
@SpringBootTest
@RecordApplicationEvents
class PetAdoptedEventTests {

	@Autowired
	private PetApplicationService petService;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private ApplicationEvents events;

	@Test
	void shouldPublishPetAdoptedEventWhenPetIsAdded() {
		// Given: An existing owner
		var owner = ownerRepository.findAll().iterator().next();
		var ownerId = owner.getId().value();

		// When: A new pet is added
		var petFormData = new PetFormData("Fluffy", "cat", LocalDate.of(2023, 1, 15));
		var pet = petService.addPet(ownerId, petFormData);

		// Then: PetAdoptedEvent should be published
		var petAdoptedEvents = events.stream(PetAdoptedEvent.class).toList();

		assertThat(petAdoptedEvents).hasSize(1);

		var event = petAdoptedEvents.get(0);
		assertThat(event.petId()).isEqualTo(pet.getId());
		assertThat(event.ownerId()).isEqualTo(owner.getId());
		assertThat(event.petTypeId()).isEqualTo(pet.getType());
		assertThat(event.adoptionDate()).isNotNull();
	}

	@Test
	void shouldCreateEventWithValidData() {
		// Given: Event components
		var petId = new PetId(UUID.randomUUID());
		var petTypeId = new PetTypeId(UUID.randomUUID());
		var ownerId = new OwnerId(UUID.randomUUID());
		var adoptionDate = LocalDate.now();

		// When: Event is created
		var event = new PetAdoptedEvent(petId, petTypeId, ownerId, adoptionDate);

		// Then: All fields should be set correctly
		assertThat(event.petId()).isEqualTo(petId);
		assertThat(event.petTypeId()).isEqualTo(petTypeId);
		assertThat(event.ownerId()).isEqualTo(ownerId);
		assertThat(event.adoptionDate()).isEqualTo(adoptionDate);
	}

	@Test
	void shouldCreateEventWithFactoryMethod() {
		// Given: Event components
		var petId = new PetId(UUID.randomUUID());
		var petTypeId = new PetTypeId(UUID.randomUUID());
		var ownerId = new OwnerId(UUID.randomUUID());

		// When: Event is created using factory method
		var event = PetAdoptedEvent.of(petId, petTypeId, ownerId);

		// Then: All fields should be set correctly with today's date
		assertThat(event.petId()).isEqualTo(petId);
		assertThat(event.petTypeId()).isEqualTo(petTypeId);
		assertThat(event.ownerId()).isEqualTo(ownerId);
		assertThat(event.adoptionDate()).isEqualTo(LocalDate.now());
	}

}
