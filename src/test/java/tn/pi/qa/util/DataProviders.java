package tn.pi.qa.util;

import org.testng.annotations.DataProvider;

public final class DataProviders {

    private DataProviders() {
    }

    @DataProvider(name = "searchScenarios")
    public static Object[][] searchScenarios() {
        return CsvUtil.read("data/search-terms.csv");
    }

    @DataProvider(name = "invalidEmails")
    public static Object[][] invalidEmails() {
        return CsvUtil.read("data/contact-invalid-emails.csv");
    }
}
