import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFDocumentPermissionInfo;
import com.compdfkit.core.document.CPDFInfo;
import com.compdfkit.core.document.CPDFUAConfig;
import java.io.File;
import com.compdfkit.core.library.CPDFLibrary;
import com.compdfkit.core.library.PermissionEnum;

public class ConvertPDFA {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("DocumentInfo");

        String rootDir = getRootDir(args);

        convertPDFX4(rootDir);
        convertPDFE1(rootDir);
        convertPDFUA1(rootDir);
        convertPDFUA1WithConfig(rootDir);
        convertPDFA1a(rootDir);
        convertPDFA2u(rootDir);
        printFooter();
    }

    static boolean convertPDFX4(String rootPath) {
        String filePath = rootPath + "/TestFiles/Annotations.pdf";
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);

        String iccpath = rootPath + "/TestFiles/CoatedFOGRA39.icc";
        String resultsFile = rootPath + "/out/convpdfX4.pdf" ;
        boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeX4, iccpath, resultsFile);
        document.close();
        System.out.println("convertPDFX4:"+ret);
        return ret;
    }

    static boolean convertPDFE1(String rootPath) {
        String filePath = rootPath + "/TestFiles/Annotations.pdf";
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);

        String iccpath = rootPath + "/TestFiles/CoatedFOGRA39.icc";
        String resultsFile = rootPath + "/out/convpdfE1.pdf";
        boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeE1, iccpath, resultsFile);
        document.close();
        System.out.println("convertPDFE1:"+ret);
        return ret;
    }

    static boolean convertPDFUA1(String rootPath) {
        String filePath = rootPath + "/TestFiles/Annotations.pdf";
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);

        String iccpath = rootPath + "/TestFiles/sRGB2014.icc";
        String resultsFile = rootPath + "/out/convpdfUA1.pdf";
        boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeUA1, iccpath, resultsFile);
        document.close();
        System.out.println("convertPDFUA1:"+ret);
        return ret;
    }

    static boolean convertPDFUA1WithConfig(String rootPath) {
        String filePath = rootPath + "/TestFiles/CommonFivePage.pdf";
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);

        String iccpath = rootPath + "/TestFiles/sRGB2014.icc";
        String resultsFile = rootPath + "/out/convpdfa1a2.pdf" ;

        CPDFUAConfig ua = new CPDFUAConfig();
        ua.title = "title";
        ua.language = CPDFUAConfig.UALanguage.cs_CZ;
        boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeUA1, iccpath, resultsFile, ua);
        System.out.println("convertPDF UA1 with config:"+ret);

        return ret;
    }

    static boolean convertPDFA1a(String rootPath) {
    String filePath = rootPath + "/TestFiles/CommonFivePage.pdf";
    CPDFDocument document = new CPDFDocument();
    document.open(filePath);

    String iccpath = rootPath + "/TestFiles/sRGB2014.icc";
    String resultsFile = rootPath + "/out/convpdfa1a.pdf" ;
    boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeA1a, iccpath, resultsFile);

    System.out.println("convertPDFA1a:"+ret);
    return ret;
}

    static boolean convertPDFA2u(String rootPath) {
        String filePath = rootPath + "/TestFiles/CommonFivePage.pdf";
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);

        String iccpath = rootPath + "/TestFiles/sRGB2014.icc";
        String resultsFile = rootPath + "/out/convpdf3.pdf" ;

        boolean ret = document.WriteStandardPDFToFilePath(CPDFDocument.PDFDocumentType.PDFTypeA2u, iccpath, resultsFile);

        System.out.println("convertPDF A2u:"+ret);

        return ret;
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
