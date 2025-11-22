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
package org.springframework.samples.petclinic.vet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmolecules.architecture.layered.ApplicationLayer;
import org.springframework.modulith.ApplicationModuleListener;
import org.springframework.samples.petclinic.owner.events.PetAdoptedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that tracks patient statistics in the vet module.
 *
 * <p>This service listens to events from the owner module to maintain
 * up-to-date information about pets in the clinic without direct coupling
 * to the owner module's internal implementation.
 *
 * <p>Demonstrates event-driven architecture and module decoupling in a
 * Spring Modulith application.
 *
 * @author Spring Modulith Migration
 */
@ApplicationLayer
@Service
@Transactional
class VetPatientTrackingService {

	private static final Log log = LogFactory.getLog(VetPatientTrackingService.class);

	/**
	 * Handles pet adoption events from the owner module.
	 *
	 * <p>This method is invoked asynchronously when a pet is adopted,
	 * allowing the vet module to react without the owner module needing
	 * to know about vet module's internal workings.
	 *
	 * @param event the pet adopted event
	 */
	@ApplicationModuleListener
	void onPetAdopted(PetAdoptedEvent event) {
		log.info(
				"New patient registered in vet module: Pet ID=" + event.petId() + ", Type=" + event.petTypeId());

		// In a real application, this would:
		// - Update patient statistics
		// - Track pets by type
		// - Prepare welcome materials for new patients
		// - Notify relevant vets about new patients

		// For now, we just log the event to demonstrate the pattern
	}

}
