package org.enhydra.jawe;

import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The main editor class.
 * 
 * @author Sasa Bojanic
 */
public class JaWE {

    //CUSTOM
    public static boolean BASIC_MODE = true;
    //END CUSTOM
    static int timeLeft = 10; // in seconds

    public static void main(String[] args) throws Throwable {
        System.out.println("Starting JAWE ....");
        System.out.println("JaWE -> JaWE is being initialized ...");
        String splash = System.getProperty("Splash");
        JFrame splashFrame = null;
        if (splash != null) {
            splashFrame = new JFrame();
            try {
                URL imageLocation = null;
                imageLocation = JaWE.class.getClassLoader().getResource(splash.startsWith("/") ? splash.substring(1) : splash);
                if (imageLocation == null) {
                    imageLocation = new URL(splash);
                }
                Image i = Toolkit.getDefaultToolkit().getImage(imageLocation);
                MediaTracker mediaTracker = new MediaTracker(splashFrame);
                mediaTracker.addImage(i, 0);
                mediaTracker.waitForID(0);
                JLabel icon = new JLabel(new ImageIcon(i));
                splashFrame.setUndecorated(true);
                splashFrame.getContentPane().add(icon);
                splashFrame.pack();
                splashFrame.setLocationRelativeTo(null);
                splashFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File cfgf = null;
        System.out.println("JaWE_CONF_HOME=" + JaWEConstants.JAWE_CONF_HOME);
        if (JaWEConstants.JAWE_CONF_HOME != null) {
            File mainConfig = new File(JaWEConstants.JAWE_CONF_HOME + "/" + "defaultconfig");
            Properties props = new Properties();
            if (mainConfig.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mainConfig);
                    props.load(fis);
                } catch (Exception ex) {
                    System.err.println("Something went wrong while reading configuration from the file " + mainConfig.getAbsolutePath());
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
            String conf_home = JaWEConstants.JAWE_CONF_HOME + "/" + props.getProperty(JaWEConstants.JAWE_CURRENT_CONFIG_HOME);
            File cfh = new File(conf_home);
            if (cfh.exists()) {
                System.setProperty(JaWEConstants.JAWE_CURRENT_CONFIG_HOME, conf_home);
                if (Utils.checkFileExistence(JaWEManager.TOGWE_BASIC_PROPERTYFILE_NAME) || Utils.checkResourceExistence(JaWEManager.TOGWE_BASIC_PROPERTYFILE_PATH, JaWEManager.TOGWE_BASIC_PROPERTYFILE_NAME)) {
                    cfgf = new File(conf_home + "/" + JaWEManager.TOGWE_BASIC_PROPERTYFILE_NAME);
                } else {
                    cfgf = new File(conf_home + "/" + JaWEConstants.JAWE_BASIC_PROPERTYFILE_NAME);
                }
            }
        }
        if (cfgf != null && cfgf.exists()) {
            JaWEManager.configure(cfgf);
        } else {
            JaWEManager.configure();
        }

        // Starting file name
        String fn = null;

        if (args != null && args.length > 0) {
            // check if there is a file that should be open at the startup
            if(args[0] != null && !args[0].trim().equals("")){
                fn = args[0];
            }

            // check if there is a locale is set
            if(args[1] != null && !args[1].trim().equals("")){
                Locale locale = null;

                String[] temp = args[1].split("_");

                if(temp.length == 1){
                    locale = new Locale(temp[0]);
                }else if (temp.length == 2){
                    locale = new Locale(temp[0], temp[1]);
                }else if (temp.length == 3){
                    locale = new Locale(temp[0], temp[1], temp[2]);
                }

                ResourceManager.setChoosen(locale);
            }
        }

        JaWEManager.getInstance().start(fn);

        if (splashFrame != null) {
            splashFrame.setVisible(false);
            splashFrame.dispose();
        }
    }

    //CUSTOM
    public static void setBASIC_MODE(boolean aBASIC_MODE) {
        BASIC_MODE = aBASIC_MODE;
    }
    //END CUSTOM

    private static class JaWEAboutDialog extends JDialog {

        JButton okButton;

        public JaWEAboutDialog(JFrame frame) {
            super(frame);
            JPanel main = new JPanel();
            main.setBackground(Color.WHITE);
            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
            okButton = new JButton("  OK  ");
            okButton.setAlignmentX(CENTER_ALIGNMENT);
            okButton.addActionListener(new ActionHandler());
            main.add(JaWESplash.getSplashPanel());
            main.add(Box.createVerticalStrut(15));
            main.add(okButton);
            getContentPane().add(main);
        }

        private class ActionHandler implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }
    }
    
    public static final int COMMUNITY_VERSION = 0;
    public static final int DEMO_VERSION = 1;
    public static final int PROFESSIONAL_VERSION = 2;
    public static final int JPED_VERSION = 2044;
    protected static int VERSION = 666;

    public static int getJaWEVersion() {
        if (VERSION == -1) {
            try {
                Class.forName("org.enhydra.jawe.base.controller.TogWEDemoController");
                VERSION = DEMO_VERSION;
            } catch (Exception ex) {
                try {
                    Class.forName("org.enhydra.jawe.ProfInfo");
                    VERSION = PROFESSIONAL_VERSION;
                } catch (Exception ex2) {
                    VERSION = COMMUNITY_VERSION;
                }
            }
        }
        return VERSION;
    }
    protected static boolean addOnsAvailable = true;

    public static boolean addOnsAvailable() {
        try {
            Class.forName("org.enhydra.jawe.AddOnInfo");
            addOnsAvailable = true;
        } catch (Exception ex) {
        }
        return addOnsAvailable;
    }
}

