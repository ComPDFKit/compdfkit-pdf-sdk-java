import com.compdfkit.core.android.graphics.PointF;
import com.compdfkit.core.android.graphics.RectF;
import com.compdfkit.core.annotation.CPDFAnnotation;
import com.compdfkit.core.annotation.CPDFImageScaleType;
import com.compdfkit.core.annotation.CPDFTextAlignment;
import com.compdfkit.core.annotation.CPDFTextAttribute;
import com.compdfkit.core.annotation.form.CPDFCheckboxWidget;
import com.compdfkit.core.annotation.form.CPDFComboboxWidget;
import com.compdfkit.core.annotation.form.CPDFListboxWidget;
import com.compdfkit.core.annotation.form.CPDFPushbuttonWidget;
import com.compdfkit.core.annotation.form.CPDFRadiobuttonWidget;
import com.compdfkit.core.annotation.form.CPDFSignatureWidget;
import com.compdfkit.core.annotation.form.CPDFTextWidget;
import com.compdfkit.core.annotation.form.CPDFWidget;
import com.compdfkit.core.annotation.form.CPDFWidgetItem;
import com.compdfkit.core.annotation.form.CPDFWidgetItems;
import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.page.CPDFPage;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Form field property samples: field flags, appearance colors and borders,
 * text field modes, check styles, choice items, signature field appearances,
 * reading the properties back from a saved file and resetting fields.
 * Rectangles use PDF page coordinates (origin bottom-left, Y axis up, top above bottom).
 */
public class FormFieldPropertiesTest {
    private static final String OUTPUT_DIR = "/out/FormFieldPropertiesTest";
    private static final String SIGNATURE_IMAGE = "/TestFiles/ComPDFKit.jpg";

