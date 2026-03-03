package org.Elias040.servercore.moderation;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class BlacklistMatcher {

    public enum MatchMode {
        CONTAINS,
        WHOLE_WORD
    }

    private final MatchMode matchMode;
    private final boolean ignoreCase;
    private final Set<String> containsWords;
    private final List<Pattern> wholeWordPatterns;

    public BlacklistMatcher(List<String> words, MatchMode matchMode, boolean ignoreCase) {
        this.matchMode = matchMode;
        this.ignoreCase = ignoreCase;

        if (matchMode == MatchMode.CONTAINS) {
            this.containsWords = words.stream()
                    .map(w -> ignoreCase ? w.toLowerCase(Locale.ROOT) : w)
                    .collect(Collectors.toUnmodifiableSet());
            this.wholeWordPatterns = List.of();
        } else {
            this.containsWords = Set.of();
            int flags = ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0;
            this.wholeWordPatterns = words.stream()
                    .map(w -> Pattern.compile("\\b" + Pattern.quote(w) + "\\b", flags))
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public boolean matches(String message) {
        if (matchMode == MatchMode.CONTAINS) {
            String toCheck = ignoreCase ? message.toLowerCase(Locale.ROOT) : message;
            for (String word : containsWords) {
                if (toCheck.contains(word)) return true;
            }
            return false;
        }
        for (Pattern pattern : wholeWordPatterns) {
            if (pattern.matcher(message).find()) return true;
        }
        return false;
    }
}