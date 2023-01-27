package org.enhydra.jawe.base.panel.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.enhydra.jawe.ButtonPropertyChangedListener;
import org.enhydra.jawe.JaWEManager;
import org.enhydra.jawe.ResourceManager;
import org.enhydra.jawe.base.controller.JaWEActions;
import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.editor.StandardXPDLElementEditor;
import org.enhydra.jawe.base.panel.InlinePanel;
import org.enhydra.jawe.base.panel.PanelSettings;
import org.enhydra.jawe.base.panel.panels.tablesorting.BasicSortingTable;
import org.enhydra.shark.xpdl.XMLCollection;
import org.enhydra.shark.xpdl.XMLCollectionElement;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.xpdl.XMLElement;
import org.enhydra.shark.xpdl.XMLElementChangeInfo;
import org.enhydra.shark.xpdl.XMLElementChangeListener;
import org.enhydra.shark.xpdl.XMLUtil;

/**
 * Creates a table panel.
 * @author Sasa Bojanic
 * @author Zoran Milakovic
 * @author Miroslav Popov
 */
public class XMLBasicTablePanel extends XMLBasicPanel implements XMLElementChangeListener {

   public static Color FOREIGN_EL_COLOR_BKG = Color.lightGray;
   public static Color SPEC_EL_COLOR_BKG = Color.orange;

   protected static Dimension miniTableDimension = new Dimension(450, 125);
   protected static Dimension smallTableDimension = new Dimension(450, 200);
   protected static Dimension mediumTableDimension = new Dimension(550, 200);
   protected static Dimension largeTableDimension = new Dimension(650, 200);

   /**
    * Object which we are replacing from one place to another within
    * the list by dragging it.
    */
   protected XMLElement movingElement;

   /**
    * Index of the object which we are replacing from one place to another
    * within the list by dragging it.
    */
   protected int movingElementPosition;

   /**
    * The new index of the object which we are replacing from one place
    * to another within the list by dragging it.
    */
   protected int newMovingElementPosition;

   /** Indicates if object is being dragged. */
   protected boolean dragging = false;

   /**
    * Indicates if the code for changing object position within the list
    * is executed.
    */
   protected boolean changing = false;

   protected JTable allItems;
   protected JPanel toolbox;

   protected Vector columnNames;
   protected List columnsToShow;

   protected InlinePanel ipc;

   public XMLBasicTablePanel(
         InlinePanel ipc,
         XMLCollection myOwner,
         List columnsToShow,
         List elementsToShow,
         String title,
         boolean hasBorder,
         boolean hasEmptyBorder,
         boolean automaticWidth,
         boolean miniDimension,
         final boolean colors,
         final boolean showArrows) {

      super(ipc,myOwner, title, true, hasBorder, hasEmptyBorder);

      this.ipc=ipc;

      myOwner.addListener(this);
      myOwner.setNotifyListeners(true);

      columnNames = getColumnNames(columnsToShow);
      this.columnsToShow = columnsToShow;
      allItems = createTable(colors);
      setupTable(miniDimension, automaticWidth, showArrows);
      fillTableContent(elementsToShow);

      toolbox = createToolbar();
      JPanel paneAndArrows = new JPanel();
      paneAndArrows.setLayout(new BoxLayout(paneAndArrows, BoxLayout.X_AXIS));
      paneAndArrows.add(createScrollPane());

//      if (showArrows) {
//         JPanel p = createArrowPanel();
//         paneAndArrows.add(Box.createRigidArea(new Dimension(5, 0)));
//         paneAndArrows.add(p);
//      }

      add(toolbox);
      add(Box.createVerticalStrut(3));
      add(paneAndArrows);

      adjustActions();
   }

   public JTable getTable() {
      return allItems;
   }

   public XMLElement getSelectedElement() {
      int row = allItems.getSelectedRow();
      if (row >= 0) {
         return (XMLElement) allItems.getValueAt(row, 0);
      }
      return null;

   }

