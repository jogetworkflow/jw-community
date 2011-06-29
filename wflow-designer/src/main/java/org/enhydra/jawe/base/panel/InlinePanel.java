package org.enhydra.jawe.base.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import org.enhydra.jawe.BarFactory;
import org.enhydra.jawe.HistoryManager;
import org.enhydra.jawe.JaWEComponent;
import org.enhydra.jawe.JaWEComponentView;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.Settings;
import org.enhydra.jawe.XPDLElementChangeInfo;
import org.enhydra.jawe.base.controller.JaWESelectionManager;
import org.enhydra.jawe.base.display.DisplayNameGenerator;
import org.enhydra.jawe.base.label.LabelGenerator;
import org.enhydra.jawe.base.panel.panels.XMLBasicPanel;
import org.enhydra.jawe.base.panel.panels.XMLPanel;
import org.enhydra.jawe.base.panel.panels.XMLTabbedPanel;
import org.enhydra.jawe.base.tooltip.TooltipGenerator;
import org.enhydra.shark.xpdl.XMLComplexChoice;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;
import org.enhydra.shark.xpdl.XMLUtil;

public class InlinePanel extends JPanel implements JaWEComponentView, PanelContainer {

   protected JaWEComponent controller;

   protected JScrollPane scrollPane;

   protected boolean displayTitle = false;

   protected JLabel title;

   protected boolean isModified = false;

   protected PanelGenerator panelGenerator;

   protected Map lastActiveTabs = new HashMap();

   protected HistoryManager hm;

   public void configure() {
   }

   public InlinePanel() {
   }

   public void setJaWEComponent(JaWEComponent jc) {
      this.controller = jc;
   }

   public PanelSettings getPanelSettings() {
      return (PanelSettings) this.controller.getSettings();
   }

   public void init() {

      ClassLoader cl = getClass().getClassLoader();
      try {
         this.panelGenerator = (PanelGenerator) cl.loadClass(JaWEManager.getInstance().getPanelGeneratorClassName())
               .newInstance();
      } catch (Exception ex) {
         String msg = "InlinePanel -> Problems while instantiating Panel Generator class '"
            + JaWEManager.getInstance().getPanelGeneratorClassName() + "' - using default implementation!";

         JaWEManager.getInstance().getLoggingManager().error(msg, ex);
         this.panelGenerator=new StandardPanelGenerator();
      }
      this.panelGenerator.setPanelContainer(this);

      try {
         String hmc = getPanelSettings().historyManagerClass();
         if (hmc != null && !hmc.equals("")) {
            hm = (HistoryManager) Class.forName(hmc).newInstance();
            hm.init(getPanelSettings().historySize());
         }
      } catch (Exception ex) {
         System.err.println("Failed to instantiate history manager - my controller is "+controller);
      }

      getPanelSettings().adjustActions();

      displayTitle = ((Boolean) controller.getSettings().getSetting("DisplayTitle")).booleanValue();

      // creates scroll panel
      scrollPane = new JScrollPane();
      if (((Boolean) controller.getSettings().getSetting("UseScrollBar")).booleanValue()) {
         scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      } else {
         scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      }

      JViewport port = scrollPane.getViewport();
      port.setScrollMode(JViewport.BLIT_SCROLL_MODE);
      scrollPane.getVerticalScrollBar().setUnitIncrement(20);
      scrollPane.getHorizontalScrollBar().setUnitIncrement(40);

      setBorder(BorderFactory.createEtchedBorder());
      setLayout(new BorderLayout());

      JPanel wp = new JPanel();
      JToolBar toolbar = BarFactory.createToolbar("defaultToolbar", controller);
      toolbar.setFloatable(false);
      toolbar.setRollover(true);
      wp.setLayout(new BoxLayout(wp, BoxLayout.Y_AXIS));

      wp.add(Box.createVerticalStrut(5));
      if (displayTitle) {
         JPanel p = new JPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
         title = new JLabel("");
         title.setAlignmentX(Component.LEFT_ALIGNMENT);
         title.setAlignmentY(Component.BOTTOM_ALIGNMENT);
         // p.add(Box.createHorizontalGlue());
         p.add(title);
         p.add(Box.createHorizontalGlue());
         wp.add(p);
      }

      wp.add(scrollPane);

      // CUSTOM: put buttons at the bottom
//      add(toolbar, BorderLayout.NORTH); // PREVIOUS CODE
      JPanel toolbarPanel = new JPanel();
      toolbarPanel.setAlignmentX(RIGHT_ALIGNMENT);
      toolbarPanel.add(toolbar);
      add(toolbarPanel, BorderLayout.SOUTH);
      // END CUSTOM
      add(wp, BorderLayout.CENTER);
   }

