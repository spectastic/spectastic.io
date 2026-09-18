package xyz.briancorbin.pk.enforcement;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

/**
 * SC-002 of 001-position-core: the domain core depends on the JDK only (NFR-001) and the ports are
 * the only way in (FR-003). Plain {@code @Test} methods that evaluate a rule (design D-003), so a
 * result can later be written out beside the assertion.
 */
class CoreRulesTest {

  private static final JavaClasses PRODUCTION =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages("xyz.briancorbin.pk");

  private static void assertHolds(ArchRule rule) {
    EvaluationResult result = rule.evaluate(PRODUCTION);
    assertFalse(
        result.hasViolation(), () -> String.join("\n", result.getFailureReport().getDetails()));
  }

  @Test
  void theCoreDependsOnTheJdkAndItselfOnly() {
    assertHolds(
        classes()
            .that()
            .resideInAPackage("..hex.core..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage("java..", "..hex.core..")
            .because("NFR-001: the domain imports the JDK only"));
  }

  @Test
  void theCoreAndTheUseCasesNeverTouchJdbc() {
    assertHolds(
        noClasses()
            .that()
            .resideInAnyPackage("..hex.core..", "..hex.app..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.sql..")
            .because("NFR-001: persistence is an adapter's concern, never the domain's"));
  }

  @Test
  void theCoreKnowsNothingOfTheUseCasesOrAdapters() {
    assertHolds(
        noClasses()
            .that()
            .resideInAPackage("..hex.core..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..hex.app..", "..hex.persistence..")
            .because("FR-003: dependencies point inward; the domain is reached through ports"));
  }

  @Test
  void referenceDataDependsOnNothingInTheHexagon() {
    assertHolds(
        noClasses()
            .that()
            .resideInAPackage("..refdata..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..hex..")
            .because("005 NFR-001: reference data is its own module, separate from positions"));
  }

  @Test
  void theHexagonDependsOnNothingInReferenceData() {
    assertHolds(
        noClasses()
            .that()
            .resideInAPackage("..hex..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..refdata..")
            .because("005 NFR-001: positions never reach into reference data"));
  }
}
