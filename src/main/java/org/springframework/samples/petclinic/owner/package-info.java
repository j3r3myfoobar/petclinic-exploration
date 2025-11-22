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

/**
 * Owner Management Module - Pet Ownership & Care Lifecycle
 *
 * <p>This module represents the bounded context for managing pet owners, their pets,
 * and veterinary visits. It handles the complete lifecycle of pet ownership from
 * registration through ongoing care.
 *
 * <h2>Domain Model</h2>
 * <ul>
 *   <li><strong>Owner Aggregate</strong> - The primary aggregate root containing owner
 *       information, their collection of pets, and associated visits</li>
 *   <li><strong>Pet Entity</strong> - Pets owned by an owner, with birth dates and type
 *       associations</li>
 *   <li><strong>Visit Entity</strong> - Individual veterinary visits for pets</li>
 *   <li><strong>PetType Aggregate</strong> - Reference data for pet types (dog, cat, etc.)</li>
 * </ul>
 *
 * <h2>Module Dependencies</h2>
 * <ul>
 *   <li><strong>model</strong> - Shared kernel providing base domain primitives (Person, PersonName)</li>
 * </ul>
 *
 * <h2>Published Events</h2>
 * <ul>
 *   <li>{@link org.springframework.samples.petclinic.owner.events.PetAdoptedEvent}
 *       - Published when a pet is adopted by an owner</li>
 * </ul>
 * <p>Future events: Visit scheduling and owner update notifications.
 *
 * <h2>API Surface</h2>
 * <p>The module exposes controllers for:
 * <ul>
 *   <li>Owner management (create, update, search)</li>
 *   <li>Pet management (add, update, pet details)</li>
 *   <li>Visit scheduling and management</li>
 * </ul>
 */
@ApplicationModule(
	displayName = "Owner Management",
	allowedDependencies = "model"
)
@NullMarked
package org.springframework.samples.petclinic.owner;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;