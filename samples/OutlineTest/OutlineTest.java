import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFOutline;
import com.compdfkit.core.document.CPDFDestination;
import com.compdfkit.core.document.action.CPDFAction;
import java.io.File;

public class OutlineTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Outline");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/OutlineTest/").mkdirs();
        readOutline(rootDir);
        createOutline(rootDir);
        deleteOutline(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Read existing outline from PDF
     * Tests: #1 getOutlineRoot(), #18 getTitle(), #19 getParent(), #21 getChildList(), #23 getLevel(), #27 getChildAt()
     */
    private static void readOutline(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Read Outline");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFOutline root = document.getOutlineRoot();
        if (root != null) {
            CPDFOutline[] children = root.getChildList();
            if (children != null && children.length > 0) {
                System.out.println("Outline count: " + children.length);
                for (int i = 0; i < children.length; i++) {
                    CPDFOutline child = root.getChildAt(i);
                    if (child != null) {
                        System.out.println("Title: " + child.getTitle());
                        System.out.println("Level: " + child.getLevel());
                    }
                }
                CPDFOutline firstChild = children[0];
                CPDFDestination destination = firstChild.getDestination();
                System.out.println("Destination: " + (destination != null ? "pageIndex=" + destination.getPageIndex() : "null"));
                CPDFAction action = firstChild.getAction();
                System.out.println("Action: " + (action != null ? action.toString() : "null"));
                CPDFOutline parent = firstChild.getParent();
                System.out.println("Parent level: " + (parent != null ? parent.getLevel() : "null"));
            } else {
                System.out.println("No outline children found.");
            }
        } else {
            System.out.println("No outline root found.");
        }
        document.close();
        System.out.println("Done. readOutline completed.");
        printDividingLine();
    }

    /**
     * Sample 2: Create new outline in PDF
     * Tests: #2 newOutlineRoot(), #20 insertChildAtIndex(), #22 setTitle(), #24 getDestination(), #25 setDestination()
     */
    private static void createOutline(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Create Outline");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        CPDFOutline root = document.newOutlineRoot();
        if (root != null) {
            CPDFOutline child1 = root.insertChildAtIndex(0);
            if (child1 != null) {
                child1.setTitle("Chapter 1");
                CPDFDestination dest1 = new CPDFDestination(0, 0, 0, 1.0f);
                child1.setDestination(dest1);
                System.out.println("Child1 title: " + child1.getTitle());
                System.out.println("Child1 level: " + child1.getLevel());
            }
            CPDFOutline child2 = root.insertChildAtIndex(1);
            if (child2 != null) {
                child2.setTitle("Chapter 2");
                CPDFDestination dest2 = new CPDFDestination(1, 0, 0, 1.0f);
                child2.setDestination(dest2);
                System.out.println("Child2 title: " + child2.getTitle());
            }
            // Move child2 to position 0
            if (child1 != null && child2 != null) {
                root.moveOutline(child2, 0);
                CPDFOutline firstAfterMove = root.getChildAt(0);
                System.out.println("After move, first child title: " + (firstAfterMove != null ? firstAfterMove.getTitle() : "null"));
            }
        }
        File file = new File(rootDir + "/out/OutlineTest/CreateOutlineTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in CreateOutlineTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 3: Delete outline from PDF
     * Tests: #17 moveOutline(), #26 getAction(), #28 removeFromParent()
     */
    private static void deleteOutline(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Delete Outline");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/OutlineTest/CreateOutlineTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        CPDFOutline root = document.getOutlineRoot();
        if (root != null) {
            CPDFOutline[] children = root.getChildList();
            if (children != null && children.length > 0) {
                children[0].removeFromParent();
                System.out.println("Removed first child from outline.");
            }
        }
        File file = new File(rootDir + "/out/OutlineTest/DeleteOutlineTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in DeleteOutlineTest.pdf");
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
