import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;
import com.compdfkit.core.page.CPDFPage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PDFPageTest {
    static {
        try {
            NativeKMPDFKit.initialize("your license","your deviceId" ,"",Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        printHead("PDFPage");
        String rootDir = getRootDir(args);
        insertBlankPage(rootDir);
        insertPdfPage(rootDir);
        splitPages(rootDir);
        mergePages(rootDir);
        deletePages(rootDir);
        rotatePage(rootDir);
        replacePages(rootDir);
        extractPages(rootDir);
        printFooter();

    }

    /**
     * Samples - 1: Insert a blank A4-sized page into the sample document
     */
    private static void insertBlankPage(String rootPath) {
        printDividingLine();
        System.out.println("Samples 1: Insert a blank A4-sized page into the sample document");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        document.insertBlankPage(1, 595, 842);
        System.out.println("Insert PageIndex : 1");
        System.out.println("Size : 595*842");
        document.save(rootPath + "/out/Insert_Blank_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in Insert_Blank_Page.pdf");
        printDividingLine();
    }

    /**
     * Samples - 2: Import pages from another document into the example document
     */
    private static void insertPdfPage(String rootPath) {
        System.out.println("Samples 2: Import pages from another document into the example document");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        System.out.println("Open the document to be imported");
        //Open the document to be imported
        CPDFDocument document2 = new CPDFDocument();
        document2.open(rootPath +  "/TestFiles/text.pdf");
        document.importPages(document2, new int[]{0}, 1);
        document.save(rootPath + "/out/Import_Document_Page.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in Import_Document_Page.pdf");
        printDividingLine();
    }

    /**
     * Samples - 3: Split a PDF document into multiple pages
     */
    private static void splitPages(String rootPath) {
        System.out.println("Samples 3: Split a PDF document into multiple pages");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        for (int i = 0; i < document.getPageCount(); i++) {
            CPDFDocument newDocument = CPDFDocument.createDocument();
            newDocument.importPages(document, new int[]{i}, 0);
            String outputPath = rootPath + "/out/CommonFivePage_Split_Page_" + (i + 1) + ".pdf";
            System.out.println("Done. Result saved in \nCommonFivePage_Split_Page_" + (i + 1) + ".pdf");
            newDocument.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        }
        document.close();
        System.out.println("Done!");
        printDividingLine();
    }

    /**
     * Samples - 4: Merge split documents
     */
    private static void mergePages(String rootPath) {
        System.out.println("Samples 4: Merge split documents");
        int pageNum = 5;
        CPDFDocument document = CPDFDocument.createDocument();
        for (int i = 0; i < pageNum; i++) {
            String outputPath = rootPath + "/out/CommonFivePage_Split_Page_" + (i + 1) + ".pdf";
            File file = new File(outputPath);
            if (file.exists()) {
                System.out.println("Opening " + file.getName());
                CPDFDocument newDocument = new CPDFDocument();
                newDocument.open(file.getAbsolutePath());
                document.importPages(newDocument, new int[]{0}, i);
            }
        }
        File file = new File(rootPath + "/out/Merge_Pages.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in\nMerge_Pages.pdf");
        printDividingLine();
    }

    /**
     * Samples - 5: Delete the specified page of the document
     */
    private static void deletePages(String rootPath) {
        System.out.println("Samples 5: Delete the specified page of the document");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        int[] evenNumbers = getEvenNumbers(1, document.getPageCount() - 1);
        document.removePages(evenNumbers);
        File file = new File(rootPath + "/out/Remove_Page.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in\nRemove_Page.pdf");
        printDividingLine();
    }

    public static int[] getEvenNumbers(int start, int end) {
        int size = (end - start) / 2 + 1;
        int[] evenNumbers = new int[size];
        int index = 0;
        for (int i = start; i <= end; i++) {
            if (i % 2 != 0) {
                evenNumbers[index] = i;
                index++;
            }
        }
        return evenNumbers;
    }

    /**
     * Samples - 6: Rotate document pages
     */
    private static void rotatePage(String rootPath) {
        System.out.println("Samples 6: Rotate document pages");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        document.pageAtIndex(0).setRotation(90);
        File file = new File(rootPath + "/out/Rotate_Pages.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in\nRotate_Pages.pdf");
        printDividingLine();
    }

    /**
     * Samples - 7: Replace specified pages of example documentation with other documentation specified pages
     */
    private static void replacePages(String rootPath) {
        System.out.println("Samples 7: Replace specified pages of example documentation with other documentation specified pages");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        document.removePages(new int[]{1});
        //open second pdf Document
        CPDFDocument document2 = new CPDFDocument();
        document2.open( rootPath +  "/TestFiles/text.pdf");
        document.importPages(document2, new int[]{0}, 1);
        File file = new File(rootPath + "/out/Replace_Pages.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in\nReplace_Pages.pdf");
        printDividingLine();
    }

    /**
     * Samples - 8: Extract specific pages of a document
     */
    private static void extractPages(String rootPath) {
        System.out.println("Samples 8: Extract specific pages of a document");
        System.out.println("Opening the Samples PDF File");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        File file = new File(rootPath + "/out/ExtractPages.pdf");
        CPDFDocument newDocument = CPDFDocument.createDocument();
        newDocument.importPages(document, new int[]{1}, 0);
        System.out.println("Done. Result saved in \nExtractPages.pdf");
        newDocument.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        newDocument.close();
        printDividingLine();
    }

    private static void printHead(String name) {
        String head = "Running %s sample";
        String formattedHead = String.format(head, name);
        System.out.println(formattedHead);
    }

    private static void printFooter() {
        String footer = "Done!";
        System.out.println(footer);
        printDividingLine();
    }
    private static void printDividingLine() {
        System.out.println("--------------------------------------------");
    }

    private static String getRootDir(String[] args) {
        String dir;
        if ((args == null) || args.length == 0) {
            dir = System.getProperty("user.dir");
            if (!dir.endsWith("samples")) dir += "/samples";
        } else {
            dir = args[0];
        }
        File file = new File(dir);
        if(!file.exists()){
            throw new RuntimeException("home directory does not exist");
        }
        return file.getAbsolutePath();
    }
}
