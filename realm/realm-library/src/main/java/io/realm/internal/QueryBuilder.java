package io.realm.internal;

import io.realm.Sort;

public class QueryBuilder {
    private static final String EMPTY_STRING = "";
    private static final String SPACE = " ";
    private static final String ARGUMENT = "$";
    private static final String AND_CONNECTOR = " AND ";
    private static final String OR_CONNECTOR = " OR ";
    private static final String NOT_CONNECTOR = "NOT ";
    private static final String BEGIN_GROUP = "(";
    private static final String END_GROUP = ")";
    private static final String EQUAL_OPERATOR = "=";
    private static final String NOT_EQUAL_OPERATOR = "!=";
    private static final String EQUAL_NOT_SENSITIVE_OPERATOR = "=[c]";
    private static final String NOT_EQUAL_NOT_SENSITIVE_OPERATOR = "!=[c]";
    private static final String GREATER_THAN_OPERATOR = ">";
    private static final String GREATER_THAN_EQUALS_OPERATOR = ">=";
    private static final String LESS_THAN_OPERATOR = "<";
    private static final String LESS_THAN_EQUALS_OPERATOR = "<=";
    private static final String BEGINS_WITH_OPERATOR = "BEGINSWITH";
    private static final String BEGINS_WITH_NOT_SENSITIVE_OPERATOR = "BEGINSWITH[c]";
    private static final String ENDS_WITH_OPERATOR = "ENDSWITH";
    private static final String ENDS_WITH_NOT_SENSITIVE_OPERATOR = "ENDSWITH[c]";
    private static final String LIKE_OPERATOR = "LIKE";
    private static final String LIKE_NOT_SENSITIVE_OPERATOR = "LIKE[c]";
    private static final String CONTAINS_OPERATOR = "CONTAINS";
    private static final String CONTAINS_NOT_SENSITIVE_OPERATOR = "CONTAINS[c]";
    private static final String SORT = "SORT";
    private static final String ASCENDING = "ASC";
    private static final String DESCENDING = "DESC";
    private static final String COMMA_SEPARATOR = ", ";
    private static final String DISTINCT = "DISTINCT";
    private static final String LIMIT = "LIMIT";
    private static final String TRUE_PREDICATE = "TRUEPREDICATE";
    private static final String FALSE_PREDICATE = "FALSEPREDICATE";
    private static final String NULL = "NULL";

    private StringBuilder predicateBuilder = new StringBuilder();
    private StringBuilder descriptorBuilder = new StringBuilder();

    private String nextPredicateConnector = EMPTY_STRING;
    private String nextDescriptorSeparator = EMPTY_STRING;
    private boolean validated = true;
    private boolean isOrConnected = false;

    private void appendSingleArgumentOperator(String fieldName, String operator, long argPosition) {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(fieldName)
                .append(SPACE)
                .append(operator)
                .append(SPACE)
                .append(ARGUMENT)
                .append(argPosition);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void appendEqualTo(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, EQUAL_OPERATOR, argPosition);
    }

