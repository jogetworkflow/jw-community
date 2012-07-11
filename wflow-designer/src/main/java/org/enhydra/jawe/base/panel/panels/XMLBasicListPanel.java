package org.enhydra.jawe.base.panel.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.base.controller.JaWEActions;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.editor.StandardXPDLElementEditor;
import org.enhydra.jawe.base.panel.InlinePanel;
import org.enhydra.jawe.base.panel.PanelSettings;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;
import org.enhydra.shark.xpdl.XMLElementChangeListener;
import org.enhydra.shark.xpdl.XMLUtil;

/**
 * Creates a list panel.
 *
 * @author Sasa Bojanic
 * @author Zoran Milakovic
 * @author Miroslav Popov
 *
 */
public class XMLBasicListPanel extends XMLBasicPanel implements XMLElementChangeListener {

   protected static Dimension minimalDimension = new Dimension(250, 60);

   protected static Dimension listDimension = new Dimension(450, 150);

   /**
    * Object which we are replacing from one place to another within the list by dragging it.
    */
   protected XMLElementView movingElement;

   /**
    * Index of the object which we are replacing from one place to another within the list by
    * dragging it.
    */
   protected int movingElementPosition;

   /**
    * The new index of the object which we are replacing from one place to another within the list
    * by dragging it.
    */
   protected int newMovingElementPosition;

   /** Indicates if object is being dragged. */
   protected boolean dragging = false;

   /**
    * Indicates if the code for changing object position within the list is executed.
    */
   protected boolean changing = false;

   protected JList allParam;

   protected JPanel toolbox;

   protected InlinePanel ipc;

   public XMLBasicListPanel(InlinePanel ipc, XMLCollection myOwner, List elementsToShow, String title,
         boolean hasBorder, boolean hasEmptyBorder, final boolean enableEditing, boolean minDimension) {

      super(ipc, myOwner, title, true, hasBorder, hasEmptyBorder);
      this.ipc=ipc;

      myOwner.addListener(this);
      myOwner.setNotifyListeners(true);

      allParam = createList();
      setupList(enableEditing);
      fillListContent(elementsToShow);

      JScrollPane scrollParam = new JScrollPane();
      scrollParam.setAlignmentX(Component.LEFT_ALIGNMENT);
      //scrollParam.setAlignmentY(Component.TOP_ALIGNMENT);

      scrollParam.setViewportView(allParam);
      if (!minDimension) {
         scrollParam.setPreferredSize(new Dimension(listDimension));
      } else {
         scrollParam.setPreferredSize(new Dimension(minimalDimension));
      }

      toolbox = createToolbar();
      JPanel paneAndArrows = new JPanel();
      paneAndArrows.setLayout(new BoxLayout(paneAndArrows, BoxLayout.X_AXIS));
      paneAndArrows.add(scrollParam);

//      JPanel p = createArrowPanel();
//      paneAndArrows.add(Box.createRigidArea(new Dimension(5, 0)));
//      paneAndArrows.add(p);

      add(toolbox);
      add(Box.createVerticalStrut(3));
      add(paneAndArrows);

      adjustActions();
   }

   public boolean isItemChangingPosition() {
      return (changing || dragging);
   }

   public JList getList() {
      return allParam;
   }

   public XMLElement getSelectedElement() {
      if (allParam.getSelectedIndex() == -1)
         return null;

      return ((XMLElementView) allParam.getSelectedValue()).getElement();
   }

   public boolean setSelectedElement(XMLElement el) {
      int selIndex = -1;
      XMLElementView ev = getRow(el);
      for (int i = 0; i < allParam.getModel().getSize(); i++) {
         XMLElementView elat = (XMLElementView) allParam.getModel().getElementAt(i);
         if (ev.equals(elat)) {
            selIndex = i;
            break;
         }
      }

      if (selIndex != -1) {
         allParam.setSelectedIndex(selIndex);
      }
      return (selIndex!=-1);
   }

   protected void moveItem(int upOrDown) {
      newMovingElementPosition = movingElementPosition;
      if (newMovingElementPosition == -1) {
         return;
      } else if (upOrDown == 0) {
         newMovingElementPosition--;
      } else {
         newMovingElementPosition++;
      }

      moveItem();
   }

