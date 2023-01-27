/**
 * Miroslav Popov, Jul 20, 2005
 */
package org.enhydra.jawe.components.graph;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Default panel for jawe object such as activity, route etc. It has icon and name.
 *
 * @author Miroslav Popov
 */
public class DefaultCellPanel extends JPanel {

   // 0 - divLocation = icon space, divLocation - with = name space
   protected int divLocation = 20;

   protected JSplitPane split;
   protected JTextArea name = new JTextArea();
   protected JLabel mainIcon = new JLabel();

   protected int orientation = 0;

   protected DefaultCellPanel() {
      name.setText("-");
      name.setOpaque(false);
      // CUSTOM: hide border for NimROD L&F
      name.setBorder(null);
      // END CUSTOM
      mainIcon.setIcon(null);
      mainIcon.setVerticalAlignment(SwingConstants.TOP);

      setOpaque(false);
      setLayout(new BorderLayout());
      split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainIcon, name);
      split.setDividerLocation(divLocation);
      split.setBorder(null);
      split.setDividerSize(0);
      split.setOpaque(false);

      add(split);
   }

   public void showIcon(boolean show) {
      mainIcon.setVisible(show);
   }

   public Icon getMainIcon() {
      return mainIcon.getIcon();
   }

   public void setMainIcon(Icon mainIcon) {
      this.mainIcon.setIcon(mainIcon);
   }

   public String getDisplayName() {
      return name.getText();
   }

   public void setDisplayName(String name) {
      this.name.setForeground(GraphUtilities.getGraphController().getGraphSettings().getTextColor());
      this.name.setText(name);
   }

   public void wrapName(boolean wrap) {
      name.setLineWrap(wrap);
   }

   public void wrapStyle(boolean word) {
      name.setWrapStyleWord(word);
   }

   public void setFont(Font font) {
      if (name != null) name.setFont(font);
   }

   /**
    * Set text and icon on panel depending on parameter place 1 - icon bottom, text up 2 - icon top,
    * text bottom 3 - icon right, text left default - icon left, text right
    *
    * @param place
    */
   public void setTextPosition(int place) {
      orientation = place;
      arrangeSplit();
   }

   public void arrangeSplit() {
      remove(split);
      switch (orientation) {
      case 1:
         split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, name, mainIcon);
         split.setDividerLocation( GraphUtilities.getGraphController().getGraphSettings().getActivityHeight() - divLocation);
         break;
      case 2:
         split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainIcon, name);
         split.setDividerLocation(divLocation);
         break;
      case 3:
         split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, name, mainIcon);
         split.setDividerLocation( GraphUtilities.getGraphController().getGraphSettings().getActivityWidth() - divLocation);
         break;
      default:
         split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainIcon, name);
         split.setDividerLocation(divLocation);
         break;
      }
      split.setBorder(null);
      split.setDividerSize(0);
      split.setOpaque(false);
      add(split);
   }

    public void setBounds(Rectangle rect) {
      super.setBounds(rect);
      if (split != null) {
       int iconSize = 0;

       switch (orientation) {
       case GraphSettings.UP:
          if (mainIcon.isVisible()) {
             iconSize = mainIcon.getIcon().getIconWidth();
          }

          name.setBounds(name.getX(), name.getY(), rect.width, rect.height  - iconSize - 3);
          mainIcon.setLocation(0, rect.height - iconSize);
          break;
       case GraphSettings.DOWN:
          if (mainIcon.isVisible()) {
             iconSize = mainIcon.getIcon().getIconWidth();
          }
          name.setBounds(name.getX(), name.getY(), rect.width, rect.height  - iconSize - 3);
          break;
       case GraphSettings.LEFT:
          if (mainIcon.isVisible()) {
             iconSize = mainIcon.getIcon().getIconWidth();
          }
          name.setBounds(name.getX(), name.getY(), rect.width - iconSize - 3, rect.height);
          mainIcon.setLocation(rect.width - iconSize, 0);
          break;
       default:
          if (mainIcon.isVisible()) {
             iconSize = mainIcon.getIcon().getIconWidth();
          }
          name.setBounds(name.getX(), name.getY(), rect.width - iconSize - 3, rect.height);
          break;
       }

         split.setBounds(rect);
      }
   }
}
