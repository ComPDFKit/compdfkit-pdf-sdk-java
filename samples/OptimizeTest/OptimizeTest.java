import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFDocument.CPDFOptimizeOptions;
import com.compdfkit.core.document.CPDFDocument.CPDFImageOptimizeOptions;
import com.compdfkit.core.document.CPDFDocument.CPDFCompressAlgorithm;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OptimizeTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Optimize");
        String rootDir = getRootDir(args);
        getOptimizeOptions(rootDir);
        optimizeWithCustomOptions(rootDir);
        optimizeWithAggressiveOptions(rootDir);
        optimizeToStream(rootDir);
        printFooter();
    }

    /**
     * Samples 1 : Get current optimize options
     */
    private static void getOptimizeOptions(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1 : Get Current Optimize Options");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFOptimizeOptions options = document.getOptimizeOptions();
        if (options != null) {
            System.out.println("Optimize flags: " + options.getOptimizeFlags());
            System.out.println("Image quality: " + options.getImageQuality());
            CPDFImageOptimizeOptions colorOpts = options.getColorImageOptions();
            if (colorOpts != null) {
                System.out.println("Color image upper PPI: " + colorOpts.upperPpi);
                System.out.println("Color image target PPI: " + colorOpts.targetPpi);
            }
        }
        document.close();
        printDividingLine();
    }

    /**
     * Samples 2 : Optimize with custom options
     */
    private static void optimizeWithCustomOptions(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2 : Optimize With Custom Options");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");

        CPDFOptimizeOptions customOptions = new CPDFOptimizeOptions();
        int flags = CPDFOptimizeOptions.OPTIMIZE_FLAG_RMNOTUSE |
                    CPDFOptimizeOptions.OPTIMIZE_FLAG_RMEPTOBJ |
                    CPDFOptimizeOptions.OPTIMIZE_FLAG_BCOMPRESSIMAGE |
                    CPDFOptimizeOptions.OPTIMIZE_FLAG_RMINVALINK |
                    CPDFOptimizeOptions.OPTIMIZE_FLAG_OPTIMIZEPAGECONTENT;
        customOptions.setOptimizeFlags(flags);
        customOptions.setImageQuality(50.0f);

        CPDFImageOptimizeOptions colorImageOpt = new CPDFImageOptimizeOptions();
        colorImageOpt.upperPpi = 300;
        colorImageOpt.targetPpi = 150;
        colorImageOpt.compressAlgorithm = CPDFCompressAlgorithm.JPEG;
        colorImageOpt.quality = 50;
        colorImageOpt.blockSize = 256;
        customOptions.setColorImageOptions(colorImageOpt);

        CPDFImageOptimizeOptions grayImageOpt = new CPDFImageOptimizeOptions();
        grayImageOpt.upperPpi = 300;
        grayImageOpt.targetPpi = 150;
        grayImageOpt.compressAlgorithm = CPDFCompressAlgorithm.JPEG;
        grayImageOpt.quality = 50;
        grayImageOpt.blockSize = 256;
        customOptions.setGrayscaleImageOptions(grayImageOpt);

        CPDFImageOptimizeOptions monoImageOpt = new CPDFImageOptimizeOptions();
        monoImageOpt.upperPpi = 450;
        monoImageOpt.targetPpi = 300;
        monoImageOpt.compressAlgorithm = CPDFCompressAlgorithm.JBIG2;
        monoImageOpt.quality = 50;
        monoImageOpt.blockSize = 256;
        customOptions.setMonochromeImageOptions(monoImageOpt);

        document.setOptimizeOptions(customOptions);
        new File(rootDir + "/out/OptimizeTest").mkdirs();
        if (document.optimizeSaveAsCopy(rootDir + "/out/OptimizeTest/OptimizeCustomTest.pdf", true)) {
            System.out.println("Done. Result saved in OptimizeCustomTest.pdf");
        } else {
            System.out.println("Failed to optimize document");
        }
        document.close();
        printDividingLine();
    }

    /**
     * Samples 3 : Optimize with aggressive options
     */
    private static void optimizeWithAggressiveOptions(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3 : Optimize With Aggressive Options");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");

        CPDFOptimizeOptions aggressiveOptions = new CPDFOptimizeOptions();
        int aggressiveFlags = CPDFOptimizeOptions.OPTIMIZE_FLAG_RMNOTUSE |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMEPTOBJ |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_BCOMPRESSIMAGE |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMANNOT |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMFORM |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMMULMEDIA |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMJSACTION |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_RMPAGETHUMBNAIL |
                              CPDFOptimizeOptions.OPTIMIZE_FLAG_OPTIMIZEPAGECONTENT;
        aggressiveOptions.setOptimizeFlags(aggressiveFlags);
        aggressiveOptions.setImageQuality(40.0f);
        document.setOptimizeOptions(aggressiveOptions);
        new File(rootDir + "/out/OptimizeTest").mkdirs();
        if (document.optimizeSaveAsCopy(rootDir + "/out/OptimizeTest/OptimizeAggressiveTest.pdf", true)) {
            System.out.println("Done. Result saved in OptimizeAggressiveTest.pdf");
        } else {
            System.out.println("Failed to optimize document");
        }
        document.close();
        printDividingLine();
    }

    /**
     * Samples 4 : Optimize and save to output stream
     */
    private static void optimizeToStream(String rootDir) {
        printDividingLine();
        System.out.println("Sample 4 : Optimize And Save To Stream");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        new File(rootDir + "/out/OptimizeTest").mkdirs();
        String outputPath = rootDir + "/out/OptimizeTest/OptimizeToStreamTest.pdf";
        try (OutputStream out = new FileOutputStream(outputPath)) {
            if (document.optimizeSaveAsCopy(out, true)) {
                System.out.println("Done. Result saved in OptimizeToStreamTest.pdf");
            } else {
                System.out.println("Failed to optimize document");
            }
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        } finally {
            document.close();
        }
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
        if(!file.exists()){
            throw new RuntimeException("home directory does not exist");
        }
        return file.getAbsolutePath();
    }
}