   protected void moveItem() {
      changing = true;
      DefaultListModel listModel = (DefaultListModel) allParam.getModel();
      XMLCollection owncol = (XMLCollection) getOwner();
      int rowCnt = listModel.getSize();
      if (movingElement == null || movingElementPosition == -1 || newMovingElementPosition == -1
            || newMovingElementPosition == movingElementPosition || (rowCnt - 1) < movingElementPosition
            || (rowCnt - 1) < newMovingElementPosition || !owncol.contains(movingElement.getElement())) {
         changing = false;
         return;
      }

      XMLCollection col = (XMLCollection) getOwner();
      if (JaWEManager.getInstance().getJaWEController().canRepositionElement(col, movingElement.getElement())) {
         XMLElement currentElementAtPosition = ((XMLElementView) listModel.getElementAt(newMovingElementPosition))
               .getElement();
         int newpos = owncol.indexOf(currentElementAtPosition);

         listModel.remove(movingElementPosition);
         listModel.add(newMovingElementPosition, movingElement);

         JaWEController jc = JaWEManager.getInstance().getJaWEController();
         jc.startUndouableChange();
         owncol.reposition(movingElement.getElement(), newpos);
         List toSelect = new ArrayList();
         toSelect.add(movingElement.getElement());
         jc.endUndouableChange(toSelect);

         try {
            allParam.setSelectedIndex(newMovingElementPosition);
         } catch (Exception ex) {
         }

         movingElementPosition = newMovingElementPosition;
      }
      changing = false;
   }

   protected Action newElementAction = new AbstractAction(JaWEActions.NEW_ACTION) {
      public void actionPerformed(ActionEvent ae) {
         JaWEController jc = JaWEManager.getInstance().getJaWEController();

         XMLCollection col = (XMLCollection) getOwner();
         XMLElement newEl = JaWEManager.getInstance().getXPDLObjectFactory().createXPDLObject(col, null, false);
         // CUSTOM: always show modal dialog
         boolean isForModal=true; //PanelUtilities.isForModalDialog(newEl);
         // END CUSTOM
         if (!isForModal && ipc.isModified()) {
            int sw=ipc.showModifiedWarning();
            if(sw == JOptionPane.CANCEL_OPTION || (sw==JOptionPane.YES_OPTION && ipc.isModified())) {
               return;
            }
         }

         boolean updInProg=false;
         if (isForModal) {
            StandardXPDLElementEditor ed = new StandardXPDLElementEditor();
            ed.editXPDLElement(newEl);
            boolean statOK=(ed.getStatus()==StandardXPDLElementEditor.STATUS_OK);
            boolean canIns=true;
            if (statOK) {
               canIns=jc.canInsertElement(col, newEl);
            }
            if (!statOK || !canIns) {
               if (!canIns) {
                  jc.getJaWEFrame().message(ed.getLanguageDependentString("WarningCannotInsertElement"),JOptionPane.WARNING_MESSAGE);
               }
               return;
            }
            updInProg=true;
         }
         jc.startUndouableChange();
         col.add(newEl);
         List temp = new ArrayList();
         temp.add(newEl);
         jc.endUndouableChange(temp);
         if (updInProg) {
            setSelectedElement(newEl);
         }

         adjustActions();
      }
   };

   protected Action editElementAction = new AbstractAction(JaWEActions.EDIT_PROPERTIES_ACTION) {
      public void actionPerformed(ActionEvent ae) {
         XMLElement editElement = getSelectedElement();
         if (editElement != null) {
             if (ipc.isModified()) {
                 int sw = ipc.showModifiedWarning();
                 if (sw == JOptionPane.CANCEL_OPTION || (sw == JOptionPane.YES_OPTION && ipc.isModified())) {
                     return;
                 }
             }
             // CUSTOM: show modal dialog
             StandardXPDLElementEditor ed = new StandardXPDLElementEditor();
             ed.editXPDLElement(editElement);
             XMLCollection col = (XMLCollection) getOwner();
             int idx = col.indexOf(editElement);
             col.remove(editElement);
             col.add(editElement);
             if (idx >= 0) {
                 col.reposition(editElement, idx);
             }
            //JaWEManager.getInstance().getXPDLElementEditor().editXPDLElement(editElement);
            // END CUSTOM
         }
      }
   };

