package io.realm.processor.nameformatter;

public class LowerCaseWithSeparatorConverter implements NameConverter {

    public static final LowerCaseWithSeparatorConverter INSTANCE_UNDERSCORE = new LowerCaseWithSeparatorConverter('_');

    private final WordTokenizer tokenizer = new WordTokenizer();
    private final char separator;

    public LowerCaseWithSeparatorConverter(char separator) {
        this.separator = separator;
    }

    @Override
    public String convert(String name) {
        String[] words = tokenizer.split(name);
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            output.append(word);
            if (i < words.length - 1) {
                output.append(separator);
            }
        }

        return output.toString();
    }
}
