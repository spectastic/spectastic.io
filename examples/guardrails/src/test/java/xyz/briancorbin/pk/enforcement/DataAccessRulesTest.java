package xyz.briancorbin.pk.enforcement;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * ADR-0007's fitness function (002-downstream-consumers/D-007). The ArchUnit boundary rule: only
 * the persistence adapter package may reach the data-access APIs. Its result is always written as
 * SARIF to {@code build/reports/archunit/archunit-results.sarif} — clean or not — so CI can hand it
 * to {@code spectastic verdict --enforcer-output}, which joins the rule id {@code
 * only_the_persistence_adapter_touches_data_access_apis} back to D-007. It is a type-graph rule
 * (calls between packages), not SQL content — the residual case (a legit-JDBC file writing {@code
 * positions}) is caught by the Semgrep rule instead.
 *
 * <p>Production classes only: the test fixtures (the grant backstop test) open H2 themselves.
 */
class DataAccessRulesTest {

  static final String RULE_ID = "only_the_persistence_adapter_touches_data_access_apis";

  static final ArchRule only_the_persistence_adapter_touches_data_access_apis =
      noClasses()
          .that()
          .resideOutsideOfPackage("..hex.persistence..")
          .should()
          .accessClassesThat()
          .resideInAPackage("java.sql..")
          .because(
              "ADR-0007: only the persistence adapter may touch the data store"
                  + " (002-downstream-consumers/D-007).");

  @Test
  void onlyThePersistenceAdapterTouchesDataAccessApis() throws IOException {
    JavaClasses production =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("xyz.briancorbin.pk");

    EvaluationResult result =
        only_the_persistence_adapter_touches_data_access_apis.evaluate(production);
    SarifReport.write(Path.of("build/reports/archunit/archunit-results.sarif"), RULE_ID, result);

    assertFalse(
        result.hasViolation(), () -> String.join("\n", result.getFailureReport().getDetails()));
  }
}
