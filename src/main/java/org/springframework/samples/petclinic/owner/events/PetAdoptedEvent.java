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

import java.time.LocalDate;

import org.jmolecules.event.types.DomainEvent;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.PetId;
import org.springframework.samples.petclinic.owner.PetTypeId;

/**
 * Domain event published when a pet is adopted by an owner.
 *
 * <p>This event signals that a new pet has been registered in the system
 * and is now associated with an owner. Other modules can listen to this
 * event to react accordingly (e.g., vet module tracking new patients).
 *
 * @param petId the unique identifier of the adopted pet
 * @param petTypeId the type of pet (dog, cat, etc.)
 * @param ownerId the owner who adopted the pet
 * @param adoptionDate the date the pet was adopted
 *
 * @author Spring Modulith Migration
 */
public record PetAdoptedEvent(
	PetId petId,
	PetTypeId petTypeId,
	OwnerId ownerId,
	LocalDate adoptionDate
) implements DomainEvent {

	/**
	 * Creates a new PetAdoptedEvent with the current date as adoption date.
	 */
	public static PetAdoptedEvent of(PetId petId, PetTypeId petTypeId, OwnerId ownerId) {
		return new PetAdoptedEvent(petId, petTypeId, ownerId, LocalDate.now());
	}

}
