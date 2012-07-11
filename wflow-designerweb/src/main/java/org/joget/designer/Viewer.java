package org.joget.designer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

import org.enhydra.jawe.base.controller.JaWEController;
import org.enhydra.jawe.base.xpdlhandler.XPDLHandler;
import org.enhydra.jawe.components.graph.Graph;
import org.enhydra.jawe.components.graph.GraphController;
import org.enhydra.jawe.components.graph.GraphManager;
import org.enhydra.jawe.JaWEManager;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Viewer {

    static JaWEManager jaweManager;

    static {
        JaWEManager.configure();
        jaweManager = JaWEManager.getInstance();
        try {
            jaweManager.init();
        } catch (Exception e) {
            Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public Viewer() {
    }

    public void saveProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds, String file) throws IOException {

        BufferedImage img = generateProcessImage(xpdl, packageId, processDefId, runningActivityIds);
        if (img != null) {
            // output to jpeg
            FileOutputStream fos = new FileOutputStream(file);
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Saving process image to JPEG using ImageIO");
            ImageIO.write(img, "jpeg", fos);
            fos.flush();
            fos.close();
        }

    }

    public void outputProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds, OutputStream out) throws IOException {

        BufferedImage img = generateProcessImage(xpdl, packageId, processDefId, runningActivityIds);
        if (img != null) {
            // output to jpeg
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Saving process image to JPEG using ImageIO");
            ImageIO.write(img, "jpeg", out);
            out.flush();
        }

    }

    public BufferedImage generateProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds) {

        Logger.getLogger(getClass().getName()).log(Level.INFO, "Generating process image");

        JaWEController jaweController = jaweManager.getJaWEController();
        synchronized (jaweController) {
            try {
                // check process id
                String[] split = processDefId.split("#");
                if (split.length == 3) {
                    processDefId = split[2];
                }

                // load package definition
                XPDLHandler xpdlHandler = jaweManager.getXPDLHandler();
                xpdlHandler.setValidation(false);
                jaweController.openPackageFromStream(xpdl.getBytes("UTF-8"));
                org.enhydra.shark.xpdl.elements.Package pkg = xpdlHandler.getPackageById(packageId);
                org.enhydra.shark.xpdl.elements.WorkflowProcess wp = pkg.getWorkflowProcess(processDefId);
                GraphController gc = (GraphController) jaweManager.getComponentManager().getComponent("GraphComponent");
                gc.selectGraphForElement(wp);
                Graph graph = gc.getGraph(wp);

                // highlight running activities
                if (runningActivityIds != null && runningActivityIds.length > 0) {
                    graph.clearSelection();
                    try {
                        for (int i = 0; i < runningActivityIds.length; i++) {
                            try {
                                GraphManager wm = graph.getGraphManager();
                                Object go = wm.getGraphActivity(runningActivityIds[i]);
                                if (go != null) {
                                    graph.addSelectionCell(go);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Problems while updating selection");
                    }
                }

                // generate image
                BufferedImage img = null;
                Object[] cells = graph.getRoots();

                if (cells.length > 0) {
                    graph.setSize(graph.getPreferredSize());
                    Rectangle bounds = graph.getCellBounds(cells).getBounds();// HM, JGraph3.4.1
                    graph.toScreen(bounds);

                    // Create a Buffered Image
                    Dimension d = bounds.getSize();
                    img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = img.createGraphics();
                    graph.paint(graphics);
                }

                Logger.getLogger(getClass().getName()).log(Level.INFO, "Completed generating process image");

                return img;
            }catch(Exception e){
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
                return null;
            } finally {
                try {
                    jaweController.closePackage(null, true);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
