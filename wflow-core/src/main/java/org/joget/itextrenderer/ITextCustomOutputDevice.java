package org.joget.itextrenderer;

import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.render.JustificationInfo;

public class ITextCustomOutputDevice extends ITextOutputDevice {
    
    public ITextCustomOutputDevice(float dotsPerPoint) {
        super(dotsPerPoint);
    }
    
    @Override
    public void drawString(String s, float x, float y, JustificationInfo info) {
        if (textIsRTL(s)) {
            s = (new StringBuilder(s)).reverse().toString();
        }
        super.drawString(s, x, y, info);
    }
    
    protected static boolean textIsRTL(String text) {
        for (char charac : text.toCharArray()) {
            if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.ARABIC ||
                    Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.HEBREW) {
                return true;
            }
        }
        return false;
    }
}
