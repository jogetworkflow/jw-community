package org.enhydra.jawe.base.controller;

import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.ChoiceButton;
import org.enhydra.jawe.ChoiceButtonListener;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentSettings;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEConstants;
import org.enhydra.jawe.JaWEEAHandler;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.JaWEXMLUtil;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.UndoHistoryManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.WaitScreen;
import org.enhydra.jawe.XPDLElementChangeInfo;
import org.enhydra.jawe.XPDLListenerAndObservable;
import org.jped.base.xpdlhandler.DefaultXPDLHandlerSettings;
import org.jped.filter.PluggableValueFilter;
import org.jped.filter.ValueFilter;
import org.jped.plugins.PluginsManager;
import org.enhydra.jawe.base.xpdlhandler.XPDLHandler;
import org.enhydra.jawe.base.xpdlobjectfactory.XPDLObjectFactory;
import org.enhydra.jawe.base.xpdlvalidator.ValidationError;
import org.enhydra.jawe.components.graph.GraphUtilities;
import org.enhydra.shark.utilities.SequencedHashMap;
import org.enhydra.shark.xpdl.Path;
import org.enhydra.shark.xpdl.StandardPackageValidator;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XMLValidationError;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ActivitySet;
import org.enhydra.shark.xpdl.elements.ActivitySets;
import org.enhydra.shark.xpdl.elements.Application;
import org.enhydra.shark.xpdl.elements.Applications;
import org.enhydra.shark.xpdl.elements.DataField;
import org.enhydra.shark.xpdl.elements.DataFields;
import org.enhydra.shark.xpdl.elements.EnumerationValue;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;
import org.enhydra.shark.xpdl.elements.ExternalPackage;
import org.enhydra.shark.xpdl.elements.ExternalPackages;
import org.enhydra.shark.xpdl.elements.FormalParameter;
import org.enhydra.shark.xpdl.elements.FormalParameters;
import org.enhydra.shark.xpdl.elements.Join;
import org.enhydra.shark.xpdl.elements.Member;
import org.enhydra.shark.xpdl.elements.Namespace;
import org.enhydra.shark.xpdl.elements.Namespaces;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Participants;
import org.enhydra.shark.xpdl.elements.Responsible;
import org.enhydra.shark.xpdl.elements.Responsibles;
import org.enhydra.shark.xpdl.elements.Split;
import org.enhydra.shark.xpdl.elements.Tools;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.TransitionRef;
import org.enhydra.shark.xpdl.elements.TransitionRefs;
import org.enhydra.shark.xpdl.elements.TransitionRestriction;
import org.enhydra.shark.xpdl.elements.TransitionRestrictions;
import org.enhydra.shark.xpdl.elements.Transitions;
import org.enhydra.shark.xpdl.elements.TypeDeclaration;
import org.enhydra.shark.xpdl.elements.TypeDeclarations;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.WorkflowProcesses;
import org.w3c.dom.Document;

