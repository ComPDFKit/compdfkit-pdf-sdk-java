import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFDocumentPermissionInfo;
import com.compdfkit.core.document.CPDFInfo;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;

import java.io.File;
import java.io.FileNotFoundException;

public class DocumentInfoTest {
    static {
        try {
            NativeKMPDFKit.initialize("your license","your deviceId" ,"",Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        printHead("DocumentInfo");
        CPDFDocument document = new CPDFDocument();
        String rootDir = getRootDir(args);
        File file = new File(rootDir, "/TestFiles/CommonFivePage.pdf");
        System.out.println();
        // Obtain basic information of pdf documents
        document.open(file.getAbsolutePath());
        CPDFInfo cpdfInfo = document.getInfo();
        double fileSizeMB = (double) file.length() / (1024 * 1024);
        System.out.println("FileName : " + file.getName());
        System.out.println("FileSize : " + fileSizeMB + " MB");
        System.out.println("Title : " + cpdfInfo.getTitle());
        System.out.println("Author : " + cpdfInfo.getAuthor());
        System.out.println("Subject : " + cpdfInfo.getSubject());
        System.out.println("Keywords : " + cpdfInfo.getKeywords());

        System.out.println("Version : " + document.getMajorVersion());
        System.out.println("PageCount : " + document.getPageCount());
        System.out.println("Creator : " + cpdfInfo.getCreator());
        System.out.println("CreationDate : " + cpdfInfo.getCreationDate());
        System.out.println("ModificationDate : "+ cpdfInfo.getModificationDate());

        // Get pdf document permission information
        CPDFDocumentPermissionInfo permissionInfo = document.getPermissionsInfo();
        System.out.println("Printing : " + permissionInfo.isAllowsPrinting());
        System.out.println("Content Copying : " + permissionInfo.isAllowsCopying());
        System.out.println("Document Change : " + permissionInfo.isAllowsDocumentChanges());
        System.out.println("Document Assembly : " + permissionInfo.isAllowsDocumentAssembly());
        System.out.println("Document Commenting : " + permissionInfo.isAllowsCommenting());
        System.out.println("Document Filling of form field : " + permissionInfo.isAllowsFormFieldEntry());
        document.close();
        printFooter();

    }


    private static void printHead(String name) {
        String head = "Running %s sample";
        String formattedHead = String.format(head, name);
        System.out.println(formattedHead);
    }

    private static void printFooter() {
        String footer = "Done!";
        System.out.println(footer);
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
