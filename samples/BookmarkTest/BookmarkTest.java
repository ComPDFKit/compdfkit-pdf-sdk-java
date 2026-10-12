import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.CPDFBookmark;

import java.io.File;
import java.util.*;

public class BookmarkTest {
    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("Bookmark");
        String rootDir = getRootDir(args);
        new File(rootDir + "/out/BookmarkTest/").mkdirs();
        addBookmarks(rootDir);
        readBookmarks(rootDir);
        editBookmarks(rootDir);
        deleteBookmarks(rootDir);
        printFooter();
    }

    /**
     * Sample 1: Add bookmarks to PDF
     */
    private static void addBookmarks(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Add Bookmarks");
        CPDFDocument document = new CPDFDocument();
        document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        boolean result1 = document.addBookmark(new CPDFBookmark(0, "First Page Bookmark", "2025-02-27"));
        System.out.println("Add bookmark at page 0: " + result1);
        boolean result2 = document.addBookmark(new CPDFBookmark(2, "Third Page Bookmark", "2025-02-27"));
        System.out.println("Add bookmark at page 2: " + result2);
        File file = new File(rootDir + "/out/BookmarkTest/AddBookmarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in AddBookmarkTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 2: Read bookmarks from PDF
     */
    private static void readBookmarks(String rootDir) {
        printDividingLine();
        System.out.println("Sample 2: Read Bookmarks");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BookmarkTest/AddBookmarkTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        List<CPDFBookmark> bookmarks = document.getBookmarks();
        if (bookmarks != null) {
            System.out.println("Bookmark count: " + bookmarks.size());
            for (CPDFBookmark bookmark : bookmarks) {
                System.out.println("PageIndex: " + bookmark.getPageIndex());
                System.out.println("Title: " + bookmark.getTitle());
                System.out.println("Date: " + bookmark.getDate());
            }
        } else {
            System.out.println("No bookmarks found.");
        }
        document.close();
        System.out.println("Done. readBookmarks completed.");
        printDividingLine();
    }

    /**
     * Sample 3: Edit bookmarks in PDF
     */
    private static void editBookmarks(String rootDir) {
        printDividingLine();
        System.out.println("Sample 3: Edit Bookmarks");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BookmarkTest/AddBookmarkTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        List<CPDFBookmark> bookmarks = document.getBookmarks();
        if (bookmarks != null && bookmarks.size() > 0) {
            CPDFBookmark first = bookmarks.get(0);
            first.setPageIndex(1);
            first.setTitle("Updated Bookmark Title");
            first.setDate("2025-03-01");
            System.out.println("Updated pageIndex: " + first.getPageIndex());
            System.out.println("Updated title: " + first.getTitle());
            System.out.println("Updated date: " + first.getDate());
        }
        File file = new File(rootDir + "/out/BookmarkTest/EditBookmarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in EditBookmarkTest.pdf");
        printDividingLine();
    }

    /**
     * Sample 4: Delete bookmark from PDF
     */
    private static void deleteBookmarks(String rootDir) {
        printDividingLine();
        System.out.println("Sample 4: Delete Bookmarks");
        CPDFDocument document = new CPDFDocument();
        File sourceFile = new File(rootDir + "/out/BookmarkTest/EditBookmarkTest.pdf");
        if (sourceFile.exists()) {
            document.open(sourceFile.getAbsolutePath());
        } else {
            document.open(rootDir + "/TestFiles/CommonFivePage.pdf");
        }
        boolean result = document.removeBookmark(0);
        System.out.println("Remove bookmark at page 0: " + result);
        File file = new File(rootDir + "/out/BookmarkTest/DeleteBookmarkTest.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in DeleteBookmarkTest.pdf");
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
