package io.realm.processor.nameformatter;

import com.google.common.base.CaseFormat;

/**
 * Formatter's based on Guavas implementation.
 * Most noticeably, these only support ASCII characters.
 *
 * See https://github.com/google/guava/wiki/StringsExplained
 */
public class GuavaCaseFormatter implements CaseFormatter {

    public static CaseFormatter INSTANCE_CAMEL_CASE = new GuavaCaseFormatter(CaseFormat.LOWER_CAMEL);
    public static CaseFormatter INSTANCE_PASCAL_CASE = new GuavaCaseFormatter(CaseFormat.UPPER_CAMEL);
    public static CaseFormatter INSTANCE_LOWER_WITH_DASHES = new GuavaCaseFormatter(CaseFormat.LOWER_HYPHEN);
    public static CaseFormatter INSTANCE_LOWER_WITH_UNDERSCORE = new GuavaCaseFormatter(CaseFormat.LOWER_UNDERSCORE);

    private final CaseFormat format;

    public GuavaCaseFormatter(CaseFormat format) {
        this.format = format;
    }

    @Override
    public String format(String name) {
        // FIXME: Currently this assumes that the input name is always camelCase
        return CaseFormat.LOWER_CAMEL.to(format, name);
    }
}
