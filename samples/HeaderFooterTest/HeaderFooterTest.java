import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFHeaderFooter;

import java.io.File;

public class HeaderFooterTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("HeaderFooter");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/HeaderFooterTest/").mkdirs();
        addHeaderFooter(rootDir);
        readHeaderFooter(rootDir);
        clearHeaderFooter(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Add header and footer to PDF
     * Covers interfaces #55-70
     * Index mapping: 0=top-left, 1=top-center, 2=top-right, 3=bottom-left, 4=bottom-center, 5=bottom-right
     */
    private static void addHeaderFooter(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Add Header Footer");
        CPDFDocument document = new CPDFDocument();
        CPDFDocument.PDFDocumentError ret = document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        System.out.println("open "+ ret);
        
        // #55: Get header footer object
        CPDFHeaderFooter headerFooter = document.getHeaderFooter();
        
        // Set top-center header (index 1)
        // #56: setText
        headerFooter.setText(1, "Header - <<1>>");
        // #58: setFontName
        headerFooter.setFontName(1, "Helvetica");
        // #60: setFontSize
        headerFooter.setFontSize(1, 14f);
        // #62: setTextColor - use black color (0xFF000000)
        headerFooter.setTextColor(1, 0xFF000000);
        
        // Set bottom-center footer (index 4)
        // #56: setText
        headerFooter.setText(4, "Page <<1>>");
        // #58: setFontName
        headerFooter.setFontName(4, "Helvetica");
        // #60: setFontSize
        headerFooter.setFontSize(4, 12f);
        // #62: setTextColor - use black color
        headerFooter.setTextColor(4, 0xFF000000);
        
        // #64: setMargin - signature is (left, top, right, bottom)
        headerFooter.setMargin(20f, 20f, 20f, 20f);
        
        // #66: setPageOffset
        headerFooter.setPageOffset(1);
        
        // #68: setPages
        headerFooter.setPages("0,1,2,3,4");
        
        // #70: setRules
        headerFooter.setRules(0);
        
        // #69: update
        headerFooter.update();
        
        // Save document
        File file = new File(rootDir + "/out/HeaderFooterTest/AddHeaderFooterTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        
        // Release resources
        headerFooter.release();
        document.close();
        
        System.out.println("Done. Result saved in AddHeaderFooterTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Read header and footer from PDF
     * Covers interfaces #57, #59, #61, #63, #65, #67
     */
    private static void readHeaderFooter(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Read Header Footer");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/HeaderFooterTest/AddHeaderFooterTest.pdf");
        
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        
        // #55: Get header footer object
        CPDFHeaderFooter headerFooter = document.getHeaderFooter();
        
        // #67: isValid check
        System.out.println("isValid: " + headerFooter.isValid());
        
        // Loop through all 6 positions
        for (int i = 0; i < 6; i++) {
            // #57: getText
            String text = headerFooter.getText(i);
            if (text != null && !text.isEmpty()) {
                System.out.println("Index " + i + " text: " + text);
                
                // #59: getFontName
                System.out.println("Index " + i + " fontName: " + headerFooter.getFontName(i));
                
                // #61: getFontSize
                System.out.println("Index " + i + " fontSize: " + headerFooter.getFontSize(i));
                
                // #63: getTextColor
                System.out.println("Index " + i + " textColor: " + headerFooter.getTextColor(i));
            }
        }
        
        // #65: getMargin
        System.out.println("Margin: " + headerFooter.getMargin());
        
        // Additional read operations
        System.out.println("PageOffset: " + headerFooter.getPageOffset());
        System.out.println("Pages: " + headerFooter.getPages());
        System.out.println("Rules: " + headerFooter.getRules());
        
        // Release resources
        headerFooter.release();
        document.close();
        
        System.out.println("Done. readHeaderFooter completed.");
        printDividingLine();
    }

    /**
     * Sample 3: Clear header and footer from PDF
     */
    private static void clearHeaderFooter(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Clear Header Footer");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/HeaderFooterTest/AddHeaderFooterTest.pdf");
        
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        
        // Get header footer and clear it
        CPDFHeaderFooter headerFooter = document.getHeaderFooter();
        headerFooter.clear();
        
        // Save document
        File file = new File(rootDir + "/out/HeaderFooterTest/ClearHeaderFooterTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        
        // Release resources
        headerFooter.release();
        document.close();
        
        System.out.println("Done. Result saved in ClearHeaderFooterTest.pdf");
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