    /** Field flag bits from the PDF specification (/Ff entry). */
    private static final int FIELD_FLAG_READ_ONLY = 1;
    private static final int FIELD_FLAG_REQUIRED = 1 << 1;
    private static final int FIELD_FLAG_PASSWORD = 1 << 13;

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) {
        printHead("FormFieldProperties");
        String rootDir = getRootDir(args);
        new File(rootDir + OUTPUT_DIR).mkdirs();
        String propertiesPdf = createAndConfigureFields(rootDir);
        readBackProperties(propertiesPdf);
        resetFields(rootDir, propertiesPdf);
        printFooter();
    }

    /** Sample 1: Create every widget type and configure its properties. */
    private static String createAndConfigureFields(String rootDir) {
        printDividingLine();
        System.out.println("Sample 1: Create widgets and configure field properties");
        CPDFDocument document = CPDFDocument.createDocument();
        document.insertBlankPage(0, 595, 842);
        CPDFPage page = document.pageAtIndex(0);

        // Text field with appearance, alignment and the Required flag.
        CPDFTextWidget name = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        name.setRect(new RectF(40, 800, 300, 765));
        name.setFieldName("Name");
        name.setText("ComPDFKit");
        name.setFontName("Helvetica");
        name.setFontSize(14);
        name.setFontColor(Color.BLUE.getRGB());
        name.setTextAlignment(CPDFTextAlignment.ALIGNMENT_CENTER);
        name.setFillColor(Color.decode("#E8F0FE").getRGB());
        name.setBorderColor(Color.BLACK.getRGB());
        name.setBorderWidth(1.5f);
        name.setBorderStyles(CPDFWidget.BorderStyle.BS_Dashded);
        name.setFillAlpha(0.8f);
        name.setFieldFlag(name.getFieldFlag() | FIELD_FLAG_REQUIRED);
        name.updateAp();
        System.out.println("Name: flags=" + name.getFieldFlag() + " alignment=" + name.getTextAlignment()
                + " border=" + name.getBorderStyles() + "/" + name.getBorderWidth()
                + " fill=" + hex(name.getFillColor()) + " alpha=" + name.getFillAlpha()
                + " borderColor=" + hex(name.getBorderColor()) + " fontColor=" + hex(name.getFontColor()));

        // Multiline text field.
        CPDFTextWidget notes = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        notes.setRect(new RectF(40, 750, 300, 670));
        notes.setFieldName("Notes");
        notes.setMultiLine(true);
        notes.setText("First line\nSecond line");
        notes.setTextFieldSpecial(false);
        notes.updateAp();
        System.out.println("Notes: isMultiLine=" + notes.isMultiLine() + " special=" + notes.getTextFieldSpecial());

        // Password field via the field flag.
        CPDFTextWidget secret = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        secret.setRect(new RectF(320, 800, 560, 765));
        secret.setFieldName("Secret");
        secret.setFieldFlag(secret.getFieldFlag() | FIELD_FLAG_PASSWORD);
        secret.setText("hidden");
        secret.updateAp();
        System.out.println("Secret: isPassword=" + secret.isPassword() + " flags=" + secret.getFieldFlag());

        // Date formatted text field.
        CPDFTextWidget date = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        date.setRect(new RectF(320, 750, 560, 715));
        date.setFieldName("Date");
        date.setTextFieldSpecial(true);
        date.setText("2026-09-29");
        date.updateAp();
        System.out.println("Date: special=" + date.getTextFieldSpecial() + " fieldType=" + date.getFieldType());

        // Read-only handling: compare setReadOnly() with the field flag.
        CPDFTextWidget locked = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        locked.setRect(new RectF(320, 705, 560, 670));
        locked.setFieldName("Locked");
        locked.setText("read only");
        System.out.println("Locked: setReadOnly(true)=" + locked.setReadOnly(true) + " -> isReadOnly()=" + locked.isReadOnly()
                + " annotationFlags=" + locked.getFlags() + " fieldFlags=" + locked.getFieldFlag());
        locked.setFieldFlag(locked.getFieldFlag() | FIELD_FLAG_READ_ONLY);
        System.out.println("Locked: setFieldFlag(READ_ONLY) -> isReadOnly()=" + locked.isReadOnly()
                + " fieldFlags=" + locked.getFieldFlag());
        locked.updateAp();

        // Check box with circle border, cross mark and custom check color.
        CPDFCheckboxWidget agree = (CPDFCheckboxWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_CheckBox, document);
        agree.setRect(new RectF(40, 650, 60, 630));
        agree.setFieldName("Agree");
        agree.setCheckBoxStyle(CPDFCheckboxWidget.CheckboxStyle.CHECKBOX_CIRCLE);
        agree.setCheckStyle(CPDFWidget.CheckStyle.CK_Cross);
        agree.setColor(Color.RED.getRGB());
        agree.setBorderColor(Color.BLACK.getRGB());
        agree.setBorderWidth(1);
        agree.setChecked(true);
        agree.updateAp();
        System.out.println("Agree: checkBoxStyle=" + agree.getCheckBoxStyle() + " checkStyle=" + agree.getCheckStyle()
                + " checkColor=" + hex(agree.getColor()) + " checked=" + agree.isChecked());

        // Radio buttons sharing one field name.
        for (int i = 0; i < 2; i++) {
            CPDFRadiobuttonWidget choice = (CPDFRadiobuttonWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_RadioButton, document);
            choice.setRect(new RectF(80 + i * 30, 650, 100 + i * 30, 630));
            choice.setFieldName("Choice");
            choice.setCheckStyle(CPDFWidget.CheckStyle.CK_Star);
            choice.setColor(Color.decode("#1B8A3A").getRGB());
            choice.setBorderColor(Color.DARK_GRAY.getRGB());
            choice.setChecked(i == 1);
            choice.updateAp();
            System.out.println("Choice " + i + ": checkStyle=" + choice.getCheckStyle() + " color=" + hex(choice.getColor())
                    + " checked=" + choice.isChecked());
        }

        // Push button with font properties and beveled border.
        CPDFPushbuttonWidget submit = (CPDFPushbuttonWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_PushButton, document);
        submit.setRect(new RectF(40, 600, 200, 570));
        submit.setFieldName("Submit");
        submit.setButtonTitle("Submit");
        submit.setFontName("Helvetica-Bold");
        submit.setFontSize(12);
        submit.setFontColor(Color.WHITE.getRGB());
        submit.setFillColor(Color.decode("#1A73E8").getRGB());
        submit.setBorderStyles(CPDFWidget.BorderStyle.BS_Beveled);
        submit.updateAp();
        System.out.println("Submit: title=" + submit.getButtonTitle() + " font=" + submit.getFontName()
                + "/" + submit.getFontSize() + " fontColor=" + hex(submit.getFontColor()) + " border=" + submit.getBorderStyles());

        // Combo box with options and text properties.
        CPDFComboboxWidget country = (CPDFComboboxWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_ComboBox, document);
        country.setRect(new RectF(320, 650, 560, 620));
        country.setFieldName("Country");
        country.setTextProperty(new CPDFTextAttribute("Helvetica", 12, Color.BLACK.getRGB()));
        CPDFWidgetItem[] countries = {
                new CPDFWidgetItem("CN", "China"),
                new CPDFWidgetItem("US", "United States"),
                new CPDFWidgetItem("JP", "Japan"),
        };
        country.setOptionItems(countries, new int[]{1});
        country.updateAp();
        printChoice("Country", country);
        System.out.println("Country: isEditable=" + country.isEditable());

        // List box with several options.
        CPDFListboxWidget colors = (CPDFListboxWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_ListBox, document);
        colors.setRect(new RectF(320, 610, 560, 540));
        colors.setFieldName("Colors");
        // Set the text property before reading it back: getTextProperty() on a widget
        // without a default appearance font aborts the JVM in this SDK build.
        colors.setTextProperty(new CPDFTextAttribute("Helvetica", 11, Color.DARK_GRAY.getRGB()));
        colors.setOptionItems(new CPDFWidgetItem[]{
                new CPDFWidgetItem("R", "Red"),
                new CPDFWidgetItem("G", "Green"),
                new CPDFWidgetItem("B", "Blue"),
        });
        colors.setSelectedIndexes(new int[]{2});
        colors.updateAp();
        printChoice("Colors", colors);
        System.out.println("Colors: isMultiSelectListBox=" + colors.isMultiSelectListBox());

        // Signature fields with text, ink and image appearances.
        CPDFSignatureWidget signText = (CPDFSignatureWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_SignatureFields, document);
        signText.setRect(new RectF(40, 520, 200, 460));
        signText.setFieldName("SignText");
        System.out.println("SignText: isSigned before=" + signText.isSigned()
                + " updateApWithText=" + signText.updateApWithText("ComPDFKit", "Helvetica", Color.BLUE.getRGB())
                + " isSigned after=" + signText.isSigned());

        CPDFSignatureWidget signInk = (CPDFSignatureWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_SignatureFields, document);
        signInk.setRect(new RectF(220, 520, 380, 460));
        signInk.setFieldName("SignInk");
        PointF[][] strokes = {
                {new PointF(230, 470), new PointF(260, 510), new PointF(290, 470), new PointF(320, 505)},
                {new PointF(300, 480), new PointF(370, 490)},
        };
        System.out.println("SignInk: updateApWithInk=" + signInk.updateApWithInk(strokes, 2, Color.BLACK.getRGB()));

        CPDFSignatureWidget signImage = (CPDFSignatureWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_SignatureFields, document);
        signImage.setRect(new RectF(400, 520, 560, 460));
        signImage.setFieldName("SignImage");
        String imagePath = copySignatureImage(rootDir);
        System.out.println("SignImage: updateApWithImage=" + (imagePath != null
                && signImage.updateApWithImage(imagePath, "", CPDFImageScaleType.SCALETYPE_fitCenter)));
        System.out.println("SignImage: getSignature(document)=" + signImage.getSignature(document)
                + " (null: appearance only, no digital signature)");

        // Clear colors on one widget to show the transparent defaults.
        CPDFTextWidget plain = (CPDFTextWidget) page.addFormWidget(CPDFWidget.WidgetType.Widget_TextField, document);
        plain.setRect(new RectF(40, 440, 300, 405));
        plain.setFieldName("Plain");
        plain.setFillColor(Color.YELLOW.getRGB());
        plain.setBorderColor(Color.RED.getRGB());
        System.out.println("Plain: clearFillColor=" + plain.clearFillColor() + " clearBorderColor=" + plain.clearBorderColor()
                + " -> fill=" + hex(plain.getFillColor()) + " border=" + hex(plain.getBorderColor()));
        plain.updateAp();

        System.out.println("page.updateFormAp(): " + page.updateFormAp());
        String outputPath = rootDir + OUTPUT_DIR + "/Form_Properties.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();
        System.out.println("Done. Result saved in Form_Properties.pdf");
        printDividingLine();
        return outputPath;
    }

    /** Sample 2: Reopen the file and read every widget's properties. */
    private static void readBackProperties(String pdfPath) {
        System.out.println("Sample 2: Read widget properties from the saved file");
        CPDFDocument document = new CPDFDocument();
        document.open(pdfPath);
        CPDFPage page = document.pageAtIndex(0);
        for (CPDFAnnotation annotation : page.getAnnotations()) {
            if (annotation.getType() != CPDFAnnotation.Type.WIDGET || !(annotation instanceof CPDFWidget)) {
                continue;
            }
            CPDFWidget widget = (CPDFWidget) annotation;
            StringBuilder line = new StringBuilder();
            line.append(widget.getFieldName()).append(" [").append(widget.getWidgetType()).append("]")
                    .append(" flags=").append(widget.getFieldFlag())
                    .append(" readOnly=").append(widget.isReadOnly())
                    .append(" fill=").append(hex(widget.getFillColor()))
                    .append(" border=").append(hex(widget.getBorderColor()))
                    .append("/").append(widget.getBorderStyles())
                    .append("/").append(widget.getBorderWidth())
                    .append(" rect=").append(widget.getRect());
            if (widget instanceof CPDFTextWidget) {
                CPDFTextWidget text = (CPDFTextWidget) widget;
                line.append(" text=\"").append(text.getText()).append("\"")
                        .append(" multiLine=").append(text.isMultiLine())
                        .append(" password=").append(text.isPassword())
                        .append(" align=").append(text.getTextAlignment())
                        .append(" special=").append(text.getTextFieldSpecial())
                        .append(" font=").append(text.getFontName()).append("/").append(text.getFontSize())
                        .append(" fontColor=").append(hex(text.getFontColor()));
            } else if (widget instanceof CPDFCheckboxWidget) {
                CPDFCheckboxWidget box = (CPDFCheckboxWidget) widget;
                line.append(" checked=").append(box.isChecked())
                        .append(" boxStyle=").append(box.getCheckBoxStyle())
                        .append(" checkStyle=").append(box.getCheckStyle())
                        .append(" checkColor=").append(hex(box.getColor()));
            } else if (widget instanceof CPDFRadiobuttonWidget) {
                CPDFRadiobuttonWidget radio = (CPDFRadiobuttonWidget) widget;
                line.append(" checked=").append(radio.isChecked())
                        .append(" checkStyle=").append(radio.getCheckStyle())
                        .append(" checkColor=").append(hex(radio.getColor()));
            } else if (widget instanceof CPDFPushbuttonWidget) {
                CPDFPushbuttonWidget button = (CPDFPushbuttonWidget) widget;
                line.append(" title=").append(button.getButtonTitle())
                        .append(" font=").append(button.getFontName()).append("/").append(button.getFontSize())
                        .append(" fontColor=").append(hex(button.getFontColor()));
            } else if (widget instanceof CPDFWidgetItems) {
                CPDFWidgetItems items = (CPDFWidgetItems) widget;
                line.append(" items=").append(items.getItemCount())
                        .append(" selected=").append(Arrays.toString(items.getSelectedIndexes()));
                if (widget instanceof CPDFComboboxWidget) {
                    line.append(" editable=").append(((CPDFComboboxWidget) widget).isEditable());
                } else if (widget instanceof CPDFListboxWidget) {
                    line.append(" multiSelect=").append(((CPDFListboxWidget) widget).isMultiSelectListBox());
                }
            } else if (widget instanceof CPDFSignatureWidget) {
                line.append(" signed=").append(((CPDFSignatureWidget) widget).isSigned());
            }
            System.out.println(line);
        }
        document.close();
        printDividingLine();
    }

    /** Sample 3: Reset every field and save the result. */
    private static void resetFields(String rootDir, String pdfPath) {
        System.out.println("Sample 3: Reset fields");
        CPDFDocument document = new CPDFDocument();
        document.open(pdfPath);
        CPDFPage page = document.pageAtIndex(0);
        for (CPDFAnnotation annotation : page.getAnnotations()) {
            if (annotation instanceof CPDFWidget) {
                CPDFWidget widget = (CPDFWidget) annotation;
                System.out.println(widget.getFieldName() + " resetForm(): " + widget.resetForm());
            }
        }
        String outputPath = rootDir + OUTPUT_DIR + "/Form_Reset.pdf";
        document.save(outputPath, CPDFDocument.PDFDocumentSaveType.PDFDocumentSaveNoIncremental);
        document.close();

        CPDFDocument check = new CPDFDocument();
        check.open(outputPath);
        for (CPDFAnnotation annotation : check.pageAtIndex(0).getAnnotations()) {
            if (annotation instanceof CPDFTextWidget) {
                CPDFTextWidget text = (CPDFTextWidget) annotation;
                System.out.println("After reset " + text.getFieldName() + " text=\"" + text.getText() + "\"");
            } else if (annotation instanceof CPDFCheckboxWidget) {
                System.out.println("After reset " + ((CPDFCheckboxWidget) annotation).getFieldName()
                        + " checked=" + ((CPDFCheckboxWidget) annotation).isChecked());
            } else if (annotation instanceof CPDFWidgetItems) {
                System.out.println("After reset " + ((CPDFWidgetItems) annotation).getFieldName()
                        + " selected=" + Arrays.toString(((CPDFWidgetItems) annotation).getSelectedIndexes()));
            }
        }
        check.close();
        System.out.println("Done. Result saved in Form_Reset.pdf");
        printDividingLine();
    }

    private static void printChoice(String label, CPDFWidgetItems items) {
        int[] selected = items.getSelectedIndexes();
        StringBuilder options = new StringBuilder();
        for (CPDFWidgetItem item : items.getOptions()) {
            options.append(item.text).append(';');
        }
        CPDFTextAttribute textProperty = items.getTextProperty();
        System.out.println(label + ": itemCount=" + items.getItemCount() + " options=" + options
                + " selected=" + Arrays.toString(selected)
                + " selectedText=" + (selected != null && selected.length > 0 ? items.getOptionByIndex(selected[0]).text : "none")
                + " textProperty=" + (textProperty == null ? "null" : textProperty.getFontName() + "/" + textProperty.getFontSize()));
    }

    /** updateApWithImage requires a writable JPEG, so work on a copy in the output directory. */
    private static String copySignatureImage(String rootDir) {
        try {
            File copy = new File(rootDir + OUTPUT_DIR + "/signature.jpg");
            Files.copy(Paths.get(rootDir + SIGNATURE_IMAGE), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return copy.getAbsolutePath();
        } catch (IOException e) {
            System.out.println("Cannot copy signature image: " + e.getMessage());
            return null;
        }
    }

    private static String hex(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
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
