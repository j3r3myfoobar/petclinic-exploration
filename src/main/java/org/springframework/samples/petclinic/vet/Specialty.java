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

import org.jmolecules.ddd.types.Entity;
import org.springframework.samples.petclinic.model.NamedEntity;

import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 * Uses jMolecules Entity type with type-safe SpecialtyId. ByteBuddy will automatically
 * add @Entity annotation.
 *
 * @author Juergen Hoeller
 */
@Table(name = "specialties")
public class Specialty extends NamedEntity implements Entity<Vet, SpecialtyId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private SpecialtyId id = new SpecialtyId();

	/**
	 * Get the type-safe SpecialtyId. Required by Entity interface.
	 * @return the specialty's identifier
	 */
	public SpecialtyId getId() {
		return this.id;
	}

	/**
	 * Set the specialty's identifier using type-safe SpecialtyId.
	 * @param id the specialty's identifier
	 */
	public void setId(SpecialtyId id) {
		this.id = id;
	}

}
