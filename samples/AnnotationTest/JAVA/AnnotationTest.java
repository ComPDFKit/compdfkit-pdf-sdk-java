import com.compdfkit.core.android.graphics.PointF;
import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.*;
import com.compdfkit.core.document.CPDFDestination;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.action.CPDFGoToAction;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;
import com.compdfkit.core.page.CPDFPage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class AnnotationTest {
    static{
        try {
            NativeKMPDFKit.initialize("your license","your deviceId" ,"",Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args)  {
        printHead("Annotation");
        String rootDir = getRootDir(args);
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        printDividingLine();
        //------------------------------------------
        //Samples 1 : add freetext annotation
        addFreeText(document);

        //------------------------------------------
        //Samples 2 : add ink annotation
        addInk(document);

        //------------------------------------------
        //Samples 3 : add line annotation
        addLine(document);

        //------------------------------------------
        //Samples 4 : add circle annotation
        addCircleShape(document);

        //------------------------------------------
        // Samples 5 : add square annotation
        addSquareShape(document);

        //------------------------------------------
        //Samples 6 : add highlight(markup) annotation
        addHighlight(document);

        //------------------------------------------
        //Samples 7 : add link annotation
        addLink(document);

        //------------------------------------------
        //Samples 7 : add note annotation
        addNote(document);

        //------------------------------------------
        //Samples 9 : add stamp annotation
        addStamp(document,rootDir);

        File file = new File(rootDir + "/out/CreateAnnotationTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done.");
        System.out.println("\nDone. Result saved in CreateAnnotationTest.pdf");
        printDividingLine();

        //------------------------------------------
        //Samples 10 : print annotation list info
        printAnnotationList(rootDir);

        //------------------------------------------
        //Samples 11 : delete annotation
        deleteAnnotation(rootDir);

        printFooter();
     }

    /**
     * Samples 1 : add freetext annotation
     *
     * @param document document
     */
    private static void addFreeText(CPDFDocument document) {
        // Insert the free text annotation into the first page of the PDF document.
        CPDFPage page1 = document.pageAtIndex(0);
        CPDFFreetextAnnotation freetextAnnotation = (CPDFFreetextAnnotation) page1.addAnnot(CPDFAnnotation.Type.FREETEXT,document);
        RectF freeText1Rect =  new RectF(10, 200, 160, 570);
        freetextAnnotation.setRect(freeText1Rect);
        // set text alignment
        freetextAnnotation.setFreetextAlignment(CPDFFreetextAnnotation.Alignment.ALIGNMENT_LEFT);
        // Set text font, bold, italic, color and font size
        CPDFTextAttribute textAttribute = new CPDFTextAttribute("", 12, Color.RED.getRGB());
        freetextAnnotation.setFreetextDa(textAttribute);
        // set text color opacity
        freetextAnnotation.setAlpha(255);
        freetextAnnotation.setContent("Some swift brown fox snatched a gray hare out of the air by freezing it with an angry glare.\n" +
                "\n" +
                "Aha!\n" +
                "\n" +
                "And there was much rejoicing!");
        freetextAnnotation.updateAp();
    }


    /**
     * Samples 2 : add ink annotation
     *
     * @param document document
     */
    private static void addInk(CPDFDocument document) {
        // Insert the ink annotation into the first page of the PDF document.
        CPDFPage page1 = document.pageAtIndex(0);
        ArrayList<ArrayList<PointF>> mDrawing = new ArrayList<>();
        ArrayList<PointF> arc1 = new ArrayList<>();
        arc1.add(new PointF(100, 100));
        arc1.add(new PointF(110, 110));
        arc1.add(new PointF(120, 120));
        mDrawing.add(arc1);
        ArrayList<PointF> arc2 = new ArrayList<>();
        arc2.add(new PointF(115, 115));
        arc2.add(new PointF(130, 130));
        arc2.add(new PointF(160, 160));
        mDrawing.add(arc2);
        float scaleValue = 1F;
        float borderWidth = 5F;
        CPDFInkAnnotation inkAnnotation = (CPDFInkAnnotation) page1.addAnnot(CPDFInkAnnotation.Type.INK,document);
        inkAnnotation.setColor(Color.RED.getRGB());
        inkAnnotation.setAlpha(255);
        inkAnnotation.setBorderWidth(borderWidth);
        RectF rect = null;
        RectF size = document.getPageSize(0);
        if (size.isEmpty()) {
            return;
        }
        int lineCount = mDrawing.size();
        PointF[][] path = new PointF[lineCount][];
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            ArrayList<PointF> line = mDrawing.get(lineIndex);
            int pointCount = line.size();
            PointF[] linePath = new PointF[pointCount];
            for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
                PointF point = line.get(pointIndex);
                // Calculate the smallest Rect that the Path is surrounded by
                if (rect == null) {
                    rect = new RectF(point.x, point.y, point.x, point.y);
                } else {
                    rect.union(point.x, point.y);
                }
                // Calculate the coordinate points that are converted to the Page and stored in the linePath collection
                linePath[pointIndex] = point;
            }
            path[lineIndex] = linePath;
        }
        float dx = borderWidth / scaleValue / 2;
        rect.inset(-dx, -dx);
        rect.set(rect);
        inkAnnotation.setInkPath(path);
        inkAnnotation.setRect(rect);
        inkAnnotation.updateAp();
        mDrawing.clear();
    }


    /**
     * Samples 3 : add line shape annotation
     *
     * @param document document
     */
    private static void addLine(CPDFDocument document) {
        // Add a green dotted line annotation
        CPDFPage page2 = document.pageAtIndex(1);
        CPDFLineAnnotation lineAnnotation = (CPDFLineAnnotation) page2.addAnnot(CPDFAnnotation.Type.LINE,document);
        // line start point
        PointF startPoint = new PointF(200, 100);
        // line end point
        PointF endPoint = new PointF(50, 300);
        RectF area = new RectF(200, 100, 50, 300);
        // Get the position of the line on the page
        lineAnnotation.setRect(area);
        lineAnnotation.setLinePoints(startPoint, endPoint);
        // Sets the arrowhead shape at both ends of the line
        lineAnnotation.setLineType(CPDFLineAnnotation.LineType.LINETYPE_NONE, CPDFLineAnnotation.LineType.LINETYPE_NONE);
        // Set line to dash and spacer width
        CPDFBorderStyle borderStyle = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Dashed, 10, new float[]{8, 4F});
        lineAnnotation.setBorderStyle(borderStyle);
        // set line width
        lineAnnotation.setBorderAlpha(255);
        lineAnnotation.setBorderColor(Color.GREEN.getRGB());
        lineAnnotation.updateAp();
        //Add a solid red line annotation with an arrow type
        CPDFLineAnnotation lineAnnotation2 = (CPDFLineAnnotation) page2.addAnnot(CPDFAnnotation.Type.LINE,document);
        PointF startPoint2 = new PointF(200, 350);
        PointF endPoint2 = new PointF(50, 550);
        RectF area2 = new RectF(200, 350, 50, 550);
        lineAnnotation2.setRect(area2);
        lineAnnotation2.setLinePoints(startPoint2, endPoint2);
        // Set the start position of the arrow as circle and the end of the arrow as the arrow type
        lineAnnotation2.setLineType(CPDFLineAnnotation.LineType.LINETYPE_CIRCLE, CPDFLineAnnotation.LineType.LINETYPE_ARROW);
        // set line to solid
        CPDFBorderStyle borderStyle2 = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Solid, 10, new float[]{8, 0});
        lineAnnotation2.setBorderStyle(borderStyle2);
        lineAnnotation2.setBorderAlpha(255);
        lineAnnotation2.setBorderColor(Color.RED.getRGB());
        lineAnnotation2.updateAp();
        // Add a solid red line annotation
        CPDFLineAnnotation lineAnnotation3 = (CPDFLineAnnotation) page2.addAnnot(CPDFAnnotation.Type.LINE,document);
        PointF startPoint3 = new PointF(400, 100);
        PointF endPoint3 = new PointF(250, 300);
        RectF area3 = new RectF(400, 100, 250, 300);
        lineAnnotation3.setRect(area3);
        lineAnnotation3.setLinePoints(startPoint3, endPoint3);
        lineAnnotation3.setLineType(CPDFLineAnnotation.LineType.LINETYPE_NONE, CPDFLineAnnotation.LineType.LINETYPE_NONE);
        CPDFBorderStyle borderStyle3 = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Solid, 10, new float[]{8, 0});
        lineAnnotation3.setBorderStyle(borderStyle3);
        lineAnnotation3.setBorderAlpha(255);
        lineAnnotation3.setBorderColor(Color.BLUE.getRGB());
        lineAnnotation3.updateAp();
    }

    /**
     * Samples 4 : add circle shape annotation
     *
     * @param document
     */
    private static void addCircleShape(CPDFDocument document) {
        // Add a circular annotation with a green border and blue fill
        CPDFPage page3 = document.pageAtIndex(2);
        CPDFCircleAnnotation circleAnnotation = (CPDFCircleAnnotation) page3.addAnnot(CPDFAnnotation.Type.CIRCLE,document);
        RectF insertRect = new RectF(50, 50, 150, 150);
       // RectF insertRect = getConvertRect(page3, new RectF(50, 50, 150, 150));
        circleAnnotation.setRect(insertRect);
        // set border color
        circleAnnotation.setBorderColor(Color.decode("#3863F1").getRGB());
        // Set border to solid line
        CPDFBorderStyle borderStyle = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Solid, 10, new float[]{8.0F, 0F});
        borderStyle.setBorderWidth(5F);
        circleAnnotation.setBorderStyle(borderStyle);
        circleAnnotation.setBorderAlpha(255);
        circleAnnotation.setFillColor(Color.decode("#31BC98").getRGB());
        circleAnnotation.setFillAlpha(255);
        circleAnnotation.updateAp();

        //Add a circular shape annotation with a dotted border.
        CPDFCircleAnnotation circleAnnotation2 = (CPDFCircleAnnotation) page3.addAnnot(CPDFAnnotation.Type.CIRCLE,document);
        RectF insertRect2 = new RectF(50, 200, 150, 300);
        //RectF insertRect2 = getConvertRect(page3, new RectF(50, 200, 150, 300));
        circleAnnotation2.setRect(insertRect2);
        circleAnnotation2.setBorderColor(Color.decode("#3863F1").getRGB());
        CPDFBorderStyle borderStyle2 = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Dashed, 10, new float[]{8.0F, 4F});
        borderStyle2.setBorderWidth(5F);
        circleAnnotation2.setBorderStyle(borderStyle2);
        circleAnnotation2.setBorderAlpha(127);
        circleAnnotation2.setFillColor(Color.decode("#31BC98").getRGB());
        circleAnnotation2.setFillAlpha(127);
        circleAnnotation2.updateAp();
    }

    /**
     * Samples 5 : add square shape annotation
     *
     * @param document document
     */
    private static void addSquareShape(CPDFDocument document) {
        // Add a rectangle with a blue border and green fill
        CPDFPage page3 = document.pageAtIndex(2);
        CPDFSquareAnnotation squareAnnotation = (CPDFSquareAnnotation) page3.addAnnot(CPDFAnnotation.Type.SQUARE,document);
        RectF insertRect =  new RectF(50, 350, 300, 450);
        squareAnnotation.setRect(insertRect);
        squareAnnotation.setBorderColor(Color.decode("#3863F1").getRGB());
        CPDFBorderStyle borderStyle = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Solid, 10, new float[]{8.0F, 0F});
        borderStyle.setBorderWidth(10F);
        squareAnnotation.setBorderStyle(borderStyle);
        squareAnnotation.setBorderAlpha(255);
        squareAnnotation.setFillColor(Color.decode("#31BC98").getRGB());
        squareAnnotation.setFillAlpha(255);
        squareAnnotation.updateAp();

        // Add a rectangle with a blue dotted border and a green fill with 50% transparency
        CPDFSquareAnnotation squareAnnotation2 = (CPDFSquareAnnotation) page3.addAnnot(CPDFAnnotation.Type.SQUARE,document);
        RectF insertRect2 = new RectF(50, 500, 300, 600);
        //insertRect2 = page3.convertRectToPage(false, pageSize.width(), pageSize.height(), insertRect2);
        squareAnnotation2.setRect(insertRect2);
        squareAnnotation2.setBorderColor(Color.decode("#3863F1").getRGB());
        CPDFBorderStyle borderStyle2 = new CPDFBorderStyle(CPDFBorderStyle.Style.Border_Dashed, 10, new float[]{8.0F, 4F});
        borderStyle2.setBorderWidth(10F);
        squareAnnotation2.setBorderStyle(borderStyle2);
        squareAnnotation2.setBorderAlpha(127);
        squareAnnotation2.setFillColor(Color.decode("#31BC98").getRGB());
        squareAnnotation2.setFillAlpha(127);
        squareAnnotation2.updateAp();
    }

    /**
     * Samples 6 : add high annotation
     * Here is a demonstration of searching out keywords in the search page and adding highlighted annotation
     *
     * @param document document
     */
    private static void addHighlight(CPDFDocument document) {
        //Also search for the `Page` keyword in the 3rd of the document
        CPDFPage pdfPage = document.pageAtIndex(3);
        //Then, add a highlight annotation for the specific area.
        RectF annotRect = new RectF(312.44F,578.985F,387.635F,552.985F);
        CPDFHighlightAnnotation highlightAnnotation = (CPDFHighlightAnnotation) pdfPage.addAnnot(CPDFAnnotation.Type.HIGHLIGHT,document);
        int colorInt = Color.decode("#FF0000").getRGB();
        highlightAnnotation.setColor(colorInt);
        highlightAnnotation.setAlpha((255/2));
        highlightAnnotation.setMarkedText("Page");
        highlightAnnotation.setRect(annotRect);
        highlightAnnotation.setQuadRects(new RectF[]{annotRect});
        highlightAnnotation.updateAp();
        System.out.println(annotRect.toString());
    }

    /**
     * Samples 7 : add link annotation
     *
     * @param document
     */
    private static void addLink(CPDFDocument document) {
        CPDFPage page = document.pageAtIndex(3);
        CPDFLinkAnnotation linkAnnotation = (CPDFLinkAnnotation) page.addAnnot(CPDFAnnotation.Type.LINK,document);
        RectF insertRect = new RectF(50, 50, 150, 150);
        linkAnnotation.setRect(insertRect);
        float firstPageHeight = document.getPageSize(0).height();
        // Add page jump link action
        CPDFGoToAction goToAction = new CPDFGoToAction();
        CPDFDestination destination = new CPDFDestination(1, 0, firstPageHeight, 1f);
        goToAction.setDestination(document, destination);
        linkAnnotation.setLinkAction(goToAction);
        linkAnnotation.updateAp();
    }

    /**
     * Samples 7 : add note annotation
     *
     * @param document document
     */
    private static void addNote(CPDFDocument document) {
        CPDFPage page = document.pageAtIndex(3);
        CPDFTextAnnotation textAnnotation = (CPDFTextAnnotation) page.addAnnot(CPDFAnnotation.Type.TEXT,document);
        //get the actual size of the page you want to insert
        RectF insertRect = new RectF(50, 200, 100, 250);
        textAnnotation.setRect(insertRect);
        textAnnotation.setContent("ComPDFKit");
        textAnnotation.updateAp();
    }

    /**
     * Samples 10 : add stamp annotation
     *
     * @param document
     * @param rootDir
     */
    private static void addStamp(CPDFDocument document, String rootDir)  {
        // add standard stamp annotation
        int yOffset = 50;
        float lastOffset = 0;
        for (int i = 0; i < CPDFStampAnnotation.StandardStamp.values().length; i++) {
            CPDFPage page = document.pageAtIndex(4);
            CPDFStampAnnotation.StandardStamp standardStamp = CPDFStampAnnotation.StandardStamp.values()[i];
            if (standardStamp == null || standardStamp == CPDFStampAnnotation.StandardStamp.UNKNOWN) {
                continue;
            }
            // add Standard stamp
            CPDFStampAnnotation standard = (CPDFStampAnnotation) page.addAnnot(CPDFAnnotation.Type.STAMP,document);
            if (standard == null) {
                continue;
            }
            standard.setStandardStamp(standardStamp);
            RectF pageSize = page.getSize();
            RectF insertRect = standard.getRect();
            float defaultWidth = 100F;
            int x = 50;
            if (i == 10) {
                lastOffset = 50;
            }
            if (i >= 10) {
                x = 150;
            }
            yOffset = (int) lastOffset + 10;
            PointF vertex = new PointF(x, yOffset);
            insertRect.set(vertex.x, vertex.y, vertex.x + defaultWidth, vertex.y + defaultWidth * Math.abs(insertRect.height() / insertRect.width()));
            lastOffset = insertRect.bottom;
            standard.setRect(insertRect);
            standard.updateAp();
        }

        //add text stamp annotations
        CPDFPage page = document.pageAtIndex(4);
        CPDFStampAnnotation stampAnnotation = (CPDFStampAnnotation) page.addAnnot(CPDFAnnotation.Type.STAMP,document);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        stampAnnotation.setTextStamp(new CPDFStampAnnotation.TextStamp(
                "ComPDFKit", date, CPDFStampAnnotation.TextStampShape.TEXTSTAMP_RECT.id,
                CPDFStampAnnotation.TextStampColor.TEXTSTAMP_GREEN.id));
        RectF insertRect = stampAnnotation.getRect();
        insertRect.set( insertRect);
        float defaultWidth = 150f;
        PointF vertex = new PointF(300, 50);
        insertRect.set(vertex.x, vertex.y, vertex.x + defaultWidth, vertex.y + defaultWidth * Math.abs(insertRect.height() / insertRect.width()));
        stampAnnotation.setRect(insertRect);
        stampAnnotation.updateAp();

        // add image stamp annotations
        CPDFStampAnnotation standard = (CPDFStampAnnotation) page.addAnnot(CPDFAnnotation.Type.STAMP,document);
        //String imagePath = rootDir + "/TestFiles/awesomeface.jpeg";
         String imagePath = rootDir + "/TestFiles/ComPDFKit.png";
        RectF imageInsertRect = new RectF(457, 626.5f, 513, 570.5f);
        standard.setRect(imageInsertRect);
        if (imagePath.endsWith("png")) {
            File imageFile = new File(imagePath);
            BufferedImage image = null;
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int[] pixel = getPixel(image);
            standard.updateApWithBitmap(pixel, image.getWidth(), image.getHeight());
        } else {
            standard.setImageStamp(imagePath);
            standard.updateAp();
        }
    }
    private static int[] getPixel(BufferedImage image){
        try {
            // 读取PNG图片文件
//            image = ImageIO.read(imageFile);

            // 将图片转换为base64字符串
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);

            // 将base64字符串转换为Image对象
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            BufferedImage decodedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);
            return pixels;
            // 进一步处理或显示Image对象
            // ...
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Samples 8 : print annotation list info
     */
    private static void printAnnotationList(String rootPath) {
        printDividingLine();
        File sampleFile = new File(rootPath + "/out/CreateAnnotationTest.pdf");
        CPDFDocument document = new CPDFDocument();
        document.open(sampleFile.getAbsolutePath());
        for (int i = 0; i < document.getPageCount(); i++) {
            CPDFPage page = document.pageAtIndex(i);
            for (CPDFAnnotation annotation : page.getAnnotations()) {
                System.out.println("Page: " + (i + 1));
                System.out.println("Annot Type: " + annotation.getType().name());
                RectF widgetRect = annotation.getRect();
                System.out.println(String.format("Position: %d, %d, %d, %d", (int) widgetRect.left, (int) widgetRect.top,
                        (int) widgetRect.right, (int) widgetRect.bottom));
                printDividingLine();
            }
        }
        document.close();
    }

    /**
     * Samples 9 : delete annotation
     */
    private static void deleteAnnotation(String rootPath) {
        File sampleFile = new File(rootPath + "/out/CreateAnnotationTest.pdf");
        CPDFDocument document = new CPDFDocument();
        document.open(sampleFile.getAbsolutePath());
        for (int i = 0; i < document.getPageCount(); i++) {
            CPDFPage page = document.pageAtIndex(i);
            List<CPDFAnnotation> annotations = page.getAnnotations();
            if (annotations != null && annotations.size() > 0) {
                page.deleteAnnotation(annotations.get(0));
                break;
            }
        }
        File file = new File(rootPath + "/out/DeleteAnnotationTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done.");
        System.out.println("Done. Results saved in DeleteAnnotationTest.pdf");
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
