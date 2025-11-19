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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO wrapper representing a list of veterinarians with resolved specialty associations.
 * Used for JSON and XML serialization in REST endpoints, ensuring that specialties are
 * properly resolved across aggregate boundaries following DDD principles.
 *
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@XmlRootElement(name = "vets")
public class VetsDTO {

    private @Nullable List<VetDTO> vets;

    /**
     * Default constructor for deserialization.
     */
    public VetsDTO() {
    }

    /**
     * Constructor with resolved vet DTOs.
     *
     * @param vets list of vet DTOs with resolved specialties
     */
    public VetsDTO(List<VetDTO> vets) {
        this.vets = vets;
    }

    /**
     * Create VetsDTO from a collection of Vet aggregates, resolving all specialty
     * associations.
     *
     * @param vets              the vet aggregates to convert
     * @param specialtyResolver the specialty repository/resolver
     * @return a new VetsDTO with all associations resolved
     */
    public static VetsDTO from(List<Vet> vets, AssociationResolver<Specialty, SpecialtyId> specialtyResolver) {
        List<VetDTO> vetDTOs = vets.stream()
                .map(vet -> VetDTO.from(vet, specialtyResolver))
                .collect(Collectors.toList());
        return new VetsDTO(vetDTOs);
    }

    /**
     * Get the list of vet DTOs.
     *
     * @return the list of vets with resolved specialties
     */
    @XmlElement(name = "vet")
    public List<VetDTO> getVetList() {
        if (vets == null) {
            vets = new ArrayList<>();
        }
        return vets;
    }

}
