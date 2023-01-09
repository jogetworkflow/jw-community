package org.joget.workflow.model;

import java.io.Serializable;

public class WorkflowVariable implements Serializable {

   private String id;
   private String name;
   private String description;
   private Object val;
   private Class javaClass;
   private boolean toUpdate;
   private boolean packageLevel;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Class getJavaClass() {
      return javaClass;
   }

   public void setJavaClass(Class javaClass) {
      this.javaClass = javaClass;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isToUpdate() {
      return toUpdate;
   }

   public void setToUpdate(boolean toUpdate) {
      this.toUpdate = toUpdate;
   }

   public Object getVal() {
      return val;
   }

   public void setVal(Object val) {
      this.val = val;
   }

   public boolean isPackageLevel() {
	   return packageLevel;
   }

   public void setPackageLevel(boolean packageLevel) {
	   this.packageLevel = packageLevel;
   }

}
