package org.enhydra.jawe;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.enhydra.jawe.base.controller.JaWEType;
import org.enhydra.jawe.base.controller.JaWETypeChoiceButton;
import org.enhydra.jawe.base.editor.NewStandardXPDLElementEditor;

public class BarFactory {

   public final static String CLASSNAME_POSTFIX = "ClassName";

   public final static String SETTINGSNAME_POSTFIX = "SettingsName";

   public final static String ACCELERATION_POSTFIX = "Accel";

   public final static String MNEMONIC_POSTFIX = "Mnemonic";

   public final static String LABEL_POSTFIX = "Label";

   public final static String TOOLTIP_POSTFIX = "Tooltip";

   public final static String LANGUAGEDEPENDENTNAME_POSTFIX = "LangName";

   public final static String JAWE_STANDARD_MENU_PREFIX = "jawe_";

   public final static String JAWE_STANDARD_ACTION_PREFIX = "jaweAction_";

   public final static String JAWECOMPONENT_AS_MENU_PREFIX = "@";

   public final static String SUBMENU_PREFIX = "*";

   public final static String ACTION_DELIMITER = " ";

   public final static String ACTION_SEPARATOR = "-";

   public final static String CTRL_PREFIX = "CTRL";

   public final static String SHIFT_PREFIX = "SHIFT";

   public final static String ALT_PREFIX = "ALT";