   public boolean setSelectedElement(Object el) {
      try {
         int rc = allItems.getRowCount();
         if (rc > 0) {
            for (int i = 0; i < rc; i++) {
               if (el==allItems.getValueAt(i, 0)) {
                  allItems.setRowSelectionInterval(i, i);

                  // focus the row

                  JViewport viewport = (JViewport) allItems.getParent();
                  // This rectangle is relative to the table where the
                  // northwest corner of cell (0,0) is always (0,0).
                  Rectangle rect = allItems.getCellRect(i, 0, true);
                  // The location of the viewport relative to the table
                  Point pt = viewport.getViewPosition();
                  // Translate the cell location so that it is relative
                  // to the view, assuming the northwest corner of the
                  // view is (0,0)
                  rect.setLocation(rect.x - pt.x, rect.y - pt.y);
                  // Scroll the area into view
                  viewport.scrollRectToVisible(rect);

                  return true;
               }
            }
         }
      } catch (Exception ex) {
      }
      return false;
   }

   public void setSelectedRow(int row) {
      try {
         allItems.setRowSelectionInterval(row, row);

         adjustActions();
      } catch (Exception e) {
      }
   }

   public void addRow(XMLElement e) {
      int rowpos = allItems.getRowCount();
      DefaultTableModel dtm = (DefaultTableModel) allItems.getModel();
      Vector v = getRow(e);
      dtm.insertRow(rowpos, v);
   }

   public void removeRow(int row) {
      DefaultTableModel dtm = (DefaultTableModel) allItems.getModel();
      dtm.removeRow(row);
   }

   protected void moveItem(int upOrDown) {
      newMovingElementPosition = movingElementPosition;
      if (newMovingElementPosition == -1) {
         return;
      }
      if (upOrDown == 0) {
            newMovingElementPosition--;
      } else {
            newMovingElementPosition++;
      }
      moveItem();
   }

