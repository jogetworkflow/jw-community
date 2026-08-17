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
    
    /**
     * Walks the CSS font-family fallback list one family at a time until a font covers a
     * majority of s's characters, or the list is exhausted (then falls back to "serif").
     * The check is done iteratively rather than recursively so the stack depth never depends on the length of the
     * font-family list (T8018/#2167).
     */
    protected void checkFontFamily(String s, int index) {
        if (!(countMissingChar(s) > s.length() / 2)) {
            return;
        }
        FontSpecification spec = getFontSpecification();
        ITextCustomFontResolver resolver = (ITextCustomFontResolver) getSharedContext().getFontResolver();
        String[] families = spec.families;

        int i = index;
        while (i + 1 < families.length && countMissingChar(s) > s.length() / 2) {
            i++;
            FSFont font = resolver.resolveFont(getSharedContext(), families[i], spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
            if (font != null) {
                setFont(font);
            }
        }

        if (i + 1 == families.length && countMissingChar(s) > s.length() / 2) {
            FSFont font = resolver.resolveFont(getSharedContext(), "serif", spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
            if (font != null) {
                setFont(font);
            }
        }
    }

    // Excluding spaces/whitespaces/invisible characters from counting as "missing glyphs"
    // cause the fallback path might be triggered unnecessarily where it should be renderable
    private static final String EXEMPT_WHITESPACE = " \u00a0\u3000\u202f\u2007\u2009\u200b\ufeff\u2060\u00ad\u2028\u2029";

    protected int countMissingChar(String s) {
        int count = 0;
        char[] charArr = s.toCharArray();

        for (int i = 0; i < charArr.length; i++) {
            char c = charArr[i];
            if (EXEMPT_WHITESPACE.indexOf(c) < 0 && !_localFont.getFontDescription().getFont().charExists(c)) {
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
