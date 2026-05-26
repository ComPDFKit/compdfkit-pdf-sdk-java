# ComPDF SDK for Java

As part of the KDAN ecosystem, ComPDF SDK for Java comprehensive functions for quickly generating, viewing, annotating, editing, and signing PDFs, and more. It is feature-rich and battle-tested, making PDF files process and manipulation easier and faster.

If you find this library helpful, please consider giving us a ⭐ **Star** on GitHub! Have feedback or questions? Join the conversation in our [Discussions](https://github.com/orgs/ComPDFKit/discussions).

**Why ComPDF SDK for Java?**

* **Easy to Integrate:** Integrate PDF functionalities easily with our powerful SDK and clear documentation and guides with few lines of code.
  
* **Fully Customizable UI:** Design a unique interface for your products with fully customizable UI source code by a high-performing SDK.
  
* **[Comprehensive PDF Features:](https://www.compdf.com/pdf-sdk/features-list?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)** Supports PDF generation, viewing, annotation, page editing, content editing, conversion, OCR, redaction, signing, forms, parsing, measurement, compression, comparison, color separation, batch processing, and more.
  
* **Faster Time-to-Market:** Comprehensive SDK libraries save your time and expenses and roll out your applications and projects.
  
* **High-quality Service:** We provide 24/7 professional one-to-one technical support, including onsite service and remote assistance via phone and email.
  

## Table of Contents

* [Related](#related)
* [Requirements](#requirements)
* [How to Run a Demo](#how-to-run-a-demo)
* [How to Use ComPDF SDK for Java in Java Applications](#how-to-use-compdf-sdk-for-java-in-java-applications)
* [Free Trial and License](#free-trial-and-license)
* [Support](#support)
* [Changelog](#changelog)

## Related

* Developer Guides - [Java PDF Library for Server](https://www.compdf.com/guides/pdf-sdk/java/overview?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
* [How to make a Java application with ComPDF SDK](https://www.compdf.com/guides/pdf-sdk/java/make-a-program?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
* [Java PDF Library Code Samples](https://www.compdf.com/guides/pdf-sdk/java/samples?utm_source=github&utm_medium=compdfkit-pdf-sdk-java&utm_campaign=compdfkit_pdf_sdk_java_repo&ref_platform_id=github_compdfkit)
  
* [ComPDF API Library for Java](https://github.com/ComPDFKit/compdfkit-api-java)
  

## Requirements

ComPDF SDK for Java requires apps to enable Java 8 language features to build.

* Programming Environment: Java JDK 1.8 and higher.
  
* Supported Platforms
  
  * Mac (intel & M1)
  * Linux (x86)


## How to Run a Demo

ComPDF SDK for Java provides multiple demos for developers to learn how to call the SDK on Java applications. You can find them in the ***"samples"*** folder. In this guide, we take ***"DocumentInfoTest"*** as an example to show how to run it on Java applications.

1. Import the ***"samples"*** project on IDE.

2. Navigate to the ***"JAVA"*** folder which is in ***"DocumentInfoTest"*** file and run the sample with. For example `/samples/DocumentInfoTest/JAVA`.

   ```
   ./RunTest.sh
   ```

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/folder.png)

   **Note:** *This is a demo project, presenting completed ComPDF SDK functions. The functions might be different based on the license you have purchased. Please check that the functions you choose work fine in this demo project.* The output files will be in `/samples/out`.



## How to Use ComPDF SDK for Java in Java Applications

This section will help you to quickly get started with ComPDF SDK to make a Java application with step-by-step instructions.


### Create a New Project

Create a Java application using the IDE. Here we create a new **Java** project.

![](https://compdf.oss-us-east-1.aliyuncs.com/jni/create-project.png)



### Add the Package of ComPDF SDK for Java

1. The first thing we need to do is to import ComPDF SDK for Java. Copy ***"lib"*** to the **project**.

![](https://compdf.oss-us-east-1.aliyuncs.com/jni/copy-libs.png)

2. Right click on the SDK and select **Add as Library...**.

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/add-lib.png)

3. Add the following code to **Main.java**:

   ```java
   static{
       try {
          NativeKMPDFKit.initialize("your license", "your deviceId", "", Constants.LIB_DIR_NAME);
       } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
       }
   }
   ```

4. Start the `main` method to print out the information of your computer's system version. If the information is printed successfully, you have integrated ComPDF SDK for Java successfully!

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/result1.2.0.png)

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

Go to our [Changelog](https://www.compdf.com/api/changelog-compdfkit-api) to keep up with the latest updates, improvements, and bug fixes.
