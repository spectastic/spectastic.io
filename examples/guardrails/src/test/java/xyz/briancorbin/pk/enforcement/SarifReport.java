package xyz.briancorbin.pk.enforcement;

import com.tngtech.archunit.lang.EvaluationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ArchUnit → SARIF 2.1.0. ArchUnit ships no SARIF emitter and its {@code @ArchTest} runner offers
 * no hook to intercept a result, so the fitness-function tests evaluate their rule as a plain
 * {@code @Test} and hand the {@link EvaluationResult} here. One SARIF result per failure detail;
 * {@code ruleId} is the id a {@code <spec-rule>} names, {@code uri} is the source path of the
 * offending class under {@code src/main/java}, {@code startLine} is the line the detail cites. This
 * is exactly the shape {@code spectastic verdict --enforcer-output} reads.
 */
final class SarifReport {

  /** {@code <pkg.Outer$Inner.method(...)>} — the first fully-qualified class name in a detail. */
  private static final Pattern CLASS_REF = Pattern.compile("<([a-z][\\w.]*\\.[A-Z]\\w*)");

  /** {@code (File.java:20)} — the source position ArchUnit appends to each detail. */
  private static final Pattern SOURCE_LINE = Pattern.compile("\\(\\w+\\.java:(\\d+)\\)");

  private SarifReport() {}

  static void write(Path out, String ruleId, EvaluationResult result) throws IOException {
    List<String> results = new ArrayList<>();
    if (result.hasViolation()) {
      for (String detail : result.getFailureReport().getDetails()) {
        results.add(resultJson(ruleId, detail));
      }
    }
    String sarif =
        """
        {
          "version": "2.1.0",
          "$schema": "https://json.schemastore.org/sarif-2.1.0.json",
          "runs": [
            {
              "tool": { "driver": { "name": "ArchUnit", "rules": [{ "id": %s }] } },
              "results": [%s]
            }
          ]
        }
        """
            .formatted(quote(ruleId), String.join(",", results));
    Files.createDirectories(out.getParent());
    Files.writeString(out, sarif);
  }

  private static String resultJson(String ruleId, String detail) {
    Matcher cls = CLASS_REF.matcher(detail);
    String uri =
        cls.find()
            ? "src/main/java/" + cls.group(1).replace('.', '/').replaceAll("\\$.*", "") + ".java"
            : "(unknown)";
    Matcher line = SOURCE_LINE.matcher(detail);
    String region = line.find() ? ", \"region\": { \"startLine\": " + line.group(1) + " }" : "";
    return """

                {
                  "ruleId": %s,
                  "level": "error",
                  "message": { "text": %s },
                  "locations": [
                    { "physicalLocation": { "artifactLocation": { "uri": %s }%s } }
                  ]
                }"""
        .formatted(quote(ruleId), quote(detail), quote(uri), region);
  }

  private static String quote(String s) {
    return '"'
        + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")
        + '"';
  }
}
