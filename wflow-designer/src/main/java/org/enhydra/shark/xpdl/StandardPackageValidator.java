package org.enhydra.shark.xpdl;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xerces.parsers.DOMParser;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ActivitySet;
import org.enhydra.shark.xpdl.elements.ActivitySets;
import org.enhydra.shark.xpdl.elements.ActivityTypes;
import org.enhydra.shark.xpdl.elements.ActualParameter;
import org.enhydra.shark.xpdl.elements.ActualParameters;
import org.enhydra.shark.xpdl.elements.Application;
import org.enhydra.shark.xpdl.elements.ApplicationTypes;
import org.enhydra.shark.xpdl.elements.Applications;
import org.enhydra.shark.xpdl.elements.ArrayType;
import org.enhydra.shark.xpdl.elements.Author;
import org.enhydra.shark.xpdl.elements.Automatic;
import org.enhydra.shark.xpdl.elements.BasicType;
import org.enhydra.shark.xpdl.elements.BlockActivity;
import org.enhydra.shark.xpdl.elements.Codepage;
import org.enhydra.shark.xpdl.elements.Condition;
import org.enhydra.shark.xpdl.elements.ConformanceClass;
import org.enhydra.shark.xpdl.elements.Cost;
import org.enhydra.shark.xpdl.elements.CostUnit;
import org.enhydra.shark.xpdl.elements.Countrykey;
import org.enhydra.shark.xpdl.elements.Created;
import org.enhydra.shark.xpdl.elements.DataField;
import org.enhydra.shark.xpdl.elements.DataFields;
import org.enhydra.shark.xpdl.elements.DataType;
import org.enhydra.shark.xpdl.elements.DataTypes;
import org.enhydra.shark.xpdl.elements.Deadline;
import org.enhydra.shark.xpdl.elements.DeadlineCondition;
import org.enhydra.shark.xpdl.elements.Deadlines;
import org.enhydra.shark.xpdl.elements.DeclaredType;
import org.enhydra.shark.xpdl.elements.Description;
import org.enhydra.shark.xpdl.elements.Documentation;
import org.enhydra.shark.xpdl.elements.Duration;
import org.enhydra.shark.xpdl.elements.EnumerationType;
import org.enhydra.shark.xpdl.elements.EnumerationValue;
import org.enhydra.shark.xpdl.elements.ExceptionName;
import org.enhydra.shark.xpdl.elements.ExtendedAttribute;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;
import org.enhydra.shark.xpdl.elements.ExternalPackage;
import org.enhydra.shark.xpdl.elements.ExternalPackages;
import org.enhydra.shark.xpdl.elements.ExternalReference;
import org.enhydra.shark.xpdl.elements.FinishMode;
import org.enhydra.shark.xpdl.elements.FormalParameter;
import org.enhydra.shark.xpdl.elements.FormalParameters;
import org.enhydra.shark.xpdl.elements.Icon;
import org.enhydra.shark.xpdl.elements.Implementation;
import org.enhydra.shark.xpdl.elements.ImplementationTypes;
import org.enhydra.shark.xpdl.elements.InitialValue;
import org.enhydra.shark.xpdl.elements.Join;
import org.enhydra.shark.xpdl.elements.Length;
import org.enhydra.shark.xpdl.elements.Limit;
import org.enhydra.shark.xpdl.elements.ListType;
import org.enhydra.shark.xpdl.elements.Manual;
import org.enhydra.shark.xpdl.elements.Member;
import org.enhydra.shark.xpdl.elements.Namespace;
import org.enhydra.shark.xpdl.elements.Namespaces;
import org.enhydra.shark.xpdl.elements.No;
import org.enhydra.shark.xpdl.elements.Package;
import org.enhydra.shark.xpdl.elements.PackageHeader;
import org.enhydra.shark.xpdl.elements.Participant;
import org.enhydra.shark.xpdl.elements.ParticipantType;
import org.enhydra.shark.xpdl.elements.Participants;
import org.enhydra.shark.xpdl.elements.Performer;
import org.enhydra.shark.xpdl.elements.Priority;
import org.enhydra.shark.xpdl.elements.PriorityUnit;
import org.enhydra.shark.xpdl.elements.ProcessHeader;
import org.enhydra.shark.xpdl.elements.RecordType;
import org.enhydra.shark.xpdl.elements.RedefinableHeader;
import org.enhydra.shark.xpdl.elements.Responsible;
import org.enhydra.shark.xpdl.elements.Responsibles;
import org.enhydra.shark.xpdl.elements.Route;
import org.enhydra.shark.xpdl.elements.SchemaType;
import org.enhydra.shark.xpdl.elements.Script;
import org.enhydra.shark.xpdl.elements.SimulationInformation;
import org.enhydra.shark.xpdl.elements.Split;
import org.enhydra.shark.xpdl.elements.StartFinishModes;
import org.enhydra.shark.xpdl.elements.StartMode;
import org.enhydra.shark.xpdl.elements.SubFlow;
import org.enhydra.shark.xpdl.elements.TimeEstimation;
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
import org.enhydra.shark.xpdl.elements.ValidFrom;
import org.enhydra.shark.xpdl.elements.ValidTo;
import org.enhydra.shark.xpdl.elements.Vendor;
import org.enhydra.shark.xpdl.elements.Version;
import org.enhydra.shark.xpdl.elements.WaitingTime;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.WorkflowProcesses;
import org.enhydra.shark.xpdl.elements.WorkingTime;
import org.enhydra.shark.xpdl.elements.XPDLVersion;
import org.xml.sax.InputSource;

public class StandardPackageValidator implements XMLValidator{

    public StandardPackageValidator()
    {
        epsValidationErrors = new HashMap();
        schemaValidationErrors = new HashMap();
    }

    public StandardPackageValidator(Properties settings)
    {
        epsValidationErrors = new HashMap();
        schemaValidationErrors = new HashMap();
        this.settings = settings;
    }

    public void init(XMLInterface pXmlInterface, Package pPkg, boolean pGetExistingSchemaValidationErrors, String pEncoding, String pLocale)
    {
        Properties tempProperties = new Properties();
        tempProperties.putAll(settings);
        tempProperties.put("GetExistingSchemaValidationErrors", String.valueOf(pGetExistingSchemaValidationErrors));
        tempProperties.put("CheckExternalPackages", "true");
        tempProperties.put("Encoding", pEncoding);
        tempProperties.put("Locale", pLocale);
        init(tempProperties, pXmlInterface);
    }

    public void clearCache(Package pkg)
    {
        epsValidationErrors.remove(pkg);
        schemaValidationErrors.remove(pkg);
    }

    public void clearCache()
    {
        epsValidationErrors.clear();
        schemaValidationErrors.clear();
        xmlInterface = null;
    }

    public Map getExtPkgValidationErrors()
    {
        return epsValidationErrors;
    }

    public void init(Properties props)
    {
        properties = props;
        if(props == null)
            clearCache();
    }

    public void init(Properties pProps, XMLInterface pXmlInterface)
    {
        init(pProps);
        xmlInterface = pXmlInterface;
    }

