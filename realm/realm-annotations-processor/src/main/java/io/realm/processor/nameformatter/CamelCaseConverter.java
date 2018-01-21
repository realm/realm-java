package io.realm.processor.nameformatter;

public class CamelCaseConverter implements NameConverter {

    public static final CamelCaseConverter INSTANCE = new CamelCaseConverter();

    private final WordTokenizer tokenizer = new WordTokenizer();

    @Override
    public String convert(String name) {
        String[] words = tokenizer.split(name);
        StringBuilder output = new StringBuilder();
        boolean firstWordEmitted = false;
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (firstWordEmitted) {
                int codepoint = word.codePointAt(0);
                output.appendCodePoint(Character.toUpperCase(codepoint));
                output.append(word.substring(Character.charCount(codepoint)));
            } else {
                output.append(word);
                firstWordEmitted = true;
            }
        }

        return output.toString();
    }
}
