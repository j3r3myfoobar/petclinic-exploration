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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the Spring Modulith structure of the application.
 *
 * <p>This test ensures that:
 * <ul>
 *   <li>Module boundaries are properly defined</li>
 *   <li>Modules don't have circular dependencies</li>
 *   <li>Allowed dependencies are respected</li>
 *   <li>Module structure is valid according to Spring Modulith rules</li>
 * </ul>
 *
 * @author Spring Modulith Migration
 */
class ModulithStructureTest {

	private static final Log log = LogFactory.getLog(ModulithStructureTest.class);

	private static final String BASE_PACKAGE = "org.springframework.samples.petclinic";

	ApplicationModules modules = ApplicationModules.of(BASE_PACKAGE);

	/**
	 * Verifies that the overall module structure is valid.
	 * This includes checking for proper package structure, no cycles,
	 * and adherence to defined module boundaries.
	 */
	@Test
	void verifiesModularStructure() {
		modules.verify();
	}

	/**
	 * Prints the module structure to the console for inspection.
	 * Useful for understanding the current module layout.
	 */
	@Test
	void printModuleStructure() {
		log.info("\n=== Spring Modulith Structure ===");
		modules.forEach(module -> {
			log.info("\nModule: " + module.getDisplayName());
			log.info("  Base Package: " + module.getBasePackage());
			log.info("  Named Interfaces: " + module.getNamedInterfaces());

			// Get direct dependencies (what this module depends ON)
			var dependencies = module.getDirectDependencies(modules);
			if (!dependencies.isEmpty()) {
				log.info("  Dependencies:");
				dependencies.stream().forEach(dep ->
					log.info("    -> " + dep.getTargetModule().getDisplayName())
				);
			}

			// Get reverse dependencies (what modules depend ON this module)
			var dependents = modules.stream()
				.filter(m -> !m.equals(module))
				.filter(m -> m.getDirectDependencies(modules).stream()
					.anyMatch(d -> d.getTargetModule().equals(module)))
				.toList();

			if (!dependents.isEmpty()) {
				log.info("  Used by:");
				dependents.forEach(dep ->
					log.info("    <- " + dep.getDisplayName())
				);
			}
		});
		log.info("\n=================================\n");
	}

	/**
	 * Verifies that there are no cycles in module dependencies.
	 * Circular dependencies between modules violate modulith principles.
	 */
	@Test
	void detectsCycles() {
		// This is included in verify() but we test it explicitly for clarity
		modules.verify();
	}

	/**
	 * Generates PlantUML documentation for the module structure.
	 * The output can be found in target/modulith-docs/
	 */
	@Test
	void writeDocumentation() {
		new Documenter(modules)
			.writeModulesAsPlantUml()
			.writeIndividualModulesAsPlantUml();
	}

}
