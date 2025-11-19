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

import org.jmolecules.ddd.types.Entity;
import org.springframework.samples.petclinic.model.NamedEntity;

import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

/**
 * Models a type of pet (for example, cat, dog, hamster).
 *
 * Uses jMolecules Entity type with type-safe PetTypeId.
 * ByteBuddy automatically adds @Entity annotation at compile time.
 *
 * @author Juergen Hoeller
 */
@Table(name = "types")
public class PetType extends NamedEntity implements org.jmolecules.ddd.types.AggregateRoot<PetType, PetTypeId> {

	@jakarta.persistence.Id
	@jakarta.persistence.AttributeOverride(name = "value", column = @jakarta.persistence.Column(name = "id"))
	private PetTypeId id = new PetTypeId();

	/**
	 * Get the type-safe PetTypeId. Required by Entity interface.
	 * @return the pet type's identifier
	 */
	public PetTypeId getId() {
		return this.id;
	}

	/**
	 * Set the pet type's identifier using type-safe PetTypeId.
	 * @param id the pet type's identifier
	 */
	public void setId(PetTypeId id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String name = this.getName();
		return (name != null) ? name : "<null>";
	}

}