   protected Action deleteElementAction = new AbstractAction(JaWEActions.DELETE_ACTION) {
      public void actionPerformed(ActionEvent ae) {
         XMLElement deleteElement = getSelectedElement();
         if (deleteElement != null) {
            JaWEController jc = JaWEManager.getInstance().getJaWEController();
            List sel=new ArrayList();
            sel.add(deleteElement.getParent());
            if (jc.confirmDelete(sel, deleteElement)) {
               XMLCollection parent = (XMLCollection)getOwner();
               jc.startUndouableChange();
               parent.remove(deleteElement);
               jc.endUndouableChange(sel);
               ipc.getPanelSettings().adjustActions();
               adjustActions();
            }
         }
      }
   };

   protected Action moveUpAction = new AbstractAction("MoveUp") {
      public void actionPerformed(ActionEvent ae) {
         moveItem(0);
         adjustActions();
      }
   };

   protected Action moveDownAction = new AbstractAction("MoveDown") {
      public void actionPerformed(ActionEvent ae) {
         moveItem(1);
         adjustActions();
      }
   };

   protected JList createList() {
      DefaultListModel listModel = new DefaultListModel();

      JList l = new JList(listModel);
      Color bkgCol=new Color(245,245,245);
      if (ipc.getSettings() instanceof PanelSettings) {
         bkgCol=((PanelSettings)ipc.getSettings()).getBackgroundColor();
      }
      l.setBackground(bkgCol);

      return l;
   }

   protected void setupList(final boolean enableEditing) {
      allParam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      allParam.setAlignmentX(Component.LEFT_ALIGNMENT);
      allParam.setAlignmentY(Component.TOP_ALIGNMENT);

      final XMLCollection col = (XMLCollection) getOwner();
      final boolean canRepos = JaWEManager.getInstance().getJaWEController().canRepositionElement(col, null);

      // mouse listener for editing on double-click and dragging list items
      allParam.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            // implement some action only if editing is enabled
            if (enableEditing && me.getClickCount() > 1) {
               editElementAction.actionPerformed(null);
            }
         }

         /** Marks the object which place within the list will be changed. */
         public void mousePressed(MouseEvent me) {
            if (!getOwner().isReadOnly() && canRepos) {
               dragging = true;
            }
            movingElement = null;
            movingElementPosition = -1;
            try {
               movingElementPosition = allParam.getSelectedIndex();
               if (movingElementPosition >= 0) {
                  movingElement = (XMLElementView) allParam.getSelectedValue();
                  adjustActions();
               }
            } catch (Exception ex) {
            }

            if (changing) {
               changing = false;
               return;
            }
         }

