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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Integration tests for {@link PetController} DTO-based endpoints.
 * <p>
 * Tests the layered validation approach using {@link PetFormData} DTOs
 * instead of direct entity binding. These tests verify that Bean Validation
 * works correctly in the web layer while keeping domain objects clean.
 * </p>
 * <p>
 * As of Week 4, DTO-based validation is the default approach for all pet
 * creation and editing operations.
 * </p>
 *
 * @author Wick Dynex
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerDtoTests {

	private static final UUID TEST_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	private static final UUID TEST_PET_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	@MockitoBean
	private PetApplicationService petService;

	private Pet existingPet;

	private Pet dog;

	private PetType hamster;

	@BeforeEach
	void setup() {
		hamster = new PetType();
		hamster.setName("hamster");
		given(this.types.findPetTypes()).willReturn(List.of(hamster));

		Owner owner = new Owner();
		existingPet = new Pet();
		dog = new Pet();
		owner.addPet(existingPet);
		owner.addPet(dog);
		existingPet.setId(new PetId(TEST_PET_UUID));
		dog.setId(new PetId(UUID.fromString("00000000-0000-0000-0000-000000000002")));
		existingPet.setName("petty");
		existingPet.setBirthDate(LocalDate.of(2015, 2, 12));
		existingPet.setType(hamster);
		dog.setName("doggy");
		dog.setBirthDate(LocalDate.of(2016, 3, 15));
		dog.setType(hamster);
		given(this.owners.findById(new OwnerId(TEST_OWNER_UUID))).willReturn(Optional.of(owner));

		// Mock PetApplicationService behaviors
		// Create form data before stubbing to avoid nested mock calls
		PetFormData editFormData = PetFormData.fromDomainObject(existingPet, this.types);

		given(this.petService.prepareNewPetForm()).willReturn(PetFormData.empty());
		given(this.petService.prepareEditPetForm(TEST_OWNER_UUID, TEST_PET_UUID))
			.willReturn(editFormData);
	}

	@Test
	void testInitCreationFormDto() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_UUID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeExists("petForm"));
	}

	@Test
	void testProcessCreationFormDtoSuccess() throws Exception {
		// Mock successful pet creation
		Pet newPet = new Pet();
		newPet.setName("Betty");
		newPet.setBirthDate(LocalDate.of(2015, 2, 12));
		newPet.setType(hamster);
		given(this.petService.addPet(eq(TEST_OWNER_UUID), any(PetFormData.class))).willReturn(newPet);

		mockMvc
			.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "Betty")
				.param("typeName", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessCreationFormDtoHasErrors {

		@Test
		void testProcessCreationFormDtoWithBlankName() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "\t \n")
					.param("typeName", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "name"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "name", "NotBlank"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormDtoWithDuplicateName() throws Exception {
			// Mock duplicate pet name exception
			given(PetControllerDtoTests.this.petService.addPet(eq(TEST_OWNER_UUID), any(PetFormData.class)))
				.willThrow(new PetApplicationService.DuplicatePetNameException(
						"A pet named 'petty' already exists for this owner"));

			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "petty")
					.param("typeName", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "name"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "name", "duplicate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormDtoWithMissingPetType() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "Betty")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "typeName"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "typeName", "NotNull"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormDtoWithInvalidBirthDate() throws Exception {
			// Test boundary: date after now should fail validation
			LocalDate currentDate = LocalDate.now();
			String futureBirthDate = currentDate.plusDays(1).toString();

			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "Betty")
					.param("typeName", "hamster")
					.param("birthDate", futureBirthDate))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "birthDate", "PastOrPresent"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));

			// Test boundary: date before now should pass validation
			String pastBirthDate = currentDate.minusDays(1).toString();

			// Mock successful pet creation for valid date
			Pet newPet = new Pet();
			newPet.setName("Betty");
			newPet.setBirthDate(currentDate.minusDays(1));
			newPet.setType(PetControllerDtoTests.this.hamster);
			given(PetControllerDtoTests.this.petService.addPet(eq(TEST_OWNER_UUID), any(PetFormData.class))).willReturn(newPet);

			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "Betty")
					.param("typeName", "hamster")
					.param("birthDate", pastBirthDate))
				.andExpect(status().is3xxRedirection());
		}

		@Test
		void testProcessCreationFormDtoWithMissingBirthDate() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/new", TEST_OWNER_UUID).param("name", "Betty")
					.param("typeName", "hamster"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "birthDate", "NotNull"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

	@Test
	void testInitUpdateFormDto() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_UUID, TEST_PET_UUID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("petForm"))
			.andExpect(view().name("pets/createOrUpdatePetForm"));
	}

	@Test
	void testProcessUpdateFormDtoSuccess() throws Exception {
		// Mock successful pet update
		Pet updatedPet = new Pet();
		updatedPet.setId(new PetId(TEST_PET_UUID));
		updatedPet.setName("Betty Jr");
		updatedPet.setBirthDate(LocalDate.of(2015, 2, 12));
		updatedPet.setType(hamster);
		given(this.petService.updatePet(eq(TEST_OWNER_UUID), eq(TEST_PET_UUID), any(PetFormData.class)))
			.willReturn(updatedPet);

		mockMvc
			.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_UUID, TEST_PET_UUID)
				.param("name", "Betty Jr")
				.param("typeName", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessUpdateFormDtoHasErrors {

		@Test
		void testProcessUpdateFormDtoWithBlankName() throws Exception {
			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_UUID, TEST_PET_UUID)
					.param("name", "")
					.param("typeName", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "name"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "name", "NotBlank"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessUpdateFormDtoWithDuplicateName() throws Exception {
			// Mock duplicate pet name exception for update
			given(PetControllerDtoTests.this.petService.updatePet(eq(TEST_OWNER_UUID), eq(TEST_PET_UUID), any(PetFormData.class)))
				.willThrow(new PetApplicationService.DuplicatePetNameException(
						"A pet named 'doggy' already exists for this owner"));

			mockMvc
				.perform(post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_UUID, TEST_PET_UUID)
					.param("name", "doggy")
					.param("typeName", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasErrors("petForm"))
				.andExpect(model().attributeHasFieldErrors("petForm", "name"))
				.andExpect(model().attributeHasFieldErrorCode("petForm", "name", "duplicate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

}
