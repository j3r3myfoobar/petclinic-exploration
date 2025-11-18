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

import java.util.List;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jspecify.annotations.Nullable;

/**
 * Data Transfer Object for Vet with resolved specialty associations. This DTO is used
 * in the view layer to display vet information with resolved Specialty names while
 * maintaining DDD aggregate boundaries in the domain model.
 *
 * @author Wick Dynex
 */
public class VetDTO {

	private final Vet vet;

	private final List<Specialty> specialties;

	private VetDTO(Vet vet, List<Specialty> specialties) {
		this.vet = vet;
		this.specialties = specialties;
	}

	/**
	 * Create a VetDTO from a Vet entity, resolving all Specialty associations.
	 * @param vet the vet entity
	 * @param specialtyResolver the Specialty repository/resolver
	 * @return a new VetDTO with resolved specialties
	 */
	public static VetDTO from(Vet vet, AssociationResolver<Specialty, SpecialtyId> specialtyResolver) {
		List<Specialty> specialties = vet.resolveSpecialties(specialtyResolver);
		return new VetDTO(vet, specialties);
	}

	public VetId getId() {
		return this.vet.getId();
	}

	public @Nullable String getFirstName() {
		return this.vet.getFirstName();
	}

	public @Nullable String getLastName() {
		return this.vet.getLastName();
	}

	public List<Specialty> getSpecialties() {
		return this.specialties;
	}

	public int getNrOfSpecialties() {
		return this.specialties.size();
	}

	/**
	 * Get the underlying Vet entity. Useful for operations that need the actual
	 * aggregate root.
	 * @return the vet entity
	 */
	public Vet getVet() {
		return this.vet;
	}

}
