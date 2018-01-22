package io.realm.processor.nameformatter;

/**
 * Converter that converts input to "PascalCase".
 */
public class PascalCaseConverter implements NameConverter {

    public static final PascalCaseConverter INSTANCE = new PascalCaseConverter();

    private final WordTokenizer tokenizer = new WordTokenizer();

    @Override
    public String convert(String name) {
        String[] words = tokenizer.split(name);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            int codepoint = word.codePointAt(0);
            output.appendCodePoint(Character.toUpperCase(codepoint));
            output.append(word.substring(Character.charCount(codepoint)));
        }

        return output.toString();
    }
}
