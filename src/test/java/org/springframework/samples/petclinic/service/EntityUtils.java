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

package org.springframework.samples.petclinic.service;

import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.Collection;
import java.util.UUID;

/**
 * Utility methods for handling entities with type-safe UUID identifiers.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 29.10.2003
 */
public abstract class EntityUtils {

	/**
	 * Look up the entity of the given class with the given UUID in the given collection.
	 * @param entities the collection to search
	 * @param entityClass the entity class to look up
	 * @param entityId the entity UUID to look up
	 * @return the found entity
	 * @throws ObjectRetrievalFailureException if the entity was not found
	 */
	public static <T> T getById(Collection<T> entities, Class<T> entityClass, UUID entityId)
			throws ObjectRetrievalFailureException {
		for (T entity : entities) {
			UUID id = extractId(entity);
			if (id != null && id.equals(entityId) && entityClass.isInstance(entity)) {
				return entity;
			}
		}
		throw new ObjectRetrievalFailureException(entityClass, entityId);
	}

	/**
	 * Extract the ID value from an entity with type-safe UUID identifiers.
	 * @param entity the entity to extract the ID from
	 * @return the ID value as a UUID, or null if no ID
	 */
	private static UUID extractId(Object entity) {
		try {
			// Try to get getId() method
			var method = entity.getClass().getMethod("getId");
			Object id = method.invoke(entity);
			if (id == null) {
				return null;
			}
			// If it's a type-safe ID (has a value() method), extract the UUID
			if (id.getClass().isRecord()) {
				try {
					var valueMethod = id.getClass().getMethod("value");
					Object value = valueMethod.invoke(id);
					return (UUID) value;
				}
				catch (NoSuchMethodException e) {
					// Not a record with value(), treat as UUID directly
					return (UUID) id;
				}
			}
			// Direct UUID ID
			return (UUID) id;
		}
		catch (Exception e) {
			return null;
		}
	}

}
