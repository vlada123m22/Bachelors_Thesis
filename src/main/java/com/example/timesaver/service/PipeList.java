package com.example.timesaver.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PipeList {
    private PipeList() {}

    public static final String SEP = "|";
    private static final Pattern SPLIT = Pattern.compile("\\|");

    public static String join(List<String> items) {
        if (items == null || items.isEmpty()) return null;
        List<String> cleaned = items.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .peek(PipeList::validateNoPipe)
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : String.join(SEP, cleaned);
    }

    public static List<String> split(String s) {
        if (s == null || s.isBlank()) return Collections.emptyList();
        return Arrays.stream(SPLIT.split(s))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toList());
    }

    private static void validateNoPipe(String v) {
        if (v.contains(SEP)) {
            throw new IllegalArgumentException("Values must not contain '|' character");
        }
    }
}
