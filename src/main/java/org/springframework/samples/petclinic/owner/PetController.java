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

import java.util.Collection;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.jmolecules.architecture.layered.InterfaceLayer;

/**
 * Controller for Pet management operations.
 * <p>
 * Thin adapter layer that handles HTTP concerns and delegates business logic to
 * {@link PetApplicationService}. Following DDD principles, this controller focuses
 * on request/response handling while the application service orchestrates domain
 * operations.
 * </p>
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@InterfaceLayer
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final PetApplicationService petService;

	private final OwnerRepository ownerRepository;

	private final PetTypeRepository petTypeRepository;

	public PetController(PetApplicationService petService, OwnerRepository ownerRepository,
			PetTypeRepository petTypeRepository) {
		this.petService = petService;
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.petTypeRepository.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") UUID ownerId) {
		return this.ownerRepository.findById(new OwnerId(ownerId))
			.orElseThrow(() -> new IllegalArgumentException(
					"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));
	}

	@ModelAttribute("pet")
	public @Nullable Pet findPet(@PathVariable("ownerId") UUID ownerId,
			@PathVariable(name = "petId", required = false) @Nullable UUID petId) {

		if (petId == null) {
			return new Pet();
		}

		Owner owner = findOwner(ownerId);
		return owner.getPet(new PetId(petId));
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Shows form for creating a new pet.
	 * @param ownerId the owner's ID
	 * @param owner the pet's owner (injected via @ModelAttribute)
	 * @param model the model
	 * @return the view name
	 */
	@GetMapping("/pets/new")
	public String initCreationForm(@PathVariable("ownerId") UUID ownerId, Owner owner, ModelMap model) {
		PetFormData formData = this.petService.prepareNewPetForm();
		model.put("petForm", formData);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes pet creation form.
	 * <p>
	 * Delegates business logic to {@link PetApplicationService}. Handles validation
	 * errors and maps domain exceptions to field errors.
	 * </p>
	 * @param ownerId the owner's ID
	 * @param formData the validated form data
	 * @param result the binding result
	 * @param redirectAttributes for flash messages
	 * @return the view name or redirect
	 */
	@PostMapping("/pets/new")
	public String processCreationForm(@PathVariable("ownerId") UUID ownerId,
			@Valid @ModelAttribute("petForm") PetFormData formData, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		try {
			Pet pet = this.petService.addPet(ownerId, formData);
			redirectAttributes.addFlashAttribute("message",
					"New pet '" + pet.getName() + "' has been added successfully");
			return "redirect:/owners/{ownerId}";
		}
		catch (PetApplicationService.DuplicatePetNameException ex) {
			result.rejectValue("name", "duplicate", ex.getMessage());
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
	}

	/**
	 * Shows form for editing an existing pet.
	 * @param petId the pet ID
	 * @param ownerId the owner ID
	 * @param model the model
	 * @return the view name
	 */
	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("petId") UUID petId, @PathVariable("ownerId") UUID ownerId,
			ModelMap model) {
		PetFormData formData = this.petService.prepareEditPetForm(ownerId, petId);
		model.put("petForm", formData);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes pet update form.
	 * <p>
	 * Delegates business logic to {@link PetApplicationService}. Handles validation
	 * errors and maps domain exceptions to field errors.
	 * </p>
	 * @param ownerId the owner's ID
	 * @param petId the ID of the pet being updated
	 * @param formData the validated form data
	 * @param result the binding result
	 * @param redirectAttributes for flash messages
	 * @return the view name or redirect
	 */
	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@PathVariable("ownerId") UUID ownerId, @PathVariable("petId") UUID petId,
			@Valid @ModelAttribute("petForm") PetFormData formData, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		try {
			Pet pet = this.petService.updatePet(ownerId, petId, formData);
			redirectAttributes.addFlashAttribute("message",
					"Pet '" + pet.getName() + "' has been updated successfully");
			return "redirect:/owners/{ownerId}";
		}
		catch (PetApplicationService.DuplicatePetNameException ex) {
			result.rejectValue("name", "duplicate", ex.getMessage());
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
	}

}