   public JaWEComponent getJaWEComponent() {
      return controller;
   }

   public JComponent getDisplay() {
      return this;
   }

   public void update(XPDLElementChangeInfo info) {
      if (info.getSource() == this) {
         return;
      }

      XMLElement changedElement = info.getChangedElement();
      XMLElement current = getActiveElement();
      List removedElements = new ArrayList();

      JaWESelectionManager jsm = JaWEManager.getInstance().getJaWEController().getSelectionManager();
      if (changedElement == null || (jsm.getSelectedElements().size() == 1 && !jsm.canEditProperties())) {
         setActiveElement(null);
      } else if (info.getAction() == XMLElementChangeInfo.UPDATED) {
         if (current != null && XMLUtil.isChildsParent(current, info.getChangedElement())) {
            setActiveElement(current);
         }
      } else if (info.getAction() == XPDLElementChangeInfo.SELECTED) {
         setActiveElement(changedElement);
         // TODO: send multi sel to XMLTable and XMLList panels
         // if (changedElement instanceof XMLCollection) {
         // List chngdSubEls=info.getChangedSubElements();
         // if (chngdSubEls.size()>0) {
         // }
         // }
      } else if (info.getAction() == XMLElementChangeInfo.REMOVED) {
         List l = info.getChangedSubElements();
         if (l == null || l.size() == 0) {
            l = new ArrayList();
            l.add(info.getChangedElement());
         }
         for (int i = 0; i < l.size(); i++) {
            XMLElement el = (XMLElement) l.get(i);
            if (el==current || XMLUtil.isParentsChild(el, current)) {
               setActiveElement(null);
            }
            removedElements.add(el);
         }
      }

      if (hm != null) {
         for (int i = 0; i < removedElements.size(); i++) {
            hm.removeFromHistory((XMLElement) removedElements.get(i));
         }
      }

      getPanelSettings().adjustActions();
   }

   public void setViewPanel(XMLPanel panel) {
      XMLPanel current = getViewPanel();
      if (current != null) {
         current.cleanup();
      }
      if (displayTitle) {
         XMLElement el = panel.getOwner();
         String t = "";
         if (el != null) {
//            t = " " + getLabelGenerator().getLabel(el);
            t = " " + panel.getTitle();
         }
         title.setText(t);
      }

      this.scrollPane.setViewportView(panel);
   }

   public XMLPanel getViewPanel() {
      if (scrollPane != null)
         return (XMLPanel) this.scrollPane.getViewport().getView();

      return null;
   }

   public XMLElement getActiveElement() {
      XMLPanel p = getViewPanel();
      if (p != null) {
         XMLElement current = p.getOwner();
         if (current instanceof SpecialChoiceElement) {
            current = ((SpecialChoiceElement) current).getControlledElement();
         } else if (current instanceof ActivityTypesChoiceElement) {
            current = ((ActivityTypesChoiceElement) current).getControlledElement();
         }
         return current;
      }
      return null;
   }

   public void apply() {
      XMLPanel p = getViewPanel();
      if (p != null) {
         p.setElements();
      }
   }

   public boolean canApplyChanges() {
      if (getViewPanel() != null) {
         XMLPanel p = getViewPanel();
//         System.err.println("CAAAAAAAACCCCCCCC for "+p);
         if (p.validateEntry()) {
//            System.err.println("ENTRY IS VALID FOR "+p);
            return JaWEManager.getInstance().getPanelValidator().validatePanel(p.getOwner(), p);
         }
      }

      return false;
   }