         /** Just indicates that dragging is over. */
         public void mouseReleased(MouseEvent me) {
            dragging = false;
         }

      });

      /** Changes position of object within the list. */
      if (!myOwner.isReadOnly() && canRepos) {
         allParam.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
               if (dragging && !changing) {
                  newMovingElementPosition = -1;
                  try {
                     newMovingElementPosition = allParam.getSelectedIndex();
                  } catch (Exception ex) {
                  }

                  moveItem();
               }
               adjustActions();
            }
         });
      }

      if (enableEditing) {
         allParam.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "edit");
         allParam.getActionMap().put("edit", editElementAction);

         allParam.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false),
               "delete");
         allParam.getActionMap().put("delete", deleteElementAction);
      }

      if (!getOwner().isReadOnly() && canRepos) {
         allParam.setToolTipText(ResourceManager.getLanguageDependentString("MessageDragItemToChangeItsPosition"));
      }
   }

   protected void fillListContent(List elementsToShow) {
      // fills list
      DefaultListModel listModel = (DefaultListModel) allParam.getModel();
      Iterator it = elementsToShow.iterator();
      while (it.hasNext()) {
         XMLElement elem = (XMLElement) it.next();
         XMLElementView ev = getRow(elem);
         listModel.addElement(ev);
      }

   }

   protected XMLElementView getRow(XMLElement el) {
      //      if (el instanceof XMLComplexElement) {
      return new XMLElementView(ipc, el, XMLElementView.TONAME);
      //      } else {
      //         return new XMLElementView( el, XMLElementView.TOVALUE);
      //      }
   }

   protected JPanel createToolbar() {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      JButton buttonNew = PanelUtilities.createToolbarButton(ipc.getSettings(), newElementAction);
      buttonNew.setRolloverEnabled(true);
      JButton buttonEdit = PanelUtilities.createToolbarButton(ipc.getSettings(), editElementAction);
      buttonEdit.setRolloverEnabled(true);
      JButton buttonDelete = PanelUtilities.createToolbarButton(ipc.getSettings(), deleteElementAction);
      buttonDelete.setRolloverEnabled(true);

      panel.add(buttonNew);
      panel.add(Box.createRigidArea(new Dimension(3, 3)));
      panel.add(buttonEdit);
      panel.add(Box.createRigidArea(new Dimension(3, 3)));
      panel.add(buttonDelete);
      panel.add(Box.createHorizontalGlue());
      return panel;
   }

   protected JPanel createArrowPanel() {
      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
      XMLCollection col = (XMLCollection) getOwner();
      boolean canRepos = JaWEManager.getInstance().getJaWEController().canRepositionElement(col, null);

      JButton buttonUp = new JButton();
      buttonUp.setIcon(ipc.getPanelSettings().getArrowUpImageIcon());

      buttonUp.setPreferredSize(new Dimension(16, 16));
      buttonUp.setEnabled(!myOwner.isReadOnly() && canRepos);
      buttonUp.addActionListener(moveUpAction);
      JButton buttonDown = new JButton();

      buttonDown.setIcon(ipc.getPanelSettings().getArrowDownImageIcon());

      buttonDown.setPreferredSize(new Dimension(16, 16));
      buttonDown.setEnabled(!myOwner.isReadOnly() && canRepos);
      buttonDown.addActionListener(moveDownAction);
      p.add(buttonUp);
      p.add(Box.createVerticalGlue());
      p.add(buttonDown);
      return p;
   }

   protected void adjustActions() {
      JaWEController jc = JaWEManager.getInstance().getJaWEController();

      XMLElement selEl=getSelectedElement();
      newElementAction.setEnabled(jc.canCreateElement((XMLCollection) getOwner()));
      editElementAction.setEnabled((selEl != null && XMLUtil.getPackage(selEl)!=null));
      deleteElementAction.setEnabled((selEl != null && jc.canRemoveElement((XMLCollection)getOwner(), selEl)));

      boolean canRepos = JaWEManager.getInstance().getJaWEController().canRepositionElement((XMLCollection) getOwner(),
            null);
      moveUpAction.setEnabled(selEl != null && allParam.getSelectedIndex() > 0 && canRepos);
      moveDownAction.setEnabled(selEl != null
            && allParam.getSelectedIndex() < allParam.getModel().getSize() - 1 && canRepos);
   }

   public void xmlElementChanged(XMLElementChangeInfo info) {
      if (info.getAction() == XMLElementChangeInfo.REMOVED) {
         Iterator it = info.getChangedSubElements().iterator();
         while (it.hasNext()) {
            XMLElement el = (XMLElement) it.next();
            removeElement(el);
         }
      } else if (info.getAction() == XMLElementChangeInfo.INSERTED) {
         Iterator it = info.getChangedSubElements().iterator();
         while (it.hasNext()) {
            XMLElement el = (XMLElement) it.next();
            addElement(el);
         }
      }
   }

   public void addElement(XMLElement el) {
      DefaultListModel listModel = (DefaultListModel) allParam.getModel();
      XMLElementView ev = getRow(el);
      listModel.addElement(ev);
   }

   public void removeElement(XMLElement el) {
      DefaultListModel listModel = (DefaultListModel) allParam.getModel();
      XMLElementView ev = getRow(el);
      if (ev != null)
         listModel.removeElement(ev);
   }

   public void cleanup() {
      myOwner.removeListener(this);
      allParam = null;
   }

}