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
package org.springframework.samples.petclinic.owner.infrastructure.events;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.jmolecules.ddd.types.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.owner.domain.ports.EventPublisherPort;
import org.springframework.stereotype.Component;

/**
 * Spring event publishing adapter.
 * <p>
 * This secondary adapter implements the domain port {@link EventPublisherPort}
 * and adapts it to Spring's {@link ApplicationEventPublisher}.
 * </p>
 * <p>
 * This abstraction allows the domain to publish events without depending on
 * Spring's infrastructure classes.
 * </p>
 *
 * @author Wick Dynex
 */
@SecondaryAdapter
@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {

	private final ApplicationEventPublisher springEventPublisher;

	public SpringEventPublisherAdapter(ApplicationEventPublisher springEventPublisher) {
		this.springEventPublisher = springEventPublisher;
	}

	@Override
	public void publish(DomainEvent event) {
		springEventPublisher.publishEvent(event);
	}

}
