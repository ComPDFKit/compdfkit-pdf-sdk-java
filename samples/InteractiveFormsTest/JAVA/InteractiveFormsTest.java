import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.*;
import com.compdfkit.core.annotation.form.*;
import com.compdfkit.core.document.CPDFDestination;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.document.action.CPDFGoToAction;
import com.compdfkit.core.document.action.CPDFUriAction;
import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;
import com.compdfkit.core.page.CPDFPage;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class InteractiveFormsTest {
    static{
        try {
            NativeKMPDFKit.initialize(null, Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args)  {
        printHead("InteractiveForms");
        String rootDir = getRootDir(args);
        CPDFDocument document = CPDFDocument.createDocument();
        printDividingLine();
        // Create a blank new page and add some form fields.
        document.insertBlankPage(0, 595, 842);
        document.insertBlankPage(1, 595, 842);

        //---------------------------------
        //Samples 1 : Programmatically create new Form Fields and Widget Annotations.
        createTestForms(document);
        //---------------------------------
        //Samples 2 : Traverse all form fields in the document (and print out their names).
        printFormsMessage(document,rootDir);

        File file = new File(rootDir + "/out/Create_From_Test.pdf");
        document.save(file.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        deleteForm(rootDir);
        printFooter();
     }


    private static void createTestForms(CPDFDocument document) {
        // create new Form Fields and Widget Annotations.
        int pageNumber = 0;
        RectF pageSize = document.getPageSize(pageNumber);
        CPDFPage cpdfPage = document.pageAtIndex(pageNumber);

        //Insert a single-line TextField.
        RectF singleLineTextRect = new RectF(28, 32, 237, 75);
        CPDFTextWidget singleLineTextWidget = (CPDFTextWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_TextField);
        singleLineTextWidget.setRect(singleLineTextRect);
        singleLineTextWidget.setFieldName("TextField1");
        singleLineTextWidget.setText("Basic Text Field");
        singleLineTextWidget.setFontColor(Color.BLACK.getRGB());
        singleLineTextWidget.setFontSize(15);
        singleLineTextWidget.updateAp();
        //Insert a multiline TextField.
        RectF multilineTextRect = new RectF(28, 97, 237, 189);
        CPDFTextWidget multiLineTextWidget = (CPDFTextWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_TextField);
        multiLineTextWidget.setRect(multilineTextRect);
        multiLineTextWidget.setFieldName("TextField2");
        multiLineTextWidget.setText("Basic Text Field\nBasic Text Field\nBasic Text Field");
        multiLineTextWidget.setMultiLine(true);
        multiLineTextWidget.setFontColor(Color.BLACK.getRGB());
        multiLineTextWidget.setFontSize(15);
        multiLineTextWidget.updateAp();

        //Insert a ListBox widget.
        RectF listBoxRect = new RectF(267, 32, 567, 138);
        CPDFListboxWidget listBoxWidget = (CPDFListboxWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_ListBox);
        listBoxWidget.setRect(listBoxRect);
        listBoxWidget.setFieldName("ListBox1");
        CPDFWidgetItem[] listBoxItems = new CPDFWidgetItem[]{
                new CPDFWidgetItem("List Box No.1", "List Box No.1"),
                new CPDFWidgetItem("List Box No.2", "List Box No.2"),
                new CPDFWidgetItem("List Box No.3", "List Box No.3"),
        };
        listBoxWidget.setOptionItems(listBoxItems);
        listBoxWidget.setSelectedIndexes(new int[]{1});
        listBoxWidget.updateAp();

        //Insert a ComboBox Widget.
        RectF comboBoxRect = new RectF(267, 143, 567, 189);
        CPDFComboboxWidget comboBoxWidget = (CPDFComboboxWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_ComboBox);
        comboBoxWidget.setRect(comboBoxRect);
        comboBoxWidget.setFieldName("ComboBox1");
        CPDFWidgetItem[] comboBoxItems = new CPDFWidgetItem[]{
                new CPDFWidgetItem("Combo Box No.1", "Combo Box No.1"),
                new CPDFWidgetItem("Combo Box No.2", "Combo Box No.2"),
                new CPDFWidgetItem("Combo Box No.3", "Combo Box No.3"),
        };
        comboBoxWidget.setOptionItems(comboBoxItems, new int[]{1});
        comboBoxWidget.updateAp();

        //Insert a Form Signature Widget (unsigned)
        RectF signatureRect = new RectF(28, 206, 237, 301);
        CPDFSignatureWidget signatureWidget = (CPDFSignatureWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_SignatureFields);
        signatureWidget.setFieldName("Signature1");
        signatureWidget.setRect(signatureRect);
        signatureWidget.updateAp();

        //Insert a PushButton to jump to a page.
        RectF pushButton1Rect = new RectF(267, 203, 401, 235);
        CPDFPushbuttonWidget pushButtonWidget1 = (CPDFPushbuttonWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_PushButton);
        pushButtonWidget1.setRect(pushButton1Rect);
        pushButtonWidget1.setFieldName("PushButton1");
        pushButtonWidget1.setFontColor(Color.BLACK.getRGB());
        pushButtonWidget1.setFontSize(15);
        pushButtonWidget1.setButtonTitle("PushButton");
        //set PushButton jump to a page action
        CPDFGoToAction goToAction = new CPDFGoToAction();
        CPDFDestination destination = new CPDFDestination(1, 0, 842, 0F);
        goToAction.setDestination(document, destination);
        pushButtonWidget1.setButtonAction(goToAction);
        pushButtonWidget1.updateAp();

        //Insert a PushButton to jump to a website.
        RectF pushButton2Rect = new RectF(433, 203, 567, 235);
        CPDFPushbuttonWidget pushButtonWidget2 = (CPDFPushbuttonWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_PushButton);
        pushButtonWidget2.setRect(pushButton2Rect);
        pushButtonWidget2.setFieldName("PushButton2");
        pushButtonWidget2.setFontColor(Color.BLACK.getRGB());
        pushButtonWidget2.setFontSize(15);
        pushButtonWidget2.setButtonTitle("PushButton");
        //set PushButton jump to a website
        CPDFUriAction uriAction = new CPDFUriAction();
        uriAction.setUri("https://www.compdf.com/");
        pushButtonWidget2.setButtonAction(uriAction);
        pushButtonWidget2.updateAp();

        //Insert CheckBox Widget
        RectF checkBox1 = new RectF(339.97F,410.91F,360.97F,390.91F);
        CPDFCheckboxWidget checkboxWidget = (CPDFCheckboxWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_CheckBox);
        checkboxWidget.setRect(checkBox1);
        checkboxWidget.setFieldName("CheckBox1");
        checkboxWidget.setFillColor(Color.decode("#CCE5FF").getRGB());
        checkboxWidget.setBorderColor(Color.BLACK.getRGB());
        checkboxWidget.setBorderWidth(2F);
        checkboxWidget.setChecked(false);
        checkboxWidget.updateAp();

        RectF checkBox2 = new RectF(379.97F,410.91F,399.97F,390.91F);
        CPDFCheckboxWidget checkboxWidget2 = (CPDFCheckboxWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_CheckBox);
        checkboxWidget2.setRect(checkBox2);
        checkboxWidget2.setFieldName("CheckBox1");
        checkboxWidget2.setFillColor(Color.decode("#CCE5FF").getRGB());
        checkboxWidget2.setBorderColor(Color.BLACK.getRGB());
        checkboxWidget2.setBorderWidth(2F);
        checkboxWidget2.setChecked(true);
        checkboxWidget2.updateAp();

        //Insert RadioButton Widget
        RectF radioButton1 = new RectF(338.97F,445.91F,360.97F,424.91F);
        CPDFRadiobuttonWidget radiobuttonWidget1 = (CPDFRadiobuttonWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_RadioButton);
        radiobuttonWidget1.setRect(radioButton1);
        radiobuttonWidget1.setCheckStyle(CPDFWidget.CheckStyle.CK_Circle);
        radiobuttonWidget1.setFillColor(Color.decode("#DDE9FF").getRGB());
        radiobuttonWidget1.setBorderColor(Color.decode("#43474D").getRGB());
        radiobuttonWidget1.setBorderWidth(2F);
        radiobuttonWidget1.setChecked(false);
        radiobuttonWidget1.setFieldName("RadioButton1");
        radiobuttonWidget1.updateAp();
        RectF radioButton2 = new RectF(377.9765625F,445.91F,399.9708F,424.91F);
        CPDFRadiobuttonWidget radiobuttonWidget2 = (CPDFRadiobuttonWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_RadioButton);
        radiobuttonWidget2.setRect(radioButton2);
        radiobuttonWidget2.setCheckStyle(CPDFWidget.CheckStyle.CK_Circle);
        radiobuttonWidget2.setFillColor(Color.decode("#CCE5FF").getRGB());
        radiobuttonWidget2.setBorderColor(Color.BLACK.getRGB());
        radiobuttonWidget2.setBorderWidth(2F);
        // Check the widget (by default it is unchecked).
        radiobuttonWidget2.setChecked(true);
        radiobuttonWidget2.setFieldName("RadioButton1");
        radiobuttonWidget2.updateAp();
        RectF radioButton3 = new RectF(413.97F,445.91F,437.97F,424.91F);
        CPDFRadiobuttonWidget radiobuttonWidget3 = (CPDFRadiobuttonWidget) cpdfPage.addFormWidget(CPDFWidget.WidgetType.Widget_RadioButton);
        radiobuttonWidget3.setRect(radioButton3);
        radiobuttonWidget3.setCheckStyle(CPDFWidget.CheckStyle.CK_Circle);
        radiobuttonWidget3.setFillColor(Color.decode("#CCE5FF").getRGB());
        radiobuttonWidget3.setBorderColor(Color.BLACK.getRGB());
        radiobuttonWidget3.setBorderWidth(2F);
        radiobuttonWidget3.setChecked(false);
        radiobuttonWidget3.setFieldName("RadioButton1");
        radiobuttonWidget3.updateAp();
        System.out.println("Done.");
        System.out.println("Done. Result saved in Create_Form_Test.pdf");
        printDividingLine();
    }


    private static void deleteForm(String rootDir){
        File file = new File(rootDir + "/out/Create_From_Test.pdf");
        CPDFDocument document = new CPDFDocument();
        document.open(file.getAbsolutePath());
        CPDFPage page = document.pageAtIndex(0);
        for (CPDFAnnotation annotation : page.getAnnotations()) {
            if (annotation.getType() == CPDFAnnotation.Type.WIDGET){
                page.deleteAnnotation(annotation);
                break;
            }
        }
        File resultsForms = new File(rootDir + "/out/Delete_Form_Test.pdf");
        document.save(resultsForms.getAbsolutePath(), CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        System.out.println("Done.");
        System.out.println("Done. Result saved in Delete_Form_Test.pdf");
    }

    private static void printFormsMessage(CPDFDocument document, String rootDir) {
//        for (int i = 0; i < document.getPageCount(); i++) {
//            CPDFPage page = document.pageAtIndex(i);
//            for (CPDFAnnotation annotation : page.getAnnotations()) {
//                switch (annotation.getType()) {
//                    case WIDGET:
//                        CPDFWidget cpdfWidget = (CPDFWidget) annotation;
//                        System.out.println("Field name : " + cpdfWidget.getFieldName());
//                        switch (cpdfWidget.getWidgetType()) {
//                            case Widget_TextField:
//                                System.out.println("Field partial name : " + ((CPDFTextWidget) cpdfWidget).getText());
//                                break;
//                            case Widget_ListBox:
//                                CPDFListboxWidget listBoxWidget = (CPDFListboxWidget) cpdfWidget;
//                                CPDFWidgetItem[] options = listBoxWidget.getOptions();
//                                int[] selectedIndexs = listBoxWidget.getSelectedIndexes();
//                                if (options != null && selectedIndexs != null) {
//                                    CPDFWidgetItem selectItem = options[selectedIndexs[0]];
//                                    System.out.println("Field Select Item : " + selectItem.text);
//                                }
//                                break;
//                            case Widget_ComboBox:
//                                CPDFComboboxWidget comboBoxWidget = (CPDFComboboxWidget) cpdfWidget;
//                                CPDFWidgetItem[] comboBoxOptions = comboBoxWidget.getOptions();
//                                int[] selectedIndexs1 = comboBoxWidget.getSelectedIndexes();
//                                if (comboBoxOptions != null && selectedIndexs1 != null) {
//                                    CPDFWidgetItem selectItem = comboBoxOptions[selectedIndexs1[0]];
//                                    System.out.println("Field Select Item : " + selectItem.text);
//                                }
//                                break;
//                            case Widget_SignatureFields:
//                                CPDFSignatureWidget signatureWidget = (CPDFSignatureWidget) cpdfWidget;
//                                System.out.println("Field isSigned : " + signatureWidget.isSigned());
//                                break;
//                            case Widget_CheckBox:
//                                System.out.println("Field isChecked : " + ((CPDFCheckboxWidget) cpdfWidget).isChecked());
//                                break;
//                            case Widget_RadioButton:
//                                System.out.println("Field isChecked : " + ((CPDFRadiobuttonWidget) cpdfWidget).isChecked());
//                                break;
//                            case Widget_PushButton:
//                                CPDFPushbuttonWidget pushButtonWidget = (CPDFPushbuttonWidget) cpdfWidget;
//                                System.out.println("Field PushButton Title : " + pushButtonWidget.getButtonTitle());
//                                CPDFAction cpdfAction = pushButtonWidget.getButtonAction();
//                                if (cpdfAction != null) {
//                                    if (cpdfAction instanceof CPDFUriAction) {
//                                        System.out.println("Field PushButton Action : " + ((CPDFUriAction) cpdfAction).getUri());
//                                    } else if (cpdfAction instanceof CPDFGoToAction) {
//                                        System.out.println("Field PushButton Action : Jump to page " + (((CPDFGoToAction) cpdfAction).getDestination(document).getPageIndex() + 1) + " of the document");
//                                    }
//                                }
//                                break;
//                            default:
//                                break;
//                        }
//                        RectF widgetRect = cpdfWidget.getRect();
//                        RectF position = page.convertRectFromPage(false, document.getPageSize(i).width(),
//                                document.getPageSize(i).height(),widgetRect);
//                        System.out.println(String.format("Field Position : %d, %d, %d, %d", (int)position.left, (int)position.top, (int)position.right, (int)position.bottom));
//                        System.out.println("Widget type : " + cpdfWidget.getWidgetType().name());
//                        printDividingLine();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
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
