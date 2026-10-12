import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;

import java.io.File;

public class ImageDocTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("ImageDoc");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/ImageDocTest/").mkdirs();
        checkImageDoc(rootDir);
        checkImagePage(rootDir);
        createImageDoc(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Check if document is an image document
     */
    private static void checkImageDoc(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Check Image Document");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        boolean isImageDoc = document.isImageDoc();
        System.out.println("CommonFivePage.pdf isImageDoc: " + isImageDoc);
        document.close();
        System.out.println("Done. checkImageDoc completed.");
        printDividingLine();
    }

    /**
     * Sample 2: Check if page is an image page
     */
    private static void checkImagePage(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Check Image Page");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFPage page = document.pageAtIndex(0);
        if (page != null) {
            boolean isImagePage = page.isImagePage();
            System.out.println("Page 0 isImagePage: " + isImagePage);
        } else {
            System.out.println("Failed to get page 0.");
        }
        document.close();
        System.out.println("Done. checkImagePage completed.");
        printDividingLine();
    }

    /**
     * Sample 3: Create an image document
     */
    private static void createImageDoc(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Create Image Document");
        CPDFDocument document = CPDFDocument.createDocument();
        if (document != null) {
            String imagePath = rootDir + "/TestFiles/ComPDFKit.png";
            CPDFPage insertedPage = document.insertPageWithImagePath(0,  612, 792, imagePath, CPDFDocument.PDFDocumentImageMode.PDFDocumentImageModeScaleToFill);
            System.out.println("Insert image page: ");
            if (insertedPage != null) {
                boolean isImageDoc = document.isImageDoc();
                System.out.println("New doc isImageDoc: " + isImageDoc);
                CPDFPage page = document.pageAtIndex(0);
                if (page != null) {
                    boolean isImagePage = page.isImagePage();
                    System.out.println("Page 0 isImagePage: " + isImagePage);
                }
            }
            File file = new File(rootDir + "/out/ImageDocTest/CreateImageDocTest.pdf");
            document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
            System.out.println("Done. Result saved in CreateImageDocTest.pdf");
        } else {
            System.out.println("Failed to create document.");
        }
        document.close();
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
