package org.joget.directory.model.service;

import java.util.Map;

public interface DirectoryManagerPlugin {

    public DirectoryManager getDirectoryManagerImpl(Map properties);
}
