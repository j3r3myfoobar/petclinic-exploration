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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerId;

import java.util.Optional;

/**
 * Domain port for Owner repository operations.
 * <p>
 * This is a secondary port (driven port) that defines what the domain needs
 * from the persistence infrastructure. The infrastructure layer provides
 * the adapter implementation.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryPort
public interface OwnerRepositoryPort extends AssociationResolver<Owner, OwnerId> {

	/**
	 * Retrieve {@link Owner}s from the data store by last name, returning all owners
	 * whose last name <i>starts</i> with the given name.
	 * @param lastName Value to search for
	 * @param pageable Pagination information
	 * @return a Page of matching {@link Owner}s (or an empty Page if none found)
	 */
	Page<Owner> findByNameLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieve an {@link Owner} from the data store by id.
	 * @param id the id to search for
	 * @return an {@link Optional} containing the {@link Owner} if found, or an empty
	 * {@link Optional} if not found.
	 */
	Optional<Owner> findById(OwnerId id);

	/**
	 * Save an {@link Owner} to the data store.
	 * @param owner the owner to save
	 * @return the saved owner
	 */
	Owner save(Owner owner);

}
