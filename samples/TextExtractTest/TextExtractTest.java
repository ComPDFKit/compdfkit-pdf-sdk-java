import com.compdfkit.core.android.graphics.PointF;
import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;
import com.compdfkit.core.page.CPDFTextPage;
import com.compdfkit.core.page.CPDFTextRange;
import com.compdfkit.core.page.CPDFTextSearcher;
import com.compdfkit.core.page.CPDFTextSelection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Text extraction and positioning samples built on CPDFTextPage:
 * character counts and boxes, text inside a rectangle, hit testing by page point,
 * word and line selections, selections by range or rectangle, and full-text export.
 * Rectangles and points use PDF page coordinates (origin bottom-left, Y axis up).
 */
public class TextExtractTest {
    private static final String INPUT_PDF = "/TestFiles/text.pdf";
    private static final String OUTPUT_DIR = "/out/TextExtractTest";
    private static final String KEYWORD = "PDF";
    private static final float HIT_TOLERANCE = 2f;

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("TextExtract");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();

        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + INPUT_PDF);
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();
        if (textPage == null || !textPage.isValid() || textPage.getCountChars() == 0) {
            System.out.println("The first page has no extractable text");
            document.close();
            printFooter();
            return;
        }
        charactersAndBoxes(textPage);
        boundedText(textPage);
        hitTest(textPage);
        wordAndLineSelection(textPage);
        selectionsByRangeAndRect(textPage);
        exportAllText(document, rootDir);
        document.close();
        printFooter();
    }

    /** Sample 1: Character count, text by range and character bounding boxes. */
    private static void charactersAndBoxes(CPDFTextPage textPage) {
        printDividingLine();
        System.out.println("Sample 1: Character count, text range and character boxes");
        int count = textPage.getCountChars();
        System.out.println("getCountChars(): " + count);
        int previewLength = Math.min(count, 60);
        String preview = textPage.getText(new CPDFTextRange(0, previewLength));
        System.out.println("First " + previewLength + " characters: \"" + escape(preview) + "\"");
        for (int i = 0; i < Math.min(5, count); i++) {
            RectF box = textPage.getCharBox(i);
            String character = preview != null && i < preview.length() ? escape(preview.substring(i, i + 1)) : "?";
            System.out.println("Char " + i + " '" + character + "' box: " + box);
        }
        printDividingLine();
    }

    /** Sample 2: Text enclosed in a rectangle built from two character boxes. */
    private static void boundedText(CPDFTextPage textPage) {
        System.out.println("Sample 2: Text inside a rectangle");
        int count = textPage.getCountChars();
        RectF first = textPage.getCharBox(0);
        RectF last = textPage.getCharBox(Math.min(count - 1, 30));
        RectF area = union(first, last);
        System.out.println("Rectangle: " + area);
        String bounded = textPage.getBoundedText(area);
        System.out.println("getBoundedText(): \"" + escape(bounded) + "\"");
        printDividingLine();
    }

    /** Sample 3: Character index at a point and character range between two points. */
    private static void hitTest(CPDFTextPage textPage) {
        System.out.println("Sample 3: Hit testing by page point");
        int count = textPage.getCountChars();
        int probeIndex = Math.min(10, count - 1);
        PointF probe = center(textPage.getCharBox(probeIndex));
        int hitIndex = textPage.getCharIndexAtPos(probe, HIT_TOLERANCE, HIT_TOLERANCE);
        System.out.println("Center of char " + probeIndex + " is " + probe.x + "," + probe.y
                + " -> getCharIndexAtPos(): " + hitIndex);
        if (hitIndex >= 0) {
            System.out.println("Character at hit index: \"" + escape(textPage.getText(new CPDFTextRange(hitIndex, 1))) + "\"");
        }

        PointF start = center(textPage.getCharBox(0));
        PointF end = center(textPage.getCharBox(Math.min(20, count - 1)));
        CPDFTextRange range = textPage.getCharsRangeAtPos(start, end, HIT_TOLERANCE, HIT_TOLERANCE);
        if (range == null) {
            System.out.println("getCharsRangeAtPos(): null");
        } else {
            System.out.println("getCharsRangeAtPos(): location=" + range.location + ", length=" + range.length
                    + ", text=\"" + escape(textPage.getText(range)) + "\"");
        }
        printDividingLine();
    }

    /** Sample 4: Whole word and whole line at a page point. */
    private static void wordAndLineSelection(CPDFTextPage textPage) {
        System.out.println("Sample 4: Word and line selection at a page point");
        int count = textPage.getCountChars();
        PointF probe = center(textPage.getCharBox(Math.min(10, count - 1)));

        CPDFTextSelection[] words = textPage.getSelectionForWordPos(probe);
        System.out.println("getSelectionForWordPos(): " + (words == null ? 0 : words.length) + " selection(s)");
        printSelections(textPage, words);

        CPDFTextSelection line = textPage.getSelectionForLineAtPos(probe);
        System.out.println("getSelectionForLineAtPos(): " + (line == null ? "null" : "valid=" + line.isValid()));
        printSelections(textPage, line == null ? null : new CPDFTextSelection[]{line});
        printDividingLine();
    }

    /** Sample 5: Selections for a searched range and for a rectangle. */
    private static void selectionsByRangeAndRect(CPDFTextPage textPage) {
        System.out.println("Sample 5: Selections by text range and by rectangle");
        CPDFTextRange match = findFirst(textPage, KEYWORD);
        if (match == null) {
            System.out.println("Keyword \"" + KEYWORD + "\" not found");
            printDividingLine();
            return;
        }
        System.out.println("First \"" + KEYWORD + "\" match: location=" + match.location + ", length=" + match.length);

        CPDFTextSelection[] byLine = textPage.getSelectionsByLineForRange(match);
        System.out.println("getSelectionsByLineForRange(): " + (byLine == null ? 0 : byLine.length) + " line(s)");
        printSelections(textPage, byLine);

        CPDFTextSelection[] byText = textPage.getSelectionsByTextForRange(match);
        System.out.println("getSelectionsByTextForRange(): " + (byText == null ? 0 : byText.length) + " run(s)");
        printSelections(textPage, byText);

        if (byLine != null && byLine.length > 0 && byLine[0] != null) {
            RectF lineRect = byLine[0].getRectF();
            CPDFTextSelection[] inRect = textPage.getSelectionsByLineForRect(lineRect);
            System.out.println("getSelectionsByLineForRect(line rectangle): " + (inRect == null ? 0 : inRect.length) + " line(s)");
            printSelections(textPage, inRect);
        }

        RectF content = contentBounds(textPage);
        CPDFTextSelection[] allLines = textPage.getSelectionsByLineForRect(content);
        System.out.println("getSelectionsByLineForRect(all text bounds " + content + "): "
                + (allLines == null ? 0 : allLines.length) + " line(s)");
        printDividingLine();
    }

    /** Sample 6: Export the text of every page to a UTF-8 file. */
    private static void exportAllText(CPDFDocument document, String rootDir) {
        System.out.println("Sample 6: Export the text of all pages");
        StringBuilder builder = new StringBuilder();
        int totalChars = 0;
        for (int i = 0; i < document.getPageCount(); i++) {
            CPDFTextPage textPage = document.pageAtIndex(i).getTextPage();
            int count = textPage == null ? 0 : textPage.getCountChars();
            String text = count == 0 ? "" : textPage.getText(new CPDFTextRange(0, count));
            totalChars += count;
            builder.append("===== Page ").append(i + 1).append(" (").append(count).append(" chars) =====\n");
            builder.append(text == null ? "" : text).append('\n');
        }
        File output = new File(rootDir + OUTPUT_DIR + "/text_content.txt");
        try {
            Files.write(Paths.get(output.getAbsolutePath()), builder.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Exported " + totalChars + " characters from " + document.getPageCount()
                    + " page(s) to text_content.txt");
        } catch (IOException e) {
            System.out.println("Failed to write " + output + ": " + e.getMessage());
        }
        printDividingLine();
    }

    private static CPDFTextRange findFirst(CPDFTextPage textPage, String keyword) {
        CPDFTextSearcher searcher = new CPDFTextSearcher();
        CPDFTextRange range = null;
        if (searcher.findStart(textPage, keyword, 0, CPDFTextSearcher.PDFSearchOptions.PDFSearchCaseInsensitive)) {
            range = searcher.findNext();
            searcher.findClose();
        }
        searcher.close();
        return range;
    }

    private static void printSelections(CPDFTextPage textPage, CPDFTextSelection[] selections) {
        if (selections == null) {
            return;
        }
        for (CPDFTextSelection selection : selections) {
            if (selection == null) {
                continue;
            }
            CPDFTextRange range = selection.getTextRange();
            String text = range == null ? null : textPage.getText(range);
            System.out.println("  rect=" + selection.getRectF()
                    + " range=" + (range == null ? "null" : range.location + "+" + range.length)
                    + " text=\"" + escape(text) + "\"");
        }
    }

    private static RectF contentBounds(CPDFTextPage textPage) {
        RectF bounds = null;
        for (int i = 0; i < textPage.getCountChars(); i++) {
            RectF box = textPage.getCharBox(i);
            if (box == null || Math.abs(box.width()) < 0.01f || Math.abs(box.height()) < 0.01f) {
                continue;
            }
            bounds = bounds == null ? new RectF(box) : union(bounds, box);
        }
        return bounds == null ? new RectF(0, 0, 0, 0) : bounds;
    }

    private static RectF union(RectF a, RectF b) {
        float minX = Math.min(Math.min(a.left, a.right), Math.min(b.left, b.right));
        float maxX = Math.max(Math.max(a.left, a.right), Math.max(b.left, b.right));
        float minY = Math.min(Math.min(a.top, a.bottom), Math.min(b.top, b.bottom));
        float maxY = Math.max(Math.max(a.top, a.bottom), Math.max(b.top, b.bottom));
        return new RectF(minX, maxY, maxX, minY);
    }

    private static PointF center(RectF rect) {
        return new PointF((rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2);
    }

    private static String escape(String text) {
        return text == null ? "null" : text.replace("\r", "\\r").replace("\n", "\\n");
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