   protected void moveItem() {
      changing = true;
      XMLCollection owncol = (XMLCollection) getOwner();
      int rowCnt = allItems.getRowCount();
      if (movingElement == null || movingElementPosition == -1 || newMovingElementPosition == -1
            || newMovingElementPosition == movingElementPosition || (rowCnt - 1) < movingElementPosition
            || (rowCnt - 1) < newMovingElementPosition || !owncol.contains(movingElement)) {
         changing = false;
         return;
      }

      if (JaWEManager.getInstance().getJaWEController().canRepositionElement(owncol, movingElement)) {
         XMLElement currentElementAtPosition = (XMLElement) allItems.getValueAt(newMovingElementPosition, 0);
         int newpos = owncol.indexOf(currentElementAtPosition);

         DefaultTableModel dtm = (DefaultTableModel) allItems.getModel();
         Vector v = getRow(movingElement);
         dtm.removeRow(movingElementPosition);
         dtm.insertRow(newMovingElementPosition, v);

         JaWEController jc = JaWEManager.getInstance().getJaWEController();
         jc.startUndouableChange();
         owncol.reposition(movingElement, newpos);
         List toSelect = new ArrayList();
         toSelect.add(movingElement);
         jc.endUndouableChange(toSelect);

         setSelectedRow(newMovingElementPosition);

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
            if( sw == JOptionPane.CANCEL_OPTION || (sw==JOptionPane.YES_OPTION && ipc.isModified())) {
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
               int sw=ipc.showModifiedWarning();
               if( sw == JOptionPane.CANCEL_OPTION || (sw==JOptionPane.YES_OPTION && ipc.isModified())) {
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

   protected Vector getColumnNames(List columnsToShow) {
      // creating a table which do not allow cell editing
      Vector cnames = new Vector();
      cnames.add("Object");
      XMLElement cel = ((XMLCollection) getOwner()).generateNewElement();
      if (cel instanceof XMLComplexElement) {
         Iterator it = columnsToShow.iterator();
         while (it.hasNext()) {
            String elemName = (String) it.next();
            XMLElement el = ((XMLComplexElement) cel).get(elemName);
            if (el != null) {
               cnames.add(JaWEManager.getInstance().getLabelGenerator().getLabel(el));
            } else {
               it.remove();
            }
         }
      } else {
         cnames.add(JaWEManager.getInstance().getLabelGenerator().getLabel(cel));
      }
      return cnames;
   }

   protected JTable createTable(final boolean colors) {
      JTable t=new BasicSortingTable(this, new Vector(), columnNames) {

         public boolean isCellEditable(int row, int col) {
            return false;
         }

         // This table colors elements depending on their owner
         public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (!isCellSelected(rowIndex, vColIndex) && colors) {
               XMLElement el = (XMLElement) getValueAt(rowIndex, 0);
               if (el instanceof XMLCollectionElement) {
                  XMLCollectionElement cel = (XMLCollectionElement) el;
                  XMLCollection celOwner = (XMLCollection) cel.getParent();
                  if (celOwner == null) {
                     c.setBackground(SPEC_EL_COLOR_BKG);
                  } else if (celOwner!=getOwner()) {
                     c.setBackground(FOREIGN_EL_COLOR_BKG);
                  } else {
                     c.setBackground(getBackground());
                  }
               } else {
                  c.setBackground(getBackground());
               }
            }

            return c;
         }
      };

      Color bkgCol=new Color(245,245,245);
      if (ipc.getSettings() instanceof PanelSettings) {
         bkgCol=((PanelSettings)ipc.getSettings()).getBackgroundColor();
      }
      t.setBackground(bkgCol);

      return t;
   }

   protected void setupTable(boolean miniDimension, boolean automaticWidth, final boolean showArrows) {
      TableColumn column;
      // setting the first column (object column) to be invisible
      column = allItems.getColumnModel().getColumn(0);
      column.setMinWidth(0);
      column.setMaxWidth(0);
      column.setPreferredWidth(0);
      column.setResizable(false);
      // setting fields that will not be displayed within the table

      // setting some table properties
      allItems.setColumnSelectionAllowed(false);
      allItems.setRowSelectionAllowed(true);
      allItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      allItems.getTableHeader().setReorderingAllowed(false);

      Dimension tDim;
      int noOfVisibleColumns = columnNames.size() - 1;
      if (miniDimension) {
         tDim = new Dimension(miniTableDimension);
      } else if (noOfVisibleColumns <= 3) {
         tDim = new Dimension(smallTableDimension);
      } else if (noOfVisibleColumns <= 5) {
         tDim = new Dimension(mediumTableDimension);
      } else {
         tDim = new Dimension(largeTableDimension);
      }

      if (automaticWidth) {
         tDim.width = allItems.getPreferredScrollableViewportSize().width;
      }
      allItems.setPreferredScrollableViewportSize(new Dimension(tDim));

      allItems.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "edit");
      allItems.getActionMap().put("edit", editElementAction);

      allItems.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false), "delete");
      allItems.getActionMap().put("delete", deleteElementAction);


      final XMLCollection col = (XMLCollection) getOwner();
      final boolean canRepos = JaWEManager.getInstance().getJaWEController().canRepositionElement(col, null);

      if (!getOwner().isReadOnly())
         allItems.setToolTipText(ResourceManager.getLanguageDependentString("MessageDragItemToChangeItsPosition"));

      // mouse listener for editing on double-click
      allItems.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (me.getClickCount() > 1) {
               editElementAction.actionPerformed(null);
            }
         }

         /** Marks the object which place within the table will be changed.*/
         public void mousePressed(MouseEvent me) {
            movingElement = null;
            movingElementPosition = -1;
            if (showArrows && !getOwner().isReadOnly() && canRepos) {
               dragging = true;
            }
            try {
               movingElementPosition = allItems.getSelectedRow();
               if (movingElementPosition >= 0) {
                  movingElement = (XMLElement) allItems.getValueAt(movingElementPosition, 0);
                  adjustActions();
               }
            } catch (Exception ex) {
            }
         }

