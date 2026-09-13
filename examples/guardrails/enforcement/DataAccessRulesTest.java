package xyz.briancorbin.pk.enforcement;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * ADR-0007's fitness function (002-downstream-consumers/D-007). The ArchUnit
 * boundary rule: only the persistence adapter package may reach the data-access
 * APIs. Emits SARIF in CI; spectastic ingests it and joins the rule id
 * `only_the_persistence_adapter_touches_data_access_apis` back to D-007. It is a
 * type-graph rule (calls between packages), not SQL content — the residual case
 * (a legit-JDBC file writing `positions`) is caught by the Semgrep rule instead.
 */
@AnalyzeClasses(packages = "xyz.briancorbin.pk")
public final class DataAccessRulesTest {
  @ArchTest
  static final ArchRule only_the_persistence_adapter_touches_data_access_apis =
      noClasses().that().resideOutsideOfPackage("..hex.persistence..")
          .should().accessClassesThat().resideInAPackage("java.sql..")
          .because("ADR-0007: only the persistence adapter may touch the data store (002-downstream-consumers/D-007).");
}
