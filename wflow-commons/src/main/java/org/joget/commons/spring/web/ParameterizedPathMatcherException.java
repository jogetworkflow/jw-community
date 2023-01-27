package org.joget.commons.spring.web;

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
 * Exception thrown if a pattern given to the ParameterizedPathMatcher (configured in
 * {@link carbonfive.spring.web.pathparameter.ParameterizedUrlHandlerMapping}) is invalid.
 *
 * @author alex cruikshank
 */
public class ParameterizedPathMatcherException extends RuntimeException {

    public ParameterizedPathMatcherException(String message) {
        super(message);
    }

    public ParameterizedPathMatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
