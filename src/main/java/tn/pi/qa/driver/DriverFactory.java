package tn.pi.qa.driver;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Component;
import tn.pi.qa.config.TestProperties;

import java.time.Duration;
import java.util.Locale;

/**
 * Builds a WebDriver for the requested browser. Driver binaries are resolved
 * automatically by Selenium Manager (bundled with Selenium 4.6+), so no
 * manual chromedriver/geckodriver/msedgedriver installation is needed.
 */
@Component
public class DriverFactory {

    private final TestProperties props;

    public DriverFactory(TestProperties props) {
        this.props = props;
    }

    public WebDriver create(String requestedBrowser) {
        String browser = (requestedBrowser == null || requestedBrowser.isBlank())
                ? props.browser() : requestedBrowser;
        browser = browser.trim().toLowerCase(Locale.ROOT);

        WebDriver driver;
        switch (browser) {
            case "chrome" -> {
                ChromeOptions o = new ChromeOptions();
                if (props.headless()) o.addArguments("--headless=new");
                o.addArguments("--window-size=1920,1080", "--disable-notifications",
                        "--remote-allow-origins=*", "--lang=fr-FR");
                driver = new ChromeDriver(o);
            }
            case "firefox" -> {
                FirefoxOptions o = new FirefoxOptions();
                if (props.headless()) o.addArguments("-headless");
                o.addArguments("--width=1920", "--height=1080");
                o.addPreference("intl.accept_languages", "fr-FR");
                driver = new FirefoxDriver(o);
            }
            case "edge" -> {
                EdgeOptions o = new EdgeOptions();
                if (props.headless()) o.addArguments("--headless=new");
                o.addArguments("--window-size=1920,1080", "--disable-notifications",
                        "--remote-allow-origins=*", "--lang=fr-FR");
                driver = new EdgeDriver(o);
            }
            default -> throw new IllegalArgumentException(
                    "Navigateur non supporté: '" + browser + "' (chrome | firefox | edge)");
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        if (props.headless()) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }
        return driver;
    }
}
