import com.compdfkit.core.android.graphics.PointF;
import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;
import com.compdfkit.core.page.CPDFTextPage;
import com.compdfkit.core.page.CPDFTextRange;
import com.compdfkit.core.page.CPDFTextSelection;

import java.io.File;
import java.util.Arrays;

/**
 * Page organization samples: page labels and index lookups, copying, moving,
 * exchanging and merging pages, plus page-level operations such as image
 * extraction, erasing an area, flattening one page and refreshing form appearances.
 */
public class PageOrganizeTest {
    private static final String FIVE_PAGE_PDF = "/TestFiles/CommonFivePage.pdf";
    private static final String TEXT_PDF = "/TestFiles/text.pdf";
    private static final String ANNOTATION_PDF = "/TestFiles/Annotations.pdf";
    private static final String FORM_PDF = "/TestFiles/form.pdf";
    private static final String IMAGE_FILE = "/TestFiles/ComPDFKit.jpg";
    private static final String OUTPUT_DIR = "/out/PageOrganizeTest";

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("PageOrganize");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();
        labelsAndIndices(rootDir);
        copyAndAddPage(rootDir);
        moveAndExchangePages(rootDir);
        mergePages(rootDir);
        extractImages(rootDir);
        eraseArea(rootDir);
        flattenSinglePage(rootDir);
        refreshFormAppearance(rootDir);
        printFooter();
    }

    /** Sample 1: Page labels, page range parsing, page index lookup and page release. */
    private static void labelsAndIndices(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Page labels, ranges and indices");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + FIVE_PAGE_PDF);
        for (int i = 0; i < document.getPageCount(); i++) {
            System.out.println("Page " + i + " label: " + pageLabel(document, i) + ", size: " + document.getPageSize(i));
        }
        System.out.println("getPageNumbers(\"1,3-5\") -> " + Arrays.toString(document.getPageNumbers("1,3-5")));
        System.out.println("getPageNumbers(\"2\") -> " + Arrays.toString(document.getPageNumbers("2")));
        CPDFPage third = document.pageAtIndex(2);
        System.out.println("indexOfPage(pageAtIndex(2)) -> " + document.indexOfPage(third)
                + ", getPageNum() -> " + third.getPageNum());
        System.out.println("closePage(2): " + document.closePage(2) + ", closeAllPages(): " + document.closeAllPages());
        document.close();
        printDividingLine();
    }

    /** Sample 2: Duplicate the first page by importing it from a second handle on the same file. */
    private static void copyAndAddPage(String rootDir) {
        System.out.println("Sample 2: Duplicate a page with importPages()");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + FIVE_PAGE_PDF);
        // Pages are duplicated by importing them from another document object; opening the
        // same file again provides the source. There is no separate copy-page or add-page API.
        CPDFDocument source = new CPDFDocument();
        source.open(rootDir + FIVE_PAGE_PDF);
        int before = document.getPageCount();
        boolean imported = document.importPages(source, new int[]{0}, before);
        System.out.println("importPages(source, [0], " + before + "): " + imported
                + ", page count " + before + " -> " + document.getPageCount());
        System.out.println("First line of page 0: " + firstLine(document, 0));
        System.out.println("First line of appended page: " + firstLine(document, document.getPageCount() - 1));
        document.save(rootDir + OUTPUT_DIR + "/Copy_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        source.close();
        document.close();
        System.out.println("Done. Result saved in Copy_Page.pdf");
        printDividingLine();
    }

    /** Sample 3: Move a page and exchange two pages, printing the order each time. */
    private static void moveAndExchangePages(String rootDir) {
        System.out.println("Sample 3: Move and exchange pages");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + FIVE_PAGE_PDF);
        System.out.println("Original order: " + order(document));
        System.out.println("movePage(0, 4): " + document.movePage(0, 4));
        System.out.println("After move:     " + order(document));
        document.save(rootDir + OUTPUT_DIR + "/Move_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("exchangePage(1, 3): " + document.exchangePage(1, 3));
        System.out.println("After exchange: " + order(document));
        document.save(rootDir + OUTPUT_DIR + "/Exchange_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Results saved in Move_Page.pdf and Exchange_Page.pdf");
        printDividingLine();
    }

    /** Sample 4: Merge the first two pages into one page. */
    private static void mergePages(String rootDir) {
        System.out.println("Sample 4: Merge pages");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + FIVE_PAGE_PDF);
        System.out.println("Before: " + document.getPageCount() + " pages, page 0 size " + document.getPageSize(0));
        boolean merged = document.mergePage(new int[]{0, 1});
        System.out.println("mergePage([0, 1]): " + merged);
        System.out.println("After: " + document.getPageCount() + " pages, page 0 size " + document.getPageSize(0));
        document.save(rootDir + OUTPUT_DIR + "/Merge_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in Merge_Page.pdf");
        printDividingLine();
    }

    /** Sample 5: Build an image page, then export the images it contains. */
    private static void extractImages(String rootDir) {
        System.out.println("Sample 5: Extract images from a page");
        CPDFDocument document = CPDFDocument.createDocument();
        CPDFPage imagePage = document.insertPageWithImagePath(0, 612, 792, rootDir + IMAGE_FILE,
                CPDFDocument.PDFDocumentImageMode.PDFDocumentImageModeScaleAspectFit);
        System.out.println("insertPageWithImagePath: " + (imagePage != null && imagePage.isValid())
                + ", isImagePage: " + (imagePage != null && imagePage.isImagePage()));
        String imagePdf = rootDir + OUTPUT_DIR + "/Image_Page.pdf";
        document.save(imagePdf, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument reopened = new CPDFDocument();
        reopened.open(imagePdf);
        File imageDir = new File(rootDir + OUTPUT_DIR + "/images");
        boolean extracted = reopened.pageAtIndex(0).extractImages(imageDir.getAbsolutePath());
        String[] files = imageDir.list();
        System.out.println("extractImages(): " + extracted + ", files in images/: "
                + (files == null ? "none" : Arrays.toString(files)));
        reopened.close();
        printDividingLine();
    }

    /** Sample 6: Erase the first text line of a page and verify the text is gone. */
    private static void eraseArea(String rootDir) {
        System.out.println("Sample 6: Erase page content inside a rectangle");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + TEXT_PDF);
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();
        if (textPage == null || textPage.getCountChars() == 0) {
            System.out.println("No text on the page");
            document.close();
            printDividingLine();
            return;
        }
        RectF firstChar = textPage.getCharBox(0);
        PointF center = new PointF((firstChar.left + firstChar.right) / 2, (firstChar.top + firstChar.bottom) / 2);
        CPDFTextSelection line = textPage.getSelectionForLineAtPos(center);
        RectF area = line != null && line.getRectF() != null ? line.getRectF() : firstChar;
        System.out.println("Area: " + area);
        System.out.println("Text before erasure: \"" + trim(textPage.getBoundedText(area)) + "\"");
        System.out.println("erasureFromRect(): " + page.erasureFromRect(area));
        String outputPath = rootDir + OUTPUT_DIR + "/Erase_Area.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(outputPath);
        System.out.println("Text after erasure: \"" + trim(check.pageAtIndex(0).getTextPage().getBoundedText(area)) + "\"");
        check.close();
        System.out.println("Done. Result saved in Erase_Area.pdf");
        printDividingLine();
    }

    /** Sample 7: Flatten one page and inspect its geometry. */
    private static void flattenSinglePage(String rootDir) {
        System.out.println("Sample 7: Flatten a single page");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + ANNOTATION_PDF);
        CPDFPage page = document.pageAtIndex(0);
        System.out.println("Page bounds: " + page.getBounds() + ", size: " + page.getSize() + ", rotation: " + page.getRotation());
        int before = page.getAnnotCount();
        boolean flattened = page.flatten(CPDFPage.PDFFlattenOption.FLAT_NORMALDISPLAY);
        System.out.println("flatten(FLAT_NORMALDISPLAY): " + flattened + ", annotations " + before + " -> " + page.getAnnotCount());
        document.save(rootDir + OUTPUT_DIR + "/Flatten_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in Flatten_Page.pdf");
        printDividingLine();
    }

    /** Sample 8: Regenerate the appearance of every form field on a page. */
    private static void refreshFormAppearance(String rootDir) {
        System.out.println("Sample 8: Refresh form appearances on a page");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + FORM_PDF);
        CPDFPage page = document.pageAtIndex(0);
        System.out.println("Annotations on page 0: " + page.getAnnotCount() + ", updateFormAp(): " + page.updateFormAp());
        document.save(rootDir + OUTPUT_DIR + "/Form_Appearance.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in Form_Appearance.pdf");
        printDividingLine();
    }

    /** getPageLabel() throws NegativeArraySizeException in this SDK build when a page has no label. */
    private static String pageLabel(CPDFDocument document, int pageIndex) {
        try {
            String label = document.getPageLabel(pageIndex);
            return label == null || label.isEmpty() ? "(none)" : label;
        } catch (RuntimeException e) {
            return "(unavailable: " + e.getClass().getSimpleName() + ")";
        }
    }

    private static String order(CPDFDocument document) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < document.getPageCount(); i++) {
            builder.append('[').append(firstLine(document, i)).append("] ");
        }
        return builder.toString();
    }

    private static String firstLine(CPDFDocument document, int pageIndex) {
        CPDFPage page = document.pageAtIndex(pageIndex);
        CPDFTextPage textPage = page == null ? null : page.getTextPage();
        if (textPage == null || textPage.getCountChars() == 0) {
            return "(no text)";
        }
        String text = textPage.getText(new CPDFTextRange(0, Math.min(40, textPage.getCountChars())));
        return text == null ? "(no text)" : trim(text).split("\\R")[0];
    }

    private static String trim(String text) {
        return text == null ? "" : text.trim();
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
