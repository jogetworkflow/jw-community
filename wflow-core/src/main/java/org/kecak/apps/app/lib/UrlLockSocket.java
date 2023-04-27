package org.kecak.apps.app.lib;

import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.workflow.util.WorkflowUtil;
import org.kecak.apps.app.model.LockEntry;
import org.kecak.plugin.base.PluginWebSocket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UrlLockSocket extends ExtDefaultPlugin implements PluginWebSocket {
    final private static Set<LockEntry> locks = new HashSet<>();

    private LockEntry entry = null;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public String onMessage(String sessionId, String url) {
        final String currentUsername = WorkflowUtil.getCurrentUsername();
        final LockEntry newEntry = new LockEntry(url, currentUsername);
        final Optional<LockEntry> optEntry = locks.stream().filter(newEntry::equals).findFirst();
        if(optEntry.isPresent()) {
            return "URL is being locked by [" + optEntry.get().getUsername() + "] at [" + dateFormat.format(optEntry.get().getDate())+ "]";
        } else {
            LogUtil.info(getClass().getName(), "Acquiring lock for session [" + sessionId + "] url [" + entry.getUrl() + "]");
            locks.add(entry = newEntry);
            return "";
        }
    }

    @Override
    public void onClose(String sessionId) {
        LogUtil.info(getClass().getName(), "Releasing lock for session [" + sessionId + "]");
        locks.remove(entry);
    }

    @Override
    public String getName() {
        return "Url Lock Socket";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return "Lock based on URL";
    }
}
