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
import javax.imageio.ImageIO;
import org.apache.commons.logging.LogFactory;

public class Viewer {

    static JaWEManager jaweManager;

    static {
        JaWEManager.configure();
        jaweManager = JaWEManager.getInstance();
        try {
            jaweManager.init();
        } catch (Exception e) {
            LogFactory.getLog(Viewer.class.getName()).error(e);
        }
    }

    public Viewer() {
    }

    public void saveProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds, String file) throws IOException {

        BufferedImage img = generateProcessImage(xpdl, packageId, processDefId, runningActivityIds);
        if (img != null) {
            // output to jpeg
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                LogFactory.getLog(Viewer.class.getName()).info("Saving process image to JPEG using ImageIO");
                ImageIO.write(img, "jpeg", fos);
                fos.flush();
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }

    }

    public void outputProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds, OutputStream out) throws IOException {

        BufferedImage img = generateProcessImage(xpdl, packageId, processDefId, runningActivityIds);
        if (img != null) {
            // output to jpeg
            LogFactory.getLog(Viewer.class.getName()).info("Saving process image to JPEG using ImageIO");
            ImageIO.write(img, "jpeg", out);
            out.flush();
        }

    }

    public BufferedImage generateProcessImage(String xpdl, String packageId, String processDefId, String[] runningActivityIds) {

        LogFactory.getLog(Viewer.class.getName()).info("Generating process image");

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
                                LogFactory.getLog(Viewer.class.getName()).error(ex);
                            }
                        }
                    } catch (Exception ex) {
                        LogFactory.getLog(Viewer.class.getName()).error("Problems while updating selection", ex);
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

                LogFactory.getLog(Viewer.class.getName()).info("Completed generating process image");

                return img;
            }catch(Exception e){
                LogFactory.getLog(Viewer.class.getName()).error(e);
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
