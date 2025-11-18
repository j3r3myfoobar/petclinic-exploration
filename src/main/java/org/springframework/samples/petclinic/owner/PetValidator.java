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

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Pet forms.
 * <p>
 * Validates basic Pet field requirements. Note that duplicate name validation is handled in
 * the controller layer where Owner context is available.
 * </p>
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class PetValidator implements Validator {

	private static final String REQUIRED = "required";

	private static final String NAME_REQUIRED_MESSAGE = "Pet name is required";

	private static final String TYPE_REQUIRED_MESSAGE = "Pet type is required";

	private static final String BIRTH_DATE_REQUIRED_MESSAGE = "Birth date is required";

	@Override
	public void validate(Object obj, Errors errors) {
		Pet pet = (Pet) obj;

		// Validate name is not empty
		String name = pet.getName();
		if (!StringUtils.hasText(name)) {
			errors.rejectValue("name", REQUIRED, NAME_REQUIRED_MESSAGE);
		}

		// Validate type is selected
		if (pet.getType() == null) {
			errors.rejectValue("type", REQUIRED, TYPE_REQUIRED_MESSAGE);
		}

		// Validate birth date is provided
		// Note: @PastOrPresent annotation on Pet.getBirthDate() handles date range validation
		if (pet.getBirthDate() == null) {
			errors.rejectValue("birthDate", REQUIRED, BIRTH_DATE_REQUIRED_MESSAGE);
		}
	}

	/**
	 * This Validator validates Pet instances only.
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Pet.class.isAssignableFrom(clazz);
	}

}
