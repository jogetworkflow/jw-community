package org.enhydra.jawe.base.panel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Settings;
import org.enhydra.jawe.base.panel.panels.XMLBasicPanel;
import org.enhydra.jawe.base.panel.panels.XMLGroupPanel;
import org.enhydra.jawe.base.panel.panels.XMLPanel;
import org.enhydra.jawe.base.panel.panels.XMLTabbedPanel;
import org.enhydra.jawe.base.transitionhandler.TransitionHandler;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLComplexChoice;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLSimpleElement;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.XPDLConstants;
import org.enhydra.shark.xpdl.XPDLValidationErrorIds;
import org.enhydra.shark.xpdl.elements.*;
import org.enhydra.shark.xpdl.elements.Package;

/**
 * Class used to validate panels for all XPDL entities.
 * 
 * @author Sasa Bojanic
 */
public class StandardPanelValidator implements PanelValidator {

    protected Properties properties;

    public void configure(Properties props) throws Exception {
        this.properties = props;
    }

    public boolean validatePanel(Activities el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Activity el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ActivitySet el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ActivitySets el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ActivityTypes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ActualParameter el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ActualParameters el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Application el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Applications el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ApplicationTypes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ArrayType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Author el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Automatic el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(BasicType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(BlockActivity el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Codepage el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Condition el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ConformanceClass el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Cost el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(CostUnit el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Countrykey el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Created el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DataField el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DataFields el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DataType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DataTypes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Deadline el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DeadlineCondition el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Deadlines el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(DeclaredType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Description el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Documentation el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Duration el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(EnumerationType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(EnumerationValue el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExceptionName el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExtendedAttribute el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExtendedAttributes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExternalPackage el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExternalPackages el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ExternalReference el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(FinishMode el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(FormalParameter el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(FormalParameters el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Icon el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Implementation el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ImplementationTypes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(InitialValue el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Join el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Length el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Limit el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ListType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Manual el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Member el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Namespace el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Namespaces el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(No el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(org.enhydra.shark.xpdl.elements.Package el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(PackageHeader el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Participant el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Participants el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ParticipantType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Performer el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Priority el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(PriorityUnit el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ProcessHeader el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(RecordType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(RedefinableHeader el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Responsible el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Responsibles el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Route el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(SchemaType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Script el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(SimulationInformation el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Split el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(StartFinishModes el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(StartMode el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(SubFlow el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TimeEstimation el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Tool el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Tools el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Transition el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TransitionRef el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TransitionRefs el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TransitionRestriction el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TransitionRestrictions el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Transitions el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TypeDeclaration el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(TypeDeclarations el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(UnionType el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ValidFrom el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(ValidTo el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Vendor el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(Version el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(WaitingTime el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(WorkflowProcess el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(WorkflowProcesses el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(WorkingTime el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XPDLVersion el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLComplexChoice el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLCollection el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLCollectionElement el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLComplexElement el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLSimpleElement el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLAttribute el, XMLPanel panel) {
        return standardPanelValidation(el, panel);
    }

    public boolean validatePanel(XMLElement el, XMLPanel panel) {
        try {
            Class cl = el.getClass();
            Method m = null;
            try {
                m = this.getClass().getMethod("validatePanel", new Class[]{cl, XMLPanel.class});
            } catch (Exception ex) {
                if (!(cl == XMLSimpleElement.class || cl == XMLAttribute.class || cl == XMLComplexChoice.class || cl == XMLComplexElement.class || cl == XMLCollectionElement.class || cl == XMLCollection.class)) {
                    if (XMLComplexChoice.class.isAssignableFrom(cl)) {
                        cl = XMLComplexChoice.class;
                    } else if (XMLAttribute.class.isAssignableFrom(cl)) {
                        cl = XMLAttribute.class;
                    } else if (XMLSimpleElement.class.isAssignableFrom(cl)) {
                        cl = XMLSimpleElement.class;
                    } else if (XMLComplexElement.class.isAssignableFrom(cl)) {
                        cl = XMLComplexElement.class;
                    } else if (XMLCollection.class.isAssignableFrom(cl)) {
                        cl = XMLCollection.class;
                    }
                }
            }
            m = this.getClass().getMethod("validatePanel", new Class[]{cl, XMLPanel.class});
            return ((Boolean) m.invoke(this, new Object[]{el, panel})).booleanValue();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return standardPanelValidation(el, panel);
    }

    public boolean standardPanelValidation(XMLElement el, XMLPanel panel) {
        boolean idValidation = false;
        //CUSTOM
        boolean nameValidation = false;

        if (el instanceof Tool) {
            idValidation = false;
            nameValidation = false;
        } else if (el instanceof XMLCollectionElement || el instanceof Package) {
            idValidation = true;
            nameValidation = true;
        } else if (el instanceof XMLAttribute && el.toName().equals("Id") && (el.getParent() instanceof XMLCollectionElement || el.getParent() instanceof Package)) {
            idValidation = true;
        } else if (el instanceof XMLAttribute && el.toName().equals("Name") && (el.getParent() instanceof XMLCollectionElement || el.getParent() instanceof Package)) {
            nameValidation = true;
        }
        if (idValidation) {
            if (!validateId(panel, el)) {
                return false;
            }
        }

        if (nameValidation) {
            if (!validateName(panel, el)) {
                return false;
            }
        }
        //END CUSTOM

        if (el instanceof Transition || el instanceof Condition || el.getParent() instanceof Transition || el.getParent() instanceof Condition) {
            if (!validateTransition(panel, el)) {
                return false;
            }
        }
        return true;
    }

    protected boolean validateId(XMLPanel pnl, XMLElement el) {
        XMLComplexElement cel = null;
        String newId = null;
        XMLPanel idPanel = null;
        if (el instanceof XMLAttribute) {
            XMLElement parent = el.getParent();
            if (parent instanceof XMLCollectionElement) {
                cel = (XMLCollectionElement) parent;
                newId = cel.get("Id").toValue();
                if (pnl.getValue() instanceof String) {
                    newId = ((String) pnl.getValue()).trim();
                    idPanel = pnl;
                }
            } else if (parent instanceof Package) {
                cel = (Package) parent;
                newId = cel.get("Id").toValue();
                if (pnl.getValue() instanceof String) {
                    newId = ((String) pnl.getValue()).trim();
                    idPanel = pnl;
                }
            }
        } else if (el instanceof XMLComplexElement) {
            cel = (XMLComplexElement) el;
            newId = cel.get("Id").toValue();
            idPanel = findPanel(pnl, cel.get("Id"));
            if (idPanel != null) {
                if (idPanel.getValue() instanceof String) {
                    newId = ((String) idPanel.getValue()).trim();
                }
            }
        }

        System.err.println("Valid for " + el + ", par=" + el.getParent() + ", newid=" + newId + ", idp=" + idPanel);
        boolean isValid = XMLUtil.isIdValid(newId);

        //check for period (.)
        if(isValid && newId.contains(".")){
            isValid = false;
        }

        if (!isValid) {
            XMLBasicPanel.errorMessage(pnl.getWindow(), ResourceManager.getLanguageDependentString("ErrorMessageKey"), "",
                    ResourceManager.getLanguageDependentString(XPDLValidationErrorIds.ERROR_INVALID_ID));
            idPanel.requestFocus();
            return false;
        }

        if (cel != null) {

            boolean isUniqueId = true;
            if (cel instanceof XMLCollectionElement) {
                isUniqueId = JaWEManager.getInstance().getIdFactory().isIdUnique((XMLCollectionElement) cel, newId);
            } else {
                Package fp = JaWEManager.getInstance().getXPDLHandler().getPackageById(newId);
                if (fp != null && fp != cel && fp.getId().equals(newId)) {
                    isUniqueId = false;
                }
            }

            if (!isUniqueId) {
                XMLBasicPanel.errorMessage(pnl.getWindow(), ResourceManager.getLanguageDependentString("ErrorMessageKey"),
                        "", ResourceManager.getLanguageDependentString(XPDLValidationErrorIds.ERROR_NON_UNIQUE_ID));
                idPanel.requestFocus();
                return false;
            }
        }
        return true;
    }

    protected boolean validateName(XMLPanel pnl, XMLElement el) {
        XMLComplexElement cel = null;
        String newName = null;
        XMLPanel namePanel = null;
        if (el instanceof XMLAttribute) {
            XMLElement parent = el.getParent();
            if (parent instanceof XMLCollectionElement) {
                cel = (XMLCollectionElement) parent;
                newName = cel.get("Name").toValue();
                if (pnl.getValue() instanceof String) {
                    newName = ((String) pnl.getValue()).trim();
                    namePanel = pnl;
                }
            } else if (parent instanceof Package) {
                cel = (Package) parent;
                newName = cel.get("Name").toValue();
                if (pnl.getValue() instanceof String) {
                    newName = ((String) pnl.getValue()).trim();
                    namePanel = pnl;
                }
            }
        } else if (el instanceof XMLComplexElement) {
            cel = (XMLComplexElement) el;
            newName = cel.get("Name") != null ? cel.get("Name").toValue() : "";
            namePanel = findPanel(pnl, cel.get("Name"));
            if (namePanel != null) {
                if (namePanel.getValue() instanceof String) {
                    newName = ((String) namePanel.getValue()).trim();
                }
            }
        }

        System.err.println("Valid for " + el + ", par=" + el.getParent() + ", newname=" + newName + ", namep=" + namePanel);

        return true;
    }

    protected boolean validateTransition(XMLPanel pnl, XMLElement el) {
        Transition tra = XMLUtil.getTransition(el);
        String oldFrom = tra.getFrom();
        String oldTo = tra.getTo();
        String newFrom = oldFrom;
        String newTo = oldTo;
        String newType = tra.getCondition().getType();

        if (el instanceof Transition) {
            XMLPanel ftPanel = findPanel(pnl, tra.get("From"));
            if (ftPanel != null) {
                Object v = ftPanel.getValue();
                if (v instanceof XMLElement) {
                    if (v instanceof Activity) {
                        newFrom = ((Activity) v).getId();
                    }
                } else if (v instanceof String) {
                    newFrom = ((String) v).trim();
                }
            }
            ftPanel = findPanel(pnl, tra.get("To"));
            if (ftPanel != null) {
                Object v = ftPanel.getValue();
                if (v instanceof XMLElement) {
                    if (v instanceof Activity) {
                        newTo = ((Activity) v).getId();
                    }
                } else if (v instanceof String) {
                    newTo = ((String) v).trim();
                }
            }
            ftPanel = findPanel(pnl, ((Condition) tra.get("Condition")).getTypeAttribute());
            if (ftPanel != null) {
                Object v = ftPanel.getValue();
                if (v instanceof String) {
                    newType = ((String) v).trim();
                }
            }
        } else if (el instanceof XMLAttribute && (el.toName().equals("From") || el.toName().equals("To"))) {
            Object v = pnl.getValue();
            String toOrFrom = null;
            if (v instanceof XMLElement) {
                if (v instanceof Activity) {
                    toOrFrom = ((Activity) v).getId();
                }
            } else if (v instanceof String) {
                toOrFrom = ((String) v).trim();
            }
            if (toOrFrom != null) {
                if (el.toName().equals("From")) {
                    newFrom = toOrFrom;
                } else {
                    newTo = toOrFrom;
                }
            }
        } else if (el instanceof Condition) {
            XMLPanel ftPanel = findPanel(pnl, ((Condition) tra.get("Condition")).getTypeAttribute());
            if (ftPanel != null) {
                Object v = ftPanel.getValue();
                if (v instanceof String) {
                    newType = ((String) v).trim();
                }
            }
        } else if (el instanceof XMLAttribute && el.getParent() instanceof Condition && el.toName().equals("Type")) {
            Object v = pnl.getValue();
            if (v instanceof String) {
                newType = ((String) v).trim();
            }
        }

        boolean isExcTra = false;
        if (newType.equals(XPDLConstants.CONDITION_TYPE_EXCEPTION) ||
                newType.equals(XPDLConstants.CONDITION_TYPE_DEFAULTEXCEPTION)) {
            isExcTra = true;
        }

        Activities acts = (Activities) ((XMLCollectionElement) tra.getParent().getParent()).get("Activities");
        Activity actFrom = acts.getActivity(newFrom);
        Activity actTo = acts.getActivity(newTo);
        if (actFrom == null || actTo == null) {
            return false;
        }

        TransitionHandler th = JaWEManager.getInstance().getTransitionHandler();
        List status = new ArrayList();
        boolean ac = th.allowsConnection(actFrom, actTo, tra, isExcTra, status);

        if (!ac) {
            String errorMsg = "WarningSourceActivityCannotHaveMoreOutgoingTransitions";
            if (((Integer) status.get(0)).intValue() == 2) {
                errorMsg = "WarningTargetActivityCannotHaveMoreIncomingTransitions";
            } else if (((Integer) status.get(0)).intValue() == 3) {
                errorMsg = "ErrorActivityCannotHaveMoreThenOneIncomingOutgoingTransitionFromToTheSameActivity";
            }
            XMLBasicPanel.errorMessage(pnl.getWindow(), ResourceManager.getLanguageDependentString("ErrorMessageKey"), "",
                    ResourceManager.getLanguageDependentString(errorMsg));

        }
        return ac;
    }

    public static XMLPanel findPanel(XMLPanel p, XMLElement idEl) {
        if (p instanceof XMLTabbedPanel) {
            return ((XMLTabbedPanel) p).getPanelForElement(idEl);
        } else if (p instanceof XMLGroupPanel) {
            return ((XMLGroupPanel) p).getPanelForElement(idEl);
        }
        return null;
    }

    public Settings getSettings() {
        return null;
    }
}
