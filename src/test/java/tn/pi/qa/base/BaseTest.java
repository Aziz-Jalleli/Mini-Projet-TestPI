package tn.pi.qa.base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import tn.pi.qa.PiTestApplication;
import tn.pi.qa.config.TestProperties;
import tn.pi.qa.driver.DriverFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest(classes = PiTestApplication.class)
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

    protected static final Path SCREENSHOT_DIR = Path.of("target", "screenshots");
    protected static final Path LOG_DIR = Path.of("target", "logs");

    @Autowired
    protected TestProperties props;
    @Autowired
    private DriverFactory driverFactory;

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<String> BROWSER = new ThreadLocal<>();

    /** Every driver ever created, so none is left open at the end of the suite. */
    private static final Set<WebDriver> ALL_DRIVERS = ConcurrentHashMap.newKeySet();

    /**
     * Browser selection order:
     * 1) {@code <parameter name="browser">} in the TestNG XML (cross-browser suite)
     * 2) -Dbrowser=... / pi.browser in application.yml
     *
     * The browser is reused by all tests; a new one is created only if none is open
     * or if the requested browser type changed.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(ITestContext ctx) {
        String browser = ctx.getCurrentXmlTest().getAllParameters().get("browser");
        if (browser == null || browser.isBlank()) {
            browser = props.browser();
        }

        if (DRIVER.get() != null && !browser.equals(BROWSER.get())) {
            quitDriver(); // cross-browser suite moved on to another browser
        }
        if (DRIVER.get() == null) {
            WebDriver created = driverFactory.create(browser);
            DRIVER.set(created);
            ALL_DRIVERS.add(created);
        }
        BROWSER.set(browser);
        resetBrowserState();
    }

    /** Gives every test a clean start inside the same window. */
    private void resetBrowserState() {
        try {
            driver().manage().deleteAllCookies(); // e.g. Weglot language choice
            driver().manage().window().setSize(new Dimension(1440, 1000)); // TC18 shrinks it to mobile size
        } catch (Exception ignored) {
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        result.setAttribute("browser", BROWSER.get());
        if (result.getStatus() == ITestResult.FAILURE && DRIVER.get() != null) {
            captureFailure(result);
        }
        // browser intentionally stays open for the next test
    }

    /** Closes the browser once all tests of the <test> block (or class run from the IDE) are done. */
    @AfterTest(alwaysRun = true)
    public void closeBrowserAfterTests() {
        quitDriver();
    }

    private void quitDriver() {
        WebDriver d = DRIVER.get();
        if (d != null) {
            try {
                d.quit();
            } catch (Exception ignored) {
            }
            ALL_DRIVERS.remove(d);
        }
        DRIVER.remove();
        BROWSER.remove();
    }

    /** IDE runners may skip suite callbacks when launching a test class or method directly. */
    @AfterClass(alwaysRun = true)
    public void generateExecutionReportAfterClass(ITestContext context) {
        tn.pi.qa.report.HtmlReporter.writeTestContextReport(context);
    }

    /** Suite hook keeps Maven/TestNG suite runs covered; also closes any browser still open. */
    @AfterSuite(alwaysRun = true)
    public void generateExecutionReport(ITestContext context) {
        ALL_DRIVERS.forEach(d -> {
            try {
                d.quit();
            } catch (Exception ignored) {
            }
        });
        ALL_DRIVERS.clear();
        tn.pi.qa.report.HtmlReporter.writeSuiteReport(context.getSuite());
    }

    // ---------------------------------------------------------------- helpers

    protected WebDriver driver() {
        return DRIVER.get();
    }

    protected String browserName() {
        return BROWSER.get();
    }

    protected WebDriverWait waitFor() {
        return new WebDriverWait(driver(), Duration.ofSeconds(props.timeoutSeconds()));
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver()).executeScript(script, args);
    }

    /** Opens a path (or absolute URL), waits for document.readyState=complete, removes popups. */
    protected void open(String pathOrUrl) {
        String url = pathOrUrl.startsWith("http") ? pathOrUrl : props.baseUrl() + pathOrUrl;
        driver().get(url);
        waitFor().until(d -> "complete".equals(js("return document.readyState")));
        dismissOverlays();
        pause();
    }

    /** Visual slow motion: lets you follow what the browser does (pi.slow-motion-ms, 0 = off). */
    protected void pause() {
        if (props.slowMotionMs() > 0) {
            try {
                Thread.sleep(props.slowMotionMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** The site uses Popup Maker; popups can intercept clicks. */
    protected void dismissOverlays() {
        js("document.querySelectorAll('.pum-overlay').forEach(function(e){e.remove();});");
    }

    protected void jsClick(WebElement element) {
        dismissOverlays();
        js("arguments[0].click();", element);
        pause();
    }

    protected String text(WebElement element) {
        String t = element.getAttribute("textContent");
        return t == null ? "" : t;
    }

    /** Lower-case, typographic apostrophe -> ', collapsed whitespace (incl. NBSP). */
    protected String norm(String s) {
        return s.replace('\u2019', '\'').replaceAll("[\\s\\u00a0]+", " ").trim().toLowerCase();
    }

    protected void assertNotErrorPage() {
        String title = norm(driver().getTitle());
        Assert.assertFalse(title.contains("404") || title.contains("introuvable") || title.contains("not found"),
                "Page d'erreur détectée (titre: " + driver().getTitle() + ") - " + driver().getCurrentUrl());
        String body = text(driver().findElement(By.tagName("body"))).trim();
        Assert.assertFalse(body.isEmpty(), "Page vide: " + driver().getCurrentUrl());
    }

    private void captureFailure(ITestResult r) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        String base = (BROWSER.get() + "_" + r.getMethod().getMethodName() + "_" + stamp)
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        try {
            Files.createDirectories(SCREENSHOT_DIR);
            Files.createDirectories(SCREENSHOT_DIR.resolve("raw"));
            Files.createDirectories(LOG_DIR);

            byte[] png = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.BYTES);
            Files.write(SCREENSHOT_DIR.resolve("raw").resolve(base + ".png"), png);
            String reason = failureSummary(r);
            Files.write(SCREENSHOT_DIR.resolve(base + ".png"), annotateFailure(png,
                    r.getMethod().getMethodName(), reason));
            r.setAttribute("screenshot", base + ".png");
            r.setAttribute("rawScreenshot", "raw/" + base + ".png");
            r.setAttribute("failureLog", base + ".txt");

            StringWriter sw = new StringWriter();
            sw.write("Test    : " + r.getMethod().getMethodName() + "\n");
            sw.write("Browser : " + BROWSER.get() + "\n");
            sw.write("URL     : " + driver().getCurrentUrl() + "\n");
            sw.write("Reason  : " + reason + "\n\n");
            if (r.getThrowable() != null) {
                r.getThrowable().printStackTrace(new PrintWriter(sw));
            }
            Files.writeString(LOG_DIR.resolve(base + ".txt"), sw.toString());
        } catch (Exception e) {
            System.err.println("Impossible de sauvegarder les artefacts d'échec: " + e.getMessage());
        }
    }

    private String failureSummary(ITestResult result) {
        Throwable failure = result.getThrowable();
        if (failure == null) return "TestNG marked this invocation as failed without an exception message.";
        String message = failure.getMessage();
        if (message == null || message.isBlank()) message = "No additional failure message was provided.";
        return failure.getClass().getSimpleName() + ": "
                + message.replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s{2,}", " ");
    }

    /** Adds a readable failure banner while preserving the original browser image underneath. */
    private byte[] annotateFailure(byte[] originalPng, String testName, String reason) throws IOException {
        BufferedImage original = javax.imageio.ImageIO.read(new ByteArrayInputStream(originalPng));
        if (original == null) return originalPng;

        int bannerHeight = 140;
        BufferedImage annotated = new BufferedImage(original.getWidth(), original.getHeight() + bannerHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = annotated.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(145, 24, 38));
        g.fillRect(0, 0, annotated.getWidth(), bannerHeight);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("TEST FAILED  |  " + testName, 20, 32);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString("Failure reason:", 20, 59);
        g.setFont(new Font("Arial", Font.PLAIN, 15));
        drawWrapped(g, reason, 20, 83, annotated.getWidth() - 40, 19, 3);
        g.drawImage(original, 0, bannerHeight, null);
        g.setColor(new Color(145, 24, 38));
        g.setStroke(new BasicStroke(5));
        g.drawRect(2, bannerHeight + 2, original.getWidth() - 5, original.getHeight() - 5);
        g.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(annotated, "png", output);
        return output.toByteArray();
    }

    private void drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth,
                             int lineHeight, int maxLines) {
        FontMetrics metrics = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        int lines = 0;
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (metrics.stringWidth(candidate) > maxWidth && !line.isEmpty()) {
                g.drawString(line.toString(), x, y + lines * lineHeight);
                lines++;
                line.setLength(0);
                line.append(word);
                if (lines == maxLines - 1) {
                    String tail = " …";
                    while (!line.isEmpty() && metrics.stringWidth(line + tail) > maxWidth) {
                        line.setLength(line.length() - 1);
                    }
                    line.append(tail);
                    break;
                }
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        g.drawString(line.toString(), x, y + lines * lineHeight);
    }
}