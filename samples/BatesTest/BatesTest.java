import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFBates;
import java.io.File;

public class BatesTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Bates");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/BatesTest/").mkdirs();
        addBatesNumber(rootDir);
        readBatesNumber(rootDir);
        clearBatesNumber(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Add Bates number to PDF
     * Bates format: <<#digits#start#prefix#suffix>>
     * Example: <<#3#1#DOC-#>> = 3-digit number, starting at 1, prefix "DOC-", no suffix
     */
    private static void addBatesNumber(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Add Bates Number");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFBates bates = document.getBates();
        // Set bottom-center (index 4) with Bates format
        bates.setText(4, "<<#3#1#DOC-#>>");
        bates.setFontName(4, "Helvetica");
        bates.setFontSize(4, 12f);
        bates.setTextColor(4, 0xFF000000);
        bates.setMargin(36f, 36f, 36f, 36f);
        bates.setPages("0,1,2,3,4");
        bates.update();
        File file = new File(rootDir + "/out/BatesTest/AddBatesTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        bates.release();
        document.close();
        System.out.println("Done. Result saved in AddBatesTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Read Bates number from PDF
     */
    private static void readBatesNumber(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Read Bates Number");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BatesTest/AddBatesTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        CPDFBates bates = document.getBates();
        for (int i = 0; i < 6; i++) {
            String text = bates.getText(i);
            if (text != null && !text.isEmpty()) {
                System.out.println("Index " + i + " Bates text: " + text);
            }
        }
        bates.release();
        document.close();
        System.out.println("Done. readBatesNumber completed.");
        printDividingLine();
    }

    /**
     * Sample 3: Clear Bates number from PDF
     */
    private static void clearBatesNumber(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Clear Bates Number");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BatesTest/AddBatesTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        CPDFBates bates = document.getBates();
        bates.clear();
        File file = new File(rootDir + "/out/BatesTest/ClearBatesTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        bates.release();
        document.close();
        System.out.println("Done. Result saved in ClearBatesTest.pdf");
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
