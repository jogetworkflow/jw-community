package org.enhydra.jawe.base.tooltip;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEConstants;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.Settings;
import org.enhydra.jawe.base.display.DisplayNameGenerator;
import org.enhydra.jawe.base.label.LabelGenerator;
import org.enhydra.shark.utilities.SequencedHashMap;
import org.enhydra.shark.xpdl.XMLAttribute;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLComplexChoice;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLEmptyChoiceElement;
import org.enhydra.shark.xpdl.XMLSimpleElement;
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

/**
 * Used to generate tooltips for representing XPDL entities.
 *
 * @author Sasa Bojanic
 */
public class StandardTooltipGenerator implements TooltipGenerator {

   /** Used for tooltips */
   public static final String EMPTY_STRING = "";

   /** Used for tooltips */
   public static final String HTML_OPEN = "<html>";

   /** Used for tooltips */
   public static final String HTML_CLOSE = "</html>";

   /** Used for tooltips */
   public static final String STRONG_OPEN = "<strong>";

   /** Used for tooltips */
   public static final String STRONG_CLOSE = "</strong>";

   /** Used for tooltips */
   public static final String LINE_BREAK = "<br>";

   /** Used for tooltips */
   public static final String COLON_SPACE = ": ";

   protected TooltipGeneratorSettings settings;

   public StandardTooltipGenerator() {
      settings = new TooltipGeneratorSettings();
      settings.init((JaWEComponent) null);
   }

   public StandardTooltipGenerator(TooltipGeneratorSettings settings) {
      this.settings = settings;
      this.settings.init((JaWEComponent) null);
   }

   public String getTooltip(Activities el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Activity el) {
      // CUSTOM: hide unused fields
      LabelGenerator lg = JaWEManager.getInstance().getLabelGenerator();
      Map toDisplay = new SequencedHashMap();
      putKeyValue(toDisplay, el.get("Id"));
      putKeyValue(toDisplay, el.get("Name"));
//      putKeyValue(toDisplay, el.get("Description"));
//      putKeyValue(toDisplay, el.get("Performer"));
//      putKeyValue(toDisplay, el.getStartMode());
//      putKeyValue(toDisplay, el.getFinishMode());
//      putKeyValue(toDisplay, el.get("Priority"));
      putKeyValue(toDisplay, el.get("Limit"));
      if (el.getTransitionRestrictions().size() > 0) {
         TransitionRestriction tr = (TransitionRestriction) el.getTransitionRestrictions().get(0);
         putKeyValue(toDisplay, tr.getJoin());
         putKeyValue(toDisplay, tr.getSplit());
      } else {
         toDisplay.put(lg.getLabel(new Join(null)), "");
         toDisplay.put(lg.getLabel(new Split(null)), "");
      }
      fillTypePartOfTooltip(el, toDisplay);
      return makeTooltip(toDisplay);
      // END CUSTOM
   }

   protected void fillTypePartOfTooltip(Activity el, Map toDisplay) {
      // CUSTOM: hide type
      /*
      LabelGenerator lg = JaWEManager.getInstance().getLabelGenerator();
      String label, key;
      label = lg.getLabel(el.getActivityTypes());

      String type = JaWEManager.getInstance().getJaWEController().getTypeResolver().getJaWEType(el).getTypeId();
      key = settings.getLanguageDependentString(type); // lg.getLabel(tp);
      if (key == null || key.equals("")) {
         key = type;
      }
      toDisplay.put(label, key);

      if (type.equals(JaWEConstants.ACTIVITY_TYPE_TOOL)) {
         label = settings.getLanguageDependentString("NoOfToolsKey");
         key = String.valueOf(el.getActivityTypes().getImplementation().getImplementationTypes().getTools().size());
         toDisplay.put(label, key);
      }
      if (type.equals(JaWEConstants.ACTIVITY_TYPE_SUBFLOW)) {
         label = settings.getLanguageDependentString("ReferencedProcessKey");
         key = el.getActivityTypes().getImplementation().getImplementationTypes().getSubFlow().getId();
         if (key.equals("")) {
            key = settings.getLanguageDependentString("NoneKey");
         }
         toDisplay.put(label, key);
      }
      if (type.equals(JaWEConstants.ACTIVITY_TYPE_ROUTE)) {
         // tpSpecific=new XMLElement("Condition");
         // tpSpecific.setValue(getLoop().get("Condition").toString());
      }
      if (type.equals(JaWEConstants.ACTIVITY_TYPE_BLOCK)) {
         label = settings.getLanguageDependentString("ReferencedActivitySetKey");
         key = el.getActivityTypes().getBlockActivity().getBlockId();
         if (key.equals("")) {
            key = settings.getLanguageDependentString("NoneKey");
         }
         toDisplay.put(label, key);
      }
       */
       // END CUSTOM
   }

