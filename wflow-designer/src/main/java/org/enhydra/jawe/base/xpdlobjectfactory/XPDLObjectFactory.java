package org.enhydra.jawe.base.xpdlobjectfactory;

import java.lang.reflect.Method;
import java.util.HashSet;

import java.util.Map;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEConstants;
import org.enhydra.jawe.JaWEEAHandler;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Utils;
import org.enhydra.jawe.base.controller.JaWETypes;
import org.enhydra.jawe.base.xpdlhandler.XPDLHandler;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ActivitySet;
import org.enhydra.shark.xpdl.elements.ActivitySets;
import org.enhydra.shark.xpdl.elements.ActualParameter;
import org.enhydra.shark.xpdl.elements.ActualParameters;
import org.enhydra.shark.xpdl.elements.Application;
import org.enhydra.shark.xpdl.elements.Applications;
import org.enhydra.shark.xpdl.elements.DataField;
import org.enhydra.shark.xpdl.elements.DataFields;
import org.enhydra.shark.xpdl.elements.Deadline;
import org.enhydra.shark.xpdl.elements.Deadlines;
import org.enhydra.shark.xpdl.elements.EnumerationType;
import org.enhydra.shark.xpdl.elements.EnumerationValue;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;
import org.enhydra.shark.xpdl.elements.ExternalPackage;
import org.enhydra.shark.xpdl.elements.ExternalPackages;
import org.enhydra.shark.xpdl.elements.FormalParameter;
import org.enhydra.shark.xpdl.elements.FormalParameters;
import org.enhydra.shark.xpdl.elements.Member;
import org.enhydra.shark.xpdl.elements.Namespace;
import org.enhydra.shark.xpdl.elements.Namespaces;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.PackageHeader;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.Participants;
import org.enhydra.shark.xpdl.elements.RecordType;
import org.enhydra.shark.xpdl.elements.Responsible;
import org.enhydra.shark.xpdl.elements.Responsibles;
import org.enhydra.shark.xpdl.elements.Tool;
import org.enhydra.shark.xpdl.elements.Tools;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.TransitionRef;
import org.enhydra.shark.xpdl.elements.TransitionRefs;
import org.enhydra.shark.xpdl.elements.TransitionRestriction;
import org.enhydra.shark.xpdl.elements.TransitionRestrictions;
import org.enhydra.shark.xpdl.elements.Transitions;
import org.enhydra.shark.xpdl.elements.TypeDeclaration;
import org.enhydra.shark.xpdl.elements.TypeDeclarations;
import org.enhydra.shark.xpdl.elements.UnionType;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.WorkflowProcesses;

public class XPDLObjectFactory {

    protected XPDLObjectFactorySettings settings;

    public XPDLObjectFactory() {
        settings = new XPDLObjectFactorySettings();
        settings.init((JaWEComponent) null);
    }

    public XPDLObjectFactory(XPDLObjectFactorySettings settings) {
        this.settings = settings;
        this.settings.init((JaWEComponent) null);
    }

    public Package createPackage(String type) {
        Package pkg = new Package();
        adjustType(pkg, type);
        XPDLHandler xpdlhandler = JaWEManager.getInstance().getXPDLHandler();
        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(pkg, type);
        }
        String id = pkg.getId();
        if (!hasTemplate || id.equals("") || xpdlhandler.getPackageById(id) != null) {
            int i = 0;
            while (id.equals("") || xpdlhandler.getPackageById(id) != null) {
                id = "package" + String.valueOf(++i);
            }
            pkg.setId(id);
        }
        if (!hasTemplate || pkg.getName().equals("")) {
            //CUSTOM
            pkg.setName("New Package");
            //END CUSTOM
        }
        PackageHeader ph = pkg.getPackageHeader();
        if (!hasTemplate) {
            ph.setXPDLVersion("1.0");
            ph.setVendor("Together");
        }
        ph.setCreated(Utils.getCurrentDateAndTime());

