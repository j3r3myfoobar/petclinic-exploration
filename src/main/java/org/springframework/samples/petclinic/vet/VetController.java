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

import org.jmolecules.architecture.layered.InterfaceLayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@InterfaceLayer
@Controller
@Transactional(readOnly = true)
class VetController {

    private final VetRepository vetRepository;

    private final SpecialtyRepository specialtyRepository;

    public VetController(VetRepository vetRepository, SpecialtyRepository specialtyRepository) {
        this.vetRepository = vetRepository;
        this.specialtyRepository = specialtyRepository;
    }

    @GetMapping("/vets.html")
    public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
        // Resolve specialty associations for view layer
        Page<Vet> paginated = findPaginated(page);
        List<VetDTO> vetDTOs = paginated.getContent()
                .stream()
                .map(vet -> VetDTO.from(vet, specialtyRepository))
                .collect(Collectors.toList());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginated.getTotalPages());
        model.addAttribute("totalItems", paginated.getTotalElements());
        model.addAttribute("listVets", vetDTOs);
        return "vets/vetList";
    }

    private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
        List<VetDTO> vetDTOs = paginated.getContent()
                .stream()
                .map(vet -> VetDTO.from(vet, specialtyRepository))
                .collect(Collectors.toList());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginated.getTotalPages());
        model.addAttribute("totalItems", paginated.getTotalElements());
        model.addAttribute("listVets", vetDTOs);
        return "vets/vetList";
    }

    private Page<Vet> findPaginated(int page) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return vetRepository.findAll(pageable);
    }

    @GetMapping({"/vets"})
    public @ResponseBody VetsDTO showResourcesVetList() {
        // Return VetsDTO with resolved specialty associations for JSON/XML serialization
        List<Vet> vets = this.vetRepository.findAll();
        return VetsDTO.from(vets, this.specialtyRepository);
    }

}
