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
package org.springframework.samples.petclinic.owner.domain.ports;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeId;

import java.util.List;

/**
 * Domain port for PetType repository operations.
 * <p>
 * This is a secondary port (driven port) that defines what the domain needs
 * from the persistence infrastructure for pet type operations.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryPort
public interface PetTypeRepositoryPort extends AssociationResolver<PetType, PetTypeId> {

	/**
	 * Retrieve all {@link PetType}s from the data store.
	 * @return a List of {@link PetType}s ordered by name.
	 */
	List<PetType> findPetTypes();

}
