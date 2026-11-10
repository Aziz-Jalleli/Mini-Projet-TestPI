# PI.TN functional browser tests

This Maven project uses Java 17, Selenium WebDriver, TestNG and Spring Boot to exercise visitor journeys on https://pi.tn. All 20 scenarios are in one class: `src/test/java/tn/pi/qa/tests/PiSeleniumTests.java`.

## What the tests do

- Search for course and admission information, open a result, handle French accents, and show the no-results message for an unknown term.
- Navigate through the site menus to admissions, engineering, licence, master, student-life and international pages.
- Use the mobile menu, switch between English and French, and check that invalid contact e-mail submissions are rejected.

These tests check actions and outcomes a visitor can see. They do not inspect SEO metadata, page source, load-time metrics, social-link markup or raw internal links.

## Requirements and run commands

- JDK 17 or later, Maven 3.9 or later, and Chrome, Firefox or Edge.
- Internet access for pi.tn and Selenium Manager's first driver setup.

Run from this folder:

```text
mvn clean test
```

In IntelliJ IDEA, open `work/fresh/pi-selenium-tests` as the Maven project and run `PiSeleniumTests` (or an individual test) with the TestNG runner. Set the run configuration's **Working directory** to the project folder (`pi-selenium-tests`). After the class finishes, open `target/reports/execution-report.html` in that folder. The test runner also prints the report's absolute path as `Rapport HTML: ...` in the Run console.

Choose Firefox with `mvn clean test -Dbrowser=firefox`. Use a headless browser with `mvn clean test -Dheadless=true -Dslowmo=0`. Run Chrome, Firefox and Edge with `mvn clean test -Pcross-browser`.

## Reports and failure evidence

- HTML execution report: `target/reports/execution-report.html` (written after the test class in IntelliJ and at suite end for Maven/TestNG XML runs)
- Surefire reports: `target/surefire-reports/`
- Annotated PNG screenshots on failed tests: `target/screenshots/` (the top banner names the failed test and shows the exception summary)
- Original, unmodified browser screenshots: `target/screenshots/raw/`
- Exception traces and failing URL: `target/logs/`

`BaseTest.tearDown()` writes an annotated screenshot, the original screenshot, and a trace when an invocation fails. The HTML report links to the annotated image, original image, and full trace. CSV-driven searches and invalid e-mail examples can produce more invocations than the 20 documented scenarios.

## Data and safety

`src/test/resources/data/search-terms.csv` supplies search queries and visible result words. `contact-invalid-emails.csv` supplies only invalid values. The contact test never submits valid details, so it does not send a real enquiry.

See `docs/test-cases.csv` and the Excel cahier de tests for the scenario steps and expected user-visible outcomes.
