import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.watermark.CPDFWatermark;
import com.compdfkit.core.page.CPDFPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class WatermarkTest {
    static {
        CPDFLicense.init();
    }


    public static void main(String[] args)  {
        printHead("Watermark");
        String rootDir = getRootDir(args);
        addTextWatermark(rootDir);
        addImageWatermark(rootDir);
        addTilesWatermark(rootDir);
        deleteWatermark(rootDir);
        removeUntaggedWatermark(rootDir);
        printFooter();
     }

    /**
     * Samples 1 : Insert a text watermark in the center of all pages of the document
     */
    private static void addTextWatermark(String rootPath) {
        printDividingLine();
        System.out.println("Sample 1 : Insert Text Watermark");
        CPDFDocument document = new CPDFDocument();
        document.open(rootPath + "/TestFiles/CommonFivePage.pdf");
        CPDFWatermark watermark = document.createWatermark(CPDFWatermark.Type.WATERMARK_TYPE_TEXT);
        watermark.setText("Watet text");
        watermark.setTextRGBColor(Color.RED.getRGB());
        watermark.setFontSize(30);
        watermark.setOpacity(0.5F);
        watermark.setRotation(45);
        watermark.setVertalign(CPDFWatermark.Vertalign.WATERMARK_VERTALIGN_CENTER);
        watermark.setHorizalign(CPDFWatermark.Horizalign.WATERMARK_HORIZALIGN_CENTER);
        watermark.setVertOffset(0);
        watermark.setHorizOffset(0);
        watermark.setPages("0,1,2,3,4");
        printWatermarkInfo(watermark);
        watermark.update();
        watermark.release();
        File file = new File(rootPath + "/out/AddTextWatermarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in AddTextWatermarkTest.pdf");
        printDividingLine();
    }

    /**
     * Samples 2 : Insert a picture watermark in the center of all pages of the document
     */
    private static void addImageWatermark(String rootDir) {
        System.out.println("Sample 2 : Insert Image Watermark");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFWatermark watermark = document.createWatermark(CPDFWatermark.Type.WATERMARK_TYPE_IMG);
        Image image = getImage(rootDir + "/TestFiles/ComPDFKit.png");
        watermark.setImage(image, 100, 100);
        watermark.setOpacity(1F);
        watermark.setRotation(20);
        watermark.setVertalign(CPDFWatermark.Vertalign.WATERMARK_VERTALIGN_CENTER);
        watermark.setHorizalign(CPDFWatermark.Horizalign.WATERMARK_HORIZALIGN_CENTER);
        watermark.setVertOffset(0);
        watermark.setHorizOffset(0);
        watermark.setPages("0,1,2,3,4");
        printWatermarkInfo(watermark);
        watermark.update();
        watermark.release();
        File file = new File(rootDir + "/out/AddImageWatermarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in AddImageWatermarkTest.pdf");
        printDividingLine();
    }

    private static Image getImage(String s){
        try {
            // read .PNG
            File imageFile = new File(s);
            BufferedImage image = ImageIO.read(imageFile);

            // convert image to base64 string.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            // convert base64 string to image object
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            return ImageIO.read(new ByteArrayInputStream(decodedBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Samples 3 : Insert a tiled text watermark on all pages of the document
     */
    private static void addTilesWatermark(String rootDir) {
        System.out.println("Sample 3 : Insert Text Tiles Watermark");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFWatermark watermark = document.createWatermark(CPDFWatermark.Type.WATERMARK_TYPE_TEXT);
        watermark.setText("ComPDFKit");
        watermark.setTextRGBColor(Color.RED.getRGB());
        watermark.setFontSize(30);
        watermark.setOpacity(0.5F);
        watermark.setRotation(45);
        watermark.setVertalign(CPDFWatermark.Vertalign.WATERMARK_VERTALIGN_CENTER);
        watermark.setHorizalign(CPDFWatermark.Horizalign.WATERMARK_HORIZALIGN_CENTER);
        watermark.setVertOffset(0);
        watermark.setHorizOffset(0);
        watermark.setPages("0,1,2,3,4");
        watermark.setFullScreen(true);
        watermark.setHorizontalSpacing(100);
        watermark.setVerticalSpacing(100);
        printWatermarkInfo(watermark);
        watermark.update();
        watermark.release();
        File file = new File(rootDir + "/out/AddTilesWatermarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in AddTilesWatermarkTest.pdf");
        printDividingLine();
    }

    private static void deleteWatermark(String rootDir) {
        System.out.println("Sample 4 : Delete Watermark");
        CPDFDocument document = new CPDFDocument();
        File sampleFile = new File(rootDir + "/out/AddTextWatermarkTest.pdf");
        document.open(sampleFile.getAbsolutePath());
        CPDFWatermark watermark = document.getWatermark(0);
        if (watermark != null) {
            //remove all page watermarks
            watermark.clear();

            // Remove the watermark on the second page
//            watermark.setPages("0,2,3,4");
//            watermark.update();
//            watermark.release();
        }
        File file = new File(rootDir + "/out/DeleteWatermarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in DeleteWatermarkTest.pdf");
        printDividingLine();
    }

    private static void printWatermarkInfo(CPDFWatermark watermark){
        if (watermark.getType() == CPDFWatermark.Type.WATERMARK_TYPE_TEXT){
            System.out.println("Text : "+ watermark.getText());
            System.out.println(String.format("Color : red:%d, green:%d, blue:%d, alpha:%d",
                    watermark.getTextRGBColor(),
                    watermark.getTextRGBColor(),
                    watermark.getTextRGBColor(),
                    watermark.getTextRGBColor()));

            System.out.println("FontSize : " + watermark.getFontSize());
        }
        System.out.println("Opacity : " + watermark.getOpacity());
        System.out.println("Rotation : " + watermark.getRotation());
        System.out.println("Vertalign : " + watermark.getVertalign().name());
        System.out.println("Horizalign : " + watermark.getHorizalign().name());
        System.out.println("VertOffset : " + watermark.getVertOffset());
        System.out.println("HorizOffset : " + watermark.getHorizOffset());
        System.out.println("Pages : " + watermark.getPages());
        System.out.println("VerticalSpacing : " + watermark.getVerticalSpacing());
        System.out.println("HorizontalSpacing : " + watermark.getHorizontalSpacing());
    }


    /**
     * Remove untagged watermark.
     */
    private static void removeUntaggedWatermark(String rootDir) {
        System.out.println("Sample 4 : Remove Untagged Watermark");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/watermark.pdf");

        CPDFPage page = document.pageAtIndex(0);
        page.removeUntaggedWatermark();

        File file = new File(rootDir + "/out/RemoveUntaggedWatermarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done. Result saved in RemoveUntaggedWatermarkTest.pdf");
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
