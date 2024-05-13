import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;

import java.io.File;
import java.io.FileNotFoundException;

public class AnnotationImportExportTest {
    static{
        try {
            NativeKMPDFKit.initialize(null, Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)  {
        printHead("AnnotationImportExport");
        String rootDir = getRootDir(args);
        exportAnnotation(rootDir);
        importAnnotation(rootDir);
        printFooter();
     }


    /**
     * Samples 1 : export pdf document annotations
     */
    private static void exportAnnotation(String rootDir){
        printDividingLine();
        String exportPath = rootDir + "/TestFiles/Annotations.pdf";
        // Open the pdf document containing comments that needs to be exported
        CPDFDocument document = new CPDFDocument();
        File file = new File(exportPath);
        if(file.exists()){
            System.out.println("file path is correct");
        }
        CPDFDocument.PDFDocumentError documentError = document.open(exportPath);
        if(!documentError.equals(CPDFDocument.PDFDocumentError.PDFDocumentErrorSuccess)){
            System.out.println("file open failed");
            System.out.println("reason :" + documentError.name());
            return;
        }
        // Set annotations export path, cache directory
        String outPutPath = rootDir + "/out/ExportAnnotationTest.xfdf";
        // To create a directory, please ensure that the path exists.
        document.exportAnnotations(outPutPath,rootDir + "/out");
        document.close();
        System.out.println("Done.");
        System.out.println("Done. Results saved in ExportAnnotationTest.xfdf");
    }

    /**
     * Samples 2 : Import a previously exported comment file into a blank document
     */
    private static void importAnnotation(String rootDir) {
        String filePath = rootDir + "/TestFiles/CommonFivePage.pdf";
        String importPath = rootDir + "/out/ExportAnnotationTest.xfdf";
        String outputPath = rootDir + "/out/ImportAnnotationTest.pdf";
        // Open the pdf document containing comments that needs to be exported
        CPDFDocument document = new CPDFDocument();
        document.open(filePath);
        // Get imported annotation files
        document.importAnnotations(importPath);
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);

        System.out.println("Done.");
        System.out.println("Done. Results saved in ImportAnnotationTest.pdf");
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
