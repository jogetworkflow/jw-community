/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomcat.jakartaee;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Specification profile defining the replacements performed.
 */
public enum JogetEESpecProfiles implements EESpecProfile {

    /**
     * Specification profile matching the packages provided with Tomcat.
     */
    JOGET("javax", "jakarta",
            "javax([/\\.](annotation[/\\.](" + Patterns.ANNOTATION_CLASSES + ")" +
                    "|ejb" +
                    "|el" +
                    "|persistence" +
                    "|security[/\\.]auth[/\\.]message" +
                    "|servlet" +
                    "|transaction(?![/\\.]xa)" +
                    "|websocket))");

    private static final class Patterns {
        /*
         * Prefixes of classes provided by tomcat-annotations-api 8.5. Nullable and Notnull are present in later
         * versions but the Findbugs JSR-305 implementation also has checkers that can't be satisfied by other
         * implementations, so we avoid migrating those.
         */
        static final String ANNOTATION_CLASSES = String.join("|",
                Arrays.asList(
                        "Generated",
                        "ManagedBean",
                        "PostConstruct",
                        "PreDestroy",
                        "Priority",
                        "Resource",
                        "Resources",
                        "security[/\\.]DeclareRoles",
                        "security[/\\.]DenyAll",
                        "security[/\\.]PermitAll",
                        "security[/\\.]RolesAllowed",
                        "security[/\\.]RunAs",
                        "sql[/\\.]DataSourceDefinition"
                ));
    }

    private String source;
    private String target;
    private Pattern pattern;

    JogetEESpecProfiles(String source, String target, String pattern) {
        this.source = source;
        this.target = target;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }
}
