package com.example.seckill.common;

public final class HtmlSanitizer {

    private HtmlSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
