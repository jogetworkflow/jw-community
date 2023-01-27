package org.enhydra.jawe;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLComplexChoice;
import org.enhydra.shark.xpdl.XMLElement;

public class UndoHistoryManagerImpl implements UndoHistoryManager {

    private static boolean isUndoOrRedoInProgress = false;
    private static Stack undo;
    private static Stack redo;
    private int max;
    private static JaWEController jc;

    public UndoHistoryManagerImpl() {
        undo = new Stack();
        redo = new Stack();

        jc = JaWEManager.getInstance().getJaWEController();
    }

    public void init(int max) {
        this.max = max;
    }

    public void registerEvents(List xpdlInfoList, XPDLElementChangeInfo selectedEvent) {
        if (max <= 0) {
            return;
        }
        if (xpdlInfoList != null && xpdlInfoList.size() > 0) {
            freeStackSpace("undo");

            List undoChangedList = new ArrayList(xpdlInfoList);

            //add selected event as last event in list
            undoChangedList.add(selectedEvent);

            undo.push(undoChangedList);
            redo.clear();
        }
    }

    public void undo() {
        if (!undo.isEmpty()) {
            isUndoOrRedoInProgress = true;

            //Get last event and changed list from undo stack
            List undoChangedList = (List) undo.pop();
            XPDLElementChangeInfo lastEvent = (XPDLElementChangeInfo) undoChangedList.remove(undoChangedList.size() - 1);

            //Put current event and the changed list to redo stack
            freeStackSpace("redo");
            XPDLElementChangeInfo selectEventForRedo = jc.getCurrentSelectionEvent();
            List redoChangedList = new ArrayList(undoChangedList);
            redoChangedList.add(selectEventForRedo);
            redo.push(redoChangedList);

            //revert the undo changed list
            undoChangedList = revertChangedList(undoChangedList);

            //Apply change
            for (Object obj : undoChangedList) {
                XPDLElementChangeInfo info = (XPDLElementChangeInfo) obj;
                changeModel(info);
            }

            isUndoOrRedoInProgress = false;

            updateJaWEController(lastEvent, undoChangedList, XPDLElementChangeInfo.UNDO);
        }
    }

    public void redo() {
        if (!redo.isEmpty()) {
            isUndoOrRedoInProgress = true;

            //Get last event and changed list from redo stack
            List redoChangedList = (List) redo.pop();
            XPDLElementChangeInfo lastEvent = (XPDLElementChangeInfo) redoChangedList.remove(redoChangedList.size() - 1);

            //Put event and the changed list to undo stack
            freeStackSpace("undo");
            XPDLElementChangeInfo selectEventForRedo = jc.getCurrentSelectionEvent();
            List undoChangedList = new ArrayList(redoChangedList);
            undoChangedList.add(selectEventForRedo);
            undo.push(undoChangedList);

            //Apply change
            for (Object obj : redoChangedList) {
                XPDLElementChangeInfo info = (XPDLElementChangeInfo) obj;
                changeModel(info);
            }

            isUndoOrRedoInProgress = false;

            updateJaWEController(lastEvent, redoChangedList, XPDLElementChangeInfo.REDO);
        }
    }

    public boolean canUndo() {
        if (undo.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canRedo() {
        if (redo.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isUndoOrRedoInProgress() {
        return isUndoOrRedoInProgress;
    }

    public void cleanHistory() {
        undo.clear();
        redo.clear();
    }

    protected List revertChangedList(List lst) {
        List changed = new ArrayList();

        for (int i = lst.size() - 1; i >= 0; i--) {
            XPDLElementChangeInfo ch = (XPDLElementChangeInfo) lst.get(i);
            XPDLElementChangeInfo chNew = new XPDLElementChangeInfo(jc, ch);

            if (chNew.getAction() == XPDLElementChangeInfo.INSERTED) {
                chNew.setAction(XPDLElementChangeInfo.REMOVED);
            } else if (chNew.getAction() == XPDLElementChangeInfo.REMOVED) {
                chNew.setAction(XPDLElementChangeInfo.INSERTED);
            } else if (chNew.getAction() == XPDLElementChangeInfo.UPDATED || chNew.getAction() == XPDLElementChangeInfo.REPOSITIONED) {
                chNew.setNewValue(ch.getOldValue());
                chNew.setOldValue(ch.getNewValue());
            }
            changed.add(chNew);
        }

        return changed;
    }

    protected void freeStackSpace(String type) {
        if (type.equals("undo")) {
            if (undo.size() >= max) {
                undo.remove(0);
            }
        } else {
            if (redo.size() >= max) {
                redo.remove(0);
            }
        }
    }

    protected void changeModel(XPDLElementChangeInfo info) {
        try {
            if (info.getAction() == XPDLElementChangeInfo.INSERTED) {
                if (info.getChangedElement() instanceof XMLCollection) {
                    XMLCollection col = (XMLCollection) info.getChangedElement();
                    List lst = info.getChangedSubElements();
                    for (int i = 0; i < lst.size(); i++) {
                        col.add((XMLElement) lst.get(i));
                    }

                }
            } else if (info.getAction() == XPDLElementChangeInfo.REMOVED) {
                if (info.getChangedElement() instanceof XMLCollection) {
                    XMLCollection col = (XMLCollection) info.getChangedElement();
                    List lst = info.getChangedSubElements();
                    for (int i = 0; i < lst.size(); i++) {
                        col.remove((XMLElement) lst.get(i));
                    }

                }
            } else if (info.getAction() == XPDLElementChangeInfo.UPDATED) {
                XMLElement el = info.getChangedElement();
                if (el instanceof XMLComplexChoice) {
                    ((XMLComplexChoice) el).setChoosen((XMLElement) info.getNewValue());
                } else {
                    el.setValue(info.getNewValue().toString());
                }
            } else if (info.getAction() == XPDLElementChangeInfo.REPOSITIONED) {
                XMLCollection col = (XMLCollection) info.getChangedElement();
                List lst = info.getChangedSubElements();
                List newPositions = (List) info.getNewValue();
                for (int i = 0; i < lst.size(); i++) {
                    col.reposition((XMLElement) lst.get(i), ((Integer) newPositions.get(i)).intValue());
                }

            }
        } catch (Exception e) {
            //ignore
        }
    }

    protected void updateJaWEController(XPDLElementChangeInfo lastEvent, List changedList, int action) {

        jc.setUpdateInProgress(true);
        XPDLElementChangeInfo ucInfo = jc.createInfo(jc.getMainPackage(), action);
        ucInfo.setChangedSubElements(changedList);
        jc.sendEvent(ucInfo);
        jc.setUpdateInProgress(false);

        if (lastEvent.getChangedSubElements().size() > 0) {
            jc.getSelectionManager().setSelection(lastEvent.getChangedSubElements(), true);
        } else {
            jc.getSelectionManager().setSelection(lastEvent.getChangedElement(), true);
        }

    }
}
