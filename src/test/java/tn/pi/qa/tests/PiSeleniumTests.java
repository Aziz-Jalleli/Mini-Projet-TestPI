package tn.pi.qa.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import tn.pi.qa.base.BaseTest;
import tn.pi.qa.util.DataProviders;

import java.util.List;

/** End-to-end checks for the main visitor journeys on pi.tn. */
public class PiSeleniumTests extends BaseTest {

    @Test(description = "TC01 - Search for a site topic and see matching results",
            dataProvider = "searchScenarios", dataProviderClass = DataProviders.class)
    public void tc01_searchForCourse(String term, String expectedText) {
        search(term);
        assertSearchPage(expectedText);
    }

    @Test(description = "TC02 - Open a course from the search results")
    public void tc02_openCourseFromSearchResults() {
        search("Génie Industriel");
        clickVisiblePartialLink("Génie Industriel");
        assertPage("/course/genie-industriel/", "génie industriel");
    }

    @Test(description = "TC03 - Search with accented French words")
    public void tc03_searchWithAccents() {
        search("ingénieur privée");
        assertSearchPage("ingénieur");
    }

    @Test(description = "TC04 - Show a helpful message when a search has no matches")
    public void tc04_searchWithoutMatches() {
        search("zz-codex-no-match-739184");
        String page = bodyText();
        Assert.assertTrue(driver().getCurrentUrl().contains("s="), "La recherche n'a pas été envoyée");
        Assert.assertTrue(page.contains("aucun") || page.contains("désolé")
                        || page.contains("no results") || page.contains("no posts"),
                "Aucun message d'absence de résultat n'est affiché");
    }

    @Test(description = "TC05 - Run a second search after returning to the home page")
    public void tc05_searchAgainFromHome() {
        search("admission");
        clickHomeLogo();
        waitFor().until(ExpectedConditions.urlToBe(props.baseUrl() + "/"));
        search("Génie Industriel");
        assertSearchPage("génie");
    }

    @Test(description = "TC06 - Open Diplôme National d’Architecte from the Formations menu")
    public void tc06_openArchitectureFromMenu() {
        navigateByMenu("Formations", "Diplôme National d’Architecte");
        assertPage("/course/diplome-national-darchitecte/", "architecte");
    }

    @Test(description = "TC07 - Open Admission from the Formations menu")
    public void tc07_openAdmissionFromMenu() {
        navigateByMenu("Formations", "Admission");
        assertPage("/admission/", "admission");
    }

    @Test(description = "TC08 - Open Cycle préparatoire from the Formations menu")
    public void tc08_openPreparatoryCycleFromMenu() {
        navigateByMenu("Formations", "Cycle préparatoire");
        assertPage("/course/cycle-preparatoire/", "préparatoire");
    }

    @Test(description = "TC09 - Choose Génie Industriel from the nested course menu")
    public void tc09_openGenieIndustrielFromMenu() {
        navigateByMenu("Formations", "Cycle Ingénieur", "Génie Industriel");
        assertPage("/course/genie-industriel/", "génie industriel");
    }

    @Test(description = "TC10 - Choose Mécatronique from the nested course menu")
    public void tc10_openMecatroniqueFromMenu() {
        navigateByMenu("Formations", "Cycle Ingénieur", "Mécatronique");
        assertPage("/course/mecatronique/", "mécatronique");
    }

    @Test(description = "TC11 - Open Licence en Informatique from the Licences menu")
    public void tc11_openLicenceInformatiqueFromMenu() {
        navigateByMenu("Formations", "Licences", "Licence en Informatique");
        assertPage("/course/licence-en-informatique/", "informatique");
    }

    @Test(description = "TC12 - Open Informatique Réseaux et Multimédia from the menu")
    public void tc12_openIrmFromMenu() {
        navigateByMenu("Formations", "Cycle Ingénieur", "Informatique, Réseaux et Multimédia (IRM)");
        assertPage("/course/informatique-reseaux-et-multimedia", "multimédia");
    }

    @Test(description = "TC13 - Open Master en ingénierie du logiciel from the Masters menu")
    public void tc13_openSoftwareEngineeringMasterFromMenu() {
        navigateByMenu("Formations", "Masters Professionnels", "Master en ingénierie du logiciel");
        assertPage("/course/master-genie-logiciel-en-ligne/", "logiciel");
    }

    @Test(description = "TC14 - Choose Master Data Science from the menu")
    public void tc14_openDataScienceFromMenu() {
        navigateByMenu("Formations", "Masters Professionnels", "Master Data Science");
        assertPage("/course/data-science/", "data science");
    }

