import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.CPDFAnnotation;
import com.compdfkit.core.annotation.CPDFLinkAnnotation;
import com.compdfkit.core.annotation.form.CPDFPushbuttonWidget;
import com.compdfkit.core.annotation.form.CPDFTextWidget;
import com.compdfkit.core.annotation.form.CPDFWidget;
import com.compdfkit.core.document.CPDFDestination;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFOutline;
import com.compdfkit.core.document.action.CPDFAction;
import com.compdfkit.core.document.action.CPDFGoToAction;
import com.compdfkit.core.document.action.CPDFGoToRAction;
import com.compdfkit.core.document.action.CPDFJSAction;
import com.compdfkit.core.document.action.CPDFLaunchAction;
import com.compdfkit.core.document.action.CPDFNamedAction;
import com.compdfkit.core.document.action.CPDFResetFormAction;
import com.compdfkit.core.document.action.CPDFSubmitFormAction;
import com.compdfkit.core.document.action.CPDFUriAction;
import com.compdfkit.core.page.CPDFPage;

import java.awt.Color;
import java.io.File;

/**
 * Action samples: attach every supported action type to link annotations,
 * push buttons and outline items, then reopen the file and read them back.
 * Rectangles use PDF page coordinates (origin bottom-left, Y axis up, top above bottom).
 */
