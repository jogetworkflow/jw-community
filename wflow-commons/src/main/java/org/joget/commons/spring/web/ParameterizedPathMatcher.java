package org.joget.commons.spring.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import org.springframework.util.AntPathMatcher;

/*
Copyright 2007, Carbon Five, Inc.
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in
writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
 */
/**
 * Replaces Spring's AntPathMatcher, adding the capacity to recognize parameters with the path.
 * <br />
 * The ParameterizedPathMather should return the same result as AntPathMatcher when given any pattern and URL
 * that AntPathMatcher supports.  See {@link carbonfive.spring.web.pathparameter.ParameterizedUrlHandlerMapping}
 * for details on how to specify parameterized path patterns.
 * <br />
 * <strong>NOTE:</strong> For performance reasons, this class caches information for all patterns (not paths) it
 * encounters.  This should not be a problem unless the patterns are being generated or user submitted at runtime
 * (a rather strange idea).
 *
 * @author alex cruikshank
 */
public class ParameterizedPathMatcher extends AntPathMatcher {

    private static final Pattern wildcardPattern = Pattern.compile("([\\\\\\/])|(\\([^\\)]+\\))|([\\*|~]{1,2})|(\\?)|(\\\\\\/)");
    private static final Pattern wildcardFreePattern = Pattern.compile("^[\\\\\\/]?([^\\(\\*\\?\\\\\\/]*[\\\\\\/])*([^\\(\\*\\?\\\\\\/]*$)?");
    private static final Pattern regexEscapePattern = Pattern.compile("[\\\\\\/\\[\\]\\^\\$\\.\\{\\}\\&\\?\\*\\+\\|\\<\\>\\!\\=]");
    private final Map<String, NamedPattern> patternCache = new HashMap<String, NamedPattern>();

    @Override
    public boolean isPattern(String string) {
        return (string.indexOf('*') != -1 || string.indexOf('?') != -1 || string.indexOf('(') != -1);
    }

    /**
     * Return true if the given path matches the given pattern.
     * @param pattern pattern in Ant path syntax (plus parameters)
     * @param path path to test against pattern
     * @return true if the pattern matches, false otherwise.
     */
    @Override
    public boolean match(String pattern, String path) {
        return getOrCreatePattern(pattern).matcher(path).matches();
    }

    /**
     * Return a map containing all parameters found in the given path according to the given pattern.
     * @param pattern given pattern containing parameter specification
     * @param path to test against pattern
     * @return A map with the specified parameter as key and the matched path segement as a value.
     */
    public Map<String, String> namedParameters(String pattern, String path) {
        return getOrCreatePattern(pattern).namedGroups(path);
    }

    private synchronized NamedPattern getOrCreatePattern(String pattern) {
        NamedPattern compiledPattern = patternCache.get(pattern);
        if (compiledPattern == null) {
            compiledPattern = new NamedPattern(pattern);
            patternCache.put(pattern, compiledPattern);
        }
        return compiledPattern;
    }

    /**
     * Return the initial path segments of the given path up to the first wildcard of the given pattern
     * @param pattern pattern that may contain wildcards
     * @param path path to test against pattern
     * @return in regex: ^[\\\/]?([^(\*\?\\\/]*[\\\/])*([^\(\*\?\\\/]*$)?
     */
    @Override
    public String extractPathWithinPattern(String pattern, String path) {
        return getOrCreatePattern(pattern).extractPathWithinPattern(path);
    }

    @Override
    public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
        return namedParameters(pattern, path);
    }

    @Override
    public Comparator<String> getPatternComparator(String path) {
        return super.getPatternComparator(path);
    }

    @Override
    public String combine(String pattern1, String pattern2) {
        return super.combine(pattern1, pattern2);
    }

    private static class NamedPattern {

        private Pattern pattern = null;
        private List<String> names = null;
        private int patternMatchedIndex = 0;

        public NamedPattern(String pattern) {
            Matcher patternFree = wildcardFreePattern.matcher(pattern);
            if (patternFree.find()) {
                patternMatchedIndex = patternFree.group(0).length();
            }

            names = new ArrayList<String>();
            String translated = translatePattern(pattern, new BitSet(1));
            this.pattern = Pattern.compile('^' + translated + '$');
        }

        public Matcher matcher(String string) {
            return pattern.matcher(string);
        }

        public Map<String, String> namedGroups(String path) {
            Matcher matcher = matcher(path);
            if (!matcher.matches()) {
                return null;
            }

            Map<String, String> groups = new HashMap<String, String>();

            try {
                for (int i = 0; i < names.size(); i++) {
                    if ((matcher.group(i + 1) != null) && (names.get(i) != null)) {
                        groups.put(names.get(i), URLDecoder.decode(matcher.group(i + 1), "UTF-8"));
                    }
                }
            } catch (UnsupportedEncodingException uee) {
                return null;
            }

            return groups;
        }

        public String extractPathWithinPattern(String path) {
            if (path.length() < patternMatchedIndex) {
                return "";
            }
            return path.substring(patternMatchedIndex);
        }

        /**
         * Convert an ant style pattern into a regex expression
         * @param pattern ant style pattern to translate
         * @param nextSeparatorOptional BitSet containing 1 if
         * @return
         */
        private String translatePattern(String pattern, BitSet nextSeparatorOptional) {
            StringBuffer translatedPattern = new StringBuffer();
            Matcher m = wildcardPattern.matcher(pattern);

            int lastFound = 0;
            while (m.find()) {
                String content = regexEscapePattern.matcher(pattern.substring(lastFound, m.start())).replaceAll("\\\\$0");
                translatedPattern.append(content);
                if (content.length() > 0) {
                    nextSeparatorOptional.set(0, false);
                }

                if (m.group(1) != null) {
                    translatedPattern.append("[\\\\\\/]");
                    if (nextSeparatorOptional.get(0)) {
                        translatedPattern.append('?');
                    }
                    nextSeparatorOptional.set(0, true);
                } else if ("~".equals(m.group(3))) {
                    translatedPattern.append("[^\\\\\\/]*");
                } else if ("*".equals(m.group(3))) {
                    translatedPattern.append("[^\\\\\\/]*");
                    nextSeparatorOptional.set(0, false);
                } else if ("**".equals(m.group(3))) {
                    translatedPattern.append(".*?");
                } else if (m.group(4) != null) {
                    translatedPattern.append(".");
                    nextSeparatorOptional.set(0, false);
                } else if (m.group(2) != null) {
                    int colonIndex = m.group(2).indexOf(':');
                    if (colonIndex < 0) {
                        throw new ParameterizedPathMatcherException("Named group does not contain name '" + m.group(2) + "'");
                    }
                    names.add(m.group(2).substring(colonIndex + 1, m.group(2).length() - 1));
                    translatedPattern.append("(").append(translatePattern(m.group(2).substring(1, colonIndex), nextSeparatorOptional)).append(")");
                }

                lastFound = m.end();
            }
            translatedPattern.append(regexEscapePattern.matcher(pattern.substring(lastFound)).replaceAll("\\\\$0"));

            return translatedPattern.toString();
        }
    }

    @Override
    public boolean matchStart(String pattern, String path) {
        return super.matchStart(pattern, path);
    }
}