         /** Just indicates that dragging is over.*/
         public void mouseReleased(MouseEvent me) {
            dragging = false;
         }
      });

      /** Changes position of object within the list.*/
      if (showArrows && !myOwner.isReadOnly() && canRepos) {// && ((XMLCollection)getOwner()).getParent().isReadOnly()))) {
         ListSelectionModel rowSM = allItems.getSelectionModel();
         rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
               if (dragging && !changing) {
                  newMovingElementPosition = -1;
                  try {
                     newMovingElementPosition = allItems.getSelectedRow();
                  } catch (Exception ex) {
                  }

                  moveItem();
               }
               adjustActions();
            }
         });
      }

   }

   protected void fillTableContent(List elementsToShow) {
      DefaultTableModel dtm = (DefaultTableModel) allItems.getModel();
      Iterator it = elementsToShow.iterator();

      while (it.hasNext()) {
         XMLElement elem = (XMLElement) it.next();
         Vector v = getRow(elem);
         dtm.addRow(v);
      }
   }

   protected Vector getRow(XMLElement elem) {
      Vector v = new Vector();
      if (elem instanceof XMLComplexElement) {
         Iterator itAllElems = columnsToShow.iterator();
         v = new Vector();
         XMLComplexElement cmel=(XMLComplexElement) elem;
         while (itAllElems.hasNext()) {
            String elName=(String)itAllElems.next();
            XMLElement el = cmel.get(elName);
            if (el!=null) {
               v.add(new XMLElementView(ipc,el, XMLElementView.TOVALUE));
            }
         }
      } else {
         v.add(new XMLElementView(ipc,elem, XMLElementView.TOVALUE));
      }
      v.add(0, elem);
      return v;
   }

   protected JScrollPane createScrollPane() {
      // creates panel
      JScrollPane allItemsPane = new JScrollPane();
      allItemsPane.setViewportView(allItems);
      return allItemsPane;
   }

   protected JPanel createToolbar() {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      JButton buttonNew = PanelUtilities.createToolbarButton(ipc.getSettings(),newElementAction);
      buttonNew.setRolloverEnabled(true);
      JButton buttonEdit = PanelUtilities.createToolbarButton(ipc.getSettings(),editElementAction);
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

      JButton buttonUp = new JButton();
      buttonUp.setIcon(ipc.getPanelSettings().getArrowUpImageIcon());
      buttonUp.setPreferredSize(new Dimension(16, 16));
      buttonUp.setEnabled(false);
      buttonUp.addActionListener(moveUpAction);
      moveUpAction.addPropertyChangeListener(new ButtonPropertyChangedListener(buttonUp));

      JButton buttonDown = new JButton();
      buttonDown.setIcon(ipc.getPanelSettings().getArrowDownImageIcon());
      buttonDown.setPreferredSize(new Dimension(16, 16));
      buttonDown.setEnabled(false);
      buttonDown.addActionListener(moveDownAction);
      moveDownAction.addPropertyChangeListener(new ButtonPropertyChangedListener(buttonDown));

      p.add(buttonUp);
      p.add(Box.createVerticalGlue());
      p.add(buttonDown);
      return p;
   }

   public void xmlElementChanged(XMLElementChangeInfo info) {
      if (info.getAction() == XMLElementChangeInfo.REMOVED) {
         Iterator it = info.getChangedSubElements().iterator();
         while (it.hasNext()) {
            XMLElement el = (XMLElement) it.next();
            int row = getElementRow(el);
//            System.out.println("Removing row " + row + " for element " + el);
            if (row != -1) {
               removeRow(row);
            }
         }
      } else if (info.getAction() == XMLElementChangeInfo.INSERTED) {
         Iterator it = info.getChangedSubElements().iterator();
         while (it.hasNext()) {
            XMLElement el = (XMLElement) it.next();
            addRow(el);
         }
      }
   }

   protected int getElementRow(XMLElement el) {
      int row = -1;
      for (int i = 0; i < allItems.getRowCount(); i++) {
         XMLElement toCompare = (XMLElement) allItems.getValueAt(i, 0);
         if (el==toCompare) {
            row = i;
            break;
         }
      }
      return row;
   }

   protected void adjustActions() {
      JaWEController jc = JaWEManager.getInstance().getJaWEController();

      XMLElement selEl=getSelectedElement();
      newElementAction.setEnabled(jc.canCreateElement((XMLCollection) getOwner()));
      editElementAction.setEnabled((selEl != null && XMLUtil.getPackage(selEl)!=null));
      deleteElementAction.setEnabled((selEl != null && jc.canRemoveElement((XMLCollection)getOwner(), selEl)));

      boolean canRepos = JaWEManager.getInstance().getJaWEController().canRepositionElement((XMLCollection) getOwner(),
            null);
      moveUpAction.setEnabled(selEl != null && allItems.getSelectedRow() > 0 && canRepos);
      moveDownAction.setEnabled(selEl != null
            && allItems.getSelectedRow() < allItems.getModel().getRowCount() - 1 && canRepos);
   }

   public void cleanup () {
      myOwner.removeListener(this);
   }
}
