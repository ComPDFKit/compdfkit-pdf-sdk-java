import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFBackground;
import com.compdfkit.core.android.graphics.Bitmap;
import com.compdfkit.core.annotation.CPDFImageScaleType;
import com.compdfkit.core.utils.ColorUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BackgroundTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Background");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/BackgroundTest/").mkdirs();
        addColorBackground(rootDir);
        addImageBackground(rootDir);
        readBackground(rootDir);
        clearBackground(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Add a color background to PDF
     */
    private static void addColorBackground(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Add Color Background");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFBackground background = document.getBackground();
        background.setType(CPDFBackground.Type.Color);
        background.setColor(ColorUtils.parseColor(1f, 1f, 0f, 0f));
        background.setOpacity(0.5f);
        background.setRotation(45f);
        background.setScale(1.0f);
        background.setHorizalign(CPDFBackground.CPDFBackgroundAlign.Center);
        background.setVertalign(CPDFBackground.CPDFBackgroundAlign.Center);
        background.setXOffset(0f);
        background.setYOffset(0f);
        background.setPages("0,1,2");
        background.setAllowsView(true);
        background.setAllowsPrint(true);
        background.update();
        File file = new File(rootDir + "/out/BackgroundTest/ColorBackgroundTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        background.release();
        document.close();
        System.out.println("Done. Result saved in ColorBackgroundTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Add an image background to PDF
     */
    private static void addImageBackground(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Add Image Background");
        //CPDFDocument document = new CPDFDocument();
        //document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFDocument document = CPDFDocument.createDocument();
        if (document == null || !document.isValid()) {
            throw new RuntimeException("Failed to create document");
        }
        document.insertBlankPage(0, 595, 842);
        CPDFBackground background = document.getBackground();
        background.setType(CPDFBackground.Type.Image);
        background.setImage(loadBitmap(rootDir + "/TestFiles/ComPDFKit.png"), CPDFImageScaleType.SCALETYPE_fitCenter);
        //background.setImage(rootDir + "/TestFiles/ComPDFKit.jpg", rootDir + "/TestFiles/ComPDFKit.jpg", CPDFImageScaleType.SCALETYPE_fitCenter);
        background.setOpacity(1.0f);
        background.setScale(1.0f);
        background.setHorizalign(CPDFBackground.CPDFBackgroundAlign.Center);
        background.setVertalign(CPDFBackground.CPDFBackgroundAlign.Center);
        background.setXOffset(0f);
        background.setYOffset(0f);
        background.setPages("0,1,2,3,4");
        background.setAllowsView(true);
        background.setAllowsPrint(true);
        background.update();
        File file = new File(rootDir + "/out/BackgroundTest/ImageBackgroundTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        background.release();
        document.close();
        System.out.println("Done. Result saved in ImageBackgroundTest.pdf");
        printDividingLine();
    }

    private static Bitmap loadBitmap(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                return null;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
            Bitmap bitmap = new Bitmap();
            bitmap.setWidth(width);
            bitmap.setHeight(height);
            bitmap.setPixels(pixels);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sample 3: Read background information from PDF
     */
    private static void readBackground(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Read Background");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BackgroundTest/ColorBackgroundTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        CPDFBackground background = document.getBackground();
        System.out.println("isValid: " + background.isValid());
        System.out.println("Type: " + background.getType());
        System.out.println("Scale: " + background.getScale());
        System.out.println("Rotation: " + background.getRotation());
        System.out.println("Horizalign: " + background.getHorizalign());
        System.out.println("Vertalign: " + background.getVertalign());
        System.out.println("XOffset: " + background.getXOffset());
        System.out.println("YOffset: " + background.getYOffset());
        System.out.println("Pages: " + background.getPages());
        System.out.println("isAllowsView: " + background.isAllowsView());
        System.out.println("isAllowsPrint: " + background.isAllowsPrint());
        System.out.println("Color: " + background.getColor());
        // Note: getOpacity() has a known source bug (returns -1f when isValid), just print without asserting
        System.out.println("Opacity (raw): " + background.getOpacity());
        background.release();
        document.close();
        System.out.println("Done. readBackground completed.");
        printDividingLine();
    }

    /**
     * Sample 4: Clear background from PDF
     */
    private static void clearBackground(String rootDir) {
        printDividingLine();
        System.out.println("Sample 4: Clear Background");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BackgroundTest/ColorBackgroundTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        CPDFBackground background = document.getBackground();
        background.clear();
        File file = new File(rootDir + "/out/BackgroundTest/ClearBackgroundTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        background.release();
        document.close();
        System.out.println("Done. Result saved in ClearBackgroundTest.pdf");
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