        Namespaces nss = pkg.getNamespaces();
        Namespace ns = createXPDLObject(nss, "", true);
        ns.setName("xpdl");
        ns.setLocation("http://www.wfmc.org/2002/XPDL1.0");
        adjustXPDLObject(pkg, type);

        return pkg;
    }

    public ActivitySet createXPDLObject(ActivitySets ass,
            String type,
            boolean addToCollection) {
        ActivitySet as = (ActivitySet) ass.generateNewElement();
        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        adjustType(as, type);
        if (hasTemplate) {
            jts.fillFromTemplate(as, type);
        }
        String id = as.getId();
        if (!hasTemplate || id.equals("") || ass.getActivitySet(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(ass);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(ass, new HashSet(), id);
            }
            int i = 0;
            while (ass.getActivitySet(id) != null) {
                id = id + String.valueOf(++i);
            }
            as.setId(id);
        }

        adjustXPDLObject(as, type);

        if (addToCollection) {
            ass.add(as);
        }
        return as;
    }

    public Activity createXPDLObject(Activities acts, String type, boolean addToCollection) {
        Activity act = null;
        if (type == null || type.equals("")) {
            type = JaWEConstants.ACTIVITY_TYPE_TOOL;
        }
        if (type.equals(JaWEConstants.ACTIVITY_TYPE_BLOCK) || type.equals(JaWEConstants.ACTIVITY_TYPE_NO) || type.equals(JaWEConstants.ACTIVITY_TYPE_ROUTE) || type.equals(JaWEConstants.ACTIVITY_TYPE_SUBFLOW) || type.equals(JaWEConstants.ACTIVITY_TYPE_TOOL)) {
            act = createStandardActivity(acts, type);
        } else {
            act = createSpecialActivity(acts, type);
        }

        adjustXPDLObject(act, type);

        if (addToCollection) {
            acts.add(act);
        }
        return act;
    }

    protected Activity createStandardActivity(Activities acts, String type) {
        Activity act = createXPDLActivity(acts, type);
        //CUSTOM
        String num = act.getId().substring("activity".length());
        act.setName("Activity " + num);
        //END CUSTOM
        if (type.equals(JaWEConstants.ACTIVITY_TYPE_BLOCK)) {
            act.getActivityTypes().setBlockActivity();
        } else if (type.equals(JaWEConstants.ACTIVITY_TYPE_ROUTE)) {
            int i = 1;
            String baseId = "route";
            String id = baseId + i;
            while (acts.getActivity(id) != null) {
                id = baseId + String.valueOf(++i);
            }
            act.setId(id);
            act.setName("Route " + i);
            act.getActivityTypes().setRoute();
        } else if (type.equals(JaWEConstants.ACTIVITY_TYPE_SUBFLOW)) {
            int i = 1;
            String baseId = "subflow";
            String id = baseId + i;
            while (acts.getActivity(id) != null) {
                id = baseId + String.valueOf(++i);
            }
            act.setId(id);
            act.setName("Subflow " + i);
            act.getActivityTypes().setImplementation();
            act.getActivityTypes().getImplementation().getImplementationTypes().setSubFlow();
        } else if (type.equals(JaWEConstants.ACTIVITY_TYPE_TOOL)) {
            //CUSTOM
            int i = 1;
            String baseId = "tool";
            String id = baseId + i;
            while (acts.getActivity(id) != null) {
                id = baseId + String.valueOf(++i);
            }
            act.setId(id);
            act.setName("Tool " + i);
            //END CUSTOM
            act.getActivityTypes().setImplementation();
            act.getActivityTypes().getImplementation().getImplementationTypes().setTools();

            // CUSTOM
            String defaultAppId = "default_application";
            // look for default tool
            XMLElement el = act.getParent();
            while (el.getParent() != null) {
                el = el.getParent();
            }
            Package pkg = (Package) el;
            Applications apps = pkg.getApplications();
            Application defaultApp = apps.getApplication(defaultAppId);
            if (defaultApp == null) {
                // create default app
                defaultApp = createXPDLObject(apps, null, true);
                defaultApp.setId(defaultAppId);
            }

            // create tool mapping
            Tools tools = act.getActivityTypes().getImplementation().getImplementationTypes().getTools();
            Tool tool = createXPDLObject(tools, null, true);
            tool.setId(defaultAppId);
            // END CUSTOM
        } else if (type.equals(JaWEConstants.ACTIVITY_TYPE_NO)) {
            act.getActivityTypes().setImplementation();
            act.getActivityTypes().getImplementation().getImplementationTypes().setNo();

            // CUSTOM: grab every available workflow variable from the workflow process that it belongs to and add as extended attribute (view only)
            WorkflowProcess process = (WorkflowProcess) act.getParent().getParent();
            Map m = XMLUtil.getPossibleVariables(process);
            XMLCollectionElement dataFields[] = (XMLCollectionElement[]) m.values().toArray(new XMLCollectionElement[0]);
            for (int i = 0; i < dataFields.length; i++) {
                ExtendedAttribute attribute = this.createXPDLObject(act.getExtendedAttributes(), null, true);
                attribute.setName("VariableToProcess_UPDATE");
                attribute.setVValue(dataFields[i].getId());
            }
            // END CUSTOM
        }
        return act;
    }

    protected Activity createSpecialActivity(Activities acts, String type) {
        Activity act = createXPDLActivity(acts, type);
        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);

        if (!hasTemplate) {
            act.getActivityTypes().getImplementation().getImplementationTypes().setTools();
            act.getActivityTypes().setImplementation();
        }
        return act;
    }

    protected Activity createXPDLActivity(Activities acts, String type) {
        Activity act = (Activity) acts.generateNewElement();
        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(act, type);
        }
        String id = act.getId();
        if (!hasTemplate || id.equals("") || acts.getActivity(id) != null) {

            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(acts);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(acts, new HashSet(), id);
            }

            int i = 0;
            while (acts.getActivity(id) != null) {
                id = id + String.valueOf(++i);
            }
            act.setId(id);
        }

        return act;
    }

    public ActualParameter createXPDLObject(ActualParameters aps,
            String type,
            boolean addToCollection) {
        ActualParameter ap = (ActualParameter) aps.generateNewElement();
        adjustType(ap, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(ap, type);
        }

        adjustXPDLObject(ap, type);

        if (addToCollection) {
            aps.add(ap);
        }
        return ap;
    }

    public Application createXPDLObject(Applications apps,
            String type,
            boolean addToCollection) {
        Application app = (Application) apps.generateNewElement();
        adjustType(app, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(app, type);
        }
        String id = app.getId();
        if (!hasTemplate || id.equals("") || apps.getApplication(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(apps);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(apps, new HashSet(), id);
            }
            int i = 0;
            while (apps.getApplication(id) != null) {
                id = id + String.valueOf(++i);
            }
            app.setId(id);
        }

        adjustXPDLObject(app, type);

        if (addToCollection) {
            apps.add(app);
        }
        return app;
    }

    public DataField createXPDLObject(DataFields dfs, String type, boolean addToCollection) {
        DataField df = (DataField) dfs.generateNewElement();
        adjustType(df, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(df, type);
        }
        String id = df.getId();
        if (!hasTemplate || id.equals("") || dfs.getDataField(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(dfs);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(dfs, new HashSet(), id);
            }
            int i = 0;
            while (dfs.getDataField(id) != null) {
                id = id + String.valueOf(++i);
            }
            df.setId(id);
        }

        adjustXPDLObject(df, type);

        if (addToCollection) {
            dfs.add(df);
        }

        return df;
    }

    public Deadline createXPDLObject(Deadlines dls, String type, boolean addToCollection) {
        Deadline dl = (Deadline) dls.generateNewElement();
        adjustType(dl, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(dl, type);
        }

        adjustXPDLObject(dl, type);

        if (addToCollection) {
            dls.add(dl);
        }
        return dl;
    }

    public EnumerationValue createXPDLObject(EnumerationType et,
            String type,
            boolean addToCollection) {
        EnumerationValue ev = (EnumerationValue) et.generateNewElement();
        adjustType(ev, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(ev, type);
        }

        adjustXPDLObject(ev, type);

        if (addToCollection) {
            et.add(ev);
        }
        return ev;
    }

    public ExtendedAttribute createXPDLObject(ExtendedAttributes eas,
            String type,
            boolean addToCollection) {
        ExtendedAttribute ea = (ExtendedAttribute) eas.generateNewElement();
        adjustType(ea, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(ea, type);
        }

        adjustXPDLObject(ea, type);

        if (addToCollection) {
            eas.add(ea);
        }
        return ea;
    }

    public ExternalPackage createXPDLObject(ExternalPackages eps,
            String type,
            boolean addToCollection) {
        ExternalPackage ep = (ExternalPackage) eps.generateNewElement();
        adjustType(ep, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(ep, type);
        }

        adjustXPDLObject(ep, type);

        if (addToCollection) {
            eps.add(ep);
        }
        return ep;
    }

    public FormalParameter createXPDLObject(FormalParameters fps,
            String type,
            boolean addToCollection) {
        FormalParameter fp = (FormalParameter) fps.generateNewElement();
        adjustType(fp, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(fp, type);
        }
        String id = fp.getId();
        if (!hasTemplate || id.equals("") || fps.getFormalParameter(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(fps);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(fps, new HashSet(), id);
            }
            int i = 0;
            while (fps.getFormalParameter(id) != null) {
                id = id + String.valueOf(++i);
            }
            fp.setId(id);
        }

        adjustXPDLObject(fp, type);

        if (addToCollection) {
            fps.add(fp);
        }
        return fp;
    }

    public Namespace createXPDLObject(Namespaces nss, String type, boolean addToCollection) {
        Namespace ns = (Namespace) nss.generateNewElement();
        adjustType(ns, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(ns, type);
        }

        adjustXPDLObject(ns, type);

        if (addToCollection) {
            nss.add(ns);
        }
        return ns;
    }

    public Participant createXPDLObject(Participants ps,
            String type,
            boolean addToCollection) {
        Participant par = (Participant) ps.generateNewElement();
        adjustType(par, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(par, type);
        }
        String id = par.getId();
        if (!hasTemplate || id.equals("") || ps.getParticipant(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(ps);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(ps, new HashSet(), id);
            }
            int i = 0;
            while (ps.getParticipant(id) != null) {
                id = id + String.valueOf(++i);
            }
            par.setId(id);
        }

        if (!hasTemplate || par.getName().equals("")) {
            //CUSTOM
            String num = id.substring("participant".length());
            par.setName("Participant " + num);
            //END CUSTOM
        }

        if (type == null || type.equals("")) {
            type = JaWEConstants.PARTICIPANT_TYPE_ROLE;
        }

        if (type.equals(JaWEConstants.PARTICIPANT_TYPE_HUMAN)) {
            par.getParticipantType().setTypeHUMAN();
        } else if (type.equals(JaWEConstants.PARTICIPANT_TYPE_ROLE)) {
            par.getParticipantType().setTypeROLE();
        } else if (type.equals(JaWEConstants.PARTICIPANT_TYPE_ORGANIZATIONAL_UNIT)) {
            par.getParticipantType().setTypeORGANIZATIONAL_UNIT();
        } else if (type.equals(JaWEConstants.PARTICIPANT_TYPE_RESOURCE)) {
            par.getParticipantType().setTypeRESOURCE();
        } else if (type.equals(JaWEConstants.PARTICIPANT_TYPE_RESOURCE_SET)) {
            par.getParticipantType().setTypeRESOURCE_SET();
        } else if (type.equals(JaWEConstants.PARTICIPANT_TYPE_SYSTEM)) {
            par.getParticipantType().setTypeSYSTEM();
        }

        adjustXPDLObject(par, type);

        if (addToCollection) {
            ps.add(par);
        }
        return par;
    }

    public Member createXPDLObject(RecordType rt, String type, boolean addToCollection) {
        Member m = (Member) rt.generateNewElement();
        adjustType(m, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(m, type);
        }

        adjustXPDLObject(m, type);

        if (addToCollection) {
            rt.add(m);
        }
        return m;
    }

    public Responsible createXPDLObject(Responsibles rs,
            String type,
            boolean addToCollection) {
        Responsible r = (Responsible) rs.generateNewElement();
        adjustType(r, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(r, type);
        }

        adjustXPDLObject(r, type);

        if (addToCollection) {
            rs.add(r);
        }
        return r;
    }

    public Tool createXPDLObject(Tools ts, String type, boolean addToCollection) {
        Tool t = (Tool) ts.generateNewElement();
        adjustType(t, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(t, type);
        }

        adjustXPDLObject(t, type);

        if (addToCollection) {
            ts.add(t);
        }
        return t;
    }

    public Transition createXPDLObject(Transitions tras,
            String type,
            boolean addToCollection) {
        if (type == null || type.equals("")) {
            type = JaWEConstants.TRANSITION_TYPE_UNCONDITIONAL;
        }

        Transition tra = createXPDLTransition(tras, type);
        if (type.equals(JaWEConstants.TRANSITION_TYPE_CONDITIONAL)) {
            tra.getCondition().setTypeCONDITION();
        } else if (type.equals(JaWEConstants.TRANSITION_TYPE_OTHERWISE)) {
            tra.getCondition().setTypeOTHERWISE();
        } else if (type.equals(JaWEConstants.TRANSITION_TYPE_EXCEPTION)) {
            tra.getCondition().setTypeEXCEPTION();
        } else if (type.equals(JaWEConstants.TRANSITION_TYPE_DEFAULTEXCEPTION)) {
            tra.getCondition().setTypeDEFAULTEXCEPTION();
        }

        adjustXPDLObject(tra, type);

        if (addToCollection) {
            tras.add(tra);
        }
        return tra;
    }

    protected Transition createXPDLTransition(Transitions tras, String type) {
        Transition tra = (Transition) tras.generateNewElement();
        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(tra, type);
        }
        String id = tra.getId();
        if (!hasTemplate || id.equals("") || tras.getTransition(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(tras);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(tras, new HashSet(), id);
            }
            int i = 0;
            while (tras.getTransition(id) != null) {
                id = id + String.valueOf(++i);
            }
            tra.setId(id);
        }

        return tra;
    }

    public TransitionRef createXPDLObject(TransitionRefs trs,
            String type,
            boolean addToCollection) {
        TransitionRef tr = (TransitionRef) trs.generateNewElement();
        adjustType(tr, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(tr, type);
        }

        adjustXPDLObject(tr, type);

        if (addToCollection) {
            trs.add(tr);
        }
        return tr;
    }

    public TransitionRestriction createXPDLObject(TransitionRestrictions trests,
            String type,
            boolean addToCollection) {
        TransitionRestriction tres = (TransitionRestriction) trests.generateNewElement();
        adjustType(tres, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(tres, type);
        }

        adjustXPDLObject(tres, type);

        if (addToCollection) {
            trests.add(tres);
        }
        return tres;
    }

    public TypeDeclaration createXPDLObject(TypeDeclarations tds,
            String type,
            boolean addToCollection) {
        TypeDeclaration td = (TypeDeclaration) tds.generateNewElement();
        adjustType(td, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(td, type);
        }
        String id = td.getId();
        if (!hasTemplate || id.equals("") || tds.getTypeDeclaration(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(tds);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(tds, new HashSet(), id);
            }
            int i = 0;
            while (tds.getTypeDeclaration(id) != null) {
                id = id + String.valueOf(++i);
            }
            td.setId(id);
        }

        adjustXPDLObject(td, type);

        if (addToCollection) {
            tds.add(td);
        }
        return td;
    }

    public Member createXPDLObject(UnionType ut, String type, boolean addToCollection) {
        Member m = (Member) ut.generateNewElement();
        adjustType(m, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(m, type);
        }

        adjustXPDLObject(m, type);

        if (addToCollection) {
            ut.add(m);
        }
        return m;
    }

    public WorkflowProcess createXPDLObject(WorkflowProcesses wps,
            String type,
            boolean addToCollection) {
        WorkflowProcess wp = (WorkflowProcess) wps.generateNewElement();
        adjustType(wp, type);

        JaWETypes jts = JaWEManager.getInstance().getJaWEController().getJaWETypes();
        boolean hasTemplate = jts.hasTemplateId(type);
        if (hasTemplate) {
            jts.fillFromTemplate(wp, type);
        }
        String id = wp.getId();

        if (!hasTemplate || id.equals("") || wps.getWorkflowProcess(id) != null) {
            if (id.equals("")) {
                id = JaWEManager.getInstance().getIdFactory().generateUniqueId(wps);
            } else {
                id = JaWEManager.getInstance().getIdFactory().generateSimilarOrIdenticalUniqueId(wps, new HashSet(), id);
            }
            int i = 0;
            while (wps.getWorkflowProcess(id) != null) {
                id = id + String.valueOf(++i);
            }
            wp.setId(id);
        }

        if (!hasTemplate || wp.getName().equals("")) {
            //CUSTOM
            String num = id.substring("process".length());
            wp.setName("Workflow Process " + num);
            //END CUSTOM
        }

        wp.getProcessHeader().setCreated(Utils.getCurrentDateAndTime());
        wp.getProcessHeader().setDurationUnitHOUR();
        wp.getRedefinableHeader().setAuthor(XMLUtil.getPackage(wps).getRedefinableHeader().getAuthor());

        adjustXPDLObject(wp, type);

        if (addToCollection) {
            wps.add(wp);
        }
        return wp;
    }

    public void adjustXPDLObject(XMLElement el, String type) {
        if (type != null && !type.equals("")) {
            JaWEEAHandler.setJaWEType(el, type);
        }
    }

    public XMLElement createXPDLObject(XMLCollection col,
            String type,
            boolean addToCollection) {
        try {
            Class cl = col.getClass();
            Method m = null;
            try {
                m = this.getClass().getMethod("createXPDLObject", new Class[]{
                            cl, String.class, boolean.class
                        });
            } catch (Exception ex) {
                if (!(cl == XMLCollection.class)) {
                    if (XMLCollection.class.isAssignableFrom(cl)) {
                        cl = XMLCollection.class;
                    }
                }
            }
            m = this.getClass().getMethod("createXPDLObject", new Class[]{
                        cl, String.class, boolean.class
                    });
            return (XMLElement) m.invoke(this, new Object[]{
                        col, type, new Boolean(addToCollection)
                    });
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }

    }

    public XMLElement duplicateXPDLObject(XMLCollection col, XMLElement el) {
        XMLElement dup = col.generateNewElement();
        dup.makeAs(el);
        if (dup instanceof XMLCollectionElement) {
            if (!(dup instanceof Tool)) {
                ((XMLCollectionElement) dup).setId(JaWEManager.getInstance().getIdFactory().generateUniqueId((XMLCollection) dup.getParent()));
            }
            XMLElement name = ((XMLCollectionElement) el).get("Name");
            if (name != null && name.toValue().length() > 0) {
                ((XMLCollectionElement) dup).set("Name",
                        ResourceManager.getLanguageDependentString("CopyOfKey") + " " + name.toValue());
            }
        }
        return dup;
    }

    public XMLElement makeIdenticalXPDLObject(XMLCollection col, XMLElement el) {
        XMLElement dup = col.generateNewElement();
        dup.makeAs(el);
        return dup;
    }

    protected String adjustType(XMLElement el, String type) {
        if (type == null || type.equals("")) {
            type = JaWEManager.getInstance().getJaWEController().getJaWETypes().getDefaultType(el);
        }
        return type;
    }
}
