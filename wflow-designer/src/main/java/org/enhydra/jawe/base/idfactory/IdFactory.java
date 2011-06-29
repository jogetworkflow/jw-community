package org.enhydra.jawe.base.idfactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.enhydra.jawe.JaWEComponent;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLUtil;
import org.enhydra.shark.xpdl.elements.Activities;
import org.enhydra.shark.xpdl.elements.Activity;
import org.enhydra.shark.xpdl.elements.ActivitySet;
import org.enhydra.shark.xpdl.elements.ActivitySets;
import org.enhydra.shark.xpdl.elements.Applications;
import org.enhydra.shark.xpdl.elements.DataFields;
import org.enhydra.shark.xpdl.elements.FormalParameters;
import org.enhydra.shark.xpdl.elements.Participants;
import org.enhydra.shark.xpdl.elements.Tool;
import org.enhydra.shark.xpdl.elements.Transition;
import org.enhydra.shark.xpdl.elements.Transitions;
import org.enhydra.shark.xpdl.elements.TypeDeclarations;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.WorkflowProcesses;

/**
 * Factory for generating XPDL objects.
 * @author Sasa Bojanic
 */
public class IdFactory {

   protected IdFactorySettings settings;

   public IdFactory () {
      settings = new IdFactorySettings();
      settings.init((JaWEComponent) null);
   }

   public IdFactory(IdFactorySettings settings) {
      this.settings = settings;
      this.settings.init((JaWEComponent) null);
   }

   public String generateUniqueId (XMLCollection cel) {
      return generateUniqueId(cel,new HashSet());
   }

   public String generateUniqueId (XMLCollection cel,Set skipIds) {
      // CUSTOM: use more readable names
      String id;
      long nextId=0;
      String prefix="";
      if (!(cel instanceof WorkflowProcesses)) {
         if (cel instanceof Activities) {
            prefix+="activity";
         } else if (cel instanceof ActivitySets) {
            prefix+="activitySet";
         } else if (cel instanceof Applications) {
            prefix+="tool";
         } else if (cel instanceof DataFields) {
            prefix+="variable";
         } else if (cel instanceof FormalParameters) {
            prefix+="parameter";
         } else if (cel instanceof Participants) {
            prefix+="participant";
         } else if (cel instanceof Transitions) {
            prefix+="transition";
         } else if (cel instanceof TypeDeclarations) {
            prefix+="type";
         }
      } else if (cel instanceof WorkflowProcesses) {
          prefix = "process";
      }

      XMLCollectionElement cl = (XMLCollectionElement)cel.generateNewElement();
      do {
         id=prefix+new Long(++nextId).toString();
      } while (skipIds.contains(id) || !isIdUnique(cl,id));
      return id;
      // END CUSTOM
   }

   public String generateSimilarOrIdenticalUniqueId (XMLCollection cel,Set skipIds,String origId) {
      String id=origId;
      long nextId=0;

      XMLCollectionElement cl = (XMLCollectionElement)cel.generateNewElement();
      while (id.equals("") || skipIds.contains(id) || !isIdUnique(cl,id)) {
         id=origId+new Long(++nextId).toString();
      }
      return id;
   }

   public boolean isIdUnique (XMLCollectionElement el,String newId) {

      XMLElement parent=el.getParent();
      if(el instanceof Tool) return true;
      else if(el instanceof Activity) return checkActivityId( (Activity)el,newId );
      else if(el instanceof Transition) return checkTransitionId( (Transition)el,newId );
      else if (parent instanceof XMLCollection) {
         List elsWithId=getElementsForId((XMLCollection)parent, newId);
         if (elsWithId.size()==0 || (elsWithId.size()==1 && elsWithId.contains(el))) {
            return true;
         }
         return false;
      } else {
         return true;
      }
   }

   public boolean checkActivityId (Activity newEl,String newId) {
       WorkflowProcess proc = XMLUtil.getWorkflowProcess(newEl);
       Activities acts = proc.getActivities();
       List elsWithId=getElementsForId(acts, newId);
       ActivitySets actSets = proc.getActivitySets();
       for(int y = 0; y < actSets.size(); y++) {
           ActivitySet actSet = (ActivitySet)actSets.get(y);
           acts = actSet.getActivities();
           elsWithId.addAll(getElementsForId(acts, newId));
       }
       if (elsWithId.size()==0 || (elsWithId.size()==1 && elsWithId.contains(newEl))) {
          return true;
       }return false;
    }

   public boolean checkTransitionId (Transition newEl,String newId) {
      WorkflowProcess proc = XMLUtil.getWorkflowProcess(newEl);

      Transitions trans = proc.getTransitions();
      List elsWithId=getElementsForId(trans, newId);
      ActivitySets actSets = proc.getActivitySets();
      for(int y = 0; y < actSets.size(); y++) {
         ActivitySet actSet = (ActivitySet)actSets.get(y);
         trans = actSet.getTransitions();
         elsWithId.addAll(getElementsForId(trans, newId));
      }
      if (elsWithId.size()==0 || (elsWithId.size()==1 && elsWithId.contains(newEl))) {
         return true;
      }
      return false;
    }


   public List getElementsForId (XMLCollection col,String id) {
      List elsWithId=new ArrayList();
      Iterator it=col.toElements().iterator();
      if (col.generateNewElement() instanceof XMLCollectionElement) {
         while (it.hasNext()) {
            XMLCollectionElement ce=(XMLCollectionElement)it.next();
            if (ce.getId().equals(id)) {
               elsWithId.add(ce);
            }
         }
      }

      return elsWithId;
   }

}