   public static JMenuBar createMainMenu(JaWEComponent comp) {
      JMenuBar mbar = new JMenuBar();

      String actionOrder = comp.getSettings().getMainMenuActionOrder();
      String[] act = Utils.tokenize(actionOrder, ACTION_DELIMITER);

      for (int i = 0; i < act.length; i++) {
         if (act[i].startsWith(JAWECOMPONENT_AS_MENU_PREFIX)) {
            String className = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + CLASSNAME_POSTFIX);
            String settingsName = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + SETTINGSNAME_POSTFIX);
            JMenu subMenu = getExternalMenu(className, settingsName);
            if (subMenu != null) {
               BarFactory.setAccelerator(subMenu, comp.getSettings()
                  .getLanguageDependentString(act[i].substring(1) + ACCELERATION_POSTFIX));
               BarFactory.setMnemonic(subMenu, comp.getSettings()
                  .getLanguageDependentString(act[i].substring(1) + MNEMONIC_POSTFIX));
               mbar.add(subMenu);
            }
         } else if (act[i].startsWith(JAWE_STANDARD_MENU_PREFIX)) {
            mbar.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenu(act[i], true));
         } else if (act[i].startsWith(JAWE_STANDARD_ACTION_PREFIX)) {
            mbar.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenuItem(act[i].substring(11), true));
         } else if (act[i].startsWith(SUBMENU_PREFIX)) {
            JMenu sm = createSubMenu(act[i].substring(1), comp, true);
            if (sm != null) {
               mbar.add(sm);
            }
         } else {
            JaWEAction ja = comp.getSettings().getAction(act[i]);
            if (ja != null) {
               mbar.add(createMenuItem(ja, comp, true));
            }
         }
      }

      return mbar;
   }

   public static JPopupMenu createPopupMenu(String popupMenuName, JaWEComponent comp) {
      String actionOrder = comp.getSettings().getMenuActionOrder(popupMenuName);
      return createMenu(actionOrder, comp, false).getPopupMenu();
   }

   public static JToolBar createToolbar(String toolbarName, JaWEComponent comp) {
      String actionOrder = comp.getSettings().getToolbarActionOrder(toolbarName);

      JToolBar toolbar = new JToolBar();
      toolbar.setRollover(true);
      String[] act = Utils.tokenize(actionOrder, ACTION_DELIMITER);

      for (int i = 0; i < act.length; i++) {
         if (act[i].equals(ACTION_SEPARATOR)) {
            toolbar.addSeparator();
         } else if (act[i].startsWith(JAWE_STANDARD_MENU_PREFIX)) {
            toolbar.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionToolbar(act[i]));
         } else if (act[i].startsWith(JAWE_STANDARD_ACTION_PREFIX)) {
            toolbar.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionButton(act[i].substring(11)));
         } else if (act[i].startsWith(SUBMENU_PREFIX)) {
            toolbar.add(createToolbar(act[i].substring(1), comp));
         } else {
            JaWEAction ja = comp.getSettings().getAction(act[i]);
            if (ja != null) {
               toolbar.add(createToolbarButton(ja, comp));
            }
         }
      }

      toolbar.setName(comp.getSettings().getLanguageDependentString(toolbarName
                                                                    + LABEL_POSTFIX));

      return toolbar;
   }

   public static JButton createToolbarButton(JaWEAction ja, JaWEComponent comp) {
      Action a = ja.getAction();
      String aname = null;
      if (a != null) {
         aname = (String) a.getValue(Action.NAME);
      }
      String depName = ja.getLangDepName();
      String label = comp.getSettings().getLanguageDependentString(depName
                                                                   + LABEL_POSTFIX);
      if (aname == null) {
         aname = depName;
      }
      if (label == null) {
         label = aname;
      }
      JButton b = null;

      ImageIcon ai = ja.getIcon();
      if (ai != null) {
         if (a instanceof NewActionBase && comp instanceof ChoiceButtonListener) {
            b = new JaWETypeChoiceButton(JaWEType.class,
                                         ((NewActionBase) a).getXPDLTypeClass(),
                                         (ChoiceButtonListener) comp,
                                         ai);
         } else if (comp instanceof NewStandardXPDLElementEditor) {
            // CUSTOM
            b = new JButton(label, ai) {
               public float getAlignmentY() {
                  return 0.5f;
               }
            };
            // END CUSTOM
         } else {
            b = new JButton(ai) {
               public float getAlignmentY() {
                  return 0.5f;
               }
            };
         }
      } else {
         if (a instanceof NewActionBase && comp instanceof ChoiceButtonListener) {
            b = new JaWETypeChoiceButton(JaWEType.class,
                                         ((NewActionBase) a).getXPDLTypeClass(),
                                         (ChoiceButtonListener) comp,
                                         ai);
         } else {
            b = new JButton(label) {
               public float getAlignmentY() {
                  return 0.5f;
               }
            };
         }
      }

      b.setName(aname);
      b.setMargin(new Insets(1, 1, 1, 1));
      b.setRequestFocusEnabled(false);

      b.setActionCommand(aname);
      if (a != null) {
         b.addActionListener(a);
         b.setEnabled(a.isEnabled());
         a.addPropertyChangeListener(new ButtonPropertyChangedListener(b));
      } else {
         b.setEnabled(false);
      }
      String tip = comp.getSettings().getLanguageDependentString(depName
                                                                 + TOOLTIP_POSTFIX);
      if (tip != null) {
         b.setToolTipText(tip);
      }

      return b;
   }

   protected static JMenu createMenu(String actionOrder,
                                     JaWEComponent comp,
                                     boolean addBCListener) {
      JMenu menu = new JMenu();

      String[] act = Utils.tokenize(actionOrder, ACTION_DELIMITER);

      for (int i = 0; i < act.length; i++) {
         if (act[i].equals(ACTION_SEPARATOR)) {
            menu.addSeparator();
         } else if (act[i].startsWith(JAWECOMPONENT_AS_MENU_PREFIX)) {
            String className = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + CLASSNAME_POSTFIX);
            String settingsName = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + SETTINGSNAME_POSTFIX);
            JMenu subMenu = getExternalMenu(className, settingsName);
            BarFactory.setAccelerator(subMenu, comp.getSettings()
               .getLanguageDependentString(act[i].substring(1) + ACCELERATION_POSTFIX));
            BarFactory.setMnemonic(subMenu, comp.getSettings()
               .getLanguageDependentString(act[i].substring(1) + MNEMONIC_POSTFIX));
            menu.add(subMenu);
         } else if (act[i].startsWith(JAWE_STANDARD_MENU_PREFIX)) {
            menu.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenu(act[i], addBCListener));
         } else if (act[i].startsWith(JAWE_STANDARD_ACTION_PREFIX)) {
            menu.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenuItem(act[i].substring(11), addBCListener));
         } else if (act[i].startsWith(SUBMENU_PREFIX)) {
            menu.add(createSubMenu(act[i].substring(1), comp, addBCListener));
         } else {
            JaWEAction ja = comp.getSettings().getAction(act[i]);
            if (ja != null) {
               menu.add(createMenuItem(ja, comp, addBCListener));
            }
         }
      }

      return menu;
   }

   protected static JMenu createSubMenu(String name,
                                        JaWEComponent comp,
                                        boolean addBCListener) {
      JMenu menu = new JMenu();

      String depName = (String) comp.getSettings()
         .getSetting(name + LANGUAGEDEPENDENTNAME_POSTFIX);
      String langDepName = comp.getSettings().getLanguageDependentString(depName
                                                                         + LABEL_POSTFIX);
      if (langDepName == null) {
         langDepName = name;
      }

      BarFactory.setAccelerator(menu, comp.getSettings()
         .getLanguageDependentString(depName + ACCELERATION_POSTFIX));
      BarFactory.setMnemonic(menu, comp.getSettings()
         .getLanguageDependentString(depName + MNEMONIC_POSTFIX));

      menu.setText(langDepName);

      String actionOrder = comp.getSettings().getMenuActionOrder(name);
      String[] act = Utils.tokenize(actionOrder, ACTION_DELIMITER);
      int cnt=0;
      for (int i = 0; i < act.length; i++) {
         if (act[i].equals(ACTION_SEPARATOR)) {
            menu.addSeparator();
            cnt++;
         } else if (act[i].startsWith(JAWECOMPONENT_AS_MENU_PREFIX)) {
            String className = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + CLASSNAME_POSTFIX);
            String settingsName = (String) comp.getSettings()
               .getSetting(act[i].substring(1) + SETTINGSNAME_POSTFIX);
            JMenu subMenu = getExternalMenu(className, settingsName);
            if (subMenu != null) {
               BarFactory.setAccelerator(subMenu, comp.getSettings()
                  .getLanguageDependentString(act[i].substring(1) + ACCELERATION_POSTFIX));
               BarFactory.setMnemonic(subMenu, comp.getSettings()
                  .getLanguageDependentString(act[i].substring(1) + MNEMONIC_POSTFIX));
               menu.add(subMenu);
               cnt++;
            } else {
               System.out.println("Can't create ext meny "
                                  + settingsName + " for cn " + className);
            }
         } else if (act[i].startsWith(JAWE_STANDARD_MENU_PREFIX)) {
            menu.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenu(act[i], addBCListener));
            cnt++;
         } else if (act[i].startsWith(JAWE_STANDARD_ACTION_PREFIX)) {
            menu.add(JaWEManager.getInstance()
               .getJaWEController()
               .getJaWEActions()
               .getActionMenuItem(act[i].substring(11), addBCListener));
            cnt++;
         } else if (act[i].startsWith(SUBMENU_PREFIX)) {
            JMenu sm=createSubMenu(act[i], comp, addBCListener);
            if (sm!=null) {
               menu.add(sm);
               cnt++;
            }
         } else {
            JaWEAction ja = comp.getSettings().getAction(act[i]);
            if (ja != null) {
               menu.add(createMenuItem(ja, comp, addBCListener));
               cnt++;
            }
         }
      }

      if (cnt>0) {
         return menu;
      }
      return null;
   }

   protected static JMenu getExternalMenu(String className, String settingsClassName) {
      try {
         Constructor sc = Class.forName(settingsClassName).getConstructor(new Class[] {});
         JaWEComponentSettings settings = (JaWEComponentSettings) sc.newInstance(new Object[0]);

         Constructor c = Class.forName(className).getConstructor(new Class[] {
            JaWEComponentSettings.class
         });
         JaWEComponent jc = (JaWEComponent) c.newInstance(new Object[] {
            settings
         });
         return (JMenu) (jc.getView());
      } catch (Exception thr) {
      }
      return null;
   }

   public static JMenuItem createMenuItem(JaWEAction ja,
                                          JaWEComponent comp,
                                          boolean addBCListener) {
      Action a = ja.getAction();

      String aname = null;
      if (a != null) {
         aname = (String) a.getValue(Action.NAME);
      }
      String depName = ja.getLangDepName();
      if (aname == null) {
         aname = depName;
      }

      if (depName == null) {
         depName = aname;
      }

      String label = comp.getSettings().getLanguageDependentString(depName
                                                                   + LABEL_POSTFIX);
      if (label == null) {
         label = aname;
      }

      JMenuItem mi = new JMenuItem(label);
      mi.setName(aname);

      ImageIcon ai = ja.getIcon();
      if (ai != null) {
         mi.setHorizontalTextPosition(SwingConstants.RIGHT);
         mi.setIcon(ai);
      }

      BarFactory.setAccelerator(mi, comp.getSettings()
         .getLanguageDependentString(depName + ACCELERATION_POSTFIX));
      BarFactory.setMnemonic(mi, comp.getSettings()
         .getLanguageDependentString(depName + MNEMONIC_POSTFIX));

      mi.setActionCommand(aname);
      if (a != null) {
         mi.addActionListener(a);
         if (addBCListener) {
            a.addPropertyChangeListener(new ButtonPropertyChangedListener(mi));
         }
         mi.setEnabled(a.isEnabled());
      } else {
         mi.setEnabled(false);
      }

      return mi;
   }

   public static void setMnemonic(JMenuItem mi, String mnemonic) {
      if (mnemonic != null && mnemonic.length() > 0) {
         mi.setMnemonic(mnemonic.toCharArray()[0]);
      }
   }

   public static void setAccelerator(JMenuItem mi, String accel) {
      if (accel != null) {
         try {
            int mask = 0;
            if (accel.startsWith(CTRL_PREFIX)) {
               mask += ActionEvent.CTRL_MASK;
               accel = accel.substring(CTRL_PREFIX.length() + 1);
            }
            if (accel.startsWith(SHIFT_PREFIX)) {
               mask += ActionEvent.SHIFT_MASK;
               accel = accel.substring(SHIFT_PREFIX.length() + 1);
            }
            if (accel.startsWith(ALT_PREFIX)) {
               mask += ActionEvent.ALT_MASK;
               accel = accel.substring(ALT_PREFIX.length() + 1);
            }
            int key = KeyEvent.class.getField("VK_" + accel).getInt(null);
            mi.setAccelerator(KeyStroke.getKeyStroke(key, mask));
         } catch (Exception e) {
            System.err.println("Error while assigning accelerator !!!");
         }
      }
   }
}
