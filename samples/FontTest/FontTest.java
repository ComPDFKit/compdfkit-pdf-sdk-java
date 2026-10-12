import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.CPDFAnnotation;
import com.compdfkit.core.annotation.CPDFFreetextAnnotation;
import com.compdfkit.core.annotation.CPDFTextAttribute;
import com.compdfkit.core.annotation.form.CPDFTextWidget;
import com.compdfkit.core.annotation.form.CPDFWidget;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.font.CPDFFont;
import com.compdfkit.core.font.CPDFFontName;
import com.compdfkit.core.page.CPDFPage;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

/**
 * Font samples: enumerate the fonts known to the SDK, map family and style names
 * to PostScript names, read font data, and use a resolved PostScript name for
 * a free text annotation and a text field.
 */
public class FontTest {
    private static final String OUTPUT_DIR = "/out/FontTest";
    private static final String FALLBACK_PS_NAME = "Helvetica";
    private static final int LISTED_FONTS = 10;

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Font");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();
        String psName = FALLBACK_PS_NAME;
        try {
            listFonts();
            psName = resolveNames();
            loadFontData(psName);
            refreshFontCache();
        } catch (UnsatisfiedLinkError e) {
            // The CPDFFont natives are registered in JNI_Font.cpp; report it when the
            // shipped native library does not bind them instead of aborting the sample.
            System.out.println("CPDFFont native method not available in this SDK build: " + e.getMessage());
            System.out.println("Continuing with the standard font \"" + FALLBACK_PS_NAME + "\"");
            printDividingLine();
        }
        useFontInDocument(rootDir, psName);
        printFooter();
    }

    /** Sample 1: List external, internal and combined font names. */
    private static void listFonts() {
        printDividingLine();
        System.out.println("Sample 1: Enumerate fonts");
        ArrayList<CPDFFontName> external = CPDFFont.getOutFontName();
        ArrayList<CPDFFontName> internal = CPDFFont.getInnerFontName();
        ArrayList<CPDFFontName> all = CPDFFont.getFontName();
        System.out.println("External fonts: " + size(external) + ", internal fonts: " + size(internal)
                + ", combined: " + size(all));
        int printed = 0;
        for (CPDFFontName font : all) {
            if (font == null || printed++ >= LISTED_FONTS) {
                continue;
            }
            System.out.println("  family=" + font.getFamilyName()
                    + " styles=" + font.getStyleName()
                    + " psNames=" + font.getPsName()
                    + " bold=" + font.isSupportBold() + " italic=" + font.isSupportItalic()
                    + " outFontMap=" + font.getOutFontMap());
        }
        if (size(all) > LISTED_FONTS) {
            System.out.println("  ... " + (size(all) - LISTED_FONTS) + " more");
        }
        printDividingLine();
    }

    /** Sample 2: Resolve style names and PostScript names for a family. */
    private static String resolveNames() {
        System.out.println("Sample 2: Map family and style names to PostScript names");
        String family = firstFamily();
        if (family == null) {
            System.out.println("No font family available; using " + FALLBACK_PS_NAME);
            printDividingLine();
            return FALLBACK_PS_NAME;
        }
        ArrayList<String> styles = CPDFFont.getStyleName(family);
        ArrayList<String> psNames = CPDFFont.getPSNameList(family);
        System.out.println("Family: " + family);
        System.out.println("getStyleName(): " + styles);
        System.out.println("getPSNameList(): " + psNames);
        String regular = CPDFFont.getPsName(family, false, false);
        String bold = CPDFFont.getPsName(family, true, false);
        String italic = CPDFFont.getPsName(family, false, true);
        System.out.println("getPsName(regular): " + regular + ", bold: " + bold + ", italic: " + italic);
        if (styles != null && !styles.isEmpty()) {
            System.out.println("getPsName(family, \"" + styles.get(0) + "\"): " + CPDFFont.getPsName(family, styles.get(0)));
        }
        String psName = regular != null ? regular : (psNames != null && !psNames.isEmpty() ? psNames.get(0) : FALLBACK_PS_NAME);
        System.out.println("getFamilyName(\"" + psName + "\"): " + CPDFFont.getFamilyName(psName));
        printDividingLine();
        return psName;
    }

    /** Sample 3: Read raw font data by PostScript name. */
    private static void loadFontData(String psName) {
        System.out.println("Sample 3: Load font data");
        byte[] data = CPDFFont.getFontDataFromPostName(psName);
        System.out.println("getFontDataFromPostName(\"" + psName + "\"): " + (data == null ? "null" : data.length + " bytes"));
        byte[] standard = CPDFFont.getFontDataFromPostName(FALLBACK_PS_NAME);
        System.out.println("getFontDataFromPostName(\"" + FALLBACK_PS_NAME + "\"): "
                + (standard == null ? "null (standard fonts are resolved from the system font directory only)" : standard.length + " bytes"));
        printDividingLine();
    }

    /** Sample 4: Use the PostScript name in a free text annotation and a text field. */
    private static void useFontInDocument(String rootDir, String psName) {
        System.out.println("Sample 4: Use font \"" + psName + "\" in a document");
        CPDFDocument document = CPDFDocument.createDocument();
        document.insertBlankPage(0, 595, 842);
        CPDFPage page = document.pageAtIndex(0);

        CPDFFreetextAnnotation freeText = (CPDFFreetextAnnotation) page.addAnnot(CPDFAnnotation.Type.FREETEXT, document);
        freeText.setRect(new RectF(40, 800, 500, 740));
        freeText.setContent("ComPDFKit font sample: " + psName);
        boolean daSet = freeText.setFreetextDa(new CPDFTextAttribute(psName, 16, Color.BLUE.getRGB()));
        freeText.updateAp();
        System.out.println("FreeText setFreetextDa: " + daSet);

        CPDFTextWidget field = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        field.setRect(new RectF(40, 720, 500, 680));
        field.setFieldName("FontField");
        boolean fontSet = field.setFontName(psName);
        field.setFontSize(14);
        field.setText("Text field using " + psName);
        field.updateAp();
        System.out.println("TextWidget setFontName: " + fontSet);

        String outputPath = rootDir + OUTPUT_DIR + "/Font_Usage.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(outputPath);
        for (CPDFAnnotation annotation : check.pageAtIndex(0).getAnnotations()) {
            if (annotation instanceof CPDFFreetextAnnotation) {
                CPDFTextAttribute da = ((CPDFFreetextAnnotation) annotation).getFreetextDa();
                System.out.println("Saved free text font: " + (da == null ? "null" : da.getFontName() + " " + da.getFontSize() + "pt"));
            } else if (annotation instanceof CPDFTextWidget) {
                CPDFTextWidget saved = (CPDFTextWidget) annotation;
                System.out.println("Saved text field font: " + saved.getFontName() + " " + saved.getFontSize() + "pt");
            }
        }
        check.close();
        System.out.println("Done. Result saved in Font_Usage.pdf");
        printDividingLine();
    }

    /** Sample 5: Clear the cached font list and reload it. */
    private static void refreshFontCache() {
        System.out.println("Sample 5: Refresh the font cache");
        int before = size(CPDFFont.getFontName());
        CPDFFont.clearFontCache();
        int after = size(CPDFFont.getFontName());
        System.out.println("Fonts before clearFontCache(): " + before + ", after reload: " + after);
        printDividingLine();
    }

    private static String firstFamily() {
        ArrayList<CPDFFontName> fonts = CPDFFont.getFontName();
        if (fonts == null) {
            return null;
        }
        for (CPDFFontName font : fonts) {
            if (font != null && font.getFamilyName() != null && !font.getFamilyName().isEmpty()) {
                return font.getFamilyName();
            }
        }
        return null;
    }

    private static int size(ArrayList<?> list) {
        return list == null ? 0 : list.size();
    }

    private static void printHead(String name) {
        System.out.println(String.format("Running %s sample", name));
    }

    private static void printFooter() {
        System.out.println("Done!");
        printDividingLine();
    }

    private static void printDividingLine() {
        System.out.println("--------------------------------------------");
    }

    private static String getRootDir(String[] args) {
        String dir;
        if ((args == null) || args.length == 0) {
            dir = System.getProperty("user.dir");
            if (!dir.endsWith("samples")) {
                dir += "/samples";
            }
        } else {
            dir = args[0];
        }
        File file = new File(dir);
        if (!file.exists()) {
            throw new RuntimeException("home directory does not exist");
        }
        return file.getAbsolutePath();
    }
}
