package tn.pi.qa.report;

import org.testng.*;
import org.testng.xml.XmlSuite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/** Generates target/reports/execution-report.html (Passed / Failed / Skipped, per browser). */
public class HtmlReporter implements IReporter {

    private record Row(String browser, String name, String desc, String params,
                       long ms, String status, String msg, String shot, String rawShot, String log) {
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        writeReport(suites);
    }

    /** Shared entry point used by the inherited @AfterSuite fallback as well as the XML listener. */
    public static void writeSuiteReport(ISuite suite) {
        new HtmlReporter().writeReport(List.of(suite));
    }

    /** Build an IntelliJ class or method run from its completed TestNG context. */
    public static void writeTestContextReport(ITestContext context) {
        new HtmlReporter().writeContexts(List.of(context));
    }

    private void writeReport(List<ISuite> suites) {
        List<ITestContext> contexts = new ArrayList<>();
        for (ISuite suite : suites) {
            for (ISuiteResult result : suite.getResults().values()) {
                contexts.add(result.getTestContext());
            }
        }
        writeContexts(contexts);
    }

    private void writeContexts(List<ITestContext> contexts) {
        List<Row> rows = new ArrayList<>();
        for (ITestContext context : contexts) {
            collect(rows, context.getPassedTests(), "PASSED", context);
            collect(rows, context.getFailedTests(), "FAILED", context);
            collect(rows, context.getSkippedTests(), "SKIPPED", context);
        }
        rows.sort(Comparator.comparing(Row::browser).thenComparing(Row::name).thenComparing(Row::params));

        long passed = rows.stream().filter(r -> r.status.equals("PASSED")).count();
        long failed = rows.stream().filter(r -> r.status.equals("FAILED")).count();
        long skipped = rows.stream().filter(r -> r.status.equals("SKIPPED")).count();

        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html><html lang='fr'><head><meta charset='UTF-8'>")
                .append("<title>Rapport d'exécution - pi.tn</title><style>")
                .append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#222}")
                .append("table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:6px 8px;font-size:13px;vertical-align:top}")
                .append("th{background:#b0232a;color:#fff;text-align:left}")
                .append(".PASSED{background:#e6f4ea}.FAILED{background:#fce8e6}.SKIPPED{background:#fff4e5}")
                .append(".kpi span{display:inline-block;margin-right:18px;font-weight:600}")
                .append("</style></head><body>");
        h.append("<h1>Rapport d'exécution - pi.tn</h1>")
                .append("<p>Généré le ").append(LocalDateTime.now().withNano(0)).append("</p>")
                .append("<p class='kpi'><span>Total: ").append(rows.size()).append("</span>")
                .append("<span style='color:#188038'>Passed: ").append(passed).append("</span>")
                .append("<span style='color:#d93025'>Failed: ").append(failed).append("</span>")
                .append("<span style='color:#e37400'>Skipped: ").append(skipped).append("</span></p>");
        h.append("<table><tr><th>Navigateur</th><th>Test</th><th>Description</th><th>Données</th>")
                .append("<th>Durée (ms)</th><th>Statut</th><th>Détails</th></tr>");
        for (Row r : rows) {
            h.append("<tr class='").append(r.status).append("'><td>").append(esc(r.browser))
                    .append("</td><td>").append(esc(r.name))
                    .append("</td><td>").append(esc(r.desc))
                    .append("</td><td>").append(esc(r.params))
                    .append("</td><td>").append(r.ms)
                    .append("</td><td><b>").append(r.status).append("</b></td><td>").append(esc(r.msg));
            if (r.shot != null) {
                h.append("<br><a href='../screenshots/").append(esc(r.shot))
                        .append("'>Capture annotée (cause affichée en haut)</a>");
                if (r.rawShot != null) {
                    h.append("<br><a href='../screenshots/").append(esc(r.rawShot))
                            .append("'>Capture originale</a>");
                }
                if (r.log != null) {
                    h.append("<br><a href='../logs/").append(esc(r.log))
                            .append("'>Trace complète</a>");
                }
            }
            h.append("</td></tr>");
        }
        h.append("</table></body></html>");

        try {
            Path out = Path.of("target", "reports", "execution-report.html");
            Files.createDirectories(out.getParent());
            Files.writeString(out, h.toString());
            System.out.println("Rapport HTML: " + out.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Rapport HTML non généré: " + e.getMessage());
        }
    }

    private void collect(List<Row> rows, IResultMap map, String status, ITestContext c) {
        for (ITestResult r : map.getAllResults()) {
            Object b = r.getAttribute("browser");
            Object s = r.getAttribute("screenshot");
            Object raw = r.getAttribute("rawScreenshot");
            Object log = r.getAttribute("failureLog");
            String msg = "";
            if (r.getThrowable() != null) {
                String m = String.valueOf(r.getThrowable().getMessage());
                msg = r.getThrowable().getClass().getSimpleName() + ": "
                        + (m.length() > 400 ? m.substring(0, 400) + "..." : m);
            }
            String params = r.getParameters() != null && r.getParameters().length > 0
                    ? Arrays.toString(r.getParameters()) : "";
            String desc = r.getMethod().getDescription() == null ? "" : r.getMethod().getDescription();
            rows.add(new Row(b != null ? b.toString() : c.getName(), r.getMethod().getMethodName(), desc, params,
                    r.getEndMillis() - r.getStartMillis(), status, msg,
                    s != null ? s.toString() : null, raw != null ? raw.toString() : null,
                    log != null ? log.toString() : null));
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("'", "&#39;");
    }
}
