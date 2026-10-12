# ComPDF SDK for Java

This repository contains **ComPDFKit PDF SDK for Java 3.2.0**. See the
[release notes](release_notes.txt), [developer guide](developer_guide_java.pdf),
and [bundled API reference](api_reference_Java/index.html).

As part of the KDAN ecosystem, ComPDF SDK for Java comprehensive functions for quickly generating, viewing, annotating, editing, and signing PDFs, and more. It is feature-rich and battle-tested, making PDF files process and manipulation easier and faster.

> If you find ComPDF SDK useful, please consider giving us a ⭐ **Star** on GitHub — it helps us grow and improve! Got questions or ideas? Join the conversation in our [Discussions](https://github.com/ComPDFKit/compdfkit-pdf-sdk-java/discussions).

**Why ComPDF SDK for Java?**

* **Easy to Integrate:** Integrate PDF functionalities easily with our powerful SDK and clear documentation and guides with few lines of code.
  
* **Fully Customizable UI:** Design a unique interface for your products with fully customizable UI source code by a high-performing SDK.
  
* **[Comprehensive PDF Features:](https://www.compdf.com/pdf-sdk/features-list?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)** Supports PDF generation, viewing, annotation, page editing, content editing, conversion, OCR, redaction, signing, forms, parsing, measurement, compression, comparison, color separation, batch processing, and more.
  
* **Faster Time-to-Market:** Comprehensive SDK libraries save your time and expenses and roll out your applications and projects.
  
* **High-quality Service:** We provide 24/7 professional one-to-one technical support, including onsite service and remote assistance via phone and email.
  

## Table of Contents


- [Supported Features](#supported-features)
- [Requirements](#requirements)
- [How to Run a Demo](#how-to-run-a-demo)
- [How to Use ComPDF SDK for Java in Java Applications](#how-to-use-compdf-sdk-for-java-in-java-applications)
- [Free Trial and License](#free-trial-and-license)
- [Support](#support)
- [Changelog](#changelog)
- [Related](#related)

## Supported Features

- **PDF Generation**: 
  
  - Generates PDFs from HTML  
  
  - Provides PDF creation through APIs 
  
  - Supports a wide range of HTML and CSS tags  
  
  - Dynamic table generation 
  
  - Text reflow

- **Annotations**:
  
  * Notes - add longer comments with adjustable icon shape and color
  
  * Ink - freehand drawing with customizable color, opacity, line thickness
  
  * Text - add, move, resize text directly on page
  
  * Inspector - adjust annotation looks (line styles, borders, colors, opacity, font)
  
  * Comment on Annotations and Update Status
  
  * Import & Export & Flatten Annotations (XFDF, FDF, JSON)
  
  * Highlight, Underline, Strikeout, Squiggly
  
  * Shapes - Rectangle, Oval, Line, Arrow, Polygon, Polyline, Cloud
  
  * Stamps, Sound, Movie, File Attachment, Link, Distance, Perimeter, Area

- **Forms**:
  
  - Process fillable and static PDF forms
  
  - Form filling, form creation, form flattening
* **Document Editor**: 
  
  - Page manipulation - insert, delete, rotate, reorder, extract, crop
  * Split PDF, Merge PDF

* **Content Editor**: Edit PDF text and images directly like in Word

* **Security**: 
  
  - Encryption - set open password, permission password
  * Restrict printing, copying, editing
- **Signatures**:
  
  - Electronic Signatures - draw, type, image signatures
  
  - Digital Signatures - certificate-based signature validation
* **OCR:**
  
  * AI OCR
  
  * Recognize Tables, Graphics, Images
  
  * Support recognition in 80+ Languages

* **Watermark:**
  
  * Add Text or Image Watermarks
  
  * Delete Watermarks
  
  * Customize Watermarks

* **Compare Documents**: Side-by-side document comparison to highlight differences

* **Redaction**: Permanently remove sensitive content from PDFs

* **Measurement**: Distance, area, perimeter measurement tools

* **Compress**: Optimize and reduce PDF file size

* **PDF/A, PDF/X, PDF/E, PDF/UA**: Standards compliance for archiving, printing, engineering, and accessibility

* **Batch Document Processing:** Provides server-side batch document processing, including PDF editing, conversion, and generation.

* **Convert Files**: 
  
  - Convert PDF to Word, Excel, PPT, HTML, CSV, images (PNG,JPEG, JPEG, JPEG2000, BMP, TIFF, TGA, GIF), RTF, TXT, JSON, XML, markdown, searchable PDF, searchable OFD.
  
  - Convert images (PNG,JPEG, JPEG, JPEG2000, BMP, TIFF, TGA, GIF) to Word, Excel, PPT, HTML, CSV, RTF, TXT, JSON, XML.
  
  - Convert Word, Excel, PPT, HTML, CSV, PNG, RTF, TXT to PDF
  

## Requirements

ComPDF SDK for Java requires apps to enable Java 8 language features to build.

* Programming Environment: Java JDK 1.8 and higher.
  
* Supported Platforms
  
  * Mac (intel & M1)
  * Linux (x86)


## How to Run a Demo

ComPDF SDK for Java provides multiple demos for developers to learn how to call the SDK on Java applications. You can find them in the ***"samples"*** folder. In this guide, we take ***"DocumentInfoTest"*** as an example to show how to run it on Java applications.

1. Keep the supplied layout intact: the SDK used by the examples is
   `samples/lib/compdfkit-3.2.0.jar`, and shared Java helpers are in `samples/common`.

2. Obtain your own SDK license and save it in a local file outside the checkout.
   No license key is included in this repository. Point the examples to that file:

   ```sh
   export COMPDFKIT_LICENSE_FILE="/absolute/path/to/your/license_key.txt"
   ```

   Alternatively, create `samples/common/license_key.txt` locally; it is ignored
   by Git. Do not commit license keys.

3. From the repository root, run the example directly from its directory
   (there is no intermediate `JAVA` directory):

   ```sh
   cd samples/DocumentInfoTest
   bash ./RunTest.sh
   ```

   **Note:** Available functionality depends on your license. Generated output
   is written under `samples/out` and must not be committed. Signing examples
   may need additional local certificates or services; see
   [sample setup and signing prerequisites](samples/README.md).



## How to Use ComPDF SDK for Java in Java Applications

This section will help you to quickly get started with ComPDF SDK to make a Java application with step-by-step instructions.


### Create a New Project

Create a Java application using the IDE. Here we create a new **Java** project.

![](https://compdf.oss-us-east-1.aliyuncs.com/jni/create-project.png)



### Add the Package of ComPDF SDK for Java

1. Add `lib/compdfkit-3.2.0.jar` to your application's dependencies.
   The examples separately use the identical JAR in `samples/lib`.

![](https://compdf.oss-us-east-1.aliyuncs.com/jni/copy-libs.png)

2. Right click on the SDK and select **Add as Library...**.

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/add-lib.png)

3. Add the following code to **Main.java**:

   ```java
   static{
       try {
          NativeKMPDFKit.initialize("YOUR_LICENSE_KEY", "", "ComPDFKit", Constants.LIB_DIR_NAME);
       } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
       }
   }
   ```

4. Start the `main` method to print out the information of your computer's system version. If the information is printed successfully, you have integrated ComPDF SDK for Java successfully!


## Free Trial and License

[ComPDF SDK](https://www.compdf.com/?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit) offers a **30-day free trial** so you can evaluate core PDF capabilities in your own application.

To get started:

1. Apply for a [free trial](https://www.compdf.com/pricing?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
2. Review supported trial features and licensing details
3. Follow the integration guides above and [license steps](https://www.compdf.com/guides/pdf-sdk/java/apply-the-license-key?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit) to activate the SDK in your project

For custom deployments, advanced features, or volume licensing, please [contact our sales team](https://www.compdf.com/contact-sales?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)

## Support

ComPDFKit has a professional R&D team that produces comprehensive technical documentation and guides to help developers. Also, you can get an immediate response when reporting your problems to our support team.

* For detailed information, please visit our [Guides](https://www.compdf.com/guides/pdf-sdk/java/overview?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit) page.
  
* For technical assistance, please reach out to our [Technical Support](https://www.compdf.com/support?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit).
  
* To get more details and an accurate quote, please contact our [Sales Team](https://compdf.com/contact-us?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit).
  

## Changelog

The bundled [release notes](release_notes.txt) describe version 3.2.0, including
PAdES/LTV, structured validation, AWS KMS, external CMS/PKCS#7 containers,
two-phase signing, RFC 3161 timestamps, invisible signatures, and online
license verification.

Go to our [Changelog](https://www.compdf.com/api/changelog-compdfkit-api?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit) to keep up with the latest updates, improvements, and bug fixes.

## Related

* More Guides:
  
  - [API Reference for ComPDF SDK (Java)](https://developers.compdf.com/guides/pdf-sdk/java/api-reference/index)
  
  - Developer Guides - [Java PDF Library for Server](https://www.compdf.com/guides/pdf-sdk/java/overview?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
  - [How to make a Java application with ComPDF SDK](https://www.compdf.com/guides/pdf-sdk/java/make-a-program?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
  - [Java PDF Library Code Samples](https://www.compdf.com/guides/pdf-sdk/java/samples?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
  - [ComPDF API Library for Java](https://github.com/ComPDFKit/compdfkit-api-java)
- ComPDF Cloud: We also provide Open API for developers. You can [register a free API account](https://api.compdf.com/signup?utm_source=github&utm_medium=compdfkit-pdf-sdk-android&utm_campaign=compdfkit_pdf_sdk_android_repo&ref_platform_id=github_compdfkit) to get up to 200+ API calls monthly for free.
