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
package org.springframework.samples.petclinic.owner.domain.ports;

import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.types.DomainEvent;

/**
 * Domain port for publishing domain events.
 * <p>
 * This is a secondary port (driven port) that defines what the domain needs
 * from the event publishing infrastructure. This abstraction allows the domain
 * to remain independent of Spring's ApplicationEventPublisher.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryPort
public interface EventPublisherPort {

	/**
	 * Publish a domain event.
	 * @param event the domain event to publish
	 */
	void publish(DomainEvent event);

}
