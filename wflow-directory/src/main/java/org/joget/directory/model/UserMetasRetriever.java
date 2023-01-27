package org.joget.directory.model;

import java.util.Map;

/**
 * Interface for extending User class to retrieve custom meta data. 
 * Only used for custom directory manager which read from remote source.
 */
public interface UserMetasRetriever {
    
    public Map<String, String> getMetas();
}
