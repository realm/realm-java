package io.realm.processor;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import io.realm.processor.nameconverter.CamelCaseConverter;
import io.realm.processor.nameconverter.LowerCaseWithSeparatorConverter;
import io.realm.processor.nameconverter.NameConverter;
import io.realm.processor.nameconverter.PascalCaseConverter;

import static org.junit.Assert.assertEquals;

public class NameConverterTests {

    @Test
    public void camelCase() {
        NameConverter converter = new CamelCaseConverter();
        Map<String, String> values = new LinkedHashMap<String, String>() {{
            // <JavaName, InternalName>
            put("camelCase", "camelCase");
            put("PascalCase", "pascalCase");
            put("mHungarianNotation", "hungarianNotation");
            put("_PascalCaseWithStartingSeparator", "pascalCaseWithStartingSeparator");
            put("_camelCaseWithStartingSeparator", "camelCaseWithStartingSeparator");
            put("ALL_CAPS_WITH_SEPARATOR", "allCapsWithSeparator");
            put("ALLCAPS", "allcaps");
            put("_ALL_CAPS_WITH_STARTING_SEPARATOR", "allCapsWithStartingSeparator");
            put("alllower", "alllower");
            put("all_lower_with_separator", "allLowerWithSeparator");

            // $ Separator
            put("$generatedNames", "generatedNames");
            put("generatedNames$", "generatedNames");
            put("generated$Names", "generatedNames");

            // Non-ascii chars
            put("πPi", "πPi");
            put("NonAsciiÆøÅ", "nonAsciiÆøÅ");

            // Multiple upper case letters
            put("HTMLFile", "htmlFile");
            put("aHTMLFile", "aHtmlFile");
            put("_HTMLFile", "htmlFile");

            // Emojiis are neither upper case nor lower case (Smiley)
            put("\uD83D\uDE01", "\uD83D\uDE01");
            put("m\uD83D\uDE01", "m\uD83D\uDE01");
            put("M\uD83D\uDE01", "m\uD83D\uDE01");
            put("\uD83D\uDE01Smiley", "\uD83D\uDE01smiley");
            put("_\uD83D\uDE01smiley", "\uD83D\uDE01smiley");
        }};

        for (Map.Entry<String, String> entry : values.entrySet()) {
            assertEquals(entry.getValue(), converter.convert(entry.getKey()));
        }
    }

    @Test
    public void pascalCase() {
        NameConverter converter = new PascalCaseConverter();
        Map<String, String> values = new LinkedHashMap<String, String>() {{
            // <JavaName, InternalName>
            put("camelCase", "CamelCase");
            put("PascalCase", "PascalCase");
            put("mHungarianNotation", "HungarianNotation");
            put("_PascalCaseWithStartingSeparator", "PascalCaseWithStartingSeparator");
            put("_camelCaseWithStartingSeparator", "CamelCaseWithStartingSeparator");
            put("ALL_CAPS_WITH_SEPARATOR", "AllCapsWithSeparator");
            put("ALLCAPS", "Allcaps");
            put("_ALL_CAPS_WITH_STARTING_SEPARATOR", "AllCapsWithStartingSeparator");
            put("alllower", "Alllower");
            put("all_lower_with_separator", "AllLowerWithSeparator");

            // $ Separator
            put("$generatedNames", "GeneratedNames");
            put("generatedNames$", "GeneratedNames");
            put("generated$Names", "GeneratedNames");

            // Non-ascii chars
            put("πPi", "ΠPi");
            put("NonAsciiÆøÅ", "NonAsciiÆøÅ");

            // Multiple upper case letters
            put("HTMLFile", "HtmlFile");
            put("aHTMLFile", "AHtmlFile");
            put("_HTMLFile", "HtmlFile");

            // Emojiis are neither upper case nor lower case (Smiley)
            put("\uD83D\uDE01", "\uD83D\uDE01");
            put("m\uD83D\uDE01", "M\uD83D\uDE01");
            put("M\uD83D\uDE01", "M\uD83D\uDE01");
            put("\uD83D\uDE01Smiley", "\uD83D\uDE01smiley");
            put("_\uD83D\uDE01smiley", "\uD83D\uDE01smiley");
        }};

        for (Map.Entry<String, String> entry : values.entrySet()) {
            assertEquals(entry.getValue(), converter.convert(entry.getKey()));
        }
    }

    @Test
    public void lowerCaseWithUnderscore() {
        NameConverter converter = new LowerCaseWithSeparatorConverter('_');
        Map<String, String> values = new LinkedHashMap<String, String>() {{
            // <JavaName, InternalName>
            // Common naming schemes using ASCII chars
            put("camelCase", "camel_case");
            put("PascalCase", "pascal_case");
            put("mHungarianNotation", "hungarian_notation");
            put("_mHungarianNotation", "hungarian_notation");
            put("mHungarian_mNotation", "hungarian_m_notation");
            put("_PascalCaseWithStartingSeparator", "pascal_case_with_starting_separator");
            put("_camelCaseWithStartingSeparator", "camel_case_with_starting_separator");
            put("ALL_CAPS_WITH_SEPARATOR", "all_caps_with_separator");
            put("ALLCAPS", "allcaps");
            put("_ALL_CAPS_WITH_STARTING_SEPARATOR", "all_caps_with_starting_separator");
            put("alllower", "alllower");
            put("all_lower_with_separator", "all_lower_with_separator");

            // $ Separator
            put("$generatedNames", "generated_names");
            put("generatedNames$", "generated_names");
            put("generated$Names", "generated_names");

            // Non-ascii chars
            put("πPi", "π_pi");
            put("NonAsciiÆøÅ", "non_ascii_æø_å");

            // Multiple upper case letters
            put("HTMLFile", "html_file");
            put("aHTMLFile", "a_html_file");
            put("_HTMLFile", "html_file");

            // Emojiis are neither upper case nor lower case (Smiley)
            put("\uD83D\uDE01", "\uD83D\uDE01");
            put("m\uD83D\uDE01", "m\uD83D\uDE01");
            put("M\uD83D\uDE01", "m\uD83D\uDE01");
            put("\uD83D\uDE01Smiley", "\uD83D\uDE01smiley");
            put("_\uD83D\uDE01smiley", "\uD83D\uDE01smiley");
        }};

        for (Map.Entry<String, String> entry : values.entrySet()) {
            assertEquals(entry.getValue(), converter.convert(entry.getKey()));
        }
    }

}
