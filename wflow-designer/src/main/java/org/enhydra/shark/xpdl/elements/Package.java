package org.enhydra.shark.xpdl.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.enhydra.shark.utilities.SequencedHashMap;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;

public class Package extends XMLComplexElement {

    protected Namespaces namespaces;
    protected String internalVersion;
    protected boolean isTransient;
    protected SequencedHashMap extPkgRefsToIds = new SequencedHashMap();

    public Package() {
        super(null, true);
        namespaces = new Namespaces(this);
        internalVersion = "-1";
        isTransient = false;
    }

    public void makeAs(XMLElement el) {
        super.makeAs(el);
        this.namespaces.makeAs(((Package) el).namespaces);
        extPkgRefsToIds = new SequencedHashMap(((Package) el).extPkgRefsToIds);
        this.isTransient = ((Package) el).isTransient;
    }

    protected void fillStructure() {
        XMLAttribute attrId = new XMLAttribute(this, "Id", true); // required
        XMLAttribute attrName = new XMLAttribute(this, "Name", false);
        PackageHeader refPackageHeader = new PackageHeader(this);
        RedefinableHeader refRedefinableHeader = new RedefinableHeader(this); // min=0
        ConformanceClass refConformanceClass = new ConformanceClass(this); // min=0
        Script refScript = new Script(this); // min=0
        refScript.setType(Script.DEFAULT_TYPE);

        ExternalPackages refExternalPackages = new ExternalPackages(this); // min=0
        TypeDeclarations refTypeDeclarations = new TypeDeclarations(this); // min=0
        Participants refParticipants = new Participants(this);
        Applications refApplications = new Applications(this); // min=0
        DataFields refDataFields = new DataFields(this); // min=0
        WorkflowProcesses refWorkflowProcesses = new WorkflowProcesses(this); // min=0
        ExtendedAttributes refExtendedAttributes = new ExtendedAttributes(this);

        add(attrId);
        add(attrName);
        add(refPackageHeader);
        add(refRedefinableHeader);
        add(refConformanceClass);
        add(refScript);
        add(refExternalPackages);
        add(refTypeDeclarations);
        add(refParticipants);
        add(refApplications);
        add(refDataFields);
        add(refWorkflowProcesses);
        add(refExtendedAttributes);
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean trans) {
        this.isTransient = trans;
    }

    public String getInternalVersion() {
        return internalVersion;
    }

    public void setInternalVersion(String internalVersion) {
        this.internalVersion = internalVersion;
    }

    public void addExternalPackageMapping(String epRef, String epId) {
        extPkgRefsToIds.put(epRef, epId);
    }

    public void removeExternalPackageMapping(String epRef) {
        extPkgRefsToIds.remove(epRef);
    }

    public String getExternalPackageId(String epRef) {
        return (String) extPkgRefsToIds.get(epRef);
    }

    public Collection getExternalPackageIds() {
        return new ArrayList(extPkgRefsToIds.values());
    }

    public ExternalPackage getExternalPackage(String id) {
        ExternalPackage toRet = null;
        Iterator it = getExternalPackages().toElements().iterator();
        while (it.hasNext()) {
            ExternalPackage ep = (ExternalPackage) it.next();
            String href = ep.getHref();
            String epId = getExternalPackageId(href);
            if (epId != null && epId.equals(id)) {
                toRet = ep;
                break;
            }
        }
        return toRet;
    }

    public WorkflowProcess getWorkflowProcess(String Id) {
        return getWorkflowProcesses().getWorkflowProcess(Id);
    }

    public Application getApplication(String Id) {
        return getApplications().getApplication(Id);
    }

    public Participant getParticipant(String Id) {
        return getParticipants().getParticipant(Id);
    }

    public DataField getDataField(String Id) {
        return getDataFields().getDataField(Id);
    }

    public TypeDeclaration getTypeDeclaration(String Id) {
        return getTypeDeclarations().getTypeDeclaration(Id);
    }

    public String getId() {
        return get("Id").toValue();
    }

    public void setId(String id) {
        set("Id", id);
    }

    public String getName() {
        return get("Name").toValue();
    }

    public void setName(String name) {
        set("Name", name);
    }

    public Applications getApplications() {
        return (Applications) get("Applications");
    }

    public ConformanceClass getConformanceClass() {
        return (ConformanceClass) get("ConformanceClass");
    }

    public DataFields getDataFields() {
        return (DataFields) get("DataFields");
    }

    public ExtendedAttributes getExtendedAttributes() {
        return (ExtendedAttributes) get("ExtendedAttributes");
    }

    public ExternalPackages getExternalPackages() {
        return (ExternalPackages) get("ExternalPackages");
    }

    public PackageHeader getPackageHeader() {
        return (PackageHeader) get("PackageHeader");
    }

    public Participants getParticipants() {
        return (Participants) get("Participants");
    }

    public RedefinableHeader getRedefinableHeader() {
        return (RedefinableHeader) get("RedefinableHeader");
    }

    public Script getScript() {
        return (Script) get("Script");
    }

    public TypeDeclarations getTypeDeclarations() {
        return (TypeDeclarations) get("TypeDeclarations");
    }

    public WorkflowProcesses getWorkflowProcesses() {
        return (WorkflowProcesses) get("WorkflowProcesses");
    }

    public Namespaces getNamespaces() {
        return namespaces;
    }

    public void setNotifyMainListeners(boolean notify) {
        super.setNotifyMainListeners(notify);
        namespaces.setNotifyMainListeners(notify);
    }

    public void setReadOnly(boolean ro) {
        super.setReadOnly(ro);
        namespaces.setReadOnly(ro);
    }

    public Object clone() {
        Package d = (Package) super.clone();
        d.namespaces = (Namespaces) this.namespaces.clone();
        d.namespaces.setParent(d);
        d.extPkgRefsToIds = new SequencedHashMap(extPkgRefsToIds);
        d.isTransient = isTransient;
        d.clearCaches();
        if (d.isReadOnly) {
            d.initCaches();
        }
        return d;
    }

    public boolean equals(Object e) {
        boolean equals = super.equals(e);
        if (equals) {
            Package el = (Package) e;
            equals = (this.namespaces.equals(el.namespaces) && this.internalVersion.equals(el.internalVersion));
        }
        return equals;
    }

    protected boolean isMainElement() {
        return true;
    }

    protected void notifyMainListeners(XMLElementChangeInfo info) {
        if (parent == null) {
            return;
        }
        super.notifyMainListeners(info);
    }

    public static void dbg() {
        System.gc();
    }
}