    public void appendNotEqualTo(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, NOT_EQUAL_OPERATOR, argPosition);
    }

    public void appendEqualToNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, EQUAL_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendNotEqualToNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, NOT_EQUAL_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendGreaterThan(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, GREATER_THAN_OPERATOR, argPosition);
    }

    public void appendGreaterThanEquals(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, GREATER_THAN_EQUALS_OPERATOR, argPosition);
    }

    public void appendLessThan(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, LESS_THAN_OPERATOR, argPosition);
    }

    public void appendLessThanEquals(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, LESS_THAN_EQUALS_OPERATOR, argPosition);
    }

    public void appendBeginsWith(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, BEGINS_WITH_OPERATOR, argPosition);
    }

    public void appendBeginsWithNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, BEGINS_WITH_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendEndsWith(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, ENDS_WITH_OPERATOR, argPosition);
    }

    public void appendEndsWithNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, ENDS_WITH_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendLike(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, LIKE_OPERATOR, argPosition);
    }

    public void appendLikeNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, LIKE_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendContains(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, CONTAINS_OPERATOR, argPosition);
    }

    public void appendContainsNotSensitive(String fieldName, long argPosition) {
        appendSingleArgumentOperator(fieldName, CONTAINS_NOT_SENSITIVE_OPERATOR, argPosition);
    }

    public void appendBetween(String fieldName, long arg1Position, long arg2Position) {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(BEGIN_GROUP)
                .append(fieldName)
                .append(GREATER_THAN_EQUALS_OPERATOR)
                .append(ARGUMENT)
                .append(arg1Position)
                .append(AND_CONNECTOR)
                .append(fieldName)
                .append(LESS_THAN_EQUALS_OPERATOR)
                .append(ARGUMENT)
                .append(arg2Position)
                .append(END_GROUP);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void appendRawPredicate(String filter) {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(filter);
    }

    public void isNull(String fieldName) {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(fieldName)
                .append(SPACE)
                .append(EQUAL_OPERATOR)
                .append(SPACE)
                .append(NULL);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void isNotNull(String fieldName) {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(fieldName)
                .append(SPACE)
                .append(NOT_EQUAL_OPERATOR)
                .append(SPACE)
                .append(NULL);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void alwaysTrue() {
        validated = false;

        predicateBuilder.append(nextPredicateConnector)
                .append(TRUE_PREDICATE);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void alwaysFalse() {
        validated = false;
        predicateBuilder.append(nextPredicateConnector)
                .append(FALSE_PREDICATE);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void beingGroup() {
        validated = false;
        predicateBuilder.append(nextPredicateConnector)
                .append(BEGIN_GROUP);

        nextPredicateConnector = EMPTY_STRING;
    }

    public void endGroup() {
        validated = false;
        predicateBuilder.append(END_GROUP);

        nextPredicateConnector = AND_CONNECTOR;
    }

    public void or() {
        if(predicateBuilder.length() == 0){
            isOrConnected = true;
        } else {
            nextPredicateConnector = OR_CONNECTOR;
        }
    }

    public void not() {
        validated = false;
        predicateBuilder
                .append(nextPredicateConnector)
                .append(NOT_CONNECTOR);

        nextPredicateConnector = EMPTY_STRING;
    }

    public void sort(String[] fieldNames, Sort[] sortOrders) {
        validated = false;
        descriptorBuilder
                .append(nextDescriptorSeparator)
                .append(SORT)
                .append(BEGIN_GROUP);

        String sortSeparator = EMPTY_STRING;

        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];

            descriptorBuilder.append(sortSeparator)
                    .append(fieldName)
                    .append(SPACE)
                    .append((sortOrders[i] == Sort.ASCENDING) ? ASCENDING : DESCENDING);

            sortSeparator = COMMA_SEPARATOR;
        }

        descriptorBuilder.append(END_GROUP);

        nextDescriptorSeparator = SPACE;
    }

    public void distinct(String[] fieldNames) {
        validated = false;
        descriptorBuilder
                .append(nextDescriptorSeparator)
                .append(DISTINCT)
                .append(BEGIN_GROUP);

        String distinctSeparator = EMPTY_STRING;

        for (String fieldName : fieldNames) {
            descriptorBuilder.append(distinctSeparator)
                    .append(fieldName);

            distinctSeparator = COMMA_SEPARATOR;
        }

        descriptorBuilder.append(END_GROUP);

        nextDescriptorSeparator = SPACE;
    }

    public void limit(long limit) {
        validated = false;
        descriptorBuilder
                .append(nextDescriptorSeparator)
                .append(LIMIT)
                .append(BEGIN_GROUP)
                .append(limit)
                .append(END_GROUP);

        nextDescriptorSeparator = SPACE;
    }

    public boolean isValidated() {
        return validated;
    }

    public String build() {
        String descriptor = (descriptorBuilder.length() == 0) ? EMPTY_STRING : (SPACE + descriptorBuilder.toString());
        String predicate = (predicateBuilder.length() == 0) ? TRUE_PREDICATE : predicateBuilder.toString();

        predicateBuilder = new StringBuilder();
        descriptorBuilder = new StringBuilder();

        isOrConnected = OR_CONNECTOR.equals(nextPredicateConnector);
        nextPredicateConnector = EMPTY_STRING;
        nextDescriptorSeparator = EMPTY_STRING;

        validated = true;

        return predicate + descriptor;
    }

    public boolean isOrConnected() {
        return isOrConnected;
    }
}