   public String getTooltip(ActivitySet el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ActivitySets el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ActivityTypes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ActualParameter el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ActualParameters el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Application el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Applications el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ApplicationTypes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ArrayType el) {
      return ResourceManager.getLanguageDependentString("SubTypeKey");
      // return generateStandardLabel(el);
   }

   public String getTooltip(Author el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Automatic el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(BasicType el) {
      return ResourceManager.getLanguageDependentString("SubTypeKey");
   }

   public String getTooltip(BlockActivity el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Codepage el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Condition el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ConformanceClass el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Cost el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(CostUnit el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Countrykey el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Created el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DataField el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DataFields el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DataType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DataTypes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Deadline el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DeadlineCondition el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Deadlines el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(DeclaredType el) {
      return ResourceManager.getLanguageDependentString("SubTypeKey");
   }

   public String getTooltip(Description el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Documentation el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Duration el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(EnumerationType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(EnumerationValue el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExceptionName el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExtendedAttribute el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExtendedAttributes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExternalPackage el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExternalPackages el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ExternalReference el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(FinishMode el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(FormalParameter el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(FormalParameters el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Icon el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Implementation el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ImplementationTypes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(InitialValue el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Join el) {
      return ResourceManager.getLanguageDependentString("JoinTypeKey");
   }

   public String getTooltip(Length el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Limit el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ListType el) {
      return ResourceManager.getLanguageDependentString("SubTypeKey");
   }

   public String getTooltip(Manual el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Member el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Namespace el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Namespaces el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(No el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(org.enhydra.shark.xpdl.elements.Package el) {
      Map toDisplay = new SequencedHashMap();
      putKeyValue(toDisplay, el.get("Id"));
      putKeyValue(toDisplay, el.get("Name"));
      putKeyValue(toDisplay, el.getPackageHeader().get("Description"));
      return makeTooltip(toDisplay);
   }

   public String getTooltip(PackageHeader el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Participant el) {
      Map toDisplay = new SequencedHashMap();
      putKeyValue(toDisplay, el.get("Id"));
      putKeyValue(toDisplay, el.get("Name"));
      putKeyValue(toDisplay, el.getParticipantType());
//      putKeyValue(toDisplay, el.get("Description"));
      return makeTooltip(toDisplay);
   }

   public String getTooltip(Participants el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ParticipantType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Performer el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Priority el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(PriorityUnit el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ProcessHeader el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(RecordType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(RedefinableHeader el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Responsible el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Responsibles el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Route el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(SchemaType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Script el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(SimulationInformation el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Split el) {
      return ResourceManager.getLanguageDependentString("SplitTypeKey");
   }

   public String getTooltip(StartFinishModes el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(StartMode el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(SubFlow el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TimeEstimation el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Tool el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Tools el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Transition el) {
      LabelGenerator lg = JaWEManager.getInstance().getLabelGenerator();
      Map toDisplay = new SequencedHashMap();
      putKeyValue(toDisplay, el.get("Id"));
      putKeyValue(toDisplay, el.get("Name"));
      putKeyValue(toDisplay, el.get("Description"));
      putKeyValue(toDisplay, el.get("From"));
      putKeyValue(toDisplay, el.get("To"));
      putKeyValue(toDisplay, el.getCondition().getTypeAttribute());
      toDisplay.put(lg.getLabel(el.getCondition()), el.getCondition().toValue());
      return makeTooltip(toDisplay);
   }

   public String getTooltip(TransitionRef el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TransitionRefs el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TransitionRestriction el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TransitionRestrictions el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Transitions el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TypeDeclaration el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(TypeDeclarations el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(UnionType el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ValidFrom el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(ValidTo el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Vendor el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(Version el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(WaitingTime el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(WorkflowProcess el) {
      Map toDisplay = new SequencedHashMap();
      putKeyValue(toDisplay, el.get("Id"));
      putKeyValue(toDisplay, el.get("Name"));
      putKeyValue(toDisplay, el.get("AccessLevel"));
      putKeyValue(toDisplay, el.getProcessHeader().get("Description"));
      return makeTooltip(toDisplay);
   }

   public String getTooltip(WorkflowProcesses el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(WorkingTime el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XPDLVersion el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip (XMLEmptyChoiceElement el) {
      return generateStandardTooltip(el);
   }


   public String getTooltip (XMLComplexChoice el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLCollection el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLCollectionElement el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLComplexElement el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLSimpleElement el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLAttribute el) {
      return generateStandardTooltip(el);
   }

   public String getTooltip(XMLElement el) {
      try {
         Class cl = el.getClass();
         Method m = null;
         try {
            m = this.getClass().getMethod("getTooltip", new Class[] { cl });
         } catch (Exception ex) {
            if (!(cl == XMLSimpleElement.class || cl == XMLAttribute.class || cl == XMLComplexChoice.class
                  || cl == XMLComplexElement.class || cl == XMLCollectionElement.class || cl == XMLCollection.class)) {
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
         m = this.getClass().getMethod("getTooltip", new Class[] { cl });
         // System.err.println("calling "+m.toString());
         return (String) m.invoke(this, new Object[] { el });
      } catch (Throwable e) {
         e.printStackTrace();
      }

      return generateStandardTooltip(el);

   }

   public String generateStandardTooltip(XMLElement el) {
//      Map toDisplay = new SequencedHashMap();
//      if (el instanceof XMLCollection) {
//
//      } else if (el instanceof XMLCollectionElement) {
//
//      } else if (el instanceof XMLComplexElement) {
//
//      } else if (el instanceof XMLComplexChoice) {
//
//      } else if (el instanceof XMLAttribute) {
//
//      }
      return ResourceManager.getLanguageDependentString(el.toName() + "Key");
   }

   /**
    * Neat little thing. Makes HTML formated string for tooltip (made of
    * property names and coresponding values).
    */
   protected static String makeTooltip(Map elements) {
      if (elements == null)
         return "";
      String s = HTML_OPEN;
      Iterator it = elements.entrySet().iterator();
      while (it.hasNext()) {
         Map.Entry me = (Map.Entry) it.next();
         s += makeAnotherHtmlLine((String) me.getKey(), (String) me.getValue());
      }
      s = s.substring(0, s.length() - LINE_BREAK.length());
      s += HTML_CLOSE;
      return s;
   }

   /** Helps when generating tooltip for some element. */
   protected static String makeAnotherHtmlLine(String label, String text) {
      int MAX_LENGTH = 100;
      int MAX_LINES_PER_TEXT = 15;
      String textToAppend = "";
      textToAppend += STRONG_OPEN;
      textToAppend += label + COLON_SPACE;
      textToAppend += STRONG_CLOSE;
      String val = text;
      val = val.replaceAll("<", "&lt;");
      val = val.replaceAll(">", "&gt;");
      int vl = val.length();
      if (vl > MAX_LENGTH) {
         String newVal = "";
         int hm = vl / MAX_LENGTH;
         for (int i = 0; i <= hm; i++) {
            int startI = i * MAX_LENGTH;
            int endI = (i + 1) * MAX_LENGTH;
            if (endI > vl) {
               endI = vl;
            }
            newVal = newVal + val.substring(startI, endI);
            if (i == MAX_LINES_PER_TEXT) {
               newVal = newVal + " ...";
               break;
            }
            if (i < hm) {
               newVal += LINE_BREAK;
               newVal += StandardTooltipGenerator.makeEmptyHTMLText((label + COLON_SPACE).length());
            }
         }
         val = newVal;
      }
      textToAppend += val;
      textToAppend += LINE_BREAK;

      return textToAppend;
   }

   protected static String makeEmptyHTMLText(int length) {
      if (length < 0)
         return null;
      String es = "";
      for (int i = 0; i < length; i++) {
         es += "&nbsp;";
      }
      return es;
   }

   protected static void putKeyValue(Map toPut, XMLElement el) {
      LabelGenerator lg = JaWEManager.getInstance().getLabelGenerator();
      DisplayNameGenerator dng = JaWEManager.getInstance().getDisplayNameGenerator();
      toPut.put(lg.getLabel(el), dng.getDisplayName(el));
   }

   public Settings getSettings() {
      return settings;
   }

}
