package com.lowagie.text.pdf;

import com.lowagie.text.DocumentException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.sheet.FontFaceRule;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFSFont;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.TrueTypeUtil;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.util.FontUtil;
import org.xhtmlrenderer.util.SupportedEmbeddedFontTypes;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

public class ITextCustomFontResolver extends ITextFontResolver {
    private Map _fontFamilies = createInitialFontMap();
    private Map _fontCache = new HashMap();
    private static String defaultFonts = "";

    private final SharedContext _sharedContext;

    public ITextCustomFontResolver(SharedContext sharedContext) {
        super(sharedContext);
        _sharedContext = sharedContext;
    }

    /**
     * Utility method which uses iText libraries to determine the family name(s) for the font at the given path.
     * The iText APIs seem to indicate there can be more than one name, but this method will return a set of them.
     * Use a name from this list when referencing the font in CSS for PDF output. Note that family names as reported
     * by iText may vary from those reported by the AWT Font class, e.g. "Arial Unicode MS" for iText and
     * "ArialUnicodeMS" for AWT.
     *
     * @param path local path to the font file
     * @param encoding same as what you would use for {@link #addFont(String, String, boolean)}
     * @param embedded same as what you would use for {@link #addFont(String, String, boolean)}
     * @return set of all family names for the font file, as reported by iText libraries
     */
    public static Set getDistinctFontFamilyNames(String path, String encoding, boolean embedded) {
        BaseFont font = null;
        try {
            font = BaseFont.createFont(path, encoding, embedded);
            String[] fontFamilyNames = TrueTypeUtil.getFamilyNames(font);
            Set distinct = new HashSet();
            for (int i = 0; i < fontFamilyNames.length; i++) {
                distinct.add(fontFamilyNames[i]);
            }
            return distinct;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
    }

    public void flushCache() {
        _fontFamilies = createInitialFontMap();
        _fontCache = new HashMap();
    }

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

    public void importFontFaces(List fontFaces) {
        for (Iterator i = fontFaces.iterator(); i.hasNext(); ) {
            FontFaceRule rule = (FontFaceRule)i.next();
            CalculatedStyle style = rule.getCalculatedStyle();

            FSDerivedValue src = style.valueByName(CSSName.SRC);
            if (src == IdentValue.NONE) {
                continue;
            }

            byte[] font1 = _sharedContext.getUac().getBinaryResource(src.asString());
            if (font1 == null) {
                XRLog.exception("Could not load font " + src.asString());
                continue;
            }

            byte[] font2 = null;
            FSDerivedValue metricsSrc = style.valueByName(CSSName.FS_FONT_METRIC_SRC);
            if (metricsSrc != IdentValue.NONE) {
                font2 = _sharedContext.getUac().getBinaryResource(metricsSrc.asString());
                if (font2 == null) {
                    XRLog.exception("Could not load font metric data " + src.asString());
                    continue;
                }
            }

            if (font2 != null) {
                byte[] t = font1;
                font1 = font2;
                font2 = t;
            }

            boolean embedded = style.isIdent(CSSName.FS_PDF_FONT_EMBED, IdentValue.EMBED);
            String encoding = style.getStringProperty(CSSName.FS_PDF_FONT_ENCODING);
            String fontFamily = null;
            IdentValue fontWeight = null;
            IdentValue fontStyle = null;

            if (rule.hasFontFamily()) {
                fontFamily = style.valueByName(CSSName.FONT_FAMILY).asString();
            }

            if (rule.hasFontWeight()) {
                fontWeight = style.getIdent(CSSName.FONT_WEIGHT);
            }

            if (rule.hasFontStyle()) {
                fontStyle = style.getIdent(CSSName.FONT_STYLE);
            }

            try {
                addFontFaceFont(fontFamily, fontWeight, fontStyle, src.asString(), encoding, embedded, font1, font2);
            } catch (DocumentException e) {
                XRLog.exception("Could not load font " + src.asString(), e);
                continue;
            } catch (Exception e) {
                XRLog.exception("Could not load font " + src.asString(), e);
            }
        }
    }

    public void addFontDirectory(String dir, boolean embedded)
            throws DocumentException, IOException {
        File f = new File(dir);
        if (f.isDirectory()) {
            File[] files = f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String lower = name.toLowerCase(Locale.ENGLISH);
                    return lower.endsWith(".otf") || lower.endsWith(".ttf");
                }
            });
            for (int i = 0; i < files.length; i++) {
                addFont(files[i].getAbsolutePath(), embedded);
            }
        }
    }

    public void addFont(String path, boolean embedded)
            throws DocumentException, IOException {
        addFont(path, BaseFont.CP1252, embedded);
    }

    public void addFont(String path, String encoding, boolean embedded)
            throws DocumentException, IOException {
        addFont(path, encoding, embedded, null);
    }

    public void addFont(String path, String encoding, boolean embedded, String pathToPFB)
            throws DocumentException, IOException {
        addFont(path, null, encoding, embedded, pathToPFB);
    }

    public void addFont(String path, String fontFamilyNameOverride,
                        String encoding, boolean embedded, String pathToPFB)
            throws DocumentException, IOException {
        String lower = path.toLowerCase(Locale.ENGLISH);
        if (lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.indexOf(".ttc,") != -1) {
            BaseFont font = BaseFont.createFont(path, encoding, embedded);

            String[] fontFamilyNames;
            if (fontFamilyNameOverride != null) {
                fontFamilyNames = new String[] { fontFamilyNameOverride };
            } else {
                fontFamilyNames = TrueTypeUtil.getFamilyNames(font);
            }

            for (int i = 0; i < fontFamilyNames.length; i++) {
                String fontFamilyName = fontFamilyNames[i];
                FontFamily fontFamily = getCustomFontFamily(fontFamilyName);

                FontDescription descr = new FontDescription(font);
                try {
                    TrueTypeUtil.populateDescription(path, font, descr);
                } catch (Exception e) {
                    throw new XRRuntimeException(e.getMessage(), e);
                }

                fontFamily.addFontDescription(descr);
            }
        } else if (lower.endsWith(".ttc")) {
            String[] names = BaseFont.enumerateTTCNames(path);
            for (int i = 0; i < names.length; i++) {
                addFont(path + "," + i, fontFamilyNameOverride, encoding, embedded, null);
            }
        } else if (lower.endsWith(".afm") || lower.endsWith(".pfm")) {
            if (embedded && pathToPFB == null) {
                throw new IOException("When embedding a font, path to PFB/PFA file must be specified");
            }

            BaseFont font = BaseFont.createFont(
                    path, encoding, embedded, false, null, readFile(pathToPFB));

            String fontFamilyName;
            if (fontFamilyNameOverride != null) {
                fontFamilyName = fontFamilyNameOverride;
            } else {
                fontFamilyName = font.getFamilyFontName()[0][3];
            }

            FontFamily fontFamily = getCustomFontFamily(fontFamilyName);

            FontDescription descr = new FontDescription(font);
            // XXX Need to set weight, underline position, etc.  This information
            // is contained in the AFM file (and even parsed by Type1Font), but
            // unfortunately it isn't exposed to the caller.
            fontFamily.addFontDescription(descr);
        } else {
            throw new IOException("Unsupported font type");
        }
    }

    private boolean fontSupported(String uri) {
        String lower = uri.toLowerCase(Locale.ENGLISH);
        if(FontUtil.isEmbeddedBase64Font(uri)) {
            return SupportedEmbeddedFontTypes.isSupported(uri);
        } else {
            return lower.endsWith(".otf") ||
                    lower.endsWith(".ttf") ||
                    lower.contains(".ttc,");
        }
    }

    private void addFontFaceFont(
            String fontFamilyNameOverride, IdentValue fontWeightOverride, IdentValue fontStyleOverride, String uri, String encoding, boolean embedded, byte[] afmttf, byte[] pfb)
            throws DocumentException, IOException {
        String lower = uri.toLowerCase(Locale.ENGLISH);
        if (fontSupported(lower)) {
            String fontName = (FontUtil.isEmbeddedBase64Font(uri)) ? fontFamilyNameOverride+SupportedEmbeddedFontTypes.getExtension(uri) : uri;
            BaseFont font = BaseFont.createFont(fontName, encoding, embedded, false, afmttf, pfb);

            String[] fontFamilyNames;
            if (fontFamilyNameOverride != null) {
                fontFamilyNames = new String[] { fontFamilyNameOverride };
            } else {
                fontFamilyNames = TrueTypeUtil.getFamilyNames(font);
            }

            for (int i = 0; i < fontFamilyNames.length; i++) {
                FontFamily fontFamily = getCustomFontFamily(fontFamilyNames[i]);

                FontDescription descr = new FontDescription(font);
                try {
                    TrueTypeUtil.populateDescription(uri, afmttf, font, descr);
                } catch (Exception e) {
                    throw new XRRuntimeException(e.getMessage(), e);
                }

                descr.setFromFontFace(true);

                if (fontWeightOverride != null) {
                    descr.setWeight(convertWeightToInt(fontWeightOverride));
                }

                if (fontStyleOverride != null) {
                    descr.setStyle(fontStyleOverride);
                }

                fontFamily.addFontDescription(descr);
            }
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

    private byte[] readFile(String path) throws IOException {
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

    public FontFamily getCustomFontFamily(String fontFamilyName) {
        FontFamily fontFamily = (FontFamily)_fontFamilies.get(fontFamilyName);
        if (fontFamily == null) {
            fontFamily = new FontFamily();
            fontFamily.setName(fontFamilyName);
            _fontFamilies.put(fontFamilyName, fontFamily);
        }
        return fontFamily;
    }

    private FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
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

    private String normalizeFontFamily(String fontFamily) {
        String result = fontFamily;
        // strip off the "s if they are there
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }

        // normalize the font name
        if (result.equalsIgnoreCase("Times")) {
            result = "TimesRoman";
        } else if (result.equalsIgnoreCase("serif")) {
            result = "Serif";
        } else if (result.equalsIgnoreCase("sans-serif")) {
            result = "SansSerif";
        } else if (result.equalsIgnoreCase("monospace")) {
            result = "Monospaced";
        }

        return result;
    }

    public FSFont resolveFont(SharedContext ctx, String fontFamily, float size, IdentValue weight, IdentValue style, IdentValue variant) {
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
    
    public Set<String> getFontFamilies() {
        return _fontFamilies.keySet();
    }

    public static int convertWeightToInt(IdentValue weight) {
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

    protected static String getHashName(
            String name, IdentValue weight, IdentValue style) {
        return name + "-" + weight + "-" + style;
    }

    private static Map createInitialFontMap() {
        HashMap result = new LinkedHashMap();

        try {
            // Try and load the iTextAsian fonts
            if(ITextFontResolver.class.getClassLoader().getResource("com/lowagie/text/pdf/fonts/cjkfonts.properties") != null) {
                addCJKFonts(result);
            }
            
            addCustomLoadedFonts(result);
            
            addTimes(result);
            addCourier(result);
            addHelvetica(result);
            addSymbol(result);
            addZapfDingbats(result);
        } catch (DocumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return result;
    }

    private static BaseFont createFont(String name) throws DocumentException, IOException {
        return createFont(name, "winansi", true);
    }

    private static BaseFont createFont(String name, String encoding, boolean embedded) throws DocumentException, IOException {
        return BaseFont.createFont(name, encoding, embedded);
    }

    private static void addCourier(HashMap result) throws DocumentException, IOException {
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
        addDefaultFonts("Courier");
    }

    private static void addTimes(HashMap result) throws DocumentException, IOException {
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
        addDefaultFonts("Times");
    }

    private static void addHelvetica(HashMap result) throws DocumentException, IOException {
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
        result.put("Helvetica", helvetica);
        addDefaultFonts("Helvetica");
    }

    private static void addSymbol(Map result) throws DocumentException, IOException {
        FontFamily fontFamily = new FontFamily();
        fontFamily.setName("Symbol");

        fontFamily.addFontDescription(new FontDescription(createFont(BaseFont.SYMBOL, BaseFont.CP1252, false), IdentValue.NORMAL, 400));

        result.put("Symbol", fontFamily);
        addDefaultFonts("Symbol");
    }

    private static void addZapfDingbats(Map result) throws DocumentException, IOException {
        FontFamily fontFamily = new FontFamily();
        fontFamily.setName("ZapfDingbats");

        fontFamily.addFontDescription(new FontDescription(createFont(BaseFont.ZAPFDINGBATS, BaseFont.CP1252, false), IdentValue.NORMAL, 400));

        result.put("ZapfDingbats", fontFamily);
        addDefaultFonts("ZapfDingbats");
    }

    // fontFamilyName, fontName, encoding
    private static final String[][] cjkFonts = {
        {"STSong-Light", "STSong-Light", "UniGB-UCS2-H"},
        {"MSung-Light", "MSung-Light", "UniCNS-UCS2-H"},
        {"HeiseiMin-W3", "HeiseiMin-W3", "UniJIS-UCS2-H"},
        {"HYGoThic-Medium", "HYGoThic-Medium", "UniKS-UCS2-H"}
    };

    private static void addCJKFonts(Map fontFamilyMap) throws DocumentException, IOException {
        for(int i = 0; i < cjkFonts.length; i++) {
            String fontFamilyName = cjkFonts[i][0];
            String fontName = cjkFonts[i][1];
            String encoding = cjkFonts[i][2];

            addCJKFont(fontFamilyName, fontName, encoding, fontFamilyMap);
        }
    }

    private static void addCJKFont(String fontFamilyName, String fontName, String encoding, Map fontFamilyMap) throws DocumentException, IOException {
        FontFamily fontFamily = new FontFamily();
        fontFamily.setName(fontFamilyName);
        addDefaultFonts(fontFamilyName);

        fontFamily.addFontDescription(new FontDescription(createFont(fontName+",BoldItalic", encoding, true), IdentValue.OBLIQUE, 700));
        fontFamily.addFontDescription(new FontDescription(createFont(fontName+",Italic", encoding, true), IdentValue.OBLIQUE, 400));
        fontFamily.addFontDescription(new FontDescription(createFont(fontName+",Bold", encoding, true), IdentValue.NORMAL, 700));
        fontFamily.addFontDescription(new FontDescription(createFont(fontName, encoding, true), IdentValue.NORMAL, 400));

        fontFamilyMap.put(fontFamilyName, fontFamily);
        addDefaultFonts(fontFamilyName);
    }
    
    private static void addCustomLoadedFonts(Map fontFamilyMap) throws DocumentException, IOException {
        String path = SetupManager.getBaseDirectory() + File.separator + "fonts" + File.separator;
        File fontsFile = new File(path + "fonts.csv");
        if (fontsFile.exists()) {
            BufferedReader br = null;
            String line, name, fontPath, encoding = "";
            String[] parts = null;
            File fontFile = null;
            try {
                br = new BufferedReader(new FileReader(path + "fonts.csv"));
                while ((line = br.readLine()) != null) {
                    parts = line.split(",");
                    name = parts[0].trim();
                    fontPath = SecurityUtil.normalizedFileName(parts[1].trim());

                    encoding = parts[2].trim();
                    fontFile = new File(path + fontPath);
                    if (fontFile.exists()) {
                        addCustomFont(name, path + fontPath, encoding, fontFamilyMap);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(ITextCustomFontResolver.class.getName(), e, "");
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {}
                }
            }
        }
        
        addCustomFont("Noto Naskh Arabic", "fonts/NotoNaskhArabic/NotoNaskhArabic-Regular.ttf", BaseFont.IDENTITY_H, fontFamilyMap);
        addCustomFont("DroidSans", "fonts/Droid-Sans/DroidSans.ttf", BaseFont.IDENTITY_H, fontFamilyMap);
        addCustomFont("THSarabun", "fonts/THSarabun/THSarabun.ttf", BaseFont.IDENTITY_H, fontFamilyMap);
    }
    
    private static void addCustomFont(String fontFamilyName, String path, String encoding, Map fontFamilyMap) throws DocumentException, IOException {
        FontFamily fontFamily = new FontFamily();
        fontFamily.setName(fontFamilyName);
        addDefaultFonts(fontFamilyName);
        
        fontFamily.addFontDescription(new FontDescription(createFont(path, encoding, true), IdentValue.NORMAL, 400));
        fontFamilyMap.put(fontFamilyName, fontFamily);
    }
    
    private static void addDefaultFonts(String name) {
        if (!defaultFonts.isEmpty()) {
            defaultFonts += ", ";
        }
        defaultFonts += "\""+name+"\"";
    }
    
    public String getDefaultFonts() {
        return defaultFonts;
    }

    private static class FontFamily {
        private String _name;
        private List _fontDescriptions;

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
                            FontDescription f1 = (FontDescription)o1;
                            FontDescription f2 = (FontDescription)o2;
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

            for (Iterator i = _fontDescriptions.iterator(); i.hasNext(); ) {
                FontDescription description = (FontDescription)i.next();

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
                    candidates.addAll(_fontDescriptions);
                }
            }

            FontDescription[] matches = (FontDescription[])
                candidates.toArray(new FontDescription[candidates.size()]);
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

        private static final int SM_EXACT = 1;
        private static final int SM_LIGHTER_OR_DARKER = 2;
        private static final int SM_DARKER_OR_LIGHTER = 3;

        private FontDescription findByWeight(FontDescription[] matches,
                int desiredWeight, int searchMode) {
            if (searchMode == SM_EXACT) {
                for (int i = 0; i < matches.length; i++) {
                    FontDescription descr = matches[i];
                    if (descr.getWeight() == desiredWeight) {
                        return descr;
                    }
                }
                return null;
            } else if (searchMode == SM_LIGHTER_OR_DARKER){
                int offset = 0;
                FontDescription descr = null;
                for (offset = 0; offset < matches.length; offset++) {
                    descr = matches[offset];
                    if (descr.getWeight() > desiredWeight) {
                        break;
                    }
                }

                if (descr != null && offset > 0 && descr.getWeight() > desiredWeight) {
                    return matches[offset-1];
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

                if (descr != null && offset != matches.length - 1 && descr.getWeight() < desiredWeight) {
                    return matches[offset+1];
                } else {
                    return descr;
                }
            }

            return null;
        }
    }
}