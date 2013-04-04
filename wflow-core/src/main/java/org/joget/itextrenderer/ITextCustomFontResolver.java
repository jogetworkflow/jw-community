package org.joget.itextrenderer;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import java.io.*;
import java.util.*;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFSFont;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.TrueTypeUtil;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.util.XRRuntimeException;

public class ITextCustomFontResolver extends ITextFontResolver {
    protected Map _fontFamilies = createInitialFontMap();
    protected Map _fontCache = new HashMap();
    
    public ITextCustomFontResolver(SharedContext sharedContext) {
        super(sharedContext);
    }
    
    @Override
    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
    }
    
    @Override
    public void flushCache() {
        _fontFamilies = createInitialFontMap();
        _fontCache = new HashMap();
    }
    
    @Override
    public void flushFontFaceFonts() {
        _fontCache = new HashMap();
        
        for (Iterator i = _fontFamilies.values().iterator(); i.hasNext(); ) {
            FontFamily family = (FontFamily)i.next();
            for (Iterator j = family.getFontDescriptions().iterator(); j.hasNext(); ) {
                FontDescription d = (FontDescription)j.next();
                if (d.isFromFontFace()) {
                    j.remove();
                }
            }
            if (family.getFontDescriptions().size() == 0) {
                i.remove();
            }
        }
    }
    
    protected byte[] readFile(String path) throws IOException {
        File f = new File(path);
        if (f.exists()) {
            ByteArrayOutputStream result = new ByteArrayOutputStream((int)f.length());
            InputStream is = null;
            try {
                is = new FileInputStream(path);
                byte[] buf = new byte[10240];
                int i;
                while ( (i = is.read(buf)) != -1) {
                    result.write(buf, 0, i);
                }
                is.close();
                is = null;
                
                return result.toByteArray();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        } else {
            throw new IOException("File " + path + " does not exist or is not accessible");
        }
    }
    
    @Override
    public void addFont(String path, String encoding, boolean embedded, String pathToPFB) throws DocumentException, IOException {
        try {
            String lower = path.toLowerCase();
            if (lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.indexOf(".ttc,") != -1) {
                BaseFont font = BaseFont.createFont(path, encoding, embedded);

                String fontFamilyName = TrueTypeUtil.getFamilyName(font);
                FontFamily fontFamily = getCustomFontFamily(fontFamilyName);

                FontDescription descr = new FontDescription(font);
                try {
                    TrueTypeUtil.populateDescription(path, font, descr);
                } catch (Exception e) {
                    throw new XRRuntimeException(e.getMessage(), e);
                }

                fontFamily.addFontDescription(descr);
            } else if (lower.endsWith(".ttc")) {
                String[] names = BaseFont.enumerateTTCNames(path);
                for (int i = 0; i < names.length; i++) {
                    addFont(path + "," + i, encoding, embedded);
                }
            } else if (lower.endsWith(".afm") || lower.endsWith(".pfm")) {
                if (embedded && pathToPFB == null) {
                    throw new IOException("When embedding a font, path to PFB/PFA file must be specified");
                }

                BaseFont font = BaseFont.createFont(
                        path, encoding, embedded, false, null, readFile(pathToPFB));

                String fontFamilyName = font.getFamilyFontName()[0][3];
                FontFamily fontFamily = getCustomFontFamily(fontFamilyName);

                FontDescription descr = new FontDescription(font);
                // XXX Need to set weight, underline position, etc.  This information
                // is contained in the AFM file (and even parsed by Type1Font), but
                // unfortunately it isn't exposed to the caller.
                fontFamily.addFontDescription(descr);            
            } else {
                BaseFont font = BaseFont.createFont(path, encoding, embedded);
            
                String fontFamilyName = font.getFamilyFontName()[0][3];
                FontFamily fontFamily = getCustomFontFamily(fontFamilyName);

                FontDescription descr = new FontDescription(font);
                fontFamily.addFontDescription(descr);    
            }
        } catch (Exception ex) {
            throw new IOException("Unsupported font type");
        }
    }
    
    protected void addFontFaceFont(
            String uri, String encoding, boolean embedded, byte[] afmttf, byte[] pfb) 
            throws DocumentException, IOException {
        String lower = uri.toLowerCase();
        if (lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.indexOf(".ttc,") != -1) {
            BaseFont font = BaseFont.createFont(uri, encoding, embedded, false, afmttf, pfb);
            
            String fontFamilyName = TrueTypeUtil.getFamilyName(font);
            FontFamily fontFamily = getCustomFontFamily(fontFamilyName);
            
            FontDescription descr = new FontDescription(font);
            try {
                TrueTypeUtil.populateDescription(uri, afmttf, font, descr);
            } catch (Exception e) {
                throw new XRRuntimeException(e.getMessage(), e);
            }
            
            descr.setFromFontFace(true);
            
            fontFamily.addFontDescription(descr);
        } else if (lower.endsWith(".afm") || lower.endsWith(".pfm") || lower.endsWith(".pfb") || lower.endsWith(".pfa")) {
            if (embedded && pfb == null) {
                throw new IOException("When embedding a font, path to PFB/PFA file must be specified");
            }
            
            String name = uri.substring(0, uri.length()-4) + ".afm";
            BaseFont font = BaseFont.createFont(
                    name, encoding, embedded, false, afmttf, pfb);
            
            String fontFamilyName = font.getFamilyFontName()[0][3];
            FontFamily fontFamily = getCustomFontFamily(fontFamilyName);
            
            FontDescription descr = new FontDescription(font);
            descr.setFromFontFace(true);
            // XXX Need to set weight, underline position, etc.  This information
            // is contained in the AFM file (and even parsed by Type1Font), but
            // unfortunately it isn't exposed to the caller.
            fontFamily.addFontDescription(descr);            
        } else {
            throw new IOException("Unsupported font type");
        }
    }    
    
    protected FontFamily getCustomFontFamily(String fontFamilyName) {
        FontFamily fontFamily = (FontFamily)_fontFamilies.get(fontFamilyName);
        if (fontFamily == null) {
            fontFamily = new FontFamily();
            fontFamily.setName(fontFamilyName);
            _fontFamilies.put(fontFamilyName, fontFamily);
        }
        return fontFamily;
    }
    
    protected String normalizeFontFamily(String fontFamily) {
        String result = fontFamily;
        // strip off the "s if they are there
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }

        // normalize the font name
        if (result.equalsIgnoreCase("serif")) {
            result = "Serif";
        }
        else if (result.equalsIgnoreCase("sans-serif")) {
            result = "SansSerif";
        }
        else if (result.equalsIgnoreCase("monospace")) {
            result = "Monospaced";
        }
        
        return result;
    }
    
    protected FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        if (! (style == IdentValue.NORMAL || style == IdentValue.OBLIQUE 
                || style == IdentValue.ITALIC)) {
            style = IdentValue.NORMAL;
        }
        if (families != null) {
            for (int i = 0; i < families.length; i++) {
                FSFont font = resolveFont(ctx, families[i], size, weight, style, variant);
                if (font != null) {
                    return font;
                }
            }
        }
        
        return resolveFont(ctx, "Serif", size, weight, style, variant);
    }
    
    protected FSFont resolveFont(SharedContext ctx, String fontFamily, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        String normalizedFontFamily = normalizeFontFamily(fontFamily);

        String cacheKey = getHashName(normalizedFontFamily, weight, style);
        FontDescription result = (FontDescription)_fontCache.get(cacheKey);
        if (result != null) {
            return new ITextFSFont(result, size);
        }
        
        FontFamily family = (FontFamily)_fontFamilies.get(normalizedFontFamily);
        if (family != null) {
            result = family.match(convertWeightToInt(weight), style);
            if (result != null) {
                _fontCache.put(cacheKey, result);
                return new ITextFSFont(result, size);
            }
        }
        
        return null;
    }
    
    protected static Map createInitialFontMap() {
        HashMap result = new HashMap();
        
        try {
            addCourier(result);
            addTimes(result);
            addHelvetica(result);
            addUnicodeFont(result);
        } catch (DocumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);            
        }

        return result;
    }
    
    protected static BaseFont createFont(String name) throws DocumentException, IOException {
        return BaseFont.createFont(name, "winansi", true);
    }
    
    protected int convertWeightToInt(IdentValue weight) {
        if (weight == IdentValue.NORMAL) {
            return 400;
        } else if (weight == IdentValue.BOLD) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_100) {
            return 100;
        } else if (weight == IdentValue.FONT_WEIGHT_200) {
            return 200;
        } else if (weight == IdentValue.FONT_WEIGHT_300) {
            return 300;
        } else if (weight == IdentValue.FONT_WEIGHT_400) {
            return 400;
        } else if (weight == IdentValue.FONT_WEIGHT_500) {
            return 500;
        } else if (weight == IdentValue.FONT_WEIGHT_600) {
            return 600;
        } else if (weight == IdentValue.FONT_WEIGHT_700) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_800) {
            return 800;
        } else if (weight == IdentValue.FONT_WEIGHT_900) {
            return 900;
        } else if (weight == IdentValue.LIGHTER) {
            // FIXME
            return 400;
        } else if (weight == IdentValue.BOLDER) {
            // FIXME
            return 700;
        }
        throw new IllegalArgumentException();
    }
    
    protected static void addCourier(HashMap result) throws DocumentException, IOException {
        FontFamily courier = new FontFamily();
        courier.setName("Courier");
        
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_OBLIQUE), IdentValue.OBLIQUE, 400));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLD), IdentValue.NORMAL, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER), IdentValue.NORMAL, 400));        
        
        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }
    
    protected static void addTimes(HashMap result) throws DocumentException, IOException {
        FontFamily times = new FontFamily();
        times.setName("Times");
        
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLDITALIC), IdentValue.ITALIC, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ITALIC), IdentValue.ITALIC, 400));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLD), IdentValue.NORMAL, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ROMAN), IdentValue.NORMAL, 400));  
        
        result.put("Serif", times);
        result.put("TimesRoman", times);
    }
    
    protected static void addHelvetica(HashMap result) throws DocumentException, IOException {
        FontFamily helvetica = new FontFamily();
        helvetica.setName("Helvetica");
        
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_OBLIQUE), IdentValue.OBLIQUE, 400));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLD), IdentValue.NORMAL, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA), IdentValue.NORMAL, 400));  
        
        result.put("Dialog", helvetica);
        result.put("SansSerif", helvetica);
    } 
    
    protected static void addUnicodeFont(HashMap result) throws DocumentException, IOException {
        FontFamily stsong = new FontFamily();
        stsong.setName("STSong-Light");
        BaseFont stsongFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        stsong.addFontDescription(new FontDescription(stsongFont, IdentValue.NORMAL, 400));
        result.put("STSong-Light", stsong);
        
        FontFamily msung = new FontFamily();
        msung.setName("MSung-Light");
        BaseFont msungFont = BaseFont.createFont("MSung-Light", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
        msung.addFontDescription(new FontDescription(msungFont, IdentValue.NORMAL, 400));
        result.put("MSung-Light", msung);
        
        FontFamily heiseimin = new FontFamily();
        heiseimin.setName("HeiseiMin-W3");
        BaseFont heiseiminFont = BaseFont.createFont("HeiseiMin-W3", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
        heiseimin.addFontDescription(new FontDescription(heiseiminFont, IdentValue.NORMAL, 400));
        result.put("HeiseiMin-W3", heiseimin);
        
        FontFamily hygothic = new FontFamily();
        hygothic.setName("HYGoThic-Medium");
        BaseFont hygothicFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
        hygothic.addFontDescription(new FontDescription(hygothicFont, IdentValue.NORMAL, 400));
        result.put("HYGoThic-Medium", hygothic);
        
        FontFamily droidsan = new FontFamily();
        String path = "fonts/Droid-Sans/DroidSans.ttf";
        BaseFont font = BaseFont.createFont(path, BaseFont.IDENTITY_H, true);
        String fontFamilyName = TrueTypeUtil.getFamilyName(font);
        droidsan.setName(fontFamilyName);
        FontDescription descr = new FontDescription(font);
        try {
            TrueTypeUtil.populateDescription(path, font, descr);
        } catch (Exception e) {
            throw new XRRuntimeException(e.getMessage(), e);
        }
        droidsan.addFontDescription(descr);
        result.put(fontFamilyName, droidsan);
    }
    
    protected static class FontFamily {

        protected String _name;
        protected List _fontDescriptions;

        public FontFamily() {
        }

        public List getFontDescriptions() {
            return _fontDescriptions;
        }

        public void addFontDescription(FontDescription descr) {
            if (_fontDescriptions == null) {
                _fontDescriptions = new ArrayList();
            }
            _fontDescriptions.add(descr);
            Collections.sort(_fontDescriptions,
                    new Comparator() {

                        public int compare(Object o1, Object o2) {
                            FontDescription f1 = (FontDescription) o1;
                            FontDescription f2 = (FontDescription) o2;
                            return f1.getWeight() - f2.getWeight();
                        }
                    });
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public FontDescription match(int desiredWeight, IdentValue style) {
            if (_fontDescriptions == null) {
                throw new RuntimeException("fontDescriptions is null");
            }

            List candidates = new ArrayList();

            for (Iterator i = _fontDescriptions.iterator(); i.hasNext();) {
                FontDescription description = (FontDescription) i.next();

                if (description.getStyle() == style) {
                    candidates.add(description);
                }
            }

            if (candidates.size() == 0) {
                if (style == IdentValue.ITALIC) {
                    return match(desiredWeight, IdentValue.OBLIQUE);
                } else if (style == IdentValue.OBLIQUE) {
                    return match(desiredWeight, IdentValue.NORMAL);
                } else {
                    return null;
                }
            }

            FontDescription[] matches = (FontDescription[]) candidates.toArray(new FontDescription[candidates.size()]);
            FontDescription result;

            result = findByWeight(matches, desiredWeight, SM_EXACT);

            if (result != null) {
                return result;
            } else {
                if (desiredWeight <= 500) {
                    return findByWeight(matches, desiredWeight, SM_LIGHTER_OR_DARKER);
                } else {
                    return findByWeight(matches, desiredWeight, SM_DARKER_OR_LIGHTER);
                }
            }
        }
        protected static final int SM_EXACT = 1;
        protected static final int SM_LIGHTER_OR_DARKER = 2;
        protected static final int SM_DARKER_OR_LIGHTER = 3;

        protected FontDescription findByWeight(FontDescription[] matches,
                int desiredWeight, int searchMode) {
            if (searchMode == SM_EXACT) {
                for (int i = 0; i < matches.length; i++) {
                    FontDescription descr = matches[i];
                    if (descr.getWeight() == desiredWeight) {
                        return descr;
                    }
                }
                return null;
            } else if (searchMode == SM_LIGHTER_OR_DARKER) {
                int offset = 0;
                FontDescription descr = null;
                for (offset = 0; offset < matches.length; offset++) {
                    descr = matches[offset];
                    if (descr.getWeight() > desiredWeight) {
                        break;
                    }
                }

                if (offset > 0 && descr.getWeight() > desiredWeight) {
                    return matches[offset - 1];
                } else {
                    return descr;
                }

            } else if (searchMode == SM_DARKER_OR_LIGHTER) {
                int offset = 0;
                FontDescription descr = null;
                for (offset = matches.length - 1; offset >= 0; offset--) {
                    descr = matches[offset];
                    if (descr.getWeight() < desiredWeight) {
                        break;
                    }
                }

                if (offset != matches.length - 1 && descr.getWeight() < desiredWeight) {
                    return matches[offset + 1];
                } else {
                    return descr;
                }
            }

            return null;
        }
    }
}
