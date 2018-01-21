package io.realm.processor.nameformatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Segments a Java variable name into component words.
 *
 * Java variable names must follow the rules described in:
 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8
 *
 * In this implementation we treat word separators as any of the following:
 * <ol>
 *     <li>
 *         Any time a {@code _} or {@code $} is encountered.
 *         Example is "_FooBar" or "_Foo$Bar" which both becomes "Foo" and "Bar".
 *     </li>
 *     <li>
 *         Any time your switch from a lower case character to a upper case character as
 *         identified by a Character.isUpperCase(codepoint)` and `Character.isLowerCase(codepoint)`.
 *         Example is "FooBar" which becomes "Foo" and "Bar"
 *     </li>
 *     <li>
 *         Any time your switch from more than one uppercase character to a lower case one. As
 *         identified by `Character.isUpperCase(codepoint)` and `Character.isLowerCase(codepoint)`.
 *         Example is "FOOBar" which becomes "FOO" and "Bar.
 *     </li>
 *     <li>
 *         Some characters like emojiis are neither uppercase or lowercase characters, so they will
 *         not trigger any of the above rules.
 *
 *     </li>
 *     <li>
 *         Hungarian notation, i.e. Strings starting with lowercase "m" followed by uppercase letter
 *         is stripped.
 *     </li>
 * </ol>
 */
public class WordTokenizer {

    /**
     * Segments a string into words as described above
     */
    String[] split(String str) {
        if (str == null || str.isEmpty()) {
            return new String[0];
        }

        Integer previousCodepoint;
        Integer currentCodepoint = null;
        int length = str.length();
        int offset = 0;
        StringBuilder currentWord = new StringBuilder();
        List<String> words = new ArrayList<>();
        Boolean wordAllUpperCase = null;
        int lastCodePointCharLength = 0;
        while (offset < length) {
            previousCodepoint = currentCodepoint;
            currentCodepoint = str.codePointAt(offset);
            int currentCharCount = Character.charCount(currentCodepoint);

            // Separator char encountered not part of any word, but indicate a boundary
            if (currentCodepoint == '_' || currentCodepoint == '$') {
                if (currentWord.length() > 0) {
                    words.add(currentWord.toString());
                    currentWord.setLength(0);
                }

                wordAllUpperCase = null;
                offset += currentCharCount;
                lastCodePointCharLength = 0;
                continue;
            }

            // Change between lower case and upper case indicate a word boundary
            if ((previousCodepoint != null && Character.isLowerCase(previousCodepoint) && Character.isUpperCase(currentCodepoint))) {
                if (currentWord.length() > 0) {
                    words.add(currentWord.toString());
                    currentWord.setLength(0);
                    currentWord.appendCodePoint(currentCodepoint);
                }

                wordAllUpperCase = true;
                offset += currentCharCount;
                lastCodePointCharLength = currentCharCount;
                continue;
            }

            // Change between upper case and lower case indicate a word boundary if multiple upper
            // case characters where encountered previously.
            if (currentWord.length() > 1
                    && wordAllUpperCase
                    && previousCodepoint != null
                    && Character.isUpperCase(previousCodepoint) && Character.isLowerCase(currentCodepoint)) {
                words.add(currentWord.substring(0, currentWord.length() - lastCodePointCharLength));
                currentWord.substring(0, currentWord.length() - lastCodePointCharLength);
                currentWord.delete(0, currentWord.length() - lastCodePointCharLength);
                currentWord.appendCodePoint(currentCodepoint);

                wordAllUpperCase = false;
                offset += currentCharCount;
                lastCodePointCharLength = currentCharCount;
                continue;
            }

            currentWord.appendCodePoint(currentCodepoint);
            wordAllUpperCase = Character.isUpperCase(currentCodepoint) && (wordAllUpperCase == null || wordAllUpperCase);
            offset += currentCharCount;
            lastCodePointCharLength = currentCharCount;
        }

        if (currentWord.length() > 0) {
            words.add(currentWord.toString());
        }

        // Remove hungarian notation if found
        if (words.get(0).equals("m")) {
            words.remove(0);
        }

        String[] result = new String[words.size()];
        words.toArray(result);
        return result;
    }
}
