package com.rain.networkproxy.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RequestFilter {
    private final List<String> rules;
    private final List<String> regex;

    public RequestFilter(@NonNull List<String> rules) {
        this.rules = rules;
        this.regex = constructRegex(rules);
    }

    private List<String> constructRegex(List<String> rules) {
        List<String> regex = new ArrayList<>(this.rules.size());

        for (String rule : rules) {
            if (!rule.endsWith("/")) {
                rule = rule + "/";
            }
            if (rule.startsWith("/")) {
                rule = rule.substring(1, rule.length());
            }
            rule = rule.replace("/", "\\/");
            regex.add("^[\\S]*" + rule.replace("*", "((?!\\/)\\S)+") + "$");
        }

        return Collections.unmodifiableList(regex);
    }

    public boolean isMatch(@NonNull String url) {
        for (String r : regex) {
            String trimUrl = url.split("\\?")[0];
            if (!trimUrl.endsWith("/")) {
                trimUrl = trimUrl + "/";
            }
            if (trimUrl.matches(r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestFilter requestFilter = (RequestFilter) o;
        return rules.equals(requestFilter.rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public String toString() {
        return "RequestFilter{"
                + "rules=" + rules
                + '}';
    }
}
