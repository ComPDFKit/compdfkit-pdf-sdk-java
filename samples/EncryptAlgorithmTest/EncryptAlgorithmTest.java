import com.compdfkit.core.document.CPDFDocument;

import java.io.File;

public class EncryptAlgorithmTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("EncryptAlgorithm");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/EncryptAlgorithmTest/").mkdirs();
        encryptWithRC4(rootDir);
        encryptWithAES128(rootDir);
        encryptWithAES256(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Encrypt PDF with RC4 algorithm
     */
    private static void encryptWithRC4(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Encrypt with RC4");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        document.setEncryptAlgorithm(CPDFDocument.PDFDocumentEncryptAlgo.PDFDocumentRC4);
        document.setUserPassword("user123");
        CPDFDocument.PDFDocumentEncryptAlgo algo = document.getEncryptAlgorithm();
        System.out.println("Encrypt algorithm: " + (algo != null ? algo.name() : "null"));
        File file = new File(rootDir + "/out/EncryptAlgorithmTest/EncryptRC4Test.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in EncryptRC4Test.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Encrypt PDF with AES128 algorithm
     */
    private static void encryptWithAES128(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Encrypt with AES128");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        document.setEncryptAlgorithm(CPDFDocument.PDFDocumentEncryptAlgo.PDFDocumentAES128);
        document.setUserPassword("user123");
        CPDFDocument.PDFDocumentEncryptAlgo algo = document.getEncryptAlgorithm();
        System.out.println("Encrypt algorithm: " + (algo != null ? algo.name() : "null"));
        File file = new File(rootDir + "/out/EncryptAlgorithmTest/EncryptAES128Test.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in EncryptAES128Test.pdf");
        printDividingLine();
    }

    /**
     * Sample 3: Encrypt PDF with AES256 algorithm
     */
    private static void encryptWithAES256(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Encrypt with AES256");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        document.setEncryptAlgorithm(CPDFDocument.PDFDocumentEncryptAlgo.PDFDocumentAES256);
        document.setUserPassword("user123");
        CPDFDocument.PDFDocumentEncryptAlgo algo = document.getEncryptAlgorithm();
        System.out.println("Encrypt algorithm: " + (algo != null ? algo.name() : "null"));
        File file = new File(rootDir + "/out/EncryptAlgorithmTest/EncryptAES256Test.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in EncryptAES256Test.pdf");
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
