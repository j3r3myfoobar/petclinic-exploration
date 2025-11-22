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
package org.springframework.samples.petclinic;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.docs.Documenter.CanvasOptions;
import org.springframework.modulith.docs.Documenter.DiagramOptions;

/**
 * Generates Spring Modulith documentation.
 *
 * <p>This test class generates visual documentation of the module structure:
 * <ul>
 *   <li><strong>PlantUML diagrams</strong> - Shows module dependencies and structure</li>
 *   <li><strong>Module canvas</strong> - Details each module's APIs, events, and dependencies</li>
 * </ul>
 *
 * <p>Generated files are written to {@code target/modulith-docs/} directory.
 *
 * <p>Run with: {@code mvn test -Dtest=ModulithDocumentationTests}
 *
 * @author Spring Modulith Migration
 */
class ModulithDocumentationTests {

	private static final String BASE_PACKAGE = "org.springframework.samples.petclinic";

	ApplicationModules modules = ApplicationModules.of(BASE_PACKAGE);

	/**
	 * Generates a complete module dependency diagram showing all modules and their relationships.
	 *
	 * <p>Output: {@code target/modulith-docs/modules.puml}
	 *
	 * <p>This diagram shows:
	 * <ul>
	 *   <li>All application modules</li>
	 *   <li>Dependencies between modules</li>
	 *   <li>Named interfaces (like owner::events)</li>
	 * </ul>
	 */
	@Test
	void writeCompleteModuleDependencyDiagram() {
		new Documenter(modules)
			.writeModulesAsPlantUml();
	}

	/**
	 * Generates individual diagrams for each module showing its internal structure.
	 *
	 * <p>Output: {@code target/modulith-docs/module-<name>.puml} for each module
	 *
	 * <p>Each diagram shows:
	 * <ul>
	 *   <li>Module's packages and classes</li>
	 *   <li>Internal dependencies</li>
	 *   <li>What the module exposes</li>
	 * </ul>
	 */
	@Test
	void writeIndividualModuleDiagrams() {
		new Documenter(modules)
			.writeIndividualModulesAsPlantUml();
	}

	/**
	 * Generates module canvases documenting each module's design.
	 *
	 * <p>Output: {@code target/modulith-docs/module-<name>.adoc} for each module
	 *
	 * <p>Module canvas includes:
	 * <ul>
	 *   <li><strong>Name and Description</strong> - Module identity</li>
	 *   <li><strong>Base Package</strong> - Where the module code lives</li>
	 *   <li><strong>Dependencies</strong> - What other modules it uses</li>
	 *   <li><strong>Named Interfaces</strong> - Public APIs exposed</li>
	 *   <li><strong>Events Published</strong> - Domain events emitted</li>
	 *   <li><strong>Events Listened To</strong> - Events consumed</li>
	 * </ul>
	 */
	@Test
	void writeModuleCanvases() {
		new Documenter(modules)
			.writeModuleCanvases();
	}

	/**
	 * Generates all documentation in one go.
	 *
	 * <p>This is a convenience method that generates:
	 * <ul>
	 *   <li>Complete module dependency diagram</li>
	 *   <li>Individual module diagrams</li>
	 *   <li>Module canvases</li>
	 * </ul>
	 *
	 * <p>Perfect for generating a complete documentation set.
	 */
	@Test
	void generateAllDocumentation() {
		new Documenter(modules)
			.writeModulesAsPlantUml()
			.writeIndividualModulesAsPlantUml()
			.writeModuleCanvases();
	}

	/**
	 * Generates component diagrams with custom styling.
	 *
	 * <p>Uses custom diagram options to control the visualization.
	 */
	@Test
	void writeModuleDiagramsWithCustomOptions() {
		new Documenter(modules)
			.writeModulesAsPlantUml(DiagramOptions.defaults()
				.withColorSelector(module -> {
					if (module.getName().equals("owner")) {
						return Optional.of("#LIGHTBLUE");
					} else if (module.getName().equals("vet")) {
						return Optional.of("#LIGHTGREEN");
					} else {
						return Optional.of("#LIGHTGRAY");
					}
				})
			);
	}

}
