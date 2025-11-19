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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
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

	private final OwnerRepository owners;

	private final PetTypeRepository types;

	public PetController(OwnerRepository owners, PetTypeRepository types) {
		this.owners = owners;
		this.types = types;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.types.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") UUID ownerId) {
		return loadOwnerById(ownerId);
	}

	@ModelAttribute("pet")
	public @Nullable Pet findPet(@PathVariable("ownerId") UUID ownerId,
			@PathVariable(name = "petId", required = false) @Nullable UUID petId) {

		if (petId == null) {
			return new Pet();
		}

		Owner owner = loadOwnerById(ownerId);
		return owner.getPet(new PetId(petId));
	}

	/**
	 * Loads an owner by UUID, throwing IllegalArgumentException if not found.
	 * @param ownerId the UUID of the owner to load
	 * @return the owner
	 * @throws IllegalArgumentException if owner not found
	 */
	private Owner loadOwnerById(UUID ownerId) {
		return this.owners.findById(new OwnerId(ownerId))
				.orElseThrow(() -> new IllegalArgumentException(
						"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		// Add PetValidator alongside default JSR-303 validator to allow @PastOrPresent to work
		dataBinder.addValidators(new PetValidator());
	}

	/**
	 * Prepares form data for creating or editing a pet.
	 * Used by DTO-based endpoints for layered validation approach.
	 * @param petId optional pet ID for edit operations
	 * @param ownerId the owner's ID
	 * @return form data populated from existing pet or empty for new pets
	 */
	private PetFormData prepareFormData(@Nullable UUID petId, UUID ownerId) {
		if (petId == null) {
			return PetFormData.empty();
		}

		Owner owner = loadOwnerById(ownerId);
		Pet pet = owner.getPet(new PetId(petId));
		return PetFormData.fromDomainObject(pet, this.types);
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {

		// Check for duplicate pet name
		if (StringUtils.hasText(pet.getName()) && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "A pet named '" + pet.getName() + "' already exists for this owner");
		}

		// Birth date validation is handled by @PastOrPresent annotation on Pet.getBirthDate()

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		owner.addPet(pet);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New pet '" + pet.getName() + "' has been added successfully");
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm() {
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {

		String petName = pet.getName();

		// checking if the pet name already exists for the owner
		if (StringUtils.hasText(petName)) {
			Pet existingPet = owner.getPet(petName, false);
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				result.rejectValue("name", "duplicate", "Another pet named '" + petName + "' already exists for this owner");
			}
		}

		// Birth date validation is handled by @PastOrPresent annotation on Pet.getBirthDate()

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		updatePetDetails(owner, pet);
		redirectAttributes.addFlashAttribute("message", "Pet '" + pet.getName() + "' has been updated successfully");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Updates the pet details if it exists or adds a new pet to the owner.
	 * @param owner The owner of the pet
	 * @param pet The pet with updated details
	 */
	private void updatePetDetails(Owner owner, Pet pet) {
		PetId petId = pet.getId();
		Assert.state(petId != null, "'pet.getId()' must not be null");
		Pet existingPet = owner.getPet(petId);
		if (existingPet != null) {
			// Update existing pet's properties
			existingPet.setName(pet.getName());
			existingPet.setBirthDate(pet.getBirthDate());
			existingPet.setTypeAssociation(pet.getType());
		}
		else {
			owner.addPet(pet);
		}
		this.owners.save(owner);
	}

	// ===== DTO-based endpoints (Week 1: Parallel Implementation) =====

	/**
	 * Shows form for creating a new pet using DTO-based validation.
	 * <p>
	 * This is a parallel implementation using layered validation with DTOs.
	 * Once tested and proven, this approach will replace the entity-based validation.
	 * </p>
	 * @param owner the pet's owner
	 * @param model the model
	 * @return the view name
	 */
	@GetMapping("/pets/new-dto")
	public String initCreationFormDto(@PathVariable("ownerId") UUID ownerId, Owner owner, ModelMap model) {
		// Prepare empty form data for DTO-based validation
		model.put("petForm", prepareFormData(null, ownerId));
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes pet creation form using DTO-based validation.
	 * <p>
	 * Demonstrates layered validation: Bean Validation on DTO for framework concerns,
	 * business rules in controller, domain invariants in entity constructors.
	 * </p>
	 * @param owner the pet's owner
	 * @param formData the validated form data
	 * @param result the binding result
	 * @param redirectAttributes for flash messages
	 * @return the view name or redirect
	 */
	@PostMapping("/pets/new-dto")
	public String processCreationFormDto(Owner owner, @Valid @ModelAttribute("petForm") PetFormData formData,
			BindingResult result, RedirectAttributes redirectAttributes) {

		// Business rule validation: duplicate pet name check
		if (formData.name() != null && owner.getPet(formData.name(), true) != null) {
			result.rejectValue("name", "duplicate",
					"A pet named '" + formData.name() + "' already exists for this owner");
		}

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		// Convert DTO to domain object
		PetType type = PetFormData.findTypeByName(formData.typeName(), this.types.findPetTypes());
		Pet pet = formData.toDomainObject(type);

		owner.addPet(pet);
		this.owners.save(owner);

		redirectAttributes.addFlashAttribute("message",
				"New pet '" + pet.getName() + "' has been added successfully");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Shows form for editing an existing pet using DTO-based validation.
	 * @param petId the pet ID
	 * @param ownerId the owner ID
	 * @param model the model
	 * @return the view name
	 */
	@GetMapping("/pets/{petId}/edit-dto")
	public String initUpdateFormDto(@PathVariable("petId") UUID petId, @PathVariable("ownerId") UUID ownerId,
			ModelMap model) {
		// Prepare form data from existing pet for DTO-based validation
		model.put("petForm", prepareFormData(petId, ownerId));
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes pet update form using DTO-based validation.
	 * @param owner the pet's owner
	 * @param pet the existing pet being updated
	 * @param formData the validated form data
	 * @param result the binding result
	 * @param redirectAttributes for flash messages
	 * @return the view name or redirect
	 */
	@PostMapping("/pets/{petId}/edit-dto")
	public String processUpdateFormDto(Owner owner, Pet pet, @Valid @ModelAttribute("petForm") PetFormData formData,
			BindingResult result, RedirectAttributes redirectAttributes) {

		// Business rule validation: duplicate pet name check
		if (formData.name() != null) {
			Pet existingPet = owner.getPet(formData.name(), false);
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				result.rejectValue("name", "duplicate",
						"Another pet named '" + formData.name() + "' already exists for this owner");
			}
		}

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		// Update domain object from DTO
		PetType type = PetFormData.findTypeByName(formData.typeName(), this.types.findPetTypes());
		formData.updateDomainObject(pet, type);

		this.owners.save(owner);

		redirectAttributes.addFlashAttribute("message",
				"Pet '" + pet.getName() + "' has been updated successfully");
		return "redirect:/owners/{ownerId}";
	}

}
