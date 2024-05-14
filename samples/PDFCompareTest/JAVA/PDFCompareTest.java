import com.compdfkit.core.compare.CPDFCompare;
import com.compdfkit.core.compare.CPDFCompareDrawings;
import com.compdfkit.core.compare.CPDFCompareResults;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

public class PDFCompareTest {
    static {
        try {
            NativeKMPDFKit.initialize("your license","your deviceId" ,"",Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        printHead("PDFCompare");
        String rootDir = getRootDir(args);
        CPDFDocument baseDocument = new CPDFDocument();
        baseDocument.open(rootDir + "/TestFiles/comparefile.pdf");
        CPDFDocument comDocument = new CPDFDocument();
        comDocument.open(rootDir + "/TestFiles/comparefile1.pdf");
        printDividingLine();
        //------------------------------------------
        CPDFCompare compare = baseDocument.createCompare(baseDocument, comDocument);
        compare.setReplaceResColor(Color.decode("#93B9FD").getRGB());
        compare.setInsertResColor(Color.decode("#C0FFEC").getRGB());
        CPDFCompareResults result = compare.doCompare(0, 0, CPDFCompare.CompType.COMPAREALL.id, true);
        compare.generateNewDoc(rootDir + "/out/compareTest.pdf");
        //------------------------------------------
        CPDFCompareDrawings compareDrawings = baseDocument.createCompareDrawings(comDocument);
        compareDrawings.setBlendMode(4);
        compareDrawings.setColorCompare(Color.decode("#FBBDBF").getRGB());
        compareDrawings.setColorCompared(Color.decode("#93B9FD").getRGB());
        compareDrawings.setFillAlphaCompare(50);
        compareDrawings.setFillAlphaCompared(50);
        compareDrawings.compareContent();
        CPDFDocument document = compareDrawings.getDocGenerate();
        document.save(rootDir + "/out/compareTestOverlay.pdf", CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done.");
        System.out.println("\nDone. Result saved in compareTest.pdf");
        printDividingLine();

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
