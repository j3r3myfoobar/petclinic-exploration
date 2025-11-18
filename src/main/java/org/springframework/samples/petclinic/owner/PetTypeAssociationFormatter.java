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

import org.jmolecules.ddd.types.Association;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/**
 * Formatter for Association<PetType, PetTypeId> to enable Spring MVC form binding
 * with DDD-compliant Association types. This formatter converts between the
 * Association type used in the domain model and String representation used in forms.
 *
 * @author Wick Dynex
 */
@Component
public class PetTypeAssociationFormatter implements Formatter<Association<PetType, PetTypeId>> {

	private final PetTypeRepository types;

	public PetTypeAssociationFormatter(PetTypeRepository types) {
		this.types = types;
	}

	@Override
	public String print(Association<PetType, PetTypeId> association, Locale locale) {
		if (association == null) {
			return "";
		}

		// Get the ID from the association and find the matching PetType
		PetTypeId typeId = association.getId();
		Collection<PetType> allTypes = this.types.findPetTypes();

		for (PetType type : allTypes) {
			if (type.getId().equals(typeId)) {
				String name = type.getName();
				return (name != null) ? name : "<null>";
			}
		}

		return "";
	}

	@Override
	public Association<PetType, PetTypeId> parse(String text, Locale locale) throws ParseException {
		Collection<PetType> findPetTypes = this.types.findPetTypes();

		// First try to match by name
		for (PetType type : findPetTypes) {
			if (Objects.equals(type.getName(), text)) {
				return Association.forAggregate(type);
			}
		}

		// If not found by name, try to parse as numeric index (for backward compatibility with tests)
		try {
			int index = Integer.parseInt(text);
			int currentIndex = 0;
			for (PetType type : findPetTypes) {
				if (currentIndex == index) {
					return Association.forAggregate(type);
				}
				currentIndex++;
			}
		}
		catch (NumberFormatException ignored) {
			// Not a number, continue to throw ParseException below
		}

		throw new ParseException("type not found: " + text, 0);
	}

}
