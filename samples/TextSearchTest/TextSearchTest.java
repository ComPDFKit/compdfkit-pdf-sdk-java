import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;
import com.compdfkit.core.page.CPDFTextPage;
import com.compdfkit.core.page.CPDFTextRange;
import com.compdfkit.core.page.CPDFTextSearcher;

import java.io.File;

public class TextSearchTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Text Search");
        String rootDir = getRootDir(args);
        
        // Create output directory
        File outDir = new File(rootDir + "/out/TextSearchTest");
        outDir.mkdirs();
        
        searchText(rootDir);
        searchTextCaseSensitive(rootDir);
        printFooter();
    }

    /**
     * Sample 1 : Search text with case insensitive option
     */
    private static void searchText(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1 : Search Text (Case Insensitive)");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/text.pdf");
        
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();
        
        if (textPage == null || !textPage.isValid()) {
            System.out.println("Failed to get text page");
            return;
        }
        
        CPDFTextSearcher searcher = new CPDFTextSearcher();
        String keyword = "PDF";
        
        // Interface #12: Create CPDFTextSearcher
        System.out.println("Creating text searcher...");
        
        // Interface #13: findStart - search for keyword "PDF" case insensitive
        boolean findStartResult = searcher.findStart(textPage, keyword, 0, CPDFTextSearcher.PDFSearchOptions.PDFSearchCaseInsensitive);
        System.out.println("findStart result: " + findStartResult);
        System.out.println("Searching for keyword: \"" + keyword + "\" (case insensitive)");
        
        int count = 0;
        CPDFTextRange range;
        
        // Interface #14: findNext - loop through all matches
        while ((range = searcher.findNext()) != null) {
            count++;
            System.out.println("Match " + count + ": location=" + range.location + ", length=" + range.length);
            String matchedText = textPage.getText(range);
            System.out.println("  Text: " + matchedText);
        }
        
        System.out.println("Total matches found: " + count);
        
        // Interface #15: findClose - stop searching
        boolean findCloseResult = searcher.findClose();
        System.out.println("findClose result: " + findCloseResult);
        
        // Interface #16: close - release resources
        boolean closeResult = searcher.close();
        System.out.println("close result: " + closeResult);
        
        textPage.close();
        document.close();
        printDividingLine();
    }

    /**
     * Sample 2 : Search text with case sensitive option
     */
    private static void searchTextCaseSensitive(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2 : Search Text (Case Sensitive)");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/text.pdf");
        
        CPDFPage page = document.pageAtIndex(0);
        CPDFTextPage textPage = page.getTextPage();
        
        if (textPage == null || !textPage.isValid()) {
            System.out.println("Failed to get text page");
            return;
        }
        
        CPDFTextSearcher searcher = new CPDFTextSearcher();
        String keyword = "PDF";
        
        System.out.println("Creating text searcher...");
        
        // findStart with case sensitive option
        boolean findStartResult = searcher.findStart(textPage, keyword, 0, CPDFTextSearcher.PDFSearchOptions.PDFSearchCaseSensitive);
        System.out.println("findStart result: " + findStartResult);
        System.out.println("Searching for keyword: \"" + keyword + "\" (case sensitive)");
        
        int count = 0;
        CPDFTextRange range;
        
        // findNext - loop through all matches
        while ((range = searcher.findNext()) != null) {
            count++;
            System.out.println("Match " + count + ": location=" + range.location + ", length=" + range.length);
            String matchedText = textPage.getText(range);
            System.out.println("  Text: " + matchedText);
        }
        
        System.out.println("Total matches found: " + count);
        
        // findClose - stop searching
        boolean findCloseResult = searcher.findClose();
        System.out.println("findClose result: " + findCloseResult);
        
        // close - release resources
        boolean closeResult = searcher.close();
        System.out.println("close result: " + closeResult);
        
        textPage.close();
        document.close();
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