   public boolean validateElement(XMLElement el) {
      // boolean retVal = true;
      // XPDLValidator xpdlValidator =
      // JaWEManager.getInstance().getXPDLValidator();
      //
      // xpdlValidator.init(JaWE.getInstance().getProperties(),
      // JaWEManager.getInstance()
      // .getXPDLHandler(),
      // JaWEManager.getInstance().getJaWEController().getMainPackage(),
      // true, true, true, true,
      // JaWEManager.getInstance().getJaWEController().getEncoding());
      //
      // if (el instanceof Activity
      // && ((Activity) el).getActivityType() ==
      // XPDLConstants.ACTIVITY_TYPE_SUBFLOW) {
      // try {
      // boolean isSubFlowOK = xpdlValidator.checkActivitySubFlow((Activity) el,
      // true);
      // if (!isSubFlowOK) {
      // JOptionPane.showMessageDialog(JaWEManager.getInstance().getJaWEController().getJaWEFrame(),
      // JaWEManager.getInstance().getXPDLElementEditor().getLanguageDependentString(
      // "ErrorSubFlowFormalAndActualParametersDoNotMatch"), JaWEManager
      // .getInstance().getXPDLElementEditor().getLanguageDependentString(
      // "ErrorMessageKey"), JOptionPane.WARNING_MESSAGE);
      // retVal = false;
      // }
      // } catch (Throwable e) {
      // retVal = false;
      // }
      // }
      // return retVal;
      return true;
   }

   public void applySpecial() {
      if (!canApplyChanges())
         return;
      XMLElement el = getActiveElement();

      getJaWEComponent().setUpdateInProgress(true);
      JaWEManager.getInstance().getJaWEController().startUndouableChange();
      apply();
      List toSelect = new ArrayList();
      XMLElement toSel = el;
      if (toSel != null) {
         toSelect.add(toSel);
      }
      JaWEManager.getInstance().getJaWEController().endUndouableChange(toSelect);
      getJaWEComponent().setUpdateInProgress(false);

      validateElement(el);

      setModified(false);
   }

   public void displayParentPanel() {
      if (isModified) {
         int sw = showModifiedWarning();
         if (sw == JOptionPane.CANCEL_OPTION || (sw == JOptionPane.YES_OPTION && isModified()))
            return;
      }
      XMLElement el = getActiveElement();
      if (el != null) {
         XMLElement parent = el.getParent();
         if (parent != null) {
            XMLElement choice = null;
            while ((choice = XMLUtil.getParentElementByAssignableType(XMLComplexChoice.class, parent)) != null) {
               parent = choice.getParent();
            }
            setActiveElement(parent);
            JaWEManager.getInstance().getJaWEController().getSelectionManager().setSelection(parent, true);
         }
      }
   }

   public void displayGivenElement(XMLElement el) {
      if (isModified) {
         int sw = showModifiedWarning();
         if (sw == JOptionPane.CANCEL_OPTION || (sw == JOptionPane.YES_OPTION && isModified()))
            return;
      }
      if (el != null) {
         setActiveElement(el);
         JaWEManager.getInstance().getJaWEController().getSelectionManager().setSelection(el, true);
         if (getJaWEComponent() instanceof JDialog && ((JDialog)getJaWEComponent()).isModal()) {
            ((JDialog)getJaWEComponent()).setTitle(JaWEManager.getInstance().getLabelGenerator().getLabel(el));
         }
      }
   }