public class JaWEController extends Observable implements
        Observer,
        JaWEComponent,
        ChoiceButtonListener {

    protected ControllerSettings settings;
    protected boolean undoableChangeInProgress = false;
    protected boolean updateInProgress = false;
    protected boolean updateSpecialInProgress = false;
    protected JaWEEdit edit = new JaWEEdit();
    protected JaWETypes jtypes;
    protected JaWETypeResolver jtypeResolver;
    protected List xpdlInfoList = new ArrayList();
    protected List xpdlListenerObservables = new ArrayList();
    protected JaWESelectionManager selectionMng;
    protected JaWEActions defaultJaWEActions;
    protected UndoHistoryManager undoHistoryManager;
    protected JaWEFrame frame;
    boolean jaweFrameShown = false;
    protected XPDLElementChangeInfo undoSelectionEvent = null;
    protected String currentConfig = null;
    protected Map configInfo;
    protected PluginsManager pluginsManager;

    public JaWEController(ControllerSettings settings) {
        this.settings = settings;
        this.settings.init(this);

        jtypes = new JaWETypes();
        jtypes.setPropertyMgr(settings.getPropertyMgr());
        jtypes.init(this);
        pluginsManager = new PluginsManager(JaWEManager.getInstance());
    }

    // ********************** Observable
    public void addObserver(Observer obs) {
        super.addObserver(obs);
        if (obs instanceof Observable) {
            ((Observable) obs).addObserver(this);
        }
    }

    // **********************
    // ********************** Observer
    public void update(Observable o, Object arg) {
        if (updateInProgress) {
            JaWEManager.getInstance().getLoggingManager().warn("JaWEController -> this is nested event dispatch, because another one is in progress!");
            Thread.dumpStack();
            return;
        }
        if (!(arg instanceof XMLElementChangeInfo) || ((XMLElementChangeInfo) arg).getChangedElement() == null) {
            JaWEManager.getInstance().getLoggingManager().error("JaWEController -> Invalid arg " + arg + " or element");
            Thread.dumpStack();
            return;
        }
        XPDLElementChangeInfo info;
        if (!(arg instanceof XPDLElementChangeInfo)) {
            info = new XPDLElementChangeInfo(this, (XMLElementChangeInfo) arg);
        } else {
            info = (XPDLElementChangeInfo) arg;
        }
        if (arg instanceof XPDLElementChangeInfo && info.getSource() == this) {
            JaWEManager.getInstance().getLoggingManager().error("JaWEController -> Aborting update because JaWEController is a source!");
            Thread.dumpStack();
            return;
        }

        int action = info.getAction();

        XMLElement chel = info.getChangedElement();
        if (chel != null && chel.getParent() instanceof Package) {
            if (action == XMLElementChangeInfo.UPDATED && chel instanceof XMLAttribute && chel.toName().equals("Id") || chel.toName().equals("Name")) {
                if (chel.toName().equals("Id")) {
                    changePackageId((Package) chel.getParent(),
                            (String) info.getOldValue(),
                            (String) info.getNewValue());
                }
                updateTitle();
            }
        }

        if (isUndoOrRedoInProgress()) {
            JaWEManager.getInstance().getLoggingManager().debug("JaWEController -> event " + info + " won't be taken into account while processing undo/redo actions!");
            return;
        }
        if (undoableChangeInProgress) {
            if (!(action == XMLElementChangeInfo.INSERTED || action == XMLElementChangeInfo.REMOVED || action == XMLElementChangeInfo.UPDATED || action == XMLElementChangeInfo.REPOSITIONED)) {
                JaWEManager.getInstance().getLoggingManager().error("JaWEController -> event " + info + " won't be taken into account while processing undoable change action!");
                return;
            }

            xpdlInfoList.add(info);
            return;
        }

        if (updateSpecialInProgress) {
            JaWEManager.getInstance().getLoggingManager().warn("JaWEController -> this is nested event dispatch, because another special update is in progress!");
            Thread.dumpStack();
            return;
        }

        updateInProgress = true;
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> normal update for event " + arg + " started ...");

        if (action == XPDLElementChangeInfo.SELECTED) {
            JaWEManager.getInstance().getLoggingManager().error("JaWEController -> SELECTION event not sent from JaWEController! ");
            Thread.dumpStack();
            return;
        }
        clearHistory();

        setChanged();
        notifyObservers(info);

        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> normal update ended");
        updateTitle();
        adjustActions();
        updateInProgress = false;

        if (settings.isDesingTimeValidationEnabled()) {
            if (getMainPackage() != null && (action == XMLElementChangeInfo.INSERTED || action == XMLElementChangeInfo.REMOVED || action == XMLElementChangeInfo.UPDATED || action == XMLElementChangeInfo.REPOSITIONED)) {
                checkValidity(getMainPackage(), true, false, true);
            }
        }

        handleEvent(info);
    }

    // **********************
    // ********************** JaWEComponent
    public JaWEComponentSettings getSettings() {
        return settings;
    }

    public JaWEComponentView getView() {
        return getJaWEFrame();
    }

    public String getName() {
        return "TogWE";
    }

    public String getType() {
        return JaWEComponent.MAIN_COMPONENT;
    }

    public void setType(String type) {
    }

    public boolean adjustXPDL(Package pkg) {
        boolean changed = JaWEManager.getInstance().getXPDLUtils().correctSplitsAndJoins(pkg);

        if (JaWEManager.getInstance().getXPDLHandler().getXPDLRepositoryHandler().isXPDLPrefixEnabled()) {
            List l = pkg.getNamespaces().toElements();
            boolean hasxpdlns = false;
            for (int i = 0; i < l.size(); i++) {
                Namespace ns = (Namespace) l.get(i);
                String nsn = ns.getName();
                if (nsn.equals("xpdl")) {
                    hasxpdlns = true;
                    break;
                }
            }
            if (!hasxpdlns) {
                Namespaces nss = pkg.getNamespaces();
                XPDLObjectFactory xpdlof = JaWEManager.getInstance().getXPDLObjectFactory();
                Namespace ns = xpdlof.createXPDLObject(nss, "", false);
                ns.setName("xpdl");
                ns.setLocation("http://www.wfmc.org/2002/XPDL1.0");
                nss.add(ns);
                xpdlof.adjustXPDLObject(pkg, null);
                changed = true;
            }
        }
        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                changed = jc.adjustXPDL(pkg) || changed;
            }
        }

        // configuration ext. attribute
        if (!JaWEEAHandler.getJaWEConfig(pkg).equals(getCurrentConfig())) {
            JaWEEAHandler.setJaWEConfig(pkg, getCurrentConfig());
            changed = true;
        }

      String verInfo = JaWEManager.getVersion()
                       + "-" + JaWEManager.getRelease() + "-"
                       + JaWEManager.getBuildEdition()
                       + JaWEManager.getBuildEditionSuffix() + "-"
                       + JaWEManager.getBuildNo();
        if (!JaWEEAHandler.getEditingToolVersion(pkg).equals(verInfo)) {
            JaWEEAHandler.setEditingToolVersion(pkg, verInfo);
            changed = true;
        }
        if (!JaWEEAHandler.getEditingTool(pkg).equals(JaWEManager.getInstance().getName())) {
            JaWEEAHandler.setEditingTool(pkg, JaWEManager.getInstance().getName());
            changed = true;
        }

        if (JaWEEAHandler.removeOldPackageEAs(pkg)) {
            changed = true;
        }

        // add external package's ext. attrib. if it does not exist
        if (JaWEEAHandler.adjustExternalPackageEAs(pkg)) {
            changed = true;
        }

        return changed;
    }

    public boolean checkValidity(Package el,
            boolean fullCheck,
            boolean specNotif,
            boolean initialOrDesignTimeValidation) {
        // configure validator
        StandardPackageValidator xpdlValidator = JaWEManager.getInstance().getXPDLValidator();
        xpdlValidator.init(JaWEManager.getInstance().getXPDLHandler(),
                XMLUtil.getPackage(el),
                !specNotif,
                settings.getEncoding(),
                JaWEManager.getInstance().getStartingLocale());
        List l = checkValidity(el, fullCheck);
        XPDLElementChangeInfo info = createInfo(el,
                l,
                XPDLElementChangeInfo.VALIDATION_ERRORS);
        info.setNewValue(new Boolean(specNotif));
        info.setOldValue(new Boolean(initialOrDesignTimeValidation));
        sendEvent(info);
        return (l == null || l.size() == 0);
    }

    public List checkValidity(XMLElement el, boolean fullCheck) {
        StandardPackageValidator xpdlValidator = JaWEManager.getInstance().getXPDLValidator();

        if (el == null) {
            return null;
        }

        List existingErrors = new ArrayList();
        xpdlValidator.validateElement(el, existingErrors, fullCheck);
        List verrors = new ArrayList();
        for (int i = 0; i < existingErrors.size(); i++) {
            ValidationError verr = new ValidationError((XMLValidationError) existingErrors.get(i));
            verrors.add(verr);
        }
        existingErrors.clear();
        boolean isValid = verrors.size() == 0;

        List vers = new ArrayList();
        if (isValid || fullCheck) {
            Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
            while (comps.hasNext()) {
                JaWEComponent jc = (JaWEComponent) comps.next();
                if (jc != this) {
                    List l = jc.checkValidity(el, fullCheck);
                    isValid = (l == null || l.size() == 0) && isValid;
                    if (l != null) {
                        vers.addAll(l);
                    }
                    if (!(isValid || fullCheck)) {
                        break;
                    }
                }
            }
        }

        verrors.addAll(vers);
        verrors = Utils.sortValidationErrorList(verrors, XMLUtil.getPackage(el));

        //CUSTOM
        if (verrors.size() == 0) {
            verrors.add(ResourceManager.getLanguageDependentString("ERROR_NO_ERROR"));
        }
        //CUSTOM

        return verrors;
    }

    public boolean canCreateElement(XMLCollection col) {
        return canCreateElement(col, true);
    }

    public boolean canInsertElement(XMLCollection col, XMLElement el) {
        return canInsertElement(col, el, true);
    }

    public boolean canModifyElement(XMLElement el) {
        return canModifyElement(el, true);
    }

    public boolean canRemoveElement(XMLCollection col, XMLElement el) {
        return canRemoveElement(col, el, true);
    }

    public boolean canDuplicateElement(XMLCollection col, XMLElement el) {
        return canDuplicateElement(col, el, true);
    }

    public boolean canRepositionElement(XMLCollection col, XMLElement el) {
        return canRepositionElement(col, el, true);
    }

    // **********************
    // ********************** ChoiceButtonListener
    public void selectionChanged(ChoiceButton cbutton, Object change) {
        if (cbutton instanceof JaWETypeChoiceButton && ((JaWETypeChoiceButton) cbutton).getXPDLChoiceType() != null && cbutton.getChoiceType().equals(JaWEType.class)) {
            JaWEType jtype = (JaWEType) change;

            String typeId = null;
            if (jtype != null) {
                typeId = jtype.getTypeId();
            }

            Class xpdlClass = ((JaWETypeChoiceButton) cbutton).getXPDLChoiceType();
            if (xpdlClass == Package.class) {
                if (tryToClosePackage(getMainPackageId(), false)) {
                    newPackage(typeId);
                }
            } else {
                startUndouableChange();
                List temp = new ArrayList();
                XMLElement newEl = null;
                if (xpdlClass == Activities.class || xpdlClass == Activity.class) {
                    ActivitySet as = selectionMng.getWorkingActivitySet();
                    WorkflowProcess wp = selectionMng.getWorkingProcess();
                    if (as != null && !as.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(as.getActivities(), typeId, true);
                    } else if (wp != null && !wp.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(wp.getActivities(), typeId, true);
                    }
                } else if (xpdlClass == ActivitySet.class || xpdlClass == ActivitySets.class) {
                    WorkflowProcess wp = selectionMng.getWorkingProcess();
                    if (wp != null && !wp.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(wp.getActivitySets(), typeId, true);
                    }
                } else if (xpdlClass == Application.class || xpdlClass == Applications.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getApplications(), typeId, true);
                    }
                } else if (xpdlClass == DataField.class || xpdlClass == DataFields.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getDataFields(), typeId, true);
                    }
                } else if (xpdlClass == ExtendedAttribute.class || xpdlClass == ExtendedAttributes.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getExtendedAttributes(), typeId, true);
                    }
                } else if (xpdlClass == FormalParameter.class || xpdlClass == FormalParameters.class) {
                    WorkflowProcess wp = selectionMng.getWorkingProcess();
                    if (wp != null && !wp.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(wp.getFormalParameters(), typeId, true);
                    }
                } else if (xpdlClass == Namespace.class || xpdlClass == Namespaces.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getNamespaces(), typeId, true);
                    }
                } else if (xpdlClass == Participant.class || xpdlClass == Participants.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getParticipants(), typeId, true);
                    }
                } else if (xpdlClass == Responsible.class || xpdlClass == Responsibles.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getRedefinableHeader().getResponsibles(),
                                typeId,
                                true);
                    }
                } else if (xpdlClass == Transition.class || xpdlClass == Transitions.class) {
                    ActivitySet as = selectionMng.getWorkingActivitySet();
                    WorkflowProcess wp = selectionMng.getWorkingProcess();
                    if (as != null && !as.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(as.getTransitions(), typeId, true);
                    } else if (wp != null && !wp.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(wp.getTransitions(), typeId, true);
                    }
                } else if (xpdlClass == TypeDeclaration.class || xpdlClass == TypeDeclarations.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getTypeDeclarations(), typeId, true);
                    }
                } else if (xpdlClass == WorkflowProcess.class || xpdlClass == WorkflowProcesses.class) {
                    Package workingPkg = selectionMng.getWorkingPKG();
                    if (workingPkg != null && !workingPkg.isReadOnly()) {
                        newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(workingPkg.getWorkflowProcesses(), typeId, true);
                    }
                }
                if (newEl != null) {
                    temp.add(newEl);
                }
                endUndouableChange(temp);
            }
        } else {
            selectionMng.setSelection((XMLElement) change, true);
            updateTitle();
            adjustActions();
        }
    }

    public Object getSelectedObject(ChoiceButton cbutton) {
        return null;
    }

    public List getChoices(ChoiceButton cbutton) {
        ArrayList toRet = new ArrayList();
        if (cbutton instanceof JaWETypeChoiceButton) {
            Class xpdlElType = ((JaWETypeChoiceButton) cbutton).getXPDLChoiceType();
            toRet.addAll(jtypes.getTypes(xpdlElType));
        }

        return toRet;
    }

    // **********************
    public void init() {
        selectionMng = new JaWESelectionManager(this);
        try {
            String uhmc = getControllerSettings().undoHistoryManagerClass();
            if (uhmc != null && !uhmc.equals("")) {
                undoHistoryManager = (UndoHistoryManager) Class.forName(uhmc).newInstance();
                undoHistoryManager.init(getControllerSettings().undoHistorySize());
            }
        } catch (Exception ex) {
            System.err.println("Failed to instantiate undo history manager!");
        }

        try {
            String className = settings.getResolverTypeClassName();

            Constructor c = Class.forName(className).getConstructor(new Class[]{
                        JaWEController.class
                    });
            jtypeResolver = (JaWETypeResolver) c.newInstance(new Object[]{
                        this
                    });
            JaWEManager.getInstance().getLoggingManager().info("JaWEController -> Working with '" + className + "' as type resolver");
        } catch (Exception ex) {
            ex.printStackTrace();
            JaWEManager.getInstance().getLoggingManager().info("JaweManager -> Problems while instantiating type resolver! Using default!");
            jtypeResolver = new JaWETypeResolver(this);
        }

        defaultJaWEActions = new JaWEActions(this);
        defaultJaWEActions.init();

        settings.adjustActions();

        initPlugins();
        createJaWEFrame();
        updateTitle();
        clearAll();
    }

    private void initPlugins() {
        pluginsManager.initPlugins(JaWEManager.getProperties());
    }

    public void registerJaWEComponent(JaWEComponent jaweComponent) {
        if (getJaWEFrame() != null) {
            JComponent display = jaweComponent.getView().getDisplay();
            String name = jaweComponent.getName();
            String type = jaweComponent.getType();
            if (type.equals(JaWEComponent.MAIN_COMPONENT)) {
                getJaWEFrame().addMainComponent(name, display);
            } else if (type.equals(JaWEComponent.SPECIAL_COMPONENT)) {
                getJaWEFrame().addToSpecialComponents(name, display);
            } else if (type.equals(JaWEComponent.TREE_COMPONENT)) {
                getJaWEFrame().addToTreeComponents(name, display);
            } else if (type.equals(JaWEComponent.OTHER_COMPONENT)) {
                getJaWEFrame().addToOtherComponents(name, display);
            } else if (type.equals(JaWEComponent.UPPER_STATUS_COMPONENT)) {
                getJaWEFrame().addUpperStatusComponent(name, display);
            } else if (type.equals(JaWEComponent.LOWER_STATUS_COMPONENT)) {
                getJaWEFrame().addLowerStatusComponent(name, display);
            }
        }
    }

    public void removeJaWEComonent(JaWEComponent comp) {
        if (getJaWEFrame() != null) {
            String type = comp.getType();
            JComponent display = comp.getView().getDisplay();
            if (type.equals(JaWEComponent.MAIN_COMPONENT)) {
                getJaWEFrame().removeMainComponent(display);
            } else if (type.equals(JaWEComponent.SPECIAL_COMPONENT)) {
                getJaWEFrame().removeSpecialComponent(display);
            } else if (type.equals(JaWEComponent.TREE_COMPONENT)) {
                getJaWEFrame().removeTreeComponent(display);
            } else if (type.equals(JaWEComponent.OTHER_COMPONENT)) {
                getJaWEFrame().removeOtherComponent(display);
            } else if (type.equals(JaWEComponent.UPPER_STATUS_COMPONENT)) {
                getJaWEFrame().removeUpperStatusComponent(display);
            } else if (type.equals(JaWEComponent.LOWER_STATUS_COMPONENT)) {
                getJaWEFrame().removeLowerStatusComponent(display);
            }
        }
    }

    public void changeJaWEComponentType(JaWEComponent comp, String newType) {
        if (getJaWEFrame() != null) {
            removeJaWEComonent(comp);
            comp.setType(newType);
            registerJaWEComponent(comp);
        }
    }

    protected void createJaWEFrame() {
        //CUSTOM: catch HeadlessException
        try {
            frame = new JaWEFrame(this);
            frame.configure();
        } catch (HeadlessException e) {
            // ignore
        }
        //END CUSTOM
    }

    public void showJaWEFrame() {
        if (frame != null) {
            frame.setVisible(true);
            jaweFrameShown = true;
        }
    }

    public boolean isPackageModified(String xpdlId) {
        if (xpdlId == null) {
            xpdlId = getMainPackageId();
        }
        Iterator it = xpdlListenerObservables.iterator();
        while (it.hasNext()) {
            XPDLListenerAndObservable xpdl = (XPDLListenerAndObservable) it.next();
            if (xpdl.getPackage() != null && xpdl.getPackage().getId().equals(xpdlId)) {
                return xpdl.isModified();
            }
        }
        return false;
    }

    public Package getMainPackage() {
        return JaWEManager.getInstance().getXPDLHandler().getPackageById(getMainPackageId());
    }

    public String getMainPackageId() {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        String xpdlId = xpdlhandler.getMainPackageId();
        if (xpdlId == null && xpdlhandler.getAllPackageIds().size() > 0) {
            Iterator it = xpdlhandler.getAllPackages().iterator();
            while (it.hasNext()) {
                Package p = (Package) it.next();
                if (!p.isTransient() && !p.isReadOnly()) {
                    xpdlId = p.getId();
                    break;
                }
            }
        }
        return xpdlId;
    }

    // ///////////////////////////// XPDL File Handling /////////////////////////
    public String getPackageFilename(String xpdlId) {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        if (xpdlId == null) {
            xpdlId = getMainPackageId();
        }
        Package pkg = xpdlhandler.getPackageById(xpdlId);
        return xpdlhandler.getAbsoluteFilePath(pkg);
    }

    public void changePackageFileName(Package pkg, String newFileName) {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        xpdlhandler.registerPackageFilename(newFileName, pkg);
    }

    public void newPackage(String type) {
        clearAll();

        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> creating new XPDL, type=" + type);

        Package pkg = JaWEManager.getInstance().getXPDLObjectFactory().createPackage(type);
        xpdlhandler.registerPackage(pkg);
        adjustXPDL(pkg);
        createNewXPDLListenerObservable(pkg, true, true);
        setChanged();
        notifyObservers(createInfo(pkg, XMLElementChangeInfo.INSERTED));
        selectionMng.setSelection(pkg, true);

        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> new package with Id " + pkg.getId() + " is created");
        updateTitle();
        adjustActions();

        //CUSTOM
        JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(this.getMainPackage().getWorkflowProcesses(), JaWEConstants.WORKFLOW_PROCESS_TYPE_DEFAULT, true);
        GraphUtilities.getGraphController().getGraphMarqueeHandler().insertParticipant();

        //END CUSTOM
    }

    public Package openPackageFromFile(String filename) {
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> opening package from file " + filename);
        return openPackage(filename, null);
    }

    public Package openPackageFromStream(byte[] xpdlStream) {
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> opening package from stream ");
        return openPackage(null, xpdlStream);
    }

    protected Package openPackage(String filename, byte[] xpdlStream) {
        //CUSTOM: added exception handling for HeadlessException
        WaitScreen ws = null;
        try {
            ws = new WaitScreen(frame);
        } catch (HeadlessException e) {
            // ignore
        }
        XPDLHandler xpdlh = null;
        try {
            Package pkg = null;
            clearAll();
            XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
            if (jaweFrameShown && filename != null && xpdlStream == null) {
                if (ws != null) {
                    ws.show(null, "", settings.getLanguageDependentString("OpeningFile"));
                }
            }
            xpdlh = JaWEManager.getInstance().createXPDLHandler(xpdlhandler.getXPDLRepositoryHandler());

            try {
                if (filename != null) {
                    pkg = xpdlh.openPackage(filename, true);
                } else {
                    pkg = xpdlh.openPackageFromStream(xpdlStream, true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                clearAll();
                xpdlh.closeAllPackages();
                if (ws != null) {
                    ws.setVisible(false);
                }
                getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotOpenXPDL" + "\n" + ex.getMessage()),
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if (pkg != null) {
                // do not allow insertion if package has the same Id as the one already
                // inserted
                Set allpkgids = new HashSet(xpdlhandler.getAllPackageIds());
                boolean canInsert = true;
                if (allpkgids.contains(pkg.getId())) {
                    canInsert = false;
                }
                if (canInsert) {
                    xpdlhandler.synchronizePackages(xpdlh);

                    boolean mainChanged = false;
                    Iterator it = xpdlhandler.getAllPackages().iterator();
                    while (it.hasNext()) {
                        Package p = (Package) it.next();
                        boolean changed = false;
                        try {
                            changed = adjustXPDL(p);
                        } catch (Exception e) {
                            // ignore
                        }

                        XPDLListenerAndObservable xpdl;
                        if (p == pkg) {
                            xpdl = createNewXPDLListenerObservable(pkg, true, false);
                            if (changed) {
                                mainChanged = true;
                            }
                        } else {
                            xpdl = createNewXPDLListenerObservable(p, false, false);
                            p.setReadOnly(true);
                        }
                        xpdl.setModified(changed);
                    }

                    List inserted = new ArrayList();
                    inserted.add(pkg);

                    it = xpdlhandler.getAllPackages().iterator();
                    while (it.hasNext()) {
                        Package pkgext = (Package) it.next();
                        if (pkgext == pkg) {
                            continue;
                        }
                        inserted.add(pkgext);
                    }

                    setChanged();
                    notifyObservers(createInfo(pkg, inserted, XMLElementChangeInfo.INSERTED));

                    if (pkg.getWorkflowProcesses().size() > 0) {
                        selectionMng.setSelection(pkg.getWorkflowProcesses().get(0), true);
                    } else {
                        selectionMng.setSelection(pkg, true);
                    }

                    JaWEManager.getInstance().getLoggingManager().info("JaWEController -> opened package " + pkg.getId());
                    if (settings.isInitialXPDLValidationEnabled()) {
                        try {
                            checkValidity(pkg, true, true, true);
                        } catch (Exception e) {
                            // ignore
                        }

                    }

                    if ((mainChanged) && jaweFrameShown) {
                        if (mainChanged) {
                            if (ws != null) {
                                ws.setVisible(false);
                            }
                            // CUSTOM: hide warning message
//                            frame.message(settings.getLanguageDependentString("InformationTogWEHasAutomaticallyAdjustedSomeXPDLParts"),
//                                    JOptionPane.INFORMATION_MESSAGE);
                            // END CUSTOM
                        }
                    }

                } else {
                    if (getJaWEFrame() != null) {
                        getJaWEFrame().message(settings.getLanguageDependentString("InformationPackageCannotBeOpened"),
                                JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            } else {
                clearAll();
                Map pems = xpdlhandler.getParsingErrorMessages();
                String msg = null;
                if (pems != null && pems.size() > 0) {
                    Set s = (Set) pems.values().toArray()[0];
                    if (s != null && s.size() > 0) {
                        msg = (String) s.toArray()[0];
                    }
                }
                Iterator it = xpdlhandler.getAllPackages().iterator();
                while (it.hasNext()) {
                    Package p = (Package) it.next();
                    if (!p.isTransient()) {
                        xpdlhandler.closePackageVersion(p.getId(), p.getInternalVersion());
                    }
                }
                if (ws != null) {
                    ws.setVisible(false);
                }
                getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotOpenXPDL") + ((msg != null) ? "\n" + msg : ""),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            updateTitle();
            adjustActions();

            return pkg;
        } finally {
            if (ws != null) {
                ws.setVisible(false);
            }
        }
        //END CUSTOM
    }

    public void addExternalPackage(String filename) {
        Package mainPkg = getMainPackage();
        XPDLHandler xpdlhmain = JaWEManager.getInstance().getXPDLHandler();
        XPDLHandler xpdlh = null;
        if (filename != null && filename.length() > 0) {
            try {
                xpdlh = new XPDLHandler(new DefaultXPDLHandlerSettings());
                Package pkg = xpdlh.openPackage(filename, true);
                // do not allow insertion if package has the same Id as the main one,
                // or as some of its external packages
                Set mainPkgExtPkgIds = new HashSet(mainPkg.getExternalPackageIds());
                boolean canInsert = true;
                if (mainPkgExtPkgIds.contains(pkg.getId())) {
                    canInsert = false;
                }
                if (canInsert) {
                    canInsert = checkInsertion(xpdlhmain, xpdlh, mainPkg.getId());
                }
                if (canInsert) {
                    List l = XMLUtil.getAllExternalPackageIds(xpdlhmain,
                            mainPkg,
                            new HashSet());
                    if (!l.contains(pkg.getId()) && xpdlhmain.getPackageById(pkg.getId()) != null) {
                        canInsert = false;
                    }
                }
                if (canInsert) {
                    Set pkgIdsToInsert = new HashSet(xpdlh.getAllPackageIds());
                    Set otherEPIds = new HashSet(mainPkg.getExternalPackageIds());
                    Set allOtherEPIds = new HashSet(otherEPIds);
                    List l = XMLUtil.getAllExternalPackageIds(xpdlhmain,
                            mainPkg,
                            new HashSet());
                    List ids = new ArrayList(xpdlhmain.getAllPackageIds());
                    ids.removeAll(l);
                    allOtherEPIds.addAll(ids);
                    Iterator it = otherEPIds.iterator();
                    while (it.hasNext()) {
                        String pkgId = (String) it.next();
                        Package p = xpdlhmain.getPackageById(pkgId);
                        allOtherEPIds.addAll(XMLUtil.getAllExternalPackageIds(xpdlhmain,
                                p,
                                new HashSet()));
                    }
                    pkgIdsToInsert.removeAll(allOtherEPIds);

                    Set pkgsToInsert = new HashSet();
                    it = pkgIdsToInsert.iterator();
                    while (it.hasNext()) {
                        String pkgId = (String) it.next();
                        Package tAdd = xpdlh.getPackageById(pkgId);
                        pkgsToInsert.add(tAdd);
                    }

                    xpdlhmain.synchronizePackages(xpdlh);

                    Package realPkg = xpdlhmain.getPackageById(pkg.getId());
                    ExternalPackage ep = null;
                    // insert ExternalPackage
                    ExternalPackages eps = mainPkg.getExternalPackages();
                    File f = new File(xpdlhmain.getAbsoluteFilePath(mainPkg));
                    String parentF = f.getParent();
                    Path newPath = new Path(parentF);
                    String eppath = xpdlhmain.getAbsoluteFilePath(realPkg);
                    String relativePath = Path.getRelativePath(new Path(eppath), newPath);
                    ep = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(eps, "", false);
                    ep.setHref(relativePath);
                    JaWEEAHandler.setExternalPackageEA(ep, realPkg.getId());
                    mainPkg.addExternalPackageMapping(relativePath, realPkg.getId());
                    eps.add(ep);

                    it = pkgsToInsert.iterator();
                    while (it.hasNext()) {
                        Package p = (Package) it.next();
                        boolean changed = adjustXPDL(p);
                        XPDLListenerAndObservable xpdl;
                        xpdl = createNewXPDLListenerObservable(p, false, false);
                        p.setReadOnly(true);
                        xpdl.setModified(changed);
                    }

                    setChanged();
                    notifyObservers(createInfo(mainPkg,
                            new ArrayList(pkgsToInsert),
                            XMLElementChangeInfo.INSERTED));

                    if (ep != null) {
                        getSelectionManager().setSelection(ep, true);
                    }

                } else {
                    getJaWEFrame().message(settings.getLanguageDependentString("InformationExternalPackageCannotBeInserted"),
                            JOptionPane.INFORMATION_MESSAGE);
                    xpdlh.closeAllPackages();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                getJaWEFrame().message(settings.getLanguageDependentString("InformationExternalPackageCannotBeInserted"),
                        JOptionPane.INFORMATION_MESSAGE);
                if (xpdlh != null) {
                    xpdlh.closeAllPackages();
                }
            }
        }
    }

    public void addExternalPackage() {
        Package mainPkg = getMainPackage();
        XPDLHandler xpdlhmain = JaWEManager.getInstance().getXPDLHandler();
        getSelectionManager().setSelection(mainPkg.getExternalPackages(), false);

        String filename = "";
        String message = settings.getLanguageDependentString("DialogChooseFile");
        filename = JaWEXMLUtil.dialog(getJaWEFrame(), message, 0, 0, null);
        XPDLHandler xpdlh = null;
        if (filename != null && filename.length() > 0) {
            try {
                xpdlh = JaWEManager.getInstance().createXPDLHandler(xpdlhmain.getXPDLRepositoryHandler());
                Package pkg = xpdlh.openPackage(filename, true);
                // do not allow insertion if package has the same Id as the main one,
                // or as some of its external packages
                Set mainPkgExtPkgIds = new HashSet(mainPkg.getExternalPackageIds());
                boolean canInsert = true;
                if (mainPkgExtPkgIds.contains(pkg.getId())) {
                    canInsert = false;
                }
                if (canInsert) {
                    canInsert = checkInsertion(xpdlhmain, xpdlh, mainPkg.getId());
                }
                if (canInsert) {
                    List l = XMLUtil.getAllExternalPackageIds(xpdlhmain,
                            mainPkg,
                            new HashSet());
                    if (!l.contains(pkg.getId()) && xpdlhmain.getPackageById(pkg.getId()) != null) {
                        canInsert = false;
                    }
                }
                if (canInsert) {
                    Set pkgIdsToInsert = new HashSet(xpdlh.getAllPackageIds());
                    Set otherEPIds = new HashSet(mainPkg.getExternalPackageIds());
                    Set allOtherEPIds = new HashSet(otherEPIds);
                    List l = XMLUtil.getAllExternalPackageIds(xpdlhmain,
                            mainPkg,
                            new HashSet());
                    List ids = new ArrayList(xpdlhmain.getAllPackageIds());
                    ids.removeAll(l);
                    allOtherEPIds.addAll(ids);
                    Iterator it = otherEPIds.iterator();
                    while (it.hasNext()) {
                        String pkgId = (String) it.next();
                        Package p = xpdlhmain.getPackageById(pkgId);
                        allOtherEPIds.addAll(XMLUtil.getAllExternalPackageIds(xpdlhmain,
                                p,
                                new HashSet()));
                    }
                    pkgIdsToInsert.removeAll(allOtherEPIds);

                    Set pkgsToInsert = new HashSet();
                    it = pkgIdsToInsert.iterator();
                    while (it.hasNext()) {
                        String pkgId = (String) it.next();
                        Package tAdd = xpdlh.getPackageById(pkgId);
                        pkgsToInsert.add(tAdd);
                    }

                    xpdlhmain.synchronizePackages(xpdlh);

                    Package realPkg = xpdlhmain.getPackageById(pkg.getId());
                    ExternalPackage ep = null;
                    // insert ExternalPackage
                    ExternalPackages eps = mainPkg.getExternalPackages();
                    File f = new File(xpdlhmain.getAbsoluteFilePath(mainPkg));
                    String parentF = f.getParent();
                    Path newPath = new Path(parentF);
                    String eppath = xpdlhmain.getAbsoluteFilePath(realPkg);
                    String relativePath = Path.getRelativePath(new Path(eppath), newPath);
                    ep = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(eps, "", false);
                    ep.setHref(relativePath);
                    JaWEEAHandler.setExternalPackageEA(ep, realPkg.getId());
                    mainPkg.addExternalPackageMapping(relativePath, realPkg.getId());
                    eps.add(ep);

                    it = pkgsToInsert.iterator();
                    while (it.hasNext()) {
                        Package p = (Package) it.next();
                        boolean changed = adjustXPDL(p);
                        XPDLListenerAndObservable xpdl;
                        xpdl = createNewXPDLListenerObservable(p, false, false);
                        p.setReadOnly(true);
                        xpdl.setModified(changed);
                    }

                    setChanged();
                    notifyObservers(createInfo(mainPkg,
                            new ArrayList(pkgsToInsert),
                            XMLElementChangeInfo.INSERTED));

                    if (ep != null) {
                        getSelectionManager().setSelection(ep, true);
                    }

                } else {
                    getJaWEFrame().message(settings.getLanguageDependentString("InformationExternalPackageCannotBeInserted"),
                            JOptionPane.INFORMATION_MESSAGE);
                    xpdlh.closeAllPackages();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                getJaWEFrame().message(settings.getLanguageDependentString("InformationExternalPackageCannotBeInserted"),
                        JOptionPane.INFORMATION_MESSAGE);
                if (xpdlh != null) {
                    xpdlh.closeAllPackages();
                }
            }
        }
    }

    public void removeExternalPackage() {
        Package mainPkg = getMainPackage();
        XPDLHandler xpdlh = JaWEManager.getInstance().getXPDLHandler();
        Package toRemove = (Package) getSelectionManager().getSelectedElement();

        Set pkgIdsToRemove = new HashSet();
        pkgIdsToRemove.add(toRemove.getId());
        pkgIdsToRemove.addAll(XMLUtil.getAllExternalPackageIds(xpdlh,
                toRemove,
                new HashSet()));

        Set otherEPIds = new HashSet(mainPkg.getExternalPackageIds());
        otherEPIds.remove(toRemove.getId());

        Set allOtherEPIds = new HashSet(otherEPIds);
        Iterator it = otherEPIds.iterator();
        while (it.hasNext()) {
            String pkgId = (String) it.next();
            Package p = xpdlh.getPackageById(pkgId);
            allOtherEPIds.addAll(XMLUtil.getAllExternalPackageIds(xpdlh, p, new HashSet()));
        }

        pkgIdsToRemove.removeAll(allOtherEPIds);

        Set pkgsToRemove = new HashSet();
        it = pkgIdsToRemove.iterator();
        while (it.hasNext()) {
            String pkgId = (String) it.next();
            Package tRem = xpdlh.getPackageById(pkgId);
            if (tRem != mainPkg && !tRem.isTransient()) {
                pkgsToRemove.add(tRem);
            }
        }

        ExternalPackages eps = mainPkg.getExternalPackages();
        ExternalPackage ep = mainPkg.getExternalPackage(toRemove.getId());

        if (pkgsToRemove.size() > 0) {
            boolean warningMessage = false;
            it = pkgsToRemove.iterator();
            while (it.hasNext()) {
                Package tRem = (Package) it.next();
                List refs = JaWEManager.getInstance().getXPDLUtils().getReferences(mainPkg,
                        tRem);
                if (refs.size() > 0) {
                    warningMessage = true;
                    break;
                }
            }
            if (warningMessage) {
                int yn = JOptionPane.showConfirmDialog(getJaWEFrame(),
                        settings.getLanguageDependentString("MessageReferencedDoYouReallyWantToDeleteSelectedItem"),
                        settings.getLanguageDependentString("DeletingKey"),
                        JOptionPane.YES_NO_OPTION);
                if (yn != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }

        try {
            if (ep != null) {
                mainPkg.removeExternalPackageMapping(ep.getHref());
            }
            if (pkgsToRemove.size() > 0) {
                it = pkgsToRemove.iterator();
                while (it.hasNext()) {
                    Package pkg = (Package) it.next();
                    XPDLListenerAndObservable xpdllo = getXPDLListenerObservable(pkg);
                    xpdllo.unregisterFromXPDL();
                    xpdlListenerObservables.remove(xpdllo);
                    xpdlh.closePackageVersion(pkg.getId(), pkg.getInternalVersion());
                }
                setChanged();
                notifyObservers(createInfo(mainPkg,
                        new ArrayList(pkgsToRemove),
                        XMLElementChangeInfo.REMOVED));
            }
            if (ep != null) {
                eps.remove(ep);
                List remove = new ArrayList();
                remove.add(ep);
                setChanged();
                notifyObservers(createInfo(eps, remove, XMLElementChangeInfo.REMOVED));
            }
            getSelectionManager().setSelection(eps, true);

            if (pkgsToRemove.size() > 0) {
                if (settings.isDesingTimeValidationEnabled()) {
                    it = pkgsToRemove.iterator();
                    while (it.hasNext()) {
                        Package pkg = (Package) it.next();
                        JaWEManager.getInstance().getXPDLValidator().clearCache(pkg);
                    }
                    checkValidity(mainPkg, true, true, true);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotRemoveExternalPackage"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void addTransientPackage() {
        getSelectionManager().setSelection((XMLElement) null, true);

        String filename = "";
        String message = settings.getLanguageDependentString("DialogChooseFile");
        filename = JaWEXMLUtil.dialog(getJaWEFrame(), message, 0, 0, null);
        if (filename != null && filename.length() > 0) {
            boolean added = addTransientPackage(filename);
            if (!added) {
                getJaWEFrame().message(settings.getLanguageDependentString("InformationTransientPackageCannotBeInserted"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public boolean addTransientPackage(String filename) {
        XPDLHandler xpdlhmain = JaWEManager.getInstance().getXPDLHandler();
        XPDLHandler xpdlh = null;
        if (filename != null && filename.length() > 0) {
            try {
                xpdlh = JaWEManager.getInstance().createXPDLHandler(xpdlhmain.getXPDLRepositoryHandler());
                Package pkg = xpdlh.openPackage(filename, false);
                // do not allow insertion if package has the same Id as the main one,
                // or as some of its external packages
                Set allpkgids = new HashSet(xpdlhmain.getAllPackageIds());
                boolean canInsert = true;
                if (allpkgids.contains(pkg.getId())) {
                    canInsert = false;
                }
                if (canInsert) {
                    Package mainPkg = getMainPackage();
                    if (mainPkg != null) {
                        canInsert = checkInsertion(xpdlhmain, xpdlh, mainPkg.getId());
                    }
                }
                if (canInsert) {
                    xpdlhmain.synchronizePackages(xpdlh);

                    Package realPkg = xpdlhmain.getPackageById(pkg.getId());
                    adjustXPDL(realPkg);
                    XPDLListenerAndObservable xpdl = createNewXPDLListenerObservable(realPkg,
                            false,
                            false);
                    realPkg.setReadOnly(true);
                    xpdl.setModified(false);

                    List pkgsToInsert = new ArrayList();
                    pkgsToInsert.add(realPkg);
                    setChanged();
                    notifyObservers(createInfo(realPkg,
                            new ArrayList(pkgsToInsert),
                            XMLElementChangeInfo.INSERTED));

                    return true;
                }
                xpdlh.closeAllPackages();
                return false;
            } catch (Exception ex) {
                ex.printStackTrace();
                if (xpdlh != null) {
                    xpdlh.closeAllPackages();
                }
            }
        }
        return false;
    }

    public void removeTransientPackage() {
        XPDLHandler xpdlh = JaWEManager.getInstance().getXPDLHandler();
        Package toRemove = XMLUtil.getPackage(getSelectionManager().getSelectedElement());

        try {
            XPDLListenerAndObservable xpdllo = getXPDLListenerObservable(toRemove);
            xpdllo.unregisterFromXPDL();
            xpdlListenerObservables.remove(xpdllo);
            xpdlh.closePackageVersion(toRemove.getId(), toRemove.getInternalVersion());
            setChanged();
            List pkgsToRemove = new ArrayList();
            pkgsToRemove.add(toRemove);
            notifyObservers(createInfo(toRemove, pkgsToRemove, XMLElementChangeInfo.REMOVED));
            getSelectionManager().setSelection(getMainPackage(), true);
        } catch (Exception ex) {
            ex.printStackTrace();
            getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotRemoveTransientPackage"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected boolean checkInsertion(XPDLHandler xpdlhmain,
            XPDLHandler xpdlh,
            String mainPkgId) {
        boolean canInsert = true;
        Collection allPackages = xpdlh.getAllPackages();
        Iterator it = allPackages.iterator();
        while (it.hasNext()) {
            // do not allow insertion if package has the same path as the one of the main
            // package or some of the packages that are already referenced
            Package pkg = (Package) it.next();
            if (pkg.getId().equals(mainPkgId)) {
                canInsert = false;
                break;
            }

            String filename = xpdlh.getAbsoluteFilePath(pkg);
            Package pkgbfn = xpdlhmain.getPackageByFilename(filename);
            if (pkgbfn != null && !pkgbfn.getId().equals(pkg.getId())) {
                canInsert = false;
                break;
            }
        }
        return canInsert;
    }

    public void closePackage(String xpdlId, boolean closeTransient) {
        clearAll();
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        Package mainPackage = getMainPackage();
        if (xpdlId == null && mainPackage != null) {
            xpdlId = mainPackage.getId();
        }
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> closing package " + xpdlId);
        if (mainPackage != null && mainPackage.getId().equals(xpdlId)) {
            String filePath = JaWEManager.getInstance().getXPDLHandler().getAbsoluteFilePath(mainPackage);
            List allPackages = new ArrayList(xpdlhandler.getAllPackages());
            List notToClose = new ArrayList();
            for (int i = 0; i < allPackages.size(); i++) {
                Package pkg = (Package) allPackages.get(i);
                if (!pkg.isTransient() || closeTransient) {
                    xpdlhandler.closePackageVersion(pkg.getId(), pkg.getInternalVersion());
                } else {
                    notToClose.add(pkg);
                }
            }
            allPackages.removeAll(notToClose);
            setChanged();
            XPDLElementChangeInfo info = createInfo(mainPackage,
                    allPackages,
                    XMLElementChangeInfo.REMOVED);
            info.setOldValue(filePath);
            notifyObservers(info);
            clearXPDLListenerObservables(closeTransient);
        }
        JaWEManager.getInstance().getLoggingManager().info("JaWEController -> package " + xpdlId + " closed");
        updateTitle();
        frame.repaint();
        adjustActions();
        System.gc();
    }

    public boolean isSaveEnabled(boolean isSaveAs) {
        if (getMainPackageId() != null) {
            if (isSaveAs) {
                return true;
            }

            return isPackageModified(getMainPackageId());
        }

        return false;
    }

    public void savePackage(String xpdlId, String filename) {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        Package pkg = xpdlhandler.getPackageById(xpdlId);
        String oldFilename = xpdlhandler.getAbsoluteFilePath(pkg);
        try {

            // if SaveAs was performed and the document was previously saved,
            // change ExternalPackage's relative paths
            boolean isNewFile = !filename.equals(oldFilename);
            if (oldFilename != null && isNewFile) {
                boolean crossRefs = JaWEManager.getInstance().getXPDLUtils().doesCrossreferenceExist(pkg);
                int r = JOptionPane.YES_OPTION;
                if (crossRefs) {
                    r = JOptionPane.showConfirmDialog(getJaWEFrame(),
                            settings.getLanguageDependentString("MessageCrossReferenceExistDoYouWantToProceed"),
                            getJaWEFrame().getAppTitle(),
                            JOptionPane.YES_NO_OPTION);
                }
                if (r == JOptionPane.YES_OPTION) {
                    updateExternalPackagesRelativePaths(pkg, filename);
                } else {
                    return;
                }
            }

            Document document = null;

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbf.newDocumentBuilder();
            document = dbuilder.newDocument();

            // retrieve the file writter
            RandomAccessFile raf = xpdlhandler.getRaf(pkg);

            // output stream will either be the FileOutputStream in the
            // case of save as, or the ByteArrayOutputStream if we are
            // saving an existing file
            OutputStream os = null;
            try {
                if (isNewFile) {
                    // try to open random access file as rw, if it fails
                    // the saving shouldn't occur
                    RandomAccessFile r = null;
                    FileLock flck = null;
                    try {
                        File f = new File(filename);
                        r = new RandomAccessFile(f, "rw");
                        flck = r.getChannel().tryLock();

                        // this exception happens when using jdk1.4 under Linux
                        // if it happens, just catch it and proceed with saving
                        // because Linux with jdk1.4.0 doesn't support locking
                    } catch (IOException ioe) {
                        // ioe.printStackTrace();
                        // this happens when the locking fails, and null is returned,
                        // and after that release method is called on the null;
                        // This means that the file we want to save the given
                        // package as, is already locked, so we do not allow saving
                    } catch (NullPointerException npe) {
                        throw new Exception();
                    } finally {
                        try {
                            if (flck != null) {
                                flck.release();
                            }
                        } catch (Exception ex) {
                        }
                        try {
                            if (r != null) {
                                r.close(); // Harald Meister
                            }
                        } catch (Exception ex) {
                        }
                    }
                    // if we are at this point, this means either the locking
                    // succeeded, or we use jdk1.4 under Linux that does not
                    // support locking
                    os = new FileOutputStream(filename);
                } else {
                    os = new ByteArrayOutputStream();
                }

                // Here we get all document elements set
                JaWEManager.getInstance().getXPDLHandler().getXPDLRepositoryHandler().toXML(document, pkg);

                // Use a Transformer for output
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                transformer.setOutputProperty("indent", "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.setOutputProperty("encoding", settings.getEncoding());
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(os);
                transformer.transform(source, result);

                if (!isNewFile && raf != null && os instanceof ByteArrayOutputStream) {
                    if (raf != null) {
                        // must go to the beginning - otherwise, it will not
                        // truncate the file correctly in some Java-OS combination
                        raf.seek(0);
                        raf.getChannel().truncate(0);
                        raf.write(((ByteArrayOutputStream) os).toByteArray());
                    }
                }
            } finally {
                if (os != null) {
                    os.close();
                }
            }

            XPDLListenerAndObservable xpdl = getXPDLListenerObservable(pkg);
            if (xpdl != null) {
                xpdl.setModified(false);
            }

            if (isNewFile) {
                xpdlhandler.registerPackageFilename(filename, pkg);
            }
            try {
                System.setProperty("user.dir", xpdlhandler.getParentDirectory(pkg));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (NonWritableChannelException nwcex) {
            nwcex.printStackTrace();
            getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotSaveReadOnlyFile"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            getJaWEFrame().message(settings.getLanguageDependentString("ErrorCannotSaveDocument"),
                    JOptionPane.ERROR_MESSAGE);
        }
        updateTitle();
        adjustActions();
    }

    /*
     * Method for closing document. Returns true if the user really wants to close. Gives
     * chance to save work.
     */
    public boolean tryToClosePackage(String xpdlId, boolean closeTransient) {
        if (xpdlId == null) {
            xpdlId = getMainPackageId();
        }
        if (xpdlId == null) {
            return true;
        }
        int r = JOptionPane.NO_OPTION;
        boolean modified = isPackageModified(xpdlId);
        String filename = getPackageFilename(xpdlId);
        if (modified || filename == null) {
            r = JOptionPane.showConfirmDialog(getJaWEFrame(),
                    settings.getLanguageDependentString("DialogSaveChanges"),
                    getJaWEFrame().getAppTitle(),
                    JOptionPane.YES_NO_CANCEL_OPTION);
        }
        if (r == JOptionPane.YES_OPTION) {
            String dialogTitle = settings.getLanguageDependentString("Save" + BarFactory.LABEL_POSTFIX);
            if (filename == null) {
                dialogTitle = settings.getLanguageDependentString("SaveAs" + BarFactory.LABEL_POSTFIX);
            }

            if (filename == null) {
                filename = getJaWEFrame().saveDialog(dialogTitle, 0, xpdlId);
            }

            savePackage(xpdlId, filename);
            if (isPackageModified(xpdlId)) {
                r = JOptionPane.CANCEL_OPTION;
            }
        }
        if (r == JOptionPane.CANCEL_OPTION) {
            return false;
        }
        closePackage(xpdlId, closeTransient);
        return true;
    }

    protected XPDLListenerAndObservable createNewXPDLListenerObservable(Package pkg,
            boolean receiveEvents,
            boolean modified) {
        XPDLListenerAndObservable xpdl = new XPDLListenerAndObservable(pkg, receiveEvents);
        xpdl.setModified(modified);
        xpdlListenerObservables.add(xpdl);
        xpdl.addObserver(this);
        return xpdl;
    }

    protected XPDLListenerAndObservable getXPDLListenerObservable(Package pkg) {
        Iterator it = xpdlListenerObservables.iterator();
        while (it.hasNext()) {
            XPDLListenerAndObservable xpdl = (XPDLListenerAndObservable) it.next();
            if (xpdl.getPackage() == pkg) {
                return xpdl;
            }
        }
        return null;
    }

    protected XPDLListenerAndObservable getWorkingXPDLListenerObservable() {
        Iterator it = xpdlListenerObservables.iterator();
        while (it.hasNext()) {
            XPDLListenerAndObservable xpdl = (XPDLListenerAndObservable) it.next();
            if (xpdl.getPackage() == selectionMng.getWorkingPKG()) {
                return xpdl;
            }
        }
        return null;
    }

    protected void changePackageId(Package pkg, String oldId, String newId) {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        xpdlhandler.changePackageId(pkg, oldId, newId);
    }

    protected void updateExternalPackagesRelativePaths(Package pkg, String newFilename) {
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        File f = new File(newFilename);
        String parentF = f.getParent();
        Path newPath = new Path(parentF);

        Iterator eps = pkg.getExternalPackages().toElements().iterator();
        while (eps.hasNext()) {
            ExternalPackage ep = (ExternalPackage) eps.next();
            String oldRelativePath = ep.getHref();
            try {
                Package extP = xpdlhandler.getExternalPackageByRelativeFilePath(oldRelativePath,
                        pkg);
                String oldFullPath = xpdlhandler.getAbsoluteFilePath(extP);
                String relativePath = Path.getRelativePath(new Path(oldFullPath), newPath);
                ep.setHref(XMLUtil.replaceBackslashesWithSlashes(relativePath));
            } catch (Exception ex) {
                System.err.println("Failed to update old external package's relative path " + oldRelativePath + " for main package " + pkg.getId() + " with a new filename " + newFilename);
            }
        }
    }

    // ////////////////////////// END Of XPDL File Handling /////////////////////
    public XPDLElementChangeInfo createInfo(XMLElement el, int action) {
        XPDLElementChangeInfo info = new XPDLElementChangeInfo();
        info.setChangedElement(el);
        info.setAction(action);
        info.setSource(this);
        return info;
    }

    public XPDLElementChangeInfo createInfo(XMLElement main, List elements, int action) {
        XPDLElementChangeInfo info = new XPDLElementChangeInfo();
        info.setChangedElement(main);
        info.setChangedSubElements(elements);
        info.setAction(action);
        info.setSource(this);
        return info;
    }

    public boolean canCreateElement(XMLCollection col, boolean checkReadOnly) {
        if (checkReadOnly && col.isReadOnly()) {
            return false;
        }
        if (col instanceof TransitionRestrictions || col instanceof TransitionRefs || col instanceof ExternalPackages) {
            return false;
        }

        if (col instanceof Responsibles) {
            Map m = JaWEManager.getInstance().getXPDLUtils().getPossibleResponsibles((Responsibles) col, null);
            if (m.size() == 0) {
                return false;
            }
        }

        if (col instanceof Tools) {
            Map m = XMLUtil.getPossibleApplications(XMLUtil.getWorkflowProcess(col),
                    JaWEManager.getInstance().getXPDLHandler());
            if (m.size() == 0) {
                return false;
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canCreateElement(col);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canInsertElement(XMLCollection col, XMLElement el, boolean checkReadOnly) {
        if (checkReadOnly && col.isReadOnly()) {
            return false;
        }
        if (col instanceof TransitionRestrictions || col instanceof TransitionRefs || col instanceof ExternalPackages) {
            return false;
        } else if (col instanceof ExtendedAttributes && col.getParent() instanceof ExternalPackage) {
            if (el != null && ((ExtendedAttribute) el).getName().equals(JaWEEAHandler.EA_JAWE_EXTERNAL_PACKAGE_ID)) {
                return false;
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canInsertElement(col, el);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canModifyElement(XMLElement el, boolean checkReadOnly) {
        if (checkReadOnly && el.isReadOnly()) {
            return false;
        }
        if (el instanceof TransitionRef || el.getParent() instanceof TransitionRef) {
            return false;
        } else if (XMLUtil.getParentElement(ExternalPackage.class, el) != null) {
            if (el.toName().equals("href")) {
                return false;
            }
            ExtendedAttribute ea = (ExtendedAttribute) XMLUtil.getParentElement(ExtendedAttribute.class,
                    el);
            if (ea != null) {
                if (ea.getName().equals(JaWEEAHandler.EA_JAWE_EXTERNAL_PACKAGE_ID)) {
                    return false;
                }
            }
        }

        if ((el.getParent() instanceof Split || el.getParent() instanceof Join) && el.toName().equals("Type")) {
            if (el.getParent() instanceof Split) {
                Set ogt = XMLUtil.getOutgoingTransitions(XMLUtil.getActivity(el));
                if (ogt.size() <= 1) {
                    return false;
                }
            } else {
                Set inct = XMLUtil.getIncomingTransitions(XMLUtil.getActivity(el));
                if (inct.size() <= 1) {
                    return false;
                }
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canModifyElement(el);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canRemoveElement(XMLCollection col, XMLElement el, boolean checkReadOnly) {
        if (checkReadOnly && col.isReadOnly()) {
            return false;
        }
        if (col instanceof TransitionRestrictions || col instanceof TransitionRefs) {
            return false;
        } else if (col instanceof ExternalPackages) {
            if (el != null) {
                String href = ((ExternalPackage) el).getHref();
                Package pkg = XMLUtil.getPackage(col);
                String epId = pkg.getExternalPackageId(href);
                if (epId != null) {
                    return false;
                }
            }
        } else if (col instanceof ExtendedAttributes && col.getParent() instanceof ExternalPackage) {
            if (el != null && ((ExtendedAttribute) el).getName().equals(JaWEEAHandler.EA_JAWE_EXTERNAL_PACKAGE_ID)) {
                return false;
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canRemoveElement(col, el);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canDuplicateElement(XMLCollection col,
            XMLElement el,
            boolean checkReadOnly) {
        if (checkReadOnly && col.isReadOnly()) {
            return false;
        }
        if (el instanceof TransitionRestriction || el instanceof TransitionRef || el instanceof Member || el instanceof EnumerationValue || el instanceof ExternalPackage || el instanceof Responsible || el instanceof Transition) {
            return false;
        } else if (col instanceof ExtendedAttributes && col.getParent() instanceof ExternalPackage) {
            if (el != null && ((ExtendedAttribute) el).getName().equals(JaWEEAHandler.EA_JAWE_EXTERNAL_PACKAGE_ID)) {
                return false;
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canDuplicateElement(col, el);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canRepositionElement(XMLCollection col,
            XMLElement el,
            boolean checkReadOnly) {
        if (checkReadOnly && col.isReadOnly()) {
            return false;
        } else if (col instanceof ExtendedAttributes && col.getParent() instanceof ExternalPackage) {
            if (el != null && ((ExtendedAttribute) el).getName().equals(JaWEEAHandler.EA_JAWE_EXTERNAL_PACKAGE_ID)) {
                return false;
            }
        }

        Iterator comps = JaWEManager.getInstance().getComponentManager().getComponents().iterator();
        while (comps.hasNext()) {
            JaWEComponent jc = (JaWEComponent) comps.next();
            if (jc != this) {
                boolean approved = jc.canRepositionElement(col, el);
                if (approved == false) {
                    return false;
                }
            }
        }
        return true;
    }

    public void startUndouableChange() {
        Package mainPkg = getMainPackage();
        xpdlInfoList.clear();
        undoableChangeInProgress = true;
        undoSelectionEvent = getCurrentSelectionEvent();
        XPDLElementChangeInfo ucInfo = createInfo(mainPkg,
                XPDLElementChangeInfo.UNDOABLE_ACTION_STARTED);
        ucInfo.setChangedSubElements(xpdlInfoList);
        setChanged();
        notifyObservers(ucInfo);
    }

    public void endUndouableChange(List elementsToSelect) {
        Package mainPkg = getMainPackage();
        List infoList = new ArrayList(xpdlInfoList);

        Iterator it = infoList.iterator();
        while (it.hasNext()) {
            XPDLElementChangeInfo info = (XPDLElementChangeInfo) it.next();
            handleEvent(info);
        }

        XPDLElementChangeInfo ucInfo = createInfo(mainPkg,
                XPDLElementChangeInfo.ADJUST_UNDOABLE_ACTION);
        ucInfo.setChangedSubElements(xpdlInfoList);
        setChanged();
        notifyObservers(ucInfo);

        ucInfo = createInfo(mainPkg, XPDLElementChangeInfo.UNDOABLE_ACTION_ENDED);

        if (elementsToSelect != null && elementsToSelect.size() > 0) {
            if (!selectionMng.checkSelection(elementsToSelect)) {
                XMLElement firstToSelect = (XMLElement) elementsToSelect.get(0);
                elementsToSelect.clear();
                elementsToSelect.add(firstToSelect);
            }
        } else {
            elementsToSelect = new ArrayList();
            elementsToSelect.add(mainPkg);
        }

        ucInfo.setChangedSubElements(xpdlInfoList);

        updateInProgress = true;
        setChanged();
        notifyObservers(ucInfo);
        updateInProgress = false;

        if (undoHistoryManager != null) {
            undoHistoryManager.registerEvents(xpdlInfoList, undoSelectionEvent);
        }
        getSettings().adjustActions();
        JaWEManager.getInstance().getLoggingManager().debug("All events after undoable change:\n" + xpdlInfoList);
        xpdlInfoList.clear();
        undoableChangeInProgress = false;

        selectionMng.setSelection(elementsToSelect, true);
        if (settings.isDesingTimeValidationEnabled()) {
            checkValidity(mainPkg, true, false, true);
        }
    }

    public boolean isUndoableChangeInProgress() {
        return undoableChangeInProgress;
    }

    public boolean isUndoOrRedoInProgress() {
        if (undoHistoryManager != null) {
            return undoHistoryManager.isUndoOrRedoInProgress();
        }
        return false;
    }

    public void handleEvent(XPDLElementChangeInfo info) {
        XMLElement changedElement = info.getChangedElement();

        JaWEManager.getInstance().getLoggingManager().debug("INFO: handleEvenet(): " + info);

        int action = info.getAction();

        if (action == XPDLElementChangeInfo.SELECTED) {
            // should never happen
        } else {
            if (changedElement instanceof WorkflowProcesses) {
                if (action == XMLElementChangeInfo.REMOVED) {
                    if (info.getChangedSubElements() != null) {
                        Iterator it = info.getChangedSubElements().iterator();
                        while (it.hasNext()) {
                            WorkflowProcess wp = (WorkflowProcess) it.next();
                            if (wp == selectionMng.getWorkingProcess()) {
                                // select package
                                selectionMng.setSelection(wp.getParent().getParent(), true);
                                break;
                            }
                        }
                    }
                }
            } else if (changedElement instanceof ActivitySet) {
                if (action == XMLElementChangeInfo.REMOVED) {
                    if (info.getChangedSubElements() != null) {
                        Iterator it = info.getChangedSubElements().iterator();
                        while (it.hasNext()) {
                            ActivitySet as = (ActivitySet) it.next();
                            if (as == selectionMng.getWorkingActivitySet()) {
                                // select process
                                selectionMng.setSelection(as.getParent().getParent(), true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (action == XMLElementChangeInfo.INSERTED && (changedElement instanceof WorkflowProcesses || changedElement instanceof ActivitySets)) {
            if (info.getChangedSubElements() != null) {
                selectionMng.setSelection(info.getChangedSubElements(), true);
            }
        }

        updateTitle();
        adjustActions();

        if (action == XMLElementChangeInfo.REMOVED || action == XMLElementChangeInfo.INSERTED) {
            JaWEManager.getInstance().getLoggingManager().info("JaWEController -> performing appropriate actions on inserting/removing");
            if (changedElement instanceof Activities && action == XMLElementChangeInfo.REMOVED) {
                // for removing connected Transitions
                updateSpecialInProgress = true;
                JaWEManager.getInstance().getXPDLUtils().removeTransitionsForActivities(info.getChangedSubElements());
                selectionMng.removeFromSelection(info.getChangedSubElements());
                updateSpecialInProgress = false;
            } else if (changedElement instanceof Transitions) {
                Activities acts = (Activities) ((XMLCollectionElement) changedElement.getParent()).get("Activities");

                updateSpecialInProgress = true;
                JaWEManager.getInstance().getXPDLUtils().correctSplitsAndJoins(acts.toElements());
                updateSpecialInProgress = false;
                if (action == XMLElementChangeInfo.REMOVED) {
                    selectionMng.removeFromSelection(info.getChangedSubElements());
                }
            }
            JaWEManager.getInstance().getLoggingManager().info("JaWEController -> finished performing appropriate actions on inserting/removing");
        } else if (action == XMLElementChangeInfo.UPDATED) {
            XMLElement parent = changedElement.getParent();
            if ((changedElement.toName().equals("Id") && (parent instanceof WorkflowProcess || parent instanceof ActivitySet || parent instanceof Application || parent instanceof Participant || parent instanceof DataField || parent instanceof FormalParameter || parent instanceof Activity || parent instanceof Transition || parent instanceof TypeDeclaration)) || ((changedElement.toName().equals("From") || changedElement.toName().equals("To")) && parent instanceof Transition) || ((parent instanceof Split || parent instanceof Join) && changedElement instanceof XMLAttribute)) {

                if (parent instanceof Activity || parent instanceof Transition || parent instanceof Split || parent instanceof Join) {
                    XMLCollectionElement wpOrAs = XMLUtil.getActivitySet(parent);
                    if (wpOrAs == null) {
                        wpOrAs = XMLUtil.getWorkflowProcess(parent);
                    }
                    if (parent instanceof Activity) {
                        updateSpecialInProgress = true;
                        JaWEManager.getInstance().getXPDLUtils().updateTransitionsOnActivityIdChange((Transitions) wpOrAs.get("Transitions"),
                                (String) info.getOldValue(),
                                (String) info.getNewValue());
                        updateSpecialInProgress = false;
                    } else if (parent instanceof Transition) {
                        if (changedElement.toName().equals("Id")) {
                            updateSpecialInProgress = true;
                            JaWEManager.getInstance().getXPDLUtils().updateActivityOnTransitionIdChange((Activities) wpOrAs.get("Activities"),
                                    ((Transition) parent).getFrom(),
                                    (String) info.getOldValue(),
                                    (String) info.getNewValue());
                            updateSpecialInProgress = false;
                        } else if (changedElement.toName().equals("From")) {
                            updateSpecialInProgress = true;
                            JaWEManager.getInstance().getXPDLUtils().updateActivitiesOnTransitionFromChange((Activities) wpOrAs.get("Activities"),
                                    ((Transition) parent).getId(),
                                    (String) info.getOldValue(),
                                    (String) info.getNewValue());
                            updateSpecialInProgress = false;
                        } else {
                            updateSpecialInProgress = true;
                            JaWEManager.getInstance().getXPDLUtils().updateActivitiesOnTransitionToChange((Activities) wpOrAs.get("Activities"),
                                    ((Transition) parent).getId(),
                                    (String) info.getOldValue(),
                                    (String) info.getNewValue());
                            updateSpecialInProgress = false;
                        }
                    } else if (parent instanceof Split || parent instanceof Join) {
                        updateSpecialInProgress = true;
                        Activity act = XMLUtil.getActivity(parent);
                        JaWEManager.getInstance().getXPDLUtils().correctSplitAndJoin(act);
                        updateSpecialInProgress = false;
                    }
                } else if (parent instanceof TypeDeclaration) {
                    updateSpecialInProgress = true;
                    JaWEManager.getInstance().getXPDLUtils().updateTypeDeclarationReferences(JaWEManager.getInstance().getXPDLUtils().getTypeDeclarationReferences(XMLUtil.getPackage(parent),
                            (String) info.getOldValue()),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                } else if (parent instanceof WorkflowProcess) {
                    updateSpecialInProgress = true;
                    JaWEManager.getInstance().getXPDLUtils().updateWorkflowProcessReferences(JaWEManager.getInstance().getXPDLUtils().getWorkflowProcessReferences(XMLUtil.getPackage(parent),
                            (String) info.getOldValue()),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                } else if (parent instanceof ActivitySet) {
                    updateSpecialInProgress = true;
                    JaWEManager.getInstance().getXPDLUtils().updateActivitySetReferences(JaWEManager.getInstance().getXPDLUtils().getActivitySetReferences(XMLUtil.getWorkflowProcess(parent),
                            (String) info.getOldValue()),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                } else if (parent instanceof Application) {
                    updateSpecialInProgress = true;
                    JaWEManager.getInstance().getXPDLUtils().updateApplicationReferences(JaWEManager.getInstance().getXPDLUtils().getApplicationReferences((XMLComplexElement) parent.getParent().getParent(),
                            (String) info.getOldValue()),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                } else if (parent instanceof Participant) {
                    updateSpecialInProgress = true;
                    JaWEManager.getInstance().getXPDLUtils().updateParticipantReferences(JaWEManager.getInstance().getXPDLUtils().getParticipantReferences((XMLComplexElement) parent.getParent().getParent(),
                            (String) info.getOldValue()),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                } else if (parent instanceof DataField || (parent instanceof FormalParameter && parent.getParent().getParent() instanceof WorkflowProcess)) {
                    updateSpecialInProgress = true;
                    XMLComplexElement pkgOrWp = (XMLComplexElement) parent.getParent().getParent();
                    List refs = null;
                    if (parent instanceof DataField) {
                        refs = JaWEManager.getInstance().getXPDLUtils().getDataFieldReferences(pkgOrWp, (String) info.getOldValue());
                    } else {
                        refs = JaWEManager.getInstance().getXPDLUtils().getFormalParameterReferences((WorkflowProcess) pkgOrWp,
                                (String) info.getOldValue());
                    }
                    JaWEManager.getInstance().getXPDLUtils().updateVariableReferences(refs,
                            (String) info.getOldValue(),
                            (String) info.getNewValue());
                    updateSpecialInProgress = false;
                }
            }
        }

    }

    protected void clearHistory() {
        if (undoHistoryManager != null) {
            undoHistoryManager.cleanHistory();
            adjustActions();
        }
        undoSelectionEvent = null;
        System.gc();
    }

    protected void clearAll() {
        xpdlInfoList.clear();
        clearHistory();
        selectionMng.clear();
        edit.clear();
        undoSelectionEvent = null;
        StandardPackageValidator val = JaWEManager.getInstance().getXPDLValidator();
        if (val != null) {
            val.clearCache();
        }
        if (JaWEManager.getInstance().getXPDLElementEditor() != null && JaWEManager.getInstance().getXPDLElementEditor().getWindow() != null) {
            JaWEManager.getInstance().getXPDLElementEditor().close();
        }
        XPDLElementChangeInfo info = createInfo(null,
                new ArrayList(),
                XPDLElementChangeInfo.VALIDATION_ERRORS);
        info.setNewValue(new Boolean(false));
        sendEvent(info);
        info = createInfo(null, new ArrayList(), XPDLElementChangeInfo.REFERENCES);
        sendEvent(info);
        if (JaWEManager.getInstance().getTableEditor() != null) {
            JaWEManager.getInstance().getTableEditor().close();
        }

        System.gc();
    }

    protected void clearXPDLListenerObservables(boolean clearTransient) {
        List retain = new ArrayList();
        for (int i = 0; i < xpdlListenerObservables.size(); i++) {
            XPDLListenerAndObservable xpdllo = (XPDLListenerAndObservable) xpdlListenerObservables.get(i);
            if (clearTransient || !xpdllo.getPackage().isTransient()) {
                xpdllo.unregisterFromXPDL();
            } else {
                retain.add(xpdllo);
            }
        }
        xpdlListenerObservables.retainAll(retain);
        System.gc();
    }

    public void undo() {
        if (undoHistoryManager != null) {
            undoHistoryManager.undo();
            if (settings.isDesingTimeValidationEnabled()) {
                checkValidity(getMainPackage(), true, false, true);
            }
            getSettings().adjustActions();
        }
    }

    public void redo() {
        if (undoHistoryManager != null) {
            undoHistoryManager.redo();
            if (settings.isDesingTimeValidationEnabled()) {
                checkValidity(getMainPackage(), true, false, true);
            }
            getSettings().adjustActions();
        }
    }

    public void sendEvent(XPDLElementChangeInfo info) {
        setChanged();
        JaWEManager.getInstance().getLoggingManager().debug("Controller sending event: " + info);
        notifyObservers(info);
        updateTitle();
        adjustActions();
    }

    public XPDLElementChangeInfo getCurrentSelectionEvent() {
        // add will be selected event at the end
        List currentSelection = selectionMng.getSelectedElements();
        XMLElement selectionOwner = getMainPackage();
        if (currentSelection != null && currentSelection.size() > 0) {
            selectionOwner = (XMLElement) currentSelection.get(0);
            if (currentSelection.size() == 1) {
                currentSelection.clear();
            } else {
                selectionOwner = selectionOwner.getParent();
            }
        }
        XPDLElementChangeInfo selectionEvent = createInfo(selectionOwner,
                currentSelection,
                XPDLElementChangeInfo.SELECTED);
        return selectionEvent;
    }

    public boolean confirmDelete(List sel, XMLElement firstSelected) {
        XMLComplexElement pkgOrWP = getMainPackage();
        WorkflowProcess wp = XMLUtil.getWorkflowProcess(firstSelected);
        if (wp != null && wp != firstSelected) {
            pkgOrWP = wp;
        }

        String doNotAskOnDeletionOfReferencedElements = getControllerSettings().doNotAskOnDeletionOfReferencedElementTypes();
        Set notToAsk = refElsNotToAskOnDeletion(doNotAskOnDeletionOfReferencedElements);
        List refs = new ArrayList();
        if (getControllerSettings().shouldAskOnDeletionOfReferencedElements()) {
            for (int i = 0; i < sel.size(); i++) {
                XMLElement el = (XMLElement) sel.get(i);
                if (el instanceof Activity) {
                    if (!notToAsk.contains(el.toName())) {
                        refs.addAll(JaWEManager.getInstance().getXPDLUtils().getReferences((Activity) el));
                    }
                } else if (el instanceof Transition) {
                    if (!notToAsk.contains(el.toName())) {
                        refs.addAll(JaWEManager.getInstance().getXPDLUtils().getReferences((Transition) el));
                    }
                } else if (el instanceof XMLComplexElement) {
                    if (!notToAsk.contains(el.toName())) {
                        refs.addAll(JaWEManager.getInstance().getXPDLUtils().getReferences(pkgOrWP, (XMLComplexElement) el));
                    }
                }
            }
        }

        int yn = JOptionPane.YES_OPTION;
        if (getControllerSettings().shoudAskOnDeletion() || getControllerSettings().shouldAskOnDeletionOfReferencedElements()) {
            if (refs.size() == 0) {
                if (getControllerSettings().shoudAskOnDeletion()) {
                    yn = JOptionPane.showConfirmDialog(getJaWEFrame(),
                            getSettings().getLanguageDependentString("MessageDoYouReallyWantToRemoveSelectedItem"),
                            getSettings().getLanguageDependentString("DeletingKey"),
                            JOptionPane.YES_NO_OPTION);
                }
            } else {
                if (getControllerSettings().shouldAskOnDeletionOfReferencedElements()) {

                    yn = JOptionPane.showConfirmDialog(getJaWEFrame(),
                            getSettings().getLanguageDependentString("MessageReferencedDoYouReallyWantToDeleteSelectedItem"),
                            getSettings().getLanguageDependentString("DeletingKey"),
                            JOptionPane.YES_NO_OPTION);

                }
            }
        }
        if (yn == JOptionPane.YES_OPTION) {
            return true;
        }
        return false;
    }

    protected static Set refElsNotToAskOnDeletion(String hstr) {
        Set s = new HashSet();

        String[] hstra = Utils.tokenize(hstr, " ");
        if (hstra.length > 0) {
            s.addAll(Arrays.asList(hstra));
        }

        return s;
    }

    // Controller help classes
    public JaWEEdit getEdit() {
        return edit;
    }

    public JaWESelectionManager getSelectionManager() {
        return selectionMng;
    }

    public JaWEActions getJaWEActions() {
        return defaultJaWEActions;
    }

    public JaWEFrame getJaWEFrame() {
        return frame;
    }

    public void adjustActions() {
        //CUSTOM
        try {
            settings.adjustActions();
            defaultJaWEActions.enableDisableActions();
        } catch (NullPointerException e) {
            // ignore
        }
        //END CUSTOM
    }

    protected void updateTitle() {
        String title = "";
        if (getMainPackage() != null) {
            XPDLListenerAndObservable xpdllo = getXPDLListenerObservable(getMainPackage());
            if (xpdllo != null && xpdllo.isModified()) {
                title = "*";
            }
        }

        title += JaWEManager.getInstance().getName();

        String ccn = getCurrentConfigName();
        if (ccn == null) {
            ccn = "Default";
        }
        title += " (" + ccn + " " + getSettings().getLanguageDependentString("ConfigurationKey") + ")";

        if (getMainPackage() != null) {
            title += " - ";
            XPDLHandler xpdlh = JaWEManager.getInstance().getXPDLHandler();
            String s = xpdlh.getAbsoluteFilePath(getMainPackage());
            if (s == null || s.equals("")) {
                title += getSettings().getLanguageDependentString("NotSavedKey");
            } else {
                title += s;
            }
        }
        //CUSTOM
        if (getJaWEFrame() != null) {
            getJaWEFrame().setTitle(title);
        }
        //END CUSTOM

    }

    public JaWETypes getJaWETypes() {
        return jtypes;
    }

    public JaWETypeResolver getTypeResolver() {
        return jtypeResolver;
    }

    public ControllerSettings getControllerSettings() {
        return settings;
    }

    public void setUpdateInProgress(boolean inProgress) {
        updateInProgress = inProgress;
    }

    public boolean isUpdateInProgress() {
        return updateInProgress;
    }

    public UndoHistoryManager getUndoHistoryManager() {
        return undoHistoryManager;
    }

    /*
     * Begin of JPEd added methods to controller
     */
    ValueFilter valueFilter = new PluggableValueFilter();

    /**
     * Return the plugin manager. All operations on plugins
     * are done thru this manager.
     * @since JPEd-plugins
     */
    public PluginsManager getPluginsManager() {
        return pluginsManager;
    }

    /**
     * Acquire the global value filter. It may dispatch to plugins
     * depending on interface implementation. Always use a ValueFilter
     * instead of calling XMLElement.toValue() directly
     * @return The ValueFilter
     * @since JPEd-plugins
     */
    public ValueFilter getValueFilter() {
        return valueFilter;
    }

    /**
     * shortcut to getValueFilter().getValue(element.get(property))
     * @param element the XMLComplexElement which property is to be filtered
     * @param property the property to filter
     * @return the filtered property or null of property does not exist
     */
    public String getFilteredProperty(XMLComplexElement element, String property) {
        if (element == null) {
            return null;
        }
        XMLElement elt = element.get(property);
        return elt == null ? null : getValueFilter().getValue(elt);
    }

    public String getCurrentConfig() {
        if (currentConfig == null) {
            fillConfigInfo();
            currentConfig = "default";
            String cch = System.getProperty(JaWEConstants.JAWE_CURRENT_CONFIG_HOME);
            if (cch != null) {
                int indofs = cch.lastIndexOf("/");
                int indofbs = cch.lastIndexOf("\\");
                int ind = Math.max(indofs, indofbs);
                if (ind >= 0) {
                    cch = cch.substring(ind + 1);
                }
                currentConfig = cch;
            }
        }
        return currentConfig;
    }

    public String getCurrentConfigName() {
        String cc = JaWEManager.getInstance().getJaWEController().getCurrentConfig();
        return (String) configInfo.get(cc);
    }

    public String getConfigId(String cfgName) {
        if (configInfo != null) {
            return getConfigId(configInfo, cfgName);
        }
        return cfgName;
    }

    protected String getConfigId(Map ci, String cfgName) {
        Iterator it = ci.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry me = (Map.Entry) it.next();
            if (me.getValue().equals(cfgName)) {
                return (String) me.getKey();
            }
        }
        return cfgName;
    }

    public Map getConfigInfo() {
        if (configInfo == null) {
            fillConfigInfo();
        }
        return Collections.unmodifiableMap(configInfo);
    }

    protected void fillConfigInfo() {
        Map m = new HashMap();
        if (JaWEConstants.JAWE_CONF_HOME != null) {
            File mainConfFolder = new File(JaWEConstants.JAWE_CONF_HOME);
            if (mainConfFolder.exists()) {
                File[] confs = mainConfFolder.listFiles();
                for (int i = 0; i < confs.length; i++) {
                    if (confs[i].isDirectory()) {
                        m.put(confs[i].getName(), readConfigName(confs[i].getName()));
                    }
                }
            }
        }
        List l = new ArrayList(m.values());
        Collections.sort(l);

        configInfo = new SequencedHashMap();
        for (int i = 0; i < l.size(); i++) {
            String cfgName = (String) l.get(i);
            configInfo.put(getConfigId(m, cfgName), cfgName);
        }
    }

    protected String readConfigName(String configFolder) {
        String fn = JaWEConstants.JAWE_CONF_HOME + "/" + configFolder + "/jaweconfigname";
        File file = new File(fn);
        if (file.exists()) {
            Properties props = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(fn);
                props.load(fis);
                return props.getProperty(JaWEConstants.JAWE_CONFIG_NAME, configFolder);
            } catch (Exception ex) {
                throw new Error("Something went wrong while reading external component properties !!!",
                        ex);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception ex) {
                }
            }
        }

        return configFolder;
    }
}
