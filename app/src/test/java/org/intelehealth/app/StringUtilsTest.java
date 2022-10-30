package org.intelehealth.app;

import org.intelehealth.app.utilities.StringUtils;
import org.junit.Test;

import java.util.Objects;

public class StringUtilsTest {
    @Test
    public void test_translateStringAs() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "as");
        assert Objects.equals(translatedString, "দেওবাৰ");
    }

    @Test
    public void test_translateStringBn() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "bn");
        assert Objects.equals(translatedString, "রবিবার");
    }

    @Test
    public void test_translateStringGu() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "gu");
        assert Objects.equals(translatedString, "રવિવાર");
    }

    @Test
    public void test_translateStringHi() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "hi");
        assert Objects.equals(translatedString, "रविवार");
    }

    @Test
    public void test_translateStringKn() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "kn");
        assert Objects.equals(translatedString, "ಭಾನುವಾರ");
    }

    @Test
    public void test_translateStringMl() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "ml");
        assert Objects.equals(translatedString, "ഞായറാഴ്ച");
    }

    @Test
    public void test_translateStringMr() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "mr");
        assert Objects.equals(translatedString, "रविवार");
    }

    @Test
    public void test_translateStringOr() {
        String translatedString = StringUtils.getTranslatedDays("Sunday", "or");
        assert Objects.equals(translatedString, "ରବିବାର");
    }
}
