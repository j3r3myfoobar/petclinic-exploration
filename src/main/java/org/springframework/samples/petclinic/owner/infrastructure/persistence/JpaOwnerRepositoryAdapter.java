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
package org.springframework.samples.petclinic.owner.infrastructure.persistence;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.ddd.types.Association;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerId;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.domain.ports.OwnerRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter for Owner repository operations.
 * <p>
 * This secondary adapter implements the domain port {@link OwnerRepositoryPort}
 * and delegates to the Spring Data JPA repository {@link OwnerRepository}.
 * </p>
 * <p>
 * In the pragmatic approach, we don't need mapping code because the domain
 * models are also JPA entities (via jMolecules ByteBuddy). This adapter simply
 * delegates to the Spring Data repository.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryAdapter
@Component
public class JpaOwnerRepositoryAdapter implements OwnerRepositoryPort {

	private final OwnerRepository jpaRepository;

	public JpaOwnerRepositoryAdapter(OwnerRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Page<Owner> findByNameLastNameStartingWith(String lastName, Pageable pageable) {
		return jpaRepository.findByNameLastNameStartingWith(lastName, pageable);
	}

	@Override
	public Optional<Owner> findById(OwnerId id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Owner save(Owner owner) {
		return jpaRepository.save(owner);
	}

	@Override
	public Optional<Owner> resolveOptional(Association<Owner, OwnerId> association) {
		return jpaRepository.resolve(association);
	}

}
