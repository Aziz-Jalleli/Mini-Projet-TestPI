package tn.pi.qa.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
    }

    /** Reads a CSV from the test classpath (first line = header) as TestNG data-provider rows. */
    public static Object[][] read(String classpathResource) {
        try (InputStream in = CsvUtil.class.getClassLoader().getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalArgumentException("Jeu de données introuvable: " + classpathResource);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                 CSVParser parser = CSVFormat.DEFAULT.builder()
                         .setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).setTrim(true)
                         .build().parse(reader)) {
                List<Object[]> rows = new ArrayList<>();
                for (CSVRecord rec : parser) {
                    rows.add(rec.stream().toArray(Object[]::new));
                }
                return rows.toArray(new Object[0][]);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Lecture CSV impossible: " + classpathResource, e);
        }
    }
}
