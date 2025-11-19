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

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Data Transfer Object for Pet with resolved associations. This DTO is used in the view
 * layer to display pet information with resolved PetType names while maintaining DDD
 * aggregate boundaries in the domain model.
 *
 * @author Wick Dynex
 */
public class PetDTO {

    private final PetId id;

    private final @Nullable String name;

    private final @Nullable LocalDate birthDate;

    private final @Nullable String typeName;

    private final @Nullable PetTypeId typeId;

    private final Collection<Visit> visits;

    private PetDTO(PetId id, @Nullable String name, @Nullable LocalDate birthDate, @Nullable String typeName,
                   @Nullable PetTypeId typeId, Collection<Visit> visits) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.typeName = typeName;
        this.typeId = typeId;
        this.visits = visits;
    }

    /**
     * Create a PetDTO from a Pet entity, resolving the PetType association.
     *
     * @param pet             the pet entity
     * @param petTypeResolver the PetType repository/resolver
     * @return a new PetDTO with resolved type name
     */
    public static PetDTO from(Pet pet, AssociationResolver<PetType, PetTypeId> petTypeResolver) {
        String typeName = pet.resolveTypeName(petTypeResolver);
        return new PetDTO(pet.getId(), pet.getName(), pet.getBirthDate(), typeName, pet.getTypeId(), pet.getVisits());
    }

    public PetId getId() {
        return this.id;
    }

    public @Nullable String getName() {
        return this.name;
    }

    public @Nullable LocalDate getBirthDate() {
        return this.birthDate;
    }

    public @Nullable String getTypeName() {
        return this.typeName;
    }

    public @Nullable PetTypeId getTypeId() {
        return this.typeId;
    }

    public Collection<Visit> getVisits() {
        return this.visits;
    }

    public boolean isNew() {
        return this.id == null || this.id.value() == null;
    }

}
