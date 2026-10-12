import com.compdfkit.core.android.graphics.PointF;
import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.CPDFAnnotation;
import com.compdfkit.core.annotation.CPDFRedactAnnotation;
import com.compdfkit.core.annotation.CPDFTextAlignment;
import com.compdfkit.core.annotation.CPDFTextAttribute;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;
import com.compdfkit.core.page.CPDFTextPage;
import com.compdfkit.core.page.CPDFTextRange;
import com.compdfkit.core.page.CPDFTextSearcher;
import com.compdfkit.core.page.CPDFTextSelection;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Redaction samples: mark text for redaction, inspect the mark properties,
 * apply redactions at page or annotation level, and remove a mark without applying it.
 * All rectangles use PDF page coordinates (origin at the bottom-left corner, Y axis up).
 */
public class RedactTest {
    private static final String INPUT_PDF = "/TestFiles/text.pdf";
    private static final String OUTPUT_DIR = "/out/RedactTest";
    private static final String KEYWORD = "PDF";
    private static final int MAX_MARKED_MATCHES = 3;

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Redact");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();
        redactByKeyword(rootDir);
        eraseRedactionMark(rootDir);
        applySingleRedaction(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Mark the first matches of a keyword, save the marked file,
     * then apply every redaction on the page and verify the text is gone.
     */
    private static void redactByKeyword(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Mark keyword \"" + KEYWORD + "\" and apply all redactions on the page");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + INPUT_PDF);
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();

        List<CPDFTextRange> matches = findAll(textPage, KEYWORD);
        System.out.println("Matches before redaction: " + matches.size());
        if (matches.isEmpty()) {
            System.out.println("Keyword not found; nothing to redact");
            document.close();
            return;
        }

        List<RectF> quads = new ArrayList<>();
        for (int i = 0; i < matches.size() && i < MAX_MARKED_MATCHES; i++) {
            CPDFTextSelection[] lines = textPage.getSelectionsByLineForRange(matches.get(i));
            if (lines == null) {
                continue;
            }
            for (CPDFTextSelection selection : lines) {
                if (selection != null && selection.getRectF() != null) {
                    quads.add(selection.getRectF());
                }
            }
        }
        RectF[] quadRects = quads.toArray(new RectF[0]);
        System.out.println("Quad rectangles collected for " + Math.min(matches.size(), MAX_MARKED_MATCHES)
                + " matches: " + quadRects.length);

        CPDFRedactAnnotation redact = (CPDFRedactAnnotation) page.addAnnot(CPDFAnnotation.Type.REDACT, document);
        redact.setRect(union(quadRects));
        redact.setQuadRects(quadRects);
        redact.setOverLayText("REDACTED");
        redact.setTextDa(new CPDFTextAttribute("Helvetica", 8, Color.WHITE.getRGB()));
        redact.setTextAlignment(CPDFTextAlignment.ALIGNMENT_CENTER);
        redact.setFillColor(Color.BLACK.getRGB());
        redact.setOutlineColor(Color.RED.getRGB());
        redact.setFillColorBeforeApply(Color.YELLOW.getRGB());
        redact.updateAp();
        printRedactProperties(redact);

