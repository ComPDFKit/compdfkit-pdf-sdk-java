import com.compdfkit.core.common.CPDFDocumentException;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFDocumentPermissionInfo;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;

import java.io.File;
import java.io.FileNotFoundException;

public class EncryptTest {
    static{
        try {
            NativeKMPDFKit.initialize("your license","your deviceId" ,"",Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)  {
        printHead("Encrypt");
        printDividingLine();
        String rootDir = getRootDir(args);
        System.out.println("Samples 1 : encrypt");
        encryptDocument(rootDir,"EncryptUseRC4Test.pdf");
        System.out.println("Encrypt by user password done.");
        encryptOwnerPassword(rootDir);
        encryptAllPassword(rootDir);
        decryptDocument(rootDir);
        printFooter();
    }

    /**
     * Encrypt document
     */
    private static void encryptDocument(String rootPath, String fileName) {
        CPDFDocument document = new CPDFDocument();
        String filePath = rootPath + "/TestFiles/CommonFivePage.pdf";
        document.open(filePath);
        document.setUserPassword("User");

        String resultsFile = rootPath + "/out/" + fileName;
        document.save(resultsFile,CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("User password is : User");
        System.out.println("Done. Results saved in " + fileName);
        System.out.println();
    }

    private static void encryptOwnerPassword(String rootPath) {
        printDividingLine();
        System.out.println("Samples 2 : Encrypt by owner passwords");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        document.setOwnerPassword("owner");
        String resultsFile = rootPath + "/out/EncryptByOwnerPasswordsTest.pdf";
        document.save(resultsFile, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Owner password is : owner");
        System.out.println("Done. Results saved in EncryptByOwnerPasswordsTest.pdf");
        System.out.println();
    }

    private static void encryptAllPassword(String rootPath) {
        printDividingLine();
        System.out.println("Samples 3 : Encrypt by Both user and owner passwords");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        document.setUserPassword("User");
        document.setOwnerPassword("owner");
        String resultsFile = rootPath + "/out/EncryptByAllPasswordsTest.pdf";
        if(document.save(resultsFile, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental)){
            document.close();
        }
        System.out.println("User password is : User");
        System.out.println("Owner password is : owner");
        System.out.println("Done. Results saved in EncryptByAllPasswordsTest.pdf");
        System.out.println();
    }


    private static void decryptDocument(String rootPath) {
        System.out.println("Samples 4 : encrypt document");

        CPDFDocument document = new CPDFDocument();
        System.out.println("Unlock with user password");
        document.open(rootPath + "/TestFiles/EncryptByAllPasswordsTest.pdf", "User");
        document.isEncrypted();

        System.out.println("Document is " + (document.isEncrypted() ? "locked" : "unlocked"));

        CPDFDocumentPermissionInfo info = document.getPermissionsInfo();
        System.out.println("AllowsPrinting : " + info.isAllowsPrinting());
        System.out.println("AllowsCopy : " + info.isAllowsCopying());
        document.close();

        CPDFDocument newDocument = new CPDFDocument();
        System.out.println("Unlock with owner password");
        newDocument.open(rootPath + "/TestFiles/EncryptByAllPasswordsTest.pdf", "owner");
        CPDFDocumentPermissionInfo info1 = newDocument.getPermissionsInfo();

        System.out.println("AllowsPrinting : " + info1.isAllowsPrinting());
        System.out.println("AllowsCopy : " + info1.isAllowsCopying());
        System.out.println("Unlock done.");
        printDividingLine();
        File decryptTestFile = new File(rootPath + "/out/DecryptTest.pdf");
        decryptTestFile.getParentFile().mkdirs();
        newDocument.save(decryptTestFile.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveRemoveSecurity);
        System.out.println("Document decrypt done.");
        System.out.println("Decrypt done.");
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
