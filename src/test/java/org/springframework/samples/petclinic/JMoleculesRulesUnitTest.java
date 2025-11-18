package org.springframework.samples.petclinic;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

@AnalyzeClasses(packages = "org.springframework.samples.petclinic")
public class JMoleculesRulesUnitTest {

	@ArchTest
	ArchRule dddRules = JMoleculesDddRules.all()
		.ignoreDependency(
			// Ignore petType field - it's a transient field used only for form binding (infrastructure concern)
			// The actual domain relationship is maintained via the Association<PetType, PetTypeId> type field
			(JavaField origin, JavaField target) ->
				origin.getName().equals("petType") &&
				origin.getOwner().isEquivalentTo(org.springframework.samples.petclinic.owner.Pet.class)
		);

	@ArchTest
	ArchRule layeredArchitecture = JMoleculesArchitectureRules.ensureLayering();

}