        String markedPath = rootDir + OUTPUT_DIR + "/Redact_Marked.pdf";
        document.save(markedPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Marked (not applied) result saved in Redact_Marked.pdf");

        boolean applied = page.applyRedactions();
        System.out.println("page.applyRedactions(): " + applied);
        String appliedPath = rootDir + OUTPUT_DIR + "/Redact_Applied.pdf";
        document.save(appliedPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(appliedPath);
        CPDFTextPage checkText = check.pageAtIndex(0).getTextPage();
        System.out.println("Matches after redaction: " + findAll(checkText, KEYWORD).size());
        String bounded = checkText.getBoundedText(quadRects[0]);
        System.out.println("Text inside the first redacted area: \"" + (bounded == null ? "" : bounded.trim()) + "\"");
        check.close();
        System.out.println("Done. Result saved in Redact_Applied.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Mark a match, clear optional colors, then call eraseInfoRedaction().
     * Like applyRedaction(), this removes the content inside the marked area and deletes
     * the annotation; the output shows the annotation count and the text left in the area.
     */
    private static void eraseRedactionMark(String rootDir) {
        System.out.println("Sample 2: Erase the marked content with eraseInfoRedaction()");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + INPUT_PDF);
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();

        List<CPDFTextRange> matches = findAll(textPage, KEYWORD);
        if (matches.isEmpty()) {
            System.out.println("Keyword not found; nothing to mark");
            document.close();
            return;
        }
        CPDFTextSelection[] lines = textPage.getSelectionsByLineForRange(matches.get(0));
        RectF[] quadRects = toRects(lines);
        if (quadRects.length == 0) {
            System.out.println("No selection rectangle for the first match");
            document.close();
            return;
        }

        int annotationsBefore = page.getAnnotCount();
        CPDFRedactAnnotation redact = (CPDFRedactAnnotation) page.addAnnot(CPDFAnnotation.Type.REDACT, document);
        redact.setRect(union(quadRects));
        redact.setQuadRects(quadRects);
        redact.setOutlineColor(Color.BLUE.getRGB());
        redact.setFillColorBeforeApply(Color.GREEN.getRGB());
        redact.updateAp();
        System.out.println("Annotations before mark: " + annotationsBefore + ", after mark: " + page.getAnnotCount());
        System.out.println("Outline color: " + hex(redact.getOutlineColor())
                + ", fill color before apply: " + hex(redact.getFillColorBeforeApply()));

        System.out.println("clearOutlineColor(): " + redact.clearOutlineColor()
                + " -> " + hex(redact.getOutlineColor()));
        System.out.println("clearFillColorBeforeApply(): " + redact.clearFillColorBeforeApply()
                + " -> " + hex(redact.getFillColorBeforeApply()));

        boolean erased = redact.eraseInfoRedaction();
        System.out.println("eraseInfoRedaction(): " + erased + ", annotations after erase: " + page.getAnnotCount());

        String outputPath = rootDir + OUTPUT_DIR + "/Redact_Erased.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(outputPath);
        CPDFTextPage checkText = check.pageAtIndex(0).getTextPage();
        System.out.println("Matches after eraseInfoRedaction(): " + findAll(checkText, KEYWORD).size());
        String revealed = checkText.getBoundedText(quadRects[0]);
        System.out.println("Text inside the erased mark area: \"" + (revealed == null ? "" : revealed.trim()) + "\"");
        System.out.println("Annotations in saved file: " + check.pageAtIndex(0).getAnnotCount());
        check.close();
        System.out.println("Done. Result saved in Redact_Erased.pdf");
        printDividingLine();
    }

    /**
     * Sample 3: Redact a whole text line located by a page point and apply
     * only that annotation with applyRedaction().
     */
    private static void applySingleRedaction(String rootDir) {
        System.out.println("Sample 3: Redact the first text line and apply that single annotation");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + INPUT_PDF);
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();
        if (textPage.getCountChars() == 0) {
            System.out.println("Page has no text");
            document.close();
            return;
        }

        RectF firstChar = textPage.getCharBox(0);
        PointF center = new PointF((firstChar.left + firstChar.right) / 2, (firstChar.top + firstChar.bottom) / 2);
        CPDFTextSelection line = textPage.getSelectionForLineAtPos(center);
        if (line == null || line.getRectF() == null) {
            System.out.println("No line found at " + center.x + "," + center.y);
            document.close();
            return;
        }
        RectF lineRect = line.getRectF();
        String lineText = textPage.getText(line.getTextRange());
        System.out.println("Line to redact: \"" + (lineText == null ? "" : lineText.trim()) + "\"");
        System.out.println("Line rectangle: " + lineRect);

        CPDFRedactAnnotation redact = (CPDFRedactAnnotation) page.addAnnot(CPDFAnnotation.Type.REDACT, document);
        redact.setRect(lineRect);
        redact.setQuadRects(new RectF[]{lineRect});
        redact.setFillColor(Color.DARK_GRAY.getRGB());
        redact.updateAp();
        System.out.println("getRect(): " + redact.getRect() + ", quad count: " + redact.getQuadRects().length);
        boolean applied = redact.applyRedaction();
        System.out.println("redact.applyRedaction(): " + applied + ", annotation still valid: " + redact.isValid());

        String outputPath = rootDir + OUTPUT_DIR + "/Redact_Single_Line.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(outputPath);
        CPDFTextPage checkText = check.pageAtIndex(0).getTextPage();
        String bounded = checkText.getBoundedText(lineRect);
        System.out.println("Text inside the redacted line after apply: \"" + (bounded == null ? "" : bounded.trim()) + "\"");
        check.close();
        System.out.println("Done. Result saved in Redact_Single_Line.pdf");
        printDividingLine();
    }

    private static List<CPDFTextRange> findAll(CPDFTextPage textPage, String keyword) {
        List<CPDFTextRange> result = new ArrayList<>();
        if (textPage == null || !textPage.isValid()) {
            return result;
        }
        CPDFTextSearcher searcher = new CPDFTextSearcher();
        if (searcher.findStart(textPage, keyword, 0, CPDFTextSearcher.PDFSearchOptions.PDFSearchCaseInsensitive)) {
            CPDFTextRange range;
            while ((range = searcher.findNext()) != null) {
                result.add(range);
            }
            searcher.findClose();
        }
        searcher.close();
        return result;
    }

    private static RectF[] toRects(CPDFTextSelection[] selections) {
        List<RectF> rects = new ArrayList<>();
        if (selections != null) {
            for (CPDFTextSelection selection : selections) {
                if (selection != null && selection.getRectF() != null) {
                    rects.add(selection.getRectF());
                }
            }
        }
        return rects.toArray(new RectF[0]);
    }

    /** Smallest rectangle containing all inputs, expressed with top above bottom. */
    private static RectF union(RectF[] rects) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (RectF rect : rects) {
            minX = Math.min(minX, Math.min(rect.left, rect.right));
            maxX = Math.max(maxX, Math.max(rect.left, rect.right));
            minY = Math.min(minY, Math.min(rect.top, rect.bottom));
            maxY = Math.max(maxY, Math.max(rect.top, rect.bottom));
        }
        return new RectF(minX, maxY, maxX, minY);
    }

    private static void printRedactProperties(CPDFRedactAnnotation redact) {
        CPDFTextAttribute textDa = redact.getTextDa();
        System.out.println("Overlay text: " + redact.getOverLayText());
        System.out.println("Overlay font: " + (textDa == null ? "null"
                : textDa.getFontName() + " " + textDa.getFontSize() + "pt " + hex(textDa.getColor())));
        System.out.println("Overlay alignment: " + redact.getTextAlignment());
        System.out.println("Fill color: " + hex(redact.getFillColor()));
        System.out.println("Outline color: " + hex(redact.getOutlineColor()));
        System.out.println("Fill color before apply: " + hex(redact.getFillColorBeforeApply()));
        System.out.println("Quad rectangles: " + redact.getQuadRects().length + ", bounds: " + redact.getRect());
    }

    private static String hex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
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