public class ActionTest {
    private static final String OUTPUT_DIR = "/out/ActionTest";
    private static final String WEBSITE = "https://www.compdf.com/";

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Action");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();
        String pdfPath = createActions(rootDir);
        readActions(pdfPath);
        printFooter();
    }

    /** Sample 1: Create links, buttons and outlines with different action types. */
    private static String createActions(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Create actions on links, push buttons and outlines");
        CPDFDocument document = CPDFDocument.createDocument();
        for (int i = 0; i < 3; i++) {
            document.insertBlankPage(i, 595, 842);
        }
        CPDFPage page = document.pageAtIndex(0);

        CPDFGoToAction goTo = new CPDFGoToAction();
        goTo.setDestination(document, new CPDFDestination(2, 0, 842, 1.0f));
        addLink(page, document, new RectF(40, 800, 280, 770), goTo, "Link GoTo page 3");

        CPDFGoToRAction goToRemote = new CPDFGoToRAction();
        goToRemote.setFileSpec("CommonFivePage.pdf");
        System.out.println("GoToR file spec after setFileSpec: " + goToRemote.getFileSpec());
        boolean remoteDestinationSet = goToRemote.setDestination(document, new CPDFDestination(1, 0, 842, 1.0f));
        System.out.println("GoToR setDestination: " + remoteDestinationSet
                + " (the current core accepts destinations on GoTo actions only)");
        System.out.println("GoToR file spec after setDestination: " + goToRemote.getFileSpec()
                + ", type: " + goToRemote.getActionType());
        addLink(page, document, new RectF(40, 760, 280, 730), goToRemote, "Link GoToR");

        CPDFLaunchAction launch = new CPDFLaunchAction();
        launch.setFileSpec("ComPDFKit.png");
        addLink(page, document, new RectF(40, 720, 280, 690), launch, "Link Launch " + launch.getFileSpec());

        CPDFNamedAction named = new CPDFNamedAction();
        named.setNamedAction("NextPage");
        addLink(page, document, new RectF(40, 680, 280, 650), named, "Link Named " + named.getNamedAction());

        CPDFUriAction uri = new CPDFUriAction();
        uri.setUri(WEBSITE);
        addLink(page, document, new RectF(40, 640, 280, 610), uri, "Link URI " + uri.getUri());

        // A link that uses a destination directly instead of an action.
        CPDFLinkAnnotation destinationLink = (CPDFLinkAnnotation) page.addAnnot(CPDFAnnotation.Type.LINK, document);
        destinationLink.setRect(new RectF(40, 600, 280, 570));
        CPDFDestination directDestination = new CPDFDestination(1, 100, 700, 1.5f);
        System.out.println("Link destination only -> setDestination: " + destinationLink.setDestination(document, directDestination)
                + ", read back: " + describe(destinationLink.getDestination(document)));
        destinationLink.updateAp();

        // A text field so the reset action has something to clear.
        CPDFTextWidget name = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        name.setRect(new RectF(320, 800, 560, 770));
        name.setFieldName("Name");
        name.setText("ComPDFKit");
        name.updateAp();

        addButton(page, document, new RectF(320, 760, 560, 730), "Reset", new CPDFResetFormAction());
        addButton(page, document, new RectF(320, 720, 560, 690), "Submit", new CPDFSubmitFormAction());
        CPDFJSAction script = new CPDFJSAction();
        System.out.println("JavaScript action created; getJavaScript() before saving: \"" + script.getJavaScript() + "\"");
        addButton(page, document, new RectF(320, 680, 560, 650), "Script", script);

        // Outline items with an action, a destination and a nested child.
        CPDFOutline root = document.newOutlineRoot();
        CPDFOutline website = root.insertChildAtIndex(0);
        website.setTitle("Open website");
        CPDFUriAction outlineUri = new CPDFUriAction();
        outlineUri.setUri(WEBSITE);
        System.out.println("Outline \"" + website.getTitle() + "\" setAction: " + website.setAction(outlineUri));
        website.setTag("website");
        System.out.println("Outline tag (Java-side only): " + website.getTag());

        CPDFOutline chapter = root.insertChildAtIndex(1);
        chapter.setTitle("Go to page 3");
        System.out.println("Outline \"" + chapter.getTitle() + "\" setDestination: "
                + chapter.setDestination(new CPDFDestination(2, 0, 842, 1.0f)));
        CPDFOutline nested = chapter.insertChildAtIndex(0);
        nested.setTitle("Nested item");
        System.out.println("Outline \"" + nested.getTitle() + "\" moveTo(root, 0): " + nested.moveTo(root, 0));
        StringBuilder order = new StringBuilder();
        for (CPDFOutline child : root.getChildList()) {
            order.append(child.getTitle()).append(" (level ").append(child.getLevel()).append("); ");
        }
        System.out.println("Root outline order: " + order);

        String outputPath = rootDir + OUTPUT_DIR + "/Actions.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in Actions.pdf");
        printDividingLine();
        return outputPath;
    }

    /** Sample 2: Reopen the file and print the action attached to every object. */
    private static void readActions(String pdfPath) {
        System.out.println("Sample 2: Read actions from the saved file");
        CPDFDocument document = new CPDFDocument();
        document.open(pdfPath);
        CPDFPage page = document.pageAtIndex(0);
        for (CPDFAnnotation annotation : page.getAnnotations()) {
            if (annotation instanceof CPDFLinkAnnotation) {
                CPDFLinkAnnotation link = (CPDFLinkAnnotation) annotation;
                System.out.println("Link at " + link.getRect() + ": " + describe(link.getLinkAction(), document)
                        + "; destination=" + describe(link.getDestination(document)));
            } else if (annotation instanceof CPDFPushbuttonWidget) {
                CPDFPushbuttonWidget button = (CPDFPushbuttonWidget) annotation;
                System.out.println("Button \"" + button.getButtonTitle() + "\": " + describe(button.getButtonAction(), document));
            }
        }
        CPDFOutline root = document.getOutlineRoot();
        if (root != null && root.getChildList() != null) {
            for (CPDFOutline child : root.getChildList()) {
                System.out.println("Outline \"" + child.getTitle() + "\" level " + child.getLevel()
                        + ": " + describe(child.getAction(), document)
                        + "; destination=" + describe(child.getDestination())
                        + "; children=" + (child.getChildList() == null ? 0 : child.getChildList().length));
            }
        }
        document.close();
        printDividingLine();
    }

    private static void addLink(CPDFPage page, CPDFDocument document, RectF rect, CPDFAction action, String label) {
        CPDFLinkAnnotation link = (CPDFLinkAnnotation) page.addAnnot(CPDFAnnotation.Type.LINK, document);
        link.setRect(rect);
        boolean set = link.setLinkAction(action);
        link.updateAp();
        System.out.println(label + " -> setLinkAction: " + set + ", type=" + action.getActionType());
    }

    private static void addButton(CPDFPage page, CPDFDocument document, RectF rect, String title, CPDFAction action) {
        CPDFPushbuttonWidget button = (CPDFPushbuttonWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_PushButton, document);
        button.setRect(rect);
        button.setFieldName(title + "Button");
        button.setButtonTitle(title);
        button.setFontColor(Color.BLACK.getRGB());
        button.setFontSize(12);
        boolean set = button.setButtonAction(action);
        button.updateAp();
        System.out.println("Button " + title + " -> setButtonAction: " + set + ", type=" + action.getActionType());
    }

    private static String describe(CPDFAction action, CPDFDocument document) {
        if (action == null) {
            return "no action";
        }
        StringBuilder text = new StringBuilder(action.getActionType() + " (" + action.getClass().getSimpleName() + ")");
        if (action instanceof CPDFGoToAction) {
            text.append(" destination=").append(describe(((CPDFGoToAction) action).getDestination(document)));
        } else if (action instanceof CPDFGoToRAction) {
            CPDFGoToRAction remote = (CPDFGoToRAction) action;
            text.append(" file=").append(remote.getFileSpec())
                    .append(" destination=").append(describe(remote.getDestination(document)));
        } else if (action instanceof CPDFLaunchAction) {
            text.append(" file=").append(((CPDFLaunchAction) action).getFileSpec());
        } else if (action instanceof CPDFNamedAction) {
            text.append(" name=").append(((CPDFNamedAction) action).getNamedAction());
        } else if (action instanceof CPDFUriAction) {
            text.append(" uri=").append(((CPDFUriAction) action).getUri());
        } else if (action instanceof CPDFJSAction) {
            text.append(" script=\"").append(((CPDFJSAction) action).getJavaScript()).append("\"");
        } else if (action instanceof CPDFResetFormAction) {
            text.append(" (reset form)");
        } else if (action instanceof CPDFSubmitFormAction) {
            text.append(" (submit form)");
        }
        return text.toString();
    }

    private static String describe(CPDFDestination destination) {
        if (destination == null) {
            return "none";
        }
        return "page " + destination.getPageIndex() + " at (" + destination.getPosition_x() + ", "
                + destination.getPosition_y() + ") zoom " + destination.getZoom();
    }

    private static void printHead(String name) {
        System.out.println(String.format("Running %s sample", name));
    }

    private static void printFooter() {
        System.out.println("Done!");
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