    @Test(description = "TC15 - Open the student guide from Vie à l’université")
    public void tc15_openStudentGuideFromMenu() {
        navigateByMenu("Vie à l’université", "Guide de l’étudiant");
        assertPage("/guide-de-letudiant/", "étudiant");
    }

    @Test(description = "TC16 - Open the academic calendar from student life navigation")
    public void tc16_openAcademicCalendarFromMenu() {
        navigateByMenu("Vie à l’université", "Calendrier académique");
        assertPage("/calendrier-academique/", "calendrier");
    }

    @Test(description = "TC17 - Open information for international students from the menu")
    public void tc17_openInternationalStudentsFromMenu() {
        navigateByMenu("International", "Étudiants internationaux");
        assertPage("/international/etudiants-internationaux/", "étudiants internationaux");
    }

    @Test(description = "TC18 - Use the mobile menu to open Admission")
    public void tc18_mobileMenuOpensAdmission() {
        driver().manage().window().setSize(new Dimension(390, 844));
        open("/");
        WebElement menuButton = waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#tx-trigger-effects button")));
        menuButton.click();
        clickVisibleLink("Admission");
        assertPage("/admission/", "admission");
    }

    @Test(description = "TC19 - Switch to English and return to the French home page")
    public void tc19_switchLanguageAndReturn() {
        open("/");
        openLanguageDropdown();
        waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("aside.country-selector a.weglot-language-en"))).click();
        waitFor().until(ExpectedConditions.urlContains("/en/"));
        waitFor().until(d -> d.findElement(By.tagName("html")).getAttribute("lang")
                .toLowerCase().startsWith("en"));

        openLanguageDropdown();
        waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("aside.country-selector a.weglot-language-fr"))).click();
        waitFor().until(ExpectedConditions.urlToBe(props.baseUrl() + "/"));
        waitFor().until(d -> d.findElement(By.tagName("html")).getAttribute("lang")
                .toLowerCase().startsWith("fr"));
        Assert.assertTrue(bodyText().contains("formation") || bodyText().contains("admission"),
                "La page d'accueil française ne s'affiche pas après le retour");
    }

    private void openLanguageDropdown() {
        WebElement dropdown = waitFor().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("aside.country-selector")));
        if (!"true".equals(dropdown.getAttribute("aria-expanded"))) {
            dropdown.click();
        }
        waitFor().until(ExpectedConditions.attributeToBe(
                By.cssSelector("aside.country-selector"), "aria-expanded", "true"));
    }

    /** Only invalid data is submitted, so this check cannot send a real contact request. */
    @Test(description = "TC20 - Fill the contact form and reject an invalid e-mail",
            dataProvider = "invalidEmails", dataProviderClass = DataProviders.class)
    public void tc20_contactFormRejectsInvalidEmail(String email) {
        open("/contact/");
        List<WebElement> forms = driver().findElements(By.cssSelector("form.wpcf7-form"));
        if (forms.isEmpty()) throw new SkipException("Le formulaire de contact n'est pas disponible");
        WebElement form = forms.stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new SkipException("Le formulaire de contact n'est pas visible"));
        List<WebElement> emailFields = form.findElements(By.cssSelector("input[type='email']"));
        List<WebElement> nameFields = form.findElements(
                By.cssSelector("input[type='text'], input:not([type])"));
        List<WebElement> phoneFields = form.findElements(
                By.cssSelector("input[type='tel'], input[name*='phone'], input[name*='tel']"));
        List<WebElement> messages = form.findElements(By.cssSelector("textarea"));
        if (emailFields.isEmpty()) throw new SkipException("Le champ e-mail n'est pas disponible");
        if (nameFields.stream().noneMatch(WebElement::isDisplayed))
            throw new SkipException("Le champ Nom & Prénom n'est pas disponible");
        if (phoneFields.stream().noneMatch(WebElement::isDisplayed))
            throw new SkipException("Le champ Téléphone n'est pas disponible");
        if (messages.stream().noneMatch(WebElement::isDisplayed))
            throw new SkipException("Le champ Message n'est pas disponible");

        nameFields.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow().sendKeys("Visiteur Test");
        phoneFields.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow().sendKeys("+21620000000");
        messages.stream().filter(WebElement::isDisplayed).findFirst().orElseThrow()
                .sendKeys("Message de test automatique. Merci de ne pas traiter cette demande.");
        WebElement emailField = emailFields.stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new SkipException("Le champ e-mail n'est pas visible"));
        emailField.clear();
        emailField.sendKeys(email);
        WebElement submit = form.findElements(By.cssSelector("input[type='submit'], button[type='submit']"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new SkipException("Le bouton d'envoi n'est pas disponible"));
        submit.click();
        waitFor().until(d -> "invalid".equals(form.getAttribute("data-status"))
                || "validation_failed".equals(form.getAttribute("data-status")));
        Assert.assertNotEquals(form.getAttribute("data-status"), "sent",
                "Le formulaire ne devrait pas accepter cette adresse");
    }

    private void search(String term) {
        open("/");
        List<WebElement> inputs = driver().findElements(
                By.cssSelector("form.form-search input[name='s']"));
        WebElement input = inputs.stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
        if (input == null) {
            clickVisibleLinkById("head-search-trigger");
            input = waitFor().until(d -> d.findElements(
                            By.cssSelector("#ar-search form.form-search input[name='s']"))
                    .stream().filter(WebElement::isDisplayed).findFirst().orElse(null));
        }
        input.clear();
        input.sendKeys(term, Keys.ENTER);
        waitFor().until(ExpectedConditions.urlContains("s="));
    }

    private void assertSearchPage(String expectedText) {
        Assert.assertTrue(driver().getCurrentUrl().contains("s="), "La page de recherche n'est pas ouverte");
        Assert.assertTrue(bodyText().contains(norm(expectedText)),
                "Les résultats visibles ne contiennent pas « " + expectedText + " »");
    }

    private void navigateByMenu(String... labels) {
        // Follow the desktop menu's actual nested <ul>/<li> structure. Global text
        // lookup can match a hidden duplicate in the off-canvas mobile menu.
        driver().manage().window().setSize(new Dimension(1440, 1000));
        open("/");
        WebElement menu = waitFor().until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#arsim-nav-header-one > ul#menu-main-menu-1")));
        WebElement destination = null;
        for (int index = 0; index < labels.length; index++) {
            final String label = labels[index];
            final WebElement currentMenu = menu;
            destination = waitFor().until(d -> directMenuLink(currentMenu, label));
            if (index < labels.length - 1) {
                final WebElement parentItem = destination.findElement(By.xpath("./ancestor::li[1]"));
                new Actions(driver()).moveToElement(destination)
                        .pause(java.time.Duration.ofMillis(350)).perform();
                menu = waitFor().until(d -> {
                    WebElement submenu = parentItem.findElement(By.cssSelector(":scope > ul.dropdown-menu"));
                    return submenu.isDisplayed() ? submenu : null;
                });
            }
        }
        final WebElement finalDestination = destination;
        waitFor().until(d -> finalDestination.isDisplayed() && finalDestination.isEnabled());
        finalDestination.click();
        waitFor().until(ExpectedConditions.not(ExpectedConditions.urlToBe(props.baseUrl() + "/")));
    }

    private WebElement directMenuLink(WebElement menu, String text) {
        return menu.findElements(By.cssSelector(":scope > li > a")).stream()
                .filter(WebElement::isDisplayed)
                .filter(link -> norm(link.getText()).equals(norm(text)))
                .findFirst().orElse(null);
    }

    private void assertPage(String path, String expectedText) {
        waitFor().until(ExpectedConditions.urlContains(path));
        Assert.assertTrue(bodyText().contains(norm(expectedText)),
                "La page ouverte ne montre pas le contenu attendu « " + expectedText + " »");
    }

    private WebElement visibleLink(String text) {
        return driver().findElements(By.cssSelector(
                        "#arsim-nav-header-one a, #menu-1 a, aside.country-selector a"))
                .stream().filter(WebElement::isDisplayed)
                .filter(link -> norm(link.getText()).equals(norm(text)))
                .findFirst().orElse(null);
    }

    private void clickVisibleLink(String text) {
        WebElement link = waitFor().until(d -> visibleLink(text));
        link.click();
    }

    private void clickVisiblePartialLink(String text) {
        WebElement link = waitFor().until(d -> d.findElements(By.partialLinkText(text)).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null));
        link.click();
    }

    private void clickVisibleLinkById(String id) {
        waitFor().until(ExpectedConditions.elementToBeClickable(By.id(id))).click();
    }

    private void clickHomeLogo() {
        waitFor().until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.navbar-brand"))).click();
    }

    private String bodyText() {
        return norm(driver().findElement(By.tagName("body")).getText());
    }
}
