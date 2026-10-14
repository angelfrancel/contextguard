package com.contextguard.policy;

import java.util.regex.Pattern;

public final class ToolNamePolicy {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    private ToolNamePolicy() {
    }

    public static String requireValid(String name) {
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("unsupported upstream tool name");
        }
        return name;
    }
}