    public void validateElement(XMLElement el, List existingErrors, boolean fullCheck)
    {
        if(!fullCheck && existingErrors.size() > 0)
            return;
        if(el.isEmpty() && !(el instanceof XMLCollection) && !el.isRequired())
            return;
        try
        {
            Class cl = el.getClass();
            Method m = null;
            try
            {
                m = getClass().getMethod("validateElement", new Class[] {
                    cl, java.util.List.class, Boolean.TYPE
                });
            }
            catch(Exception ex)
            {
                if(cl != (org.enhydra.shark.xpdl.XMLSimpleElement.class) && cl != (org.enhydra.shark.xpdl.XMLAttribute.class) && cl != (org.enhydra.shark.xpdl.XMLComplexChoice.class) && cl != (org.enhydra.shark.xpdl.XMLComplexElement.class) && cl != (org.enhydra.shark.xpdl.XMLCollectionElement.class) && cl != (org.enhydra.shark.xpdl.XMLCollection.class))
                    if((org.enhydra.shark.xpdl.XMLComplexChoice.class).isAssignableFrom(cl))
                        cl = org.enhydra.shark.xpdl.XMLComplexChoice.class;
                    else
                    if((org.enhydra.shark.xpdl.XMLAttribute.class).isAssignableFrom(cl))
                        cl = org.enhydra.shark.xpdl.XMLAttribute.class;
                    else
                    if((org.enhydra.shark.xpdl.XMLSimpleElement.class).isAssignableFrom(cl))
                        cl = org.enhydra.shark.xpdl.XMLSimpleElement.class;
                    else
                    if((org.enhydra.shark.xpdl.XMLComplexElement.class).isAssignableFrom(cl))
                        cl = org.enhydra.shark.xpdl.XMLComplexElement.class;
                    else
                    if((org.enhydra.shark.xpdl.XMLCollection.class).isAssignableFrom(cl))
                        cl = org.enhydra.shark.xpdl.XMLCollection.class;
            }
            m = getClass().getMethod("validateElement", new Class[] {
                cl, java.util.List.class, Boolean.TYPE
            });
            m.invoke(this, new Object[] {
                el, existingErrors, new Boolean(fullCheck)
            });
            return;
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(XMLAttribute el, List existingErrors, boolean fullCheck)
    {
        XMLElement parent = el.getParent();
        boolean isValid = true;
        if(el.toName().equals("Id"))
        {
            if(parent instanceof SubFlow)
                checkSubFlowId(el, existingErrors, fullCheck);
            else
            if(parent instanceof Tool)
                checkToolId(el, existingErrors, fullCheck);
            else
            if(parent instanceof TransitionRef)
                checkTransitionRefId(el, existingErrors, fullCheck);
            else
            if(parent instanceof DeclaredType)
            {
                checkDeclaredTypeId(el, existingErrors);
            } else
            {
                if(!isIdValid(el.toValue()))
                {
                    isValid = false;
                    XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_INVALID_ID", el.toValue(), el);
                    existingErrors.add(verr);
                }
                if((parent instanceof XMLCollectionElement) && (fullCheck || isValid) && !isIdUnique((XMLCollectionElement)parent))
                {
                    isValid = false;
                    XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_UNIQUE_ID", el.toValue(), el);
                    existingErrors.add(verr);
                }
            }
        } else
        if(el.toName().equals("href"))
        {
            if(parent instanceof ExternalPackage)
            {
                String val = el.toValue();
                Package pkg = XMLUtil.getPackage(el);
                String epId = pkg.getExternalPackageId(val);
                if(epId == null || epId.equals(""))
                {
                    XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_EXTERNAL_PACKAGE_REFERENCE", val, el);
                    existingErrors.add(verr);
                }
            }
        } else
        if(parent instanceof Transition)
        {
            if(el.toName().equals("From"))
                checkTransitionFrom(el, existingErrors);
            else
            if(el.toName().equals("To"))
                checkTransitionTo(el, existingErrors);
        } else
        if(parent instanceof BlockActivity)
            checkBlockId(el, existingErrors);
    }

    public void validateElement(XMLComplexChoice el, List existingErrors, boolean fullCheck)
    {
        validateElement(el.getChoosen(), existingErrors, fullCheck);
    }

    public void validateElement(XMLEmptyChoiceElement xmlemptychoiceelement, List list, boolean flag)
    {
    }

    public void validateElement(XMLCollection el, List existingErrors, boolean fullCheck)
    {
        XMLElement cel;
        for(Iterator it = el.toElements().iterator(); it.hasNext(); validateElement(cel, existingErrors, fullCheck))
            cel = (XMLElement)it.next();

    }

    public void validateElement(XMLCollectionElement el, List existingErrors, boolean fullCheck)
    {
        validateElement(((XMLComplexElement) (el)), existingErrors, fullCheck);
    }

    public void validateElement(XMLComplexElement el, List existingErrors, boolean fullCheck)
    {
        XMLElement cel;
        for(Iterator it = el.toElements().iterator(); it.hasNext(); validateElement(cel, existingErrors, fullCheck))
            cel = (XMLElement)it.next();

    }

    public void validateElement(XMLSimpleElement xmlsimpleelement, List list, boolean flag)
    {
    }

    protected void validateStandard(XMLElement el, List existingErrors, boolean fullCheck)
    {
        if(el instanceof XMLAttribute)
            validateElement((XMLAttribute)el, existingErrors, fullCheck);
        else
        if(el instanceof XMLSimpleElement)
            validateElement((XMLSimpleElement)el, existingErrors, fullCheck);
        else
        if(el instanceof XMLCollectionElement)
            validateElement((XMLCollectionElement)el, existingErrors, fullCheck);
        else
        if(el instanceof XMLComplexElement)
            validateElement((XMLComplexElement)el, existingErrors, fullCheck);
        else
        if(el instanceof XMLComplexChoice)
            validateElement((XMLComplexChoice)el, existingErrors, fullCheck);
        else
        if(el instanceof XMLCollection)
            validateElement((XMLCollection)el, existingErrors, fullCheck);
    }

    public void validateElement(Activities el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Activity el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        boolean isValid = existingErrors.size() == 0;
        if(!isValid && !fullCheck)
            return;
        Set ets = XMLUtil.getExceptionalOutgoingTransitions(el);
        if(el.getDeadlines().size() > 0 && ets.size() == 0)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_DEADLINES_NOT_PROPERLY_HANDLED_NO_EXCEPTIONAL_TRANSITIONS", "", el);
            existingErrors.add(verr);
            isValid = false;
        }
        if(!isValid && !fullCheck)
            return;
        Set outTrans = XMLUtil.getOutgoingTransitions(el);
        Set inTrans = XMLUtil.getIncomingTransitions(el);
        Split split = XMLUtil.getSplit(el);
        if((split == null || split.getType().length() == 0) && outTrans.size() > 1)
        {
            isValid = false;
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MULTIPLE_OUTGOING_TRANSITIONS_WITHOUT_SPLIT_TYPE_DEFINED", "", el);
            existingErrors.add(verr);
        }
        if(!isValid && !fullCheck)
            return;
        Join join = XMLUtil.getJoin(el);
        if((join == null || join.getType().length() == 0) && inTrans.size() > 1)
        {
            isValid = false;
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MULTIPLE_INCOMING_TRANSITIONS_WITHOUT_JOIN_TYPE_DEFINED", "", el);
            existingErrors.add(verr);
        }
        if(!fullCheck && !isValid)
        {
            return;
        } else
        {
            checkMultipleOtherwiseOrDefaultExceptionTransitions(el, outTrans, existingErrors, fullCheck);
            return;
        }
    }

    public void validateElement(ActivitySet el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        boolean isValid = true;
        if((existingErrors.size() == 0 || fullCheck) && el.getActivities().toElements().size() == 0)
        {
            isValid = false;
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_ACTIVITY_SET_NOT_DEFINED", "", el);
            existingErrors.add(verr);
        }
        if(isValid || fullCheck)
            isValid = checkGraphConnectionsForWpOrAs(el, existingErrors, fullCheck) || fullCheck;
        if(isValid || fullCheck)
            isValid = checkGraphConformanceForWpOrAs(el, existingErrors, fullCheck) || fullCheck;
    }

    public void validateElement(ActivitySets el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ActivityTypes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ActualParameter el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ActualParameters el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Application el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Applications el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ApplicationTypes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ArrayType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Author el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Automatic el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(BasicType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(BlockActivity el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Codepage el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Condition el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        String condType = el.getType();
        String condExpr = el.toValue();
        if(existingErrors.size() > 0 && !fullCheck)
            return;
        boolean validateCondByType = properties.getProperty("ValidateConditionByType", "false").equals("true");
        if(condType.equals("DEFAULTEXCEPTION"))
        {
            if(validateCondByType && condExpr.length() > 0)
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_DEFAULT_EXCEPTION_TRANSITION_WITH_EXPRESSION", condExpr, el);
                existingErrors.add(verr);
            }
            return;
        }
        if(condType.equals("OTHERWISE"))
        {
            if(validateCondByType && condExpr.length() > 0)
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_OTHERWISE_TRANSITION_WITH_EXPRESSION", condExpr, el);
                existingErrors.add(verr);
            }
            return;
        }
        if(condType.equals(""))
        {
            if(validateCondByType && condExpr.length() > 0)
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_UNCONDITIONAL_TRANSITION_WITH_EXPRESSION", condExpr, el);
                existingErrors.add(verr);
            }
        } else
        if(condType.equals("CONDITION"))
        {
            if(validateCondByType && condExpr.length() <= 0)
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_CONDITIONAL_TRANSITION_WITHOUT_EXPRESSION", "", el);
                existingErrors.add(verr);
            }
        } else
        if(condType.equals("EXCEPTION"))
        {
            if(validateCondByType && condExpr.length() <= 0)
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_EXCEPTION_TRANSITION_WITHOUT_EXPRESSION", "", el);
                existingErrors.add(verr);
            }
            return;
        }
        if((existingErrors.size() == 0 || fullCheck) && condExpr.length() > 0 && properties.getProperty("ValidateConditionExpressions", "false").equals("true"))
        {
            if(condExpr.toLowerCase().indexOf("true") >= 0 || condExpr.toLowerCase().indexOf("false") >= 0 || condExpr.toLowerCase().indexOf("boolean") >= 0 || condExpr.toLowerCase().indexOf("equals") >= 0 || condExpr.toLowerCase().indexOf(">") >= 0 || condExpr.toLowerCase().indexOf(">=") >= 0 || condExpr.toLowerCase().indexOf("<") >= 0 || condExpr.toLowerCase().indexOf("<=") >= 0 || condExpr.toLowerCase().indexOf("==") >= 0)
                return;
            if(!XMLUtil.canBeExpression(condExpr, XMLUtil.getWorkflowProcess(el).getAllVariables(), false))
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_CONDITION_EXPRESSION_POSSIBLY_INVALID", condExpr, el);
                existingErrors.add(verr);
            }
        }
    }

    public void validateElement(ConformanceClass el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Cost el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(CostUnit el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Countrykey el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Created el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(DataField el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        boolean validateVariableUsage = properties.getProperty("ValidateUnusedVariables", "false").equals("true");
        if(validateVariableUsage && (fullCheck || existingErrors.size() == 0) && getNoOfReferences(el.getParent().getParent().getClass(), (XMLComplexElement)el.getParent().getParent(), org.enhydra.shark.xpdl.elements.DataField.class, el) == 0)
        {
            XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_UNUSED_VARIABLE", el.getId(), el);
            existingErrors.add(verr);
        }
    }

    public void validateElement(DataFields el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(DataType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(DataTypes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Deadline el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(DeadlineCondition el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        String condExpr = el.toValue();
        if((existingErrors.size() == 0 || fullCheck) && condExpr.length() > 0 && properties.getProperty("ValidateDeadlineExpressions", "false").equals("true"))
        {
            if(condExpr.toLowerCase().indexOf("date") >= 0 || condExpr.toLowerCase().indexOf("calendar") >= 0)
                return;
            if(!XMLUtil.canBeExpression(condExpr, XMLUtil.getWorkflowProcess(el).getAllVariables(), false))
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_DEADLINE_EXPRESSION_POSSIBLY_INVALID", condExpr, el);
                existingErrors.add(verr);
            }
        }
    }

    public void validateElement(Deadlines el, List existingErrors, boolean fullCheck)
    {
        Iterator dls = el.toElements().iterator();
        int syncCount = 0;
        do
        {
            if(!dls.hasNext())
                break;
            Deadline dl = (Deadline)dls.next();
            if(dl.getExecution().equals("SYNCHR"))
                syncCount++;
        } while(true);
        if(syncCount > 1)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MULTIPLE_SYNC_DEADLINES_DEFINED", "", el);
            existingErrors.add(verr);
        }
        if(fullCheck || syncCount > 1)
            validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(DeclaredType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Description el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Documentation el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Duration el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(EnumerationType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(EnumerationValue el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ExceptionName el, List existingErrors, boolean fullCheck)
    {
        Activity act = XMLUtil.getActivity(el);
        Set ets = XMLUtil.getExceptionalOutgoingTransitions(act);
        boolean isValid = true;
        if(ets.size() == 0)
        {
            isValid = false;
        } else
        {
            String en = el.toValue();
            for(Iterator it = ets.iterator(); it.hasNext();)
            {
                Transition t = (Transition)it.next();
                String cond = t.getCondition().toValue();
                String ctype = t.getCondition().getType();
                if(ctype.equals("DEFAULTEXCEPTION") || cond.equals(en) || cond.length() == 0)
                    return;
            }

            isValid = false;
        }
        if(!isValid)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_DEADLINE_EXCEPTION_NOT_PROPERLY_HANDLED_MISSING_SPECIFIED_EXCEPTION_TRANSITION_OR_DEFAULT_EXCEPTION_TRANSITION", "", el);
            existingErrors.add(verr);
        }
    }

    public void validateElement(ExtendedAttribute el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ExtendedAttributes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ExternalPackage el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ExternalPackages el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ExternalReference el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(FinishMode el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(FormalParameter el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        boolean validateVariableUsage = properties.getProperty("ValidateUnusedVariables", "false").equals("true");
        if(validateVariableUsage && (el.getParent().getParent() instanceof WorkflowProcess) && (fullCheck || existingErrors.size() == 0) && getNoOfReferences(org.enhydra.shark.xpdl.elements.WorkflowProcess.class, (WorkflowProcess)el.getParent().getParent(), org.enhydra.shark.xpdl.elements.FormalParameter.class, el) == 0)
        {
            XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_UNUSED_VARIABLE", el.getId(), el);
            existingErrors.add(verr);
        }
    }

    protected int getNoOfReferences(Class parentCls, XMLComplexElement parent, Class elCls, XMLElement el)
    {
        int ret = -1;
        try
        {
            Class clsJM = Class.forName("org.enhydra.jawe.JaWEManager");
            Method mth = clsJM.getMethod("getInstance", null);
            Object jm = mth.invoke(null, null);
            mth = clsJM.getMethod("getXPDLUtils", null);
            Object xpdlu = mth.invoke(jm, null);
            mth = xpdlu.getClass().getMethod("getReferences", new Class[] {
                parentCls, elCls
            });
            List l = (List)mth.invoke(xpdlu, new Object[] {
                parent, el
            });
            ret = l.size();
        }
        catch(Exception ex) { }
        return ret;
    }

    public void validateElement(FormalParameters el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Icon el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Implementation el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ImplementationTypes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(InitialValue el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Join el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Length el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Limit el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ListType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Manual el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Member el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Namespace el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Namespaces el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(No el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Package el, List existingErrors, boolean fullCheck)
    {
        validateAgainstXPDLSchema(el, existingErrors, fullCheck);
        if(existingErrors.size() == 0 || fullCheck)
            validateStandard(el, existingErrors, fullCheck);
        if(existingErrors.size() == 0 || fullCheck)
            checkExternalPackages(el, existingErrors, fullCheck);
    }

    public void validateElement(PackageHeader el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Participant el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Participants el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ParticipantType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Performer el, List existingErrors, boolean fullCheck)
    {
        String performer = el.toValue();
        Activity act = XMLUtil.getActivity(el);
        int actType = act.getActivityType();
        boolean toolOrNoAct = true;
        if(actType != 1 && actType != 2){
            toolOrNoAct = false;
        }else{
            Participant p = XMLUtil.findParticipant(xmlInterface, XMLUtil.getWorkflowProcess(act), performer);
            String type = p.getParticipantType().getType();
            if(actType == 1 && "SYSTEM".equals(type)){ //activity in system
                XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_ACTIVITY_CANNOT_PLACE_IN_SYSTEM", "", el);
                existingErrors.add(verr);
            }
        }

        if(!toolOrNoAct && performer.length() > 0)
        {
            XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_ACTIVITY_CANNOT_HAVE_PERFORMER", "", el);
            existingErrors.add(verr);
        }
        if(toolOrNoAct && properties.getProperty("ValidatePerformerExpressions", "false").equals("true"))
        {
            Participant p = XMLUtil.findParticipant(xmlInterface, XMLUtil.getWorkflowProcess(act), performer);
            if(p == null && performer.length() > 0 && !XMLUtil.canBeExpression(performer, XMLUtil.getWorkflowProcess(act).getAllVariables(), true))
            {
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_PERFORMER_EXPRESSION_POSSIBLY_INVALID", performer, el);
                existingErrors.add(verr);
            }
        }
    }

    public void validateElement(Priority el, List existingErrors, boolean fullCheck)
    {
        boolean notInt = false;
        try
        {
            if(el.toValue().trim().length() > 0)
                Integer.parseInt(el.toValue());
        }
        catch(Exception ex)
        {
            notInt = true;
        }
        if(notInt)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_PRIORITY_INVALID_VALUE", el.toValue(), el);
            existingErrors.add(verr);
        }
    }

    public void validateElement(PriorityUnit el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ProcessHeader el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(RecordType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(RedefinableHeader el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Responsible el, List existingErrors, boolean fullCheck)
    {
        XMLComplexElement pkgOrWp = XMLUtil.getWorkflowProcess(el);
        if(pkgOrWp == null)
            pkgOrWp = XMLUtil.getPackage(el);
        String rv = el.toValue();
        Participant p;
        if(pkgOrWp instanceof Package)
            p = XMLUtil.findParticipant(xmlInterface, (Package)pkgOrWp, rv);
        else
            p = XMLUtil.findParticipant(xmlInterface, (WorkflowProcess)pkgOrWp, rv);
        if(p == null)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_PARTICIPANT_REFERENCE", rv, el);
            existingErrors.add(verr);
        }
    }

    public void validateElement(Responsibles el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Route el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(SchemaType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Script el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(SimulationInformation el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Split el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(StartFinishModes el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(StartMode el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(SubFlow el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        if(existingErrors.size() == 0 || fullCheck)
        {
            WorkflowProcess wp = XMLUtil.findWorkflowProcess(xmlInterface, XMLUtil.getPackage(el), el.getId());
            if(wp != null)
            {
                ActualParameters aps = el.getActualParameters();
                checkParameterMatching(wp.getFormalParameters(), aps, existingErrors, properties.getProperty("ValidateActualParameterExpressions", "false").equals("true"), fullCheck);
            }
        }
    }

    public void validateElement(TimeEstimation el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Tool el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        if(existingErrors.size() == 0 || fullCheck)
        {
            String toolId = el.getId();
            WorkflowProcess wp = XMLUtil.getWorkflowProcess(el);
            Application app = XMLUtil.findApplication(xmlInterface, wp, toolId);
            if(app != null)
            {
                XMLElement ch = app.getApplicationTypes().getChoosen();
                if(ch instanceof FormalParameters)
                {
                    ActualParameters aps = el.getActualParameters();
                    checkParameterMatching((FormalParameters)ch, aps, existingErrors, properties.getProperty("ValidateActualParameterExpressions", "false").equals("true"), fullCheck);
                }
            }
        }
    }

    public void validateElement(Tools el, List existingErrors, boolean fullCheck)
    {
        boolean isValid = existingErrors.size() == 0;
        if(el.size() == 0)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NO_TOOLS_DEFINED", "", el);
            existingErrors.add(verr);
            isValid = false;
        }
        if(fullCheck || isValid)
            validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Transition el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(TransitionRef el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(TransitionRefs el, List existingErrors, boolean fullCheck)
    {
        Set outTrans = XMLUtil.getOutgoingTransitions(XMLUtil.getActivity(el));
        Split split = (Split)XMLUtil.getParentElement(org.enhydra.shark.xpdl.elements.Split.class, el);
        boolean isValid = true;
        if(el.size() != outTrans.size() && outTrans.size() > 1 && !split.getType().equals("AND"))
        {
            isValid = false;
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_TRANSITION_REFS_AND_OUTGOING_TRANSITION_NUMBER_MISSMATCH", "", el);
            existingErrors.add(verr);
        }
        if(!fullCheck && !isValid)
        {
            return;
        } else
        {
            validateStandard(el, existingErrors, fullCheck);
            return;
        }
    }

    public void validateElement(TransitionRestriction el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(TransitionRestrictions el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Transitions el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
        if(fullCheck || existingErrors.size() == 0)
        {
            Map actConns = new HashMap();
            Set multipleConnections = new HashSet();
            for(int i = 0; i < el.size(); i++)
            {
                Transition t = (Transition)el.get(i);
                String actConn = "[" + t.getFrom() + "-" + t.getTo() + "]";
                if(actConns.containsKey(actConn))
                {
                    multipleConnections.add(actConns.get(actConn));
                    multipleConnections.add(t);
                    if(!fullCheck)
                        break;
                }
                actConns.put(actConn, t);
            }

            if(multipleConnections.size() > 0)
            {
                XMLValidationError verr;
                for(Iterator it = multipleConnections.iterator(); it.hasNext(); existingErrors.add(verr))
                {
                    Transition t = (Transition)it.next();
                    String actConn = "[" + t.getFrom() + "-" + t.getTo() + "]";
                    verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MULTIPLE_ACTIVITY_CONNECTIONS", actConn, t);
                }

            }
        }
    }

    public void validateElement(TypeDeclaration el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(TypeDeclarations el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(UnionType el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ValidFrom el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(ValidTo el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Vendor el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(Version el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(WaitingTime el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(WorkflowProcess el, List existingErrors, boolean fullCheck)
    {
        boolean isValid = true;
        if(el.getActivities().toElements().size() == 0)
        {
            isValid = false;
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_WORKFLOW_PROCESS_NOT_DEFINED", "", el);
            existingErrors.add(verr);
        }
        if(fullCheck || isValid)
            validateStandard(el, existingErrors, fullCheck);
        if(isValid || fullCheck)
            checkGraphConnectionsForWpOrAs(el, existingErrors, fullCheck);
        if(isValid || fullCheck)
            checkGraphConformanceForWpOrAs(el, existingErrors, fullCheck);
    }

    public void validateElement(WorkflowProcesses el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(WorkingTime el, List existingErrors, boolean fullCheck)
    {
        validateStandard(el, existingErrors, fullCheck);
    }

    public void validateElement(XPDLVersion el, List existingErrors, boolean fullCheck)
    {
        if(!el.toValue().equals("1.0"))
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_INVALID_XPDL_VERSION", el.toValue(), el);
            existingErrors.add(verr);
        }
    }

    protected void validateAgainstXPDLSchema(Package pkg, List existingErrors, boolean fullCheck)
    {
        List schValidationErrors = (List)schemaValidationErrors.get(pkg);
        if(schValidationErrors != null && properties.getProperty("GetExistingSchemaValidationErrors", "false").equals("true"))
        {
            existingErrors.addAll(schValidationErrors);
            return;
        }
        List errorMessages = new ArrayList();
        try
        {
            String encoding = properties.getProperty("Encoding", "UTF-8");
            org.w3c.dom.Document document = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbf.newDocumentBuilder();
            document = dbuilder.newDocument();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XPDLRepositoryHandler repH = new XPDLRepositoryHandler();
            repH.toXML(document, pkg);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty("encoding", encoding);
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(baos);
            transformer.transform(source, result);
            DOMParser parser = new DOMParser();
            try
            {
                String locale = properties.getProperty("Locale");
                Locale l = new Locale("");
                if(locale == null || locale.trim().length() == 0)
                    l = Locale.getDefault();
                else
                if(!locale.equals("default"))
                    l = new Locale(locale);
                parser.setLocale(l);
                parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
                ParsingErrors pErrors = new ParsingErrors();
                parser.setErrorHandler(pErrors);
                parser.setEntityResolver(new XPDLEntityResolver());
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", true);
                parser.parse(new InputSource(new StringReader(baos.toString(encoding))));
                errorMessages = pErrors.getErrorMessages();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                errorMessages.add("Fatal error while parsing document:" + ex.getMessage());
            }
            baos.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            errorMessages.add("Fatal error while validating schema for package " + pkg.getId() + " :" + ex.getMessage());
        }
        schValidationErrors = new ArrayList();
        if(errorMessages.size() > 0)
        {
            XMLValidationError verr;
            for(Iterator it2 = errorMessages.iterator(); it2.hasNext(); schValidationErrors.add(verr))
            {
                String msg = (String)it2.next();
                verr = new XMLValidationError("ERROR", "SCHEMA", "", msg, pkg);
            }

        }
        existingErrors.addAll(schValidationErrors);
        schemaValidationErrors.put(pkg, schValidationErrors);
    }

    protected void checkExternalPackages(Package pkg, List existingErrors, boolean fullCheck)
    {
        if(properties.getProperty("CheckExternalPackages", "true").equals("false"))
            return;
        List epErrors;
        for(Iterator it = XMLUtil.getAllExternalPackageIds(xmlInterface, pkg, new HashSet()).iterator(); it.hasNext(); existingErrors.addAll(epErrors))
        {
            Package p = xmlInterface.getPackageById((String)it.next());
            epErrors = (List)epsValidationErrors.get(p);
            if(epErrors == null)
                epErrors = reCheckExternalPackage(p);
        }

    }

    public List reCheckExternalPackage(Package p)
    {
        List epErrors = (List)epsValidationErrors.get(p);
        if(epErrors != null)
            epErrors.clear();
        else
            epErrors = new ArrayList();
        Properties copy = new Properties();
        for(Iterator it = properties.entrySet().iterator(); it.hasNext();)
        {
            java.util.Map.Entry me = (java.util.Map.Entry)it.next();
            String key = (String)me.getKey();
            String val = (String)me.getValue();
            if(key.equals("CheckExternalPackages"))
                copy.setProperty("CheckExternalPackages", "false");
            else
            if(key.equals("GetExistingSchemaValidationErrors"))
                copy.setProperty("GetExistingSchemaValidationErrors", "false");
            else
                copy.setProperty(key, val);
        }

        StandardPackageValidator pv = createValidatorInstance();
        pv.init(copy, xmlInterface);
        List l = new ArrayList();
        pv.validateElement(p, l, true);
        epsValidationErrors.put(p, l);
        schemaValidationErrors.remove(p);
        return l;
    }

    protected boolean checkToolId(XMLAttribute tlId, List existingErrors, boolean fullCheck)
    {
        XMLValidationError verr = null;
        String toolId = tlId.toValue();
        WorkflowProcess wp = XMLUtil.getWorkflowProcess(tlId);
        Application app = XMLUtil.findApplication(xmlInterface, wp, toolId);
        if(app == null)
        {
            verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_APPLICATION_REFERENCE", toolId, tlId);
            existingErrors.add(verr);
        }
        return verr != null;
    }

    protected boolean checkSubFlowId(XMLAttribute sbflwId, List existingErrors, boolean fullCheck)
    {
        XMLValidationError verr = null;
        String subflowId = sbflwId.toValue();
        if(subflowId.trim().equals(""))
            verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_WORKFLOW_PROCESS_REFERENCE", subflowId, sbflwId);
        Package pkg = XMLUtil.getPackage(sbflwId);
        WorkflowProcess wp = null;
        if(verr == null)
        {
            wp = XMLUtil.findWorkflowProcess(xmlInterface, pkg, subflowId);
            if(wp == null && !isRemoteSubflowIdOK(subflowId) && properties.getProperty("ValidateSubFlowReferences", "true").equals("true"))
                verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_WORKFLOW_PROCESS_REFERENCE", subflowId, sbflwId);
        }
        if(verr != null)
            existingErrors.add(verr);
        return verr == null;
    }

    protected void checkTransitionRefId(XMLAttribute trfId, List existingErrors, boolean fullCheck)
    {
        Set outTrans = XMLUtil.getOutgoingTransitions(XMLUtil.getActivity(trfId));
        String transitionId = trfId.toValue();
        if(!containsTransitionWithId(outTrans, transitionId))
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_TRANSITION_REFERENCE", transitionId, trfId);
            existingErrors.add(verr);
        }
    }

    protected boolean containsTransitionWithId(Set trans, String id)
    {
        for(Iterator it = trans.iterator(); it.hasNext();)
        {
            Transition t = (Transition)it.next();
            if(t.getId().equals(id))
                return true;
        }

        return false;
    }

    protected boolean isRemoteSubflowIdOK(String subflowId)
    {
        return false;
    }

    protected void checkMultipleOtherwiseOrDefaultExceptionTransitions(Activity act, Set outTrans, List existingErrors, boolean fullCheck)
    {
        boolean foundOtherwise = false;
        boolean foundMultipleOtherwise = false;
        boolean foundDefaultException = false;
        boolean foundMultipleDefaultException = false;
        Iterator ts = outTrans.iterator();
        do
        {
            if(!ts.hasNext())
                break;
            Transition t = (Transition)ts.next();
            String ct = t.getCondition().getType();
            if(ct.equals("OTHERWISE"))
            {
                if(foundOtherwise)
                {
                    foundMultipleOtherwise = true;
                    if(foundMultipleDefaultException || !fullCheck)
                        break;
                } else
                {
                    foundOtherwise = true;
                }
                continue;
            }
            if(!ct.equals("DEFAULTEXCEPTION"))
                continue;
            if(foundDefaultException)
            {
                foundMultipleDefaultException = true;
                if(foundMultipleOtherwise || !fullCheck)
                    break;
            } else
            {
                foundDefaultException = true;
            }
        } while(true);
        if(foundMultipleOtherwise)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MORE_THAN_ONE_OTHERWISE_TRANSITION", "", act);
            existingErrors.add(verr);
        }
        if(foundMultipleDefaultException)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_MORE_THAN_ONE_DEFAULT_EXCEPTION_TRANSITION", "", act);
            existingErrors.add(verr);
        }
    }

    protected void checkTransitionFrom(XMLAttribute from, List existingErrors)
    {
        if(XMLUtil.getFromActivity(XMLUtil.getTransition(from)) == null)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_ACTIVITY_REFERENCE", from.toValue(), from);
            existingErrors.add(verr);
        }
    }

    protected void checkTransitionTo(XMLAttribute to, List existingErrors)
    {
        if(XMLUtil.getToActivity(XMLUtil.getTransition(to)) == null)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_ACTIVITY_REFERENCE", to.toValue(), to);
            existingErrors.add(verr);
        }
    }

    protected void checkDeclaredTypeId(XMLAttribute dtId, List existingErrors)
    {
        String tdId = dtId.toValue();
        TypeDeclaration td = XMLUtil.getPackage(dtId).getTypeDeclaration(tdId);
        if(td == null)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_TYPE_DECLARATION_REFERENCE", tdId, dtId);
            existingErrors.add(verr);
        }
    }

    protected void checkBlockId(XMLAttribute bId, List existingErrors)
    {
        String blockId = bId.toValue();
        ActivitySet as = XMLUtil.getWorkflowProcess(bId).getActivitySet(blockId);
        if(as == null)
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_ACTIVITY_SET_REFERENCE", blockId, bId);
            existingErrors.add(verr);
        }
    }

    public static boolean isEmpty(String str)
    {
        return str == null || str.trim().length() == 0;
    }

    protected boolean isIdValid(String id)
    {
        return XMLUtil.isIdValid(id);
    }

    protected boolean isIdUnique(XMLCollectionElement newEl)
    {
        XMLElement parent = newEl.getParent();
        if((newEl instanceof Tool) || (newEl instanceof TransitionRef))
            return true;
        if(newEl instanceof Activity)
            return checkActivityId((Activity)newEl);
        if(newEl instanceof Transition)
            return checkTransitionId((Transition)newEl);
        if(parent instanceof XMLCollection)
            return XMLUtil.cntIds((XMLCollection)parent, newEl.getId()) <= 1;
        else
            return true;
    }

    protected boolean checkActivityId(Activity newEl)
    {
        int idCnt = 0;
        WorkflowProcess proc = XMLUtil.getWorkflowProcess(newEl);
        String newId = newEl.getId();
        Activities acts = proc.getActivities();
        idCnt += XMLUtil.cntIds(acts, newId);
        ActivitySets actSets = proc.getActivitySets();
        for(int y = 0; y < actSets.size(); y++)
        {
            ActivitySet actSet = (ActivitySet)actSets.get(y);
            acts = actSet.getActivities();
            idCnt += XMLUtil.cntIds(acts, newId);
        }

        return idCnt <= 1;
    }

    protected boolean checkTransitionId(Transition newEl)
    {
        int idCnt = 0;
        WorkflowProcess proc = XMLUtil.getWorkflowProcess(newEl);
        String newId = newEl.getId();
        Transitions trans = proc.getTransitions();
        idCnt += XMLUtil.cntIds(trans, newId);
        ActivitySets actSets = proc.getActivitySets();
        for(int y = 0; y < actSets.size(); y++)
        {
            ActivitySet actSet = (ActivitySet)actSets.get(y);
            trans = actSet.getTransitions();
            idCnt += XMLUtil.cntIds(trans, newId);
        }

        return idCnt <= 1;
    }

    public static void printIM(boolean im[][], List acts)
    {
        if(im != null)
        {
            for(int i = 0; i < im.length; i++)
            {
                for(int j = 0; j < im[i].length; j++)
                    System.out.print(acts.get(i) + "->" + acts.get(j) + "=" + im[i][j] + " ");

                System.out.println();
            }

        } else
        {
            System.out.println("Passed array is null !!!");
        }
    }

    public static void printIM2(boolean im[][], List acts)
    {
        System.out.println("Activities are" + acts);
        if(im != null)
        {
            for(int i = 0; i < im.length; i++)
            {
                for(int j = 0; j < im[i].length; j++)
                    System.out.print((im[i][j] ? "1" : "0") + " ");

                System.out.println();
            }

        } else
        {
            System.out.println("Passed array is null !!!");
        }
    }

    protected boolean checkGraphConformanceForWpOrAs(XMLCollectionElement wpOrAs, List existingErrors, boolean fullCheck)
    {
        Package pkg = XMLUtil.getPackage(wpOrAs);
        String conformanceClass = pkg.getConformanceClass().getGraphConformance();
        int ct = XMLUtil.getConformanceClassNo(conformanceClass);
        Activities acts = (Activities)wpOrAs.get("Activities");
        List activities = acts.toElements();
        if(activities.size() == 0)
            return true;
        boolean isGraphConformant = true;
        Set splitActs = XMLUtil.getSplitOrJoinActivities(activities, 0);
        Set joinActs = XMLUtil.getSplitOrJoinActivities(activities, 1);
        Set noSplitActs = new HashSet(activities);
        noSplitActs.removeAll(splitActs);
        GraphChecker gc = null;
        if(ct > 0 && (isGraphConformant || fullCheck))
        {
            boolean incidenceMatrix[][] = createIncidenceMatrix(acts);
            if(incidenceMatrix == null)
            {
                XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "Unexpected error while checking graph conformance!", "", wpOrAs);
                existingErrors.add(verr);
                return false;
            }
            gc = new GraphChecker(incidenceMatrix);
            boolean loopError = false;
            if(fullCheck)
            {
                int loopNodes[] = gc.getCyclicNodes();
                if(loopNodes != null)
                {
                    isGraphConformant = false;
                    loopError = true;
                    for(int i = 0; i < loopNodes.length; i++)
                    {
                        Activity act = (Activity)activities.get(loopNodes[i]);
                        XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_LOOP_CONTAINED_ACTIVITY_IN_LOOP_BLOCKED_MODE", "", act);
                        existingErrors.add(verr);
                    }

                }
            } else
            {
                loopError = gc.isGraphCyclic();
                if(loopError)
                {
                    isGraphConformant = false;
                    XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_CYCLIC_GRAPH_IN_LOOP_BLOCKED_MODE", "", wpOrAs);
                    existingErrors.add(verr);
                }
            }
        }
        if(ct == 2 && (isGraphConformant || fullCheck))
        {
            if(XMLUtil.getStartingActivities(wpOrAs).size() != 1)
            {
                isGraphConformant = false;
                XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_MULTIPLE_STARTING_ACTIVITIES_IN_FULL_BLOCKED_MODE", "", wpOrAs);
                existingErrors.add(verr);
            }
            if((isGraphConformant || fullCheck) && XMLUtil.getEndingActivities(wpOrAs).size() != 1)
            {
                isGraphConformant = false;
                XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_MULTIPLE_ENDING_ACTIVITIES_IN_FULL_BLOCKED_MODE", "", wpOrAs);
                existingErrors.add(verr);
            }
            boolean smerr = false;
            if((isGraphConformant || fullCheck) && splitActs.size() != joinActs.size())
            {
                if(splitActs.size() > joinActs.size())
                {
                    XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_SPLIT_JOIN_MISSMATCH_IN_FULL_BLOCKED_MODE_MORE_SPLITS", "", wpOrAs);
                    existingErrors.add(verr);
                } else
                {
                    XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_SPLIT_JOIN_MISSMATCH_IN_FULL_BLOCKED_MODE_MORE_JOINS", "", wpOrAs);
                    existingErrors.add(verr);
                }
                isGraphConformant = false;
                smerr = true;
            }
            if((isGraphConformant || fullCheck) && !smerr && getNoOfANDSplitsOrJoins(splitActs, 0) != getNoOfANDSplitsOrJoins(joinActs, 1))
            {
                XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_SPLIT_JOIN_MISSMATCH_IN_FULL_BLOCKED_MODE_DIFFERENT_TYPES", "", wpOrAs);
                existingErrors.add(verr);
                isGraphConformant = false;
            }
            if(isGraphConformant || fullCheck)
            {
                Iterator it = splitActs.iterator();
label0:
                do
                {
                    Activity act;
                    XMLValidationError verr;
label1:
                    do
                    {
label2:
                        do
                        {
                            do
                            {
                                if(!it.hasNext())
                                    break label2;
                                act = (Activity)it.next();
                                if(!XMLUtil.isANDTypeSplitOrJoin(act, 0))
                                    continue label1;
                            } while(checkANDSplit(act));
                            isGraphConformant = false;
                            verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_CONDITIONAL_TRANSITION_FOR_AND_SPLIT_IN_FULL_BLOCKED_MODE", "", act);
                            existingErrors.add(verr);
                        } while(fullCheck);
                        break label0;
                    } while(checkXORSplit(act));
                    isGraphConformant = false;
                    verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_NO_OTHERWISE_TRANSITION_FOR_XOR_SPLIT_IN_FULL_BLOCKED_MODE", "", act);
                    existingErrors.add(verr);
                } while(fullCheck);
                it = noSplitActs.iterator();
label3:
                do
                {
                    Activity act;
                    do
                    {
                        if(!it.hasNext())
                            break label3;
                        act = (Activity)it.next();
                    } while(checkXORSplit(act));
                    isGraphConformant = false;
                    XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_NO_OTHERWISE_TRANSITION_FOR_XOR_SPLIT_IN_FULL_BLOCKED_MODE", "", act);
                    existingErrors.add(verr);
                } while(fullCheck);
            }
            if(isGraphConformant || fullCheck)
            {
                Iterator it = splitActs.iterator();
label4:
                do
                {
                    Activity act;
                    int ji;
label5:
                    do
                    {
label6:
                        do
                        {
                            int splitIndex;
label7:
                            {
                                do
                                {
                                    if(!it.hasNext())
                                        break;
                                    act = (Activity)it.next();
                                    splitIndex = activities.indexOf(act);
                                    if(splitIndex != -1)
                                        break label7;
                                    XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "Unexpected error while searching for split/join matching for graph conformance!", "", wpOrAs);
                                    existingErrors.add(verr);
                                    isGraphConformant = false;
                                } while(fullCheck);
                                break label6;
                            }
                            ji = gc.getJoinIndex(splitIndex);
                            if(ji >= 0)
                                continue label5;
                            isGraphConformant = false;
                            XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_NO_CORRESPONDING_JOIN_ACTIVITY_IN_FULL_BLOCKED_MODE", "", act);
                            existingErrors.add(verr);
                        } while(fullCheck);
                        break label4;
                    } while(XMLUtil.isANDTypeSplitOrJoin(act, 0) == XMLUtil.isANDTypeSplitOrJoin((Activity)activities.get(ji), 1));
                    isGraphConformant = false;
                    if(XMLUtil.isANDTypeSplitOrJoin(act, ji))
                    {
                        XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_NO_CORRESPONDING_JOIN_ACTIVITY_TYPE_IN_FULL_BLOCKED_MODE_AND_XOR", "", act);
                        existingErrors.add(verr);
                    } else
                    {
                        XMLValidationError verr = new XMLValidationError("ERROR", "CONFORMANCE", "ERROR_NO_CORRESPONDING_JOIN_ACTIVITY_TYPE_IN_FULL_BLOCKED_MODE_XOR_AND", "", act);
                        existingErrors.add(verr);
                    }
                } while(fullCheck);
            }
        }
        return isGraphConformant;
    }

    protected boolean[][] createIncidenceMatrix(Activities activities)
    {
        int size = activities.size();
        boolean incidenceMatrix[][] = new boolean[size][size];
        for(int indAct = 0; indAct < size; indAct++)
        {
            Activity a = (Activity)activities.get(indAct);
            for(Iterator trs = XMLUtil.getOutgoingTransitions(a).iterator(); trs.hasNext();)
            {
                Transition t = (Transition)trs.next();
                String aOut = t.getTo();
                Activity toAct = activities.getActivity(aOut);
                if(toAct == null)
                    return (boolean[][])null;
                int indOut = activities.indexOf(toAct);
                incidenceMatrix[indAct][indOut] = true;
            }

        }

        return incidenceMatrix;
    }

    protected int getNoOfANDSplitsOrJoins(Set acts, int sOrJ)
    {
        int no = 0;
        Iterator it = acts.iterator();
        do
        {
            if(!it.hasNext())
                break;
            Activity act = (Activity)it.next();
            if(sOrJ == 0 && XMLUtil.isANDTypeSplitOrJoin(act, 0))
                no++;
            else
            if(sOrJ == 1 && XMLUtil.isANDTypeSplitOrJoin(act, 0))
                no++;
        } while(true);
        return no;
    }

    protected boolean checkANDSplit(Activity act)
    {
        return !hasAnyPostcondition(act);
    }

    protected boolean checkXORSplit(Activity act)
    {
        if(hasAnyPostcondition(act))
        {
            Set ots = XMLUtil.getOutgoingTransitions(act);
            for(Iterator trs = ots.iterator(); trs.hasNext();)
            {
                Transition t = (Transition)trs.next();
                if(t.getCondition().getType().equals("OTHERWISE"))
                    return true;
            }

            return false;
        } else
        {
            return true;
        }
    }

    protected boolean hasAnyPostcondition(Activity act)
    {
        Set outL = XMLUtil.getOutgoingTransitions(act);
        for(Iterator it = outL.iterator(); it.hasNext();)
            if(!((Transition)it.next()).getCondition().toValue().equals(""))
                return true;

        return false;
    }

    protected boolean checkGraphConnectionsForWpOrAs(XMLCollectionElement wpOrAs, List existingErrors, boolean fullCheck)
    {
        if(wpOrAs == null)
            return false;
        boolean isWellConnected = true;
        Collection acts = ((Activities)wpOrAs.get("Activities")).toElements();
        if(acts == null || acts.size() == 0)
            return true;
        Set startActs = null;
        Set endActs = null;
        if(fullCheck || isWellConnected)
        {
            startActs = XMLUtil.getStartingActivities(wpOrAs);
            boolean allowUndefinedStart = properties.getProperty("AllowUndefinedStart", "true").equals("true");
            if(startActs.size() == 0 && (!allowUndefinedStart || (wpOrAs instanceof ActivitySet)))
            {
                isWellConnected = false;
                XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NO_STARTING_ACTIVITY", "", wpOrAs);
                existingErrors.add(verr);
            }
        }
        if(fullCheck || isWellConnected)
        {
            endActs = XMLUtil.getEndingActivities(wpOrAs);
            boolean allowUndefinedEnd = properties.getProperty("AllowUndefinedEnd", "true").equals("true");
            if(endActs.size() == 0 && (!allowUndefinedEnd || (wpOrAs instanceof ActivitySet)))
            {
                isWellConnected = false;
                XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NO_ENDING_ACTIVITY", "", wpOrAs);
                existingErrors.add(verr);
            }
        }
        if(fullCheck || isWellConnected)
        {
            Iterator it = acts.iterator();
label0:
            do
            {
                boolean wc;
                do
                {
                    if(!it.hasNext())
                        break label0;
                    Activity act = (Activity)it.next();
                    wc = checkActivityConnection(act, existingErrors, fullCheck);
                } while(wc);
                isWellConnected = false;
            } while(fullCheck);
        }
        return isWellConnected;
    }

    protected boolean checkActivityConnection(Activity act, List existingErrors, boolean fullCheck)
    {
        return true;
    }

    protected static void checkParameterMatching(FormalParameters fps, ActualParameters aps, List existingErrors, boolean checkExpression, boolean fullCheck)
    {
        if(fps.size() != aps.size())
        {
            XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_FORMAL_AND_ACTUAL_PARAMETERS_NUMBER_MISSMATCH", "", aps);
            existingErrors.add(verr);
        }
        if(!fullCheck && existingErrors.size() != 0)
            return;
        for(int i = 0; i < fps.size(); i++)
        {
            FormalParameter fp = (FormalParameter)fps.get(i);
            if(aps.size() - 1 < i)
                return;
            ActualParameter ap = (ActualParameter)aps.get(i);
            String fpMode = fp.getMode();
            if(fpMode.equals("IN") && !checkExpression)
                continue;
            DataType fpdt = fp.getDataType();
            DataTypes fpdtt = fpdt.getDataTypes();
            XMLElement fpType = fpdtt.getChoosen();
            Map idToDFOrFP = XMLUtil.getWorkflowProcess(aps).getAllVariables();
            String apWRD = ap.toValue();
            XMLCollectionElement ce = (XMLCollectionElement)idToDFOrFP.get(apWRD);
            if(ce == null)
            {
                if(!fpMode.equals("IN"))
                {
                    XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_NON_EXISTING_VARIABLE_REFERENCE", apWRD, ap);
                    existingErrors.add(verr);
                    continue;
                }
                boolean evaluateToString = false;
                if(fpType instanceof BasicType)
                {
                    String fpAT = ((BasicType)fpType).getType();
                    if(fpAT.equals("STRING"))
                        evaluateToString = true;
                }
                if(XMLUtil.canBeExpression(apWRD, XMLUtil.getWorkflowProcess(ap).getAllVariables(), evaluateToString) || apWRD.equals("null"))
                    continue;
                if(fpType instanceof BasicType)
                {
                    String fpAT = ((BasicType)fpType).getType();
                    if(fpAT.equals("INTEGER"))
                    {
                        try
                        {
                            new Integer(apWRD);
                            continue;
                        }
                        catch(Exception ex) { }
                        if(apWRD.toLowerCase().indexOf("short") >= 0 || apWRD.toLowerCase().indexOf("integer") >= 0 || apWRD.toLowerCase().indexOf("long") >= 0)
                            continue;
                    } else
                    if(fpAT.equals("FLOAT"))
                    {
                        try
                        {
                            new Double(apWRD);
                            continue;
                        }
                        catch(Exception ex) { }
                        if(apWRD.toLowerCase().indexOf("short") >= 0 || apWRD.toLowerCase().indexOf("integer") >= 0 || apWRD.toLowerCase().indexOf("long") >= 0 || apWRD.toLowerCase().indexOf("float") >= 0 || apWRD.toLowerCase().indexOf("double") >= 0)
                            continue;
                    } else
                    if(fpAT.equals("BOOLEAN") && (apWRD.equals("false") || apWRD.equals("true") || apWRD.toLowerCase().indexOf("boolean") >= 0))
                        continue;
                }
                XMLValidationError verr = new XMLValidationError("WARNING", "LOGIC", "WARNING_ACTUAL_PARAMETER_EXPRESSION_POSSIBLY_INVALID", apWRD, ap);
                existingErrors.add(verr);
                continue;
            }
            XMLElement apType = null;
            DataType apdt = (DataType)ce.get("DataType");
            DataTypes apdtt = apdt.getDataTypes();
            apType = apdtt.getChoosen();
            boolean invalidType = false;
            if(fpType.getClass().equals(apType.getClass()))
            {
                if(fpType instanceof BasicType)
                {
                    String fpAT = ((BasicType)fpType).getType();
                    String apAT = ((BasicType)apType).getType();
                    if(!fpAT.equals(apAT))
                        invalidType = true;
                } else
                if(fpType instanceof EnumerationType)
                {
                    if(((EnumerationType)fpType).size() != ((EnumerationType)apType).size())
                    {
                        invalidType = true;
                    } else
                    {
                        for(int j = 0; j < ((EnumerationType)fpType).size(); j++)
                        {
                            EnumerationValue evFP = (EnumerationValue)((EnumerationType)fpType).get(j);
                            EnumerationValue evAP = (EnumerationValue)((EnumerationType)apType).get(j);
                            if(!evFP.getName().equals(evAP.getName()))
                                invalidType = true;
                        }

                    }
                } else
                if((fpType instanceof DeclaredType) && !((DeclaredType)fpType).getId().equals(((DeclaredType)apType).getId()))
                    invalidType = true;
            } else
            {
                invalidType = true;
            }
            if(invalidType)
            {
                XMLValidationError verr = new XMLValidationError("ERROR", "LOGIC", "ERROR_INVALID_ACTUAL_PARAMETER_VARIABLE_TYPE", "", ap);
                existingErrors.add(verr);
            }
        }

    }

    public String prepareMessageString(String msg)
    {
        if(msg != null)
            msg = msg + "; ";
        else
            msg = "";
        return msg;
    }

    public boolean hasErrors(List l)
    {
        for(int i = 0; i < l.size(); i++)
        {
            XMLValidationError verr = (XMLValidationError)l.get(i);
            if(verr.getType().equals("ERROR"))
                return true;
        }

        return false;
    }

    protected StandardPackageValidator createValidatorInstance()
    {
        return new StandardPackageValidator();
    }

    public static void main(String args[])
    {
        try
        {
            XMLInterfaceForJDK13 xmlI = new XMLInterfaceForJDK13();
            Package pkg = xmlI.parseDocument(args[0], true);
            StandardPackageValidator validator = new StandardPackageValidator();
            validator.init(new Properties(), xmlI);
            List verrors = new ArrayList();
            validator.validateElement(pkg, verrors, false);
            if(verrors.size() > 0)
                System.out.println(args[0] + " is a valid XPDL package");
            else
                System.out.println(args[0] + " is not a valid XPDL package");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static final String VALIDATE_SUBFLOW_REFERENCES = "ValidateSubFlowReferences";
    public static final String VALIDATE_PERFORMER_EXPRESSIONS = "ValidatePerformerExpressions";
    public static final String VALIDATE_ACTUAL_PARAMETER_EXPRESSIONS = "ValidateActualParameterExpressions";
    public static final String VALIDATE_DEADLINE_EXPRESSIONS = "ValidateDeadlineExpressions";
    public static final String VALIDATE_CONDITION_EXPRESSIONS = "ValidateConditionExpressions";
    public static final String VALIDATE_UNUSED_VARIABLES = "ValidateUnusedVariables";
    public static final String VALIDATE_CONDITION_BY_TYPE = "ValidateConditionByType";
    public static final String GET_EXISTING_SCHEMA_VALIDATION_ERRORS = "GetExistingSchemaValidationErrors";
    public static final String CHECK_EXTERNAL_PACKAGES = "CheckExternalPackages";
    public static final String ALLOW_UNDEFINED_START = "AllowUndefinedStart";
    public static final String ALLOW_UNDEFINED_END = "AllowUndefinedEnd";
    public static final String ENCODING = "Encoding";
    public static final String LOCALE = "Locale";
    protected static final String CURRENT_XPDL_VERSION = "1.0";
    protected Properties properties;
    protected XMLInterface xmlInterface;
    protected Map epsValidationErrors;
    protected Map schemaValidationErrors;
    protected Properties settings;
}