   public void displayElement(XMLElement el) {
      XMLPanel previousPanel = getViewPanel();
      XMLElement previousElement = getActiveElement();
      if (previousPanel instanceof XMLTabbedPanel && previousElement != null) {
         XMLTabbedPanel tp = (XMLTabbedPanel) previousPanel;
         Class ec = previousElement.getClass();
         int activeTab = tp.getActiveTab();
         lastActiveTabs.put(ec, new Integer(activeTab));
      }

      // MUST BE SET BEFORE GENERATING NEW PANEL BECAUSE PANELGENERATOR
      // CAN SET THIS FLAG TO TRUE
      isModified = false;

      XMLPanel p;
      if (el != null) {
         p = this.panelGenerator.getPanel(el);
      } else {
         p = new XMLBasicPanel();
      }
      setViewPanel(p);
      if (p instanceof XMLTabbedPanel) {
         Integer at = (Integer) lastActiveTabs.get(el.getClass());
         if (at != null) {
            int atno = at.intValue();
            XMLTabbedPanel tp = (XMLTabbedPanel) p;
            if (tp.getTabCount() <= at.intValue()) {
               atno = tp.getTabCount() - 1;
               lastActiveTabs.put(el.getClass(), new Integer(atno));
            }
            tp.setActiveTab(atno);
         }
      }
      getPanelSettings().adjustActions();
      // enableApplyAction(isModified);
      // enableRevertAction(isModified);

   }

   public void setActiveElement(XMLElement el) {
      if (hm!=null) {
         XMLElement current = getActiveElement();
         hm.addToHistory(current, el);
      }
      displayElement(el);
      getPanelSettings().adjustActions();
   }

   public void displayPreviousElement() {
      if (hm==null) return;

      if (isModified()) {
         int sw = showModifiedWarning();
         if (sw == JOptionPane.CANCEL_OPTION || (sw == JOptionPane.YES_OPTION && isModified()))
            return;
      }
      if (hm.canGoBack()) {
         XMLElement el = hm.getPrevious(getActiveElement());
         displayElement(el);
         JaWEManager.getInstance().getJaWEController().getSelectionManager().setSelection(el, true);
         getPanelSettings().adjustActions();
      }
   }

   public void displayNextElement() {
      if (hm==null) return;

      if (isModified()) {
         int sw = showModifiedWarning();
         if (sw == JOptionPane.CANCEL_OPTION || (sw == JOptionPane.YES_OPTION && isModified()))
            return;
      }
      if (hm.canGoForward()) {
         XMLElement el = hm.getNext(getActiveElement());
         displayElement(el);

         JaWEManager.getInstance().getJaWEController().getSelectionManager().setSelection(el, true);
         getPanelSettings().adjustActions();
      }
   }

   public int showModifiedWarning() {
      if (!getPanelSettings().shouldShowModifiedWarning())
         return JOptionPane.NO_OPTION;
      int option = JOptionPane.showConfirmDialog(JaWEManager.getInstance().getJaWEController().getJaWEFrame(),
            getPanelSettings().getLanguageDependentString("WarningElementChanged"), getPanelSettings()
                  .getLanguageDependentString("DialogTitle"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (option == JOptionPane.YES_OPTION) {
         applySpecial();
      }
      return option;
   }

   public void setModified(boolean isModified) {
      this.isModified = isModified;

      // enableApplyAction(isModified);
      // enableRevertAction(isModified);
      getPanelSettings().adjustActions();

   }

   public boolean isModified() {
      return this.isModified;
   }

   public void cleanup() {
      if (hm!=null) {
         hm.cleanHistory();
      }
      getPanelSettings().adjustActions();
   }

   public void panelChanged(XMLPanel panel, EventObject ev) {
      isModified = true;
      // enableApplyAction(isModified);
      // enableRevertAction(isModified);
      getPanelSettings().adjustActions();

   }

   public Settings getSettings() {
      return controller.getSettings();
   }

   public String getLanguageDependentString(String nm) {
      return controller.getSettings().getLanguageDependentString(nm);
   }

   public PanelGenerator getPanelGenerator() {
      return panelGenerator;
   }

   public LabelGenerator getLabelGenerator() {
      return JaWEManager.getInstance().getLabelGenerator();
   }

   public DisplayNameGenerator getDisplayNameGenerator() {
      return JaWEManager.getInstance().getDisplayNameGenerator();
   }

   public PanelValidator getPanelValidator() {
      return JaWEManager.getInstance().getPanelValidator();
   }

   public TooltipGenerator getTooltipGenerator() {
      return JaWEManager.getInstance().getTooltipGenerator();
   }

   public HistoryManager getHistoryManager() {
      return hm;
   }

}
