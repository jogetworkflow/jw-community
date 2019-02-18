package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import java.util.ArrayList;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.pdf.ITextFSFont;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.JustificationInfo;

public class ITextCustomOutputDevice extends ITextOutputDevice {
    private ITextFSFont _localFont;
    
    public ITextCustomOutputDevice(float dotsPerPoint) {
        super(dotsPerPoint);
    }
    
    @Override
    public void setFont(FSFont font) {
        _localFont = ((ITextFSFont) font);
        super.setFont(font);
    }
    
    @Override
    public void drawString(String s, float x, float y, JustificationInfo info) {
        
        if (textIsRTL(s)) {
            s = transformRTL(s);
        }
        checkFontFamily(s, 0);
        super.drawString(s, x, y, info);
    }
    
    protected void checkFontFamily(String s, int index) {
        if (countMissingChar(s) > s.length() / 2) {
            FontSpecification spec = getFontSpecification();
            ITextCustomFontResolver resolver = (ITextCustomFontResolver) getSharedContext().getFontResolver();
            String[] families = spec.families;
            if (families.length > index+1) {
                FSFont font = resolver.resolveFont(getSharedContext(), families[index+1], spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
                if (font != null) {
                    setFont(font);
                }
                checkFontFamily(s, index + 1);
            } else if (families.length == index+1) {
                FSFont font = resolver.resolveFont(getSharedContext(), "serif", spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
                if (font != null) {
                    setFont(font);
                }
            }
        }
    }
    
    protected int countMissingChar(String s) {
        int count = 0;
        char[] charArr = s.toCharArray();
        
        for (int i = 0; i < charArr.length; i++) {
            if (!(charArr[i] == ' ' || charArr[i] == '\u00a0' || charArr[i] == '\u3000' 
                    || _localFont.getFontDescription().getFont().charExists(charArr[i]))) {
                count++;
            }
        }
        return count;
    }
    
    public static String transformRTL(String s) {
        BidiLine bidi = new BidiLine();
        bidi.addChunk(new PdfChunk(new Chunk(s), null));
        bidi.getParagraph(PdfWriter.RUN_DIRECTION_RTL);
        ArrayList<PdfChunk> arr = bidi.createArrayOfPdfChunks(0, bidi.totalTextLength - 1);
        StringBuilder sb = new StringBuilder();
        for (PdfChunk ck : arr) {
            sb.append(ck.toString());
        }
        return sb.toString();
    }
    
    public static boolean textIsRTL(String text) {
        for (char charac : text.toCharArray()) {
            if (Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.ARABIC ||
                    Character.UnicodeBlock.of(charac) == Character.UnicodeBlock.HEBREW) {
                return true;
            }
        }
        return false;
    }
}
