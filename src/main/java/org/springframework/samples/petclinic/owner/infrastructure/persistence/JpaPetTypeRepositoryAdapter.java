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
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeId;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.domain.ports.PetTypeRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for PetType repository operations.
 * <p>
 * This secondary adapter implements the domain port {@link PetTypeRepositoryPort}
 * and delegates to the Spring Data JPA repository {@link PetTypeRepository}.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryAdapter
@Component
public class JpaPetTypeRepositoryAdapter implements PetTypeRepositoryPort {

	private final PetTypeRepository jpaRepository;

	public JpaPetTypeRepositoryAdapter(PetTypeRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public List<PetType> findPetTypes() {
		return jpaRepository.findPetTypes();
	}

	@Override
	public Optional<PetType> resolveOptional(Association<PetType, PetTypeId> association) {
		return jpaRepository.resolve(association);
	}

}
