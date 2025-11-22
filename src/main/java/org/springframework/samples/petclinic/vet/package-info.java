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
 * Vet Management Module - Veterinary Staff Management
 *
 * <p>This module represents the bounded context for managing veterinarians and their
 * specialties. It maintains the directory of veterinary professionals and their
 * areas of expertise.
 *
 * <h2>Domain Model</h2>
 * <ul>
 *   <li><strong>Vet Aggregate</strong> - The primary aggregate root representing a
 *       veterinarian with their personal information and specialty associations</li>
 *   <li><strong>Specialty Aggregate</strong> - Reference data for veterinary specialties
 *       (surgery, dentistry, radiology, etc.)</li>
 * </ul>
 *
 * <h2>Design Patterns</h2>
 * <p>This module demonstrates proper DDD aggregate boundary enforcement:
 * <ul>
 *   <li>Vets store only {@code SpecialtyId} references, not full Specialty entities</li>
 *   <li>Specialty details are resolved via {@code AssociationResolver} when needed</li>
 *   <li>Prevents inappropriate coupling between aggregates</li>
 * </ul>
 *
 * <h2>Module Dependencies</h2>
 * <ul>
 *   <li><strong>model</strong> - Shared kernel providing base domain primitives (Person, PersonName)</li>
 * </ul>
 *
 * <h2>Consumed Events</h2>
 * <ul>
 *   <li>{@link org.springframework.samples.petclinic.owner.events.PetAdoptedEvent}
 *       - From owner module, tracks new patients in the system</li>
 * </ul>
 *
 * <h2>Published Events</h2>
 * <p>Future: This module will publish domain events for significant changes such as
 * new vet registration, specialty assignments, or vet status updates.
 *
 * <h2>API Surface</h2>
 * <p>The module exposes controllers for:
 * <ul>
 *   <li>Vet directory listing and search</li>
 *   <li>Vet profile management</li>
 *   <li>Specialty management</li>
 * </ul>
 */
@ApplicationModule(
	displayName = "Vet Management",
	allowedDependencies = "model"
)
@NullMarked
package org.springframework.samples.petclinic.vet;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;