# ComPDFKit

[ComPDFKit PDF SDK](https://www.compdf.com/) is a robust PDF library, which offers comprehensive functions for quickly viewing, annotating, editing, and signing PDFs. It is feature-rich and battle-tested, making PDF files process and manipulation easier and faster.

ComPDFKit seamlessly operates on [Web](https://www.compdf.com/web), [Windows](https://www.compdf.com/windows), [Android](https://www.compdf.com/android), [iOS](https://www.compdf.com/ios), [Mac](https://www.compdf.com/contact-sales), and [Server](), with support for cross-platform frameworks such as [React Native](https://www.compdf.com/react-native), [Flutter](https://www.compdf.com/flutter), etc.


# Introduction
ComPDFKit [Java PDF Library](https://www.compdf.com/pdf-library/java) allows you to quickly add PDF functions to any Java application, elevating your Java programs to ensure efficient development. 

# Related

* Developer Guides - [Java PDF Library for Server](https://www.compdf.com/guides/pdf-sdk/java/overview)
* [How to make a Java application with ComPDFKit PDF SDK](https://www.compdf.com/guides/pdf-sdk/java/make-a-program)
* [Java PDF Library Code Samples](https://www.compdf.com/guides/pdf-sdk/java/samples)
* [ComPDFKit PDF API Library for Java](https://github.com/ComPDFKit/compdfkit-api-java)

# Get Started

It is easy to embed ComPDFKit PDF SDK in your Java application with a few lines of Java code. Take just a few minutes and get started.

The following sections introduce the requirements, structure of the installation package, and how to make a PDF Reader/Editor in Java with ComPDFKit PDF SDK.



## Requirements

ComPDFKit PDF SDK for Java requires apps to enable Java 8 language features to build.

- Programming Environment: Java JDK 1.8 and higher.

- Supported Platforms

    - Mac (intel & M1)
    - Linux (x86)


## How to Run a Demo

ComPDFKit PDF SDK for Java provides multiple demos for developers to learn how to call the SDK on Java applications. You can find them in the ***"samples"*** folder. In this guide, we take ***"DocumentInfoTest"*** as an example to show how to run it on Java applications.

1. Import the ***"samples"*** project on IDE.

2. Navigate to the ***"JAVA"*** folder which is in ***"DocumentInfoTest"*** file and run the sample with. For example `/samples/DocumentInfoTest/JAVA`.

   ```
   ./RunTest.sh
   ```

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/folder.png)

   **Note:** *This is a demo project, presenting completed ComPDFKit PDF SDK functions. The functions might be different based on the license you have purchased. Please check that the functions you choose work fine in this demo project.* The output files will be in `/samples/out`.



## How to Use ComPDFKit PDF SDK for Java in Java Applications

This section will help you to quickly get started with ComPDFKit PDF SDK to make a Java application with step-by-step instructions.



### Create a New Project

Create a Java application using the IDE. Here we create a new **Java** project.

![](https://compdf.oss-us-east-1.aliyuncs.com/jni/create-project.png)



### Add the Package of ComPDFKit PDF SDK for Java

1. The first thing we need to do is to import ComPDFKit PDF SDK for Java. Copy ***"lib"*** to the **project**.

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

4. Start the `main` method to print out the information of your computer's system version. If the information is printed successfully, you have integrated ComPDFKit PDF SDK for Java successfully!

   ![](https://compdf.oss-us-east-1.aliyuncs.com/jni/result1.2.0.png)

### Apply the License
ComPDFKit PDF SDK is a commercial SDK, which [requires a license](https://www.compdf.com/guides/pdf-sdk/java/apply-the-license-key) to grant developer permission to release their apps. Each license is only valid for one deviceId in development mode. 

Contact ComPDFKit's [sales team](https://www.compdf.com/contact-sales) to get a free 30-day license to test the project.


# Documentation
Full [documentation for ComPDFKit for Java PDF Libary](https://www.compdf.com/guides/pdf-sdk/java/overview) can be found here.


# Support
ComPDFKit has a professional R&D team that produces comprehensive technical documentation and guides to help developers. Also, you can get an immediate response when reporting your problems to our support team.

* For detailed information, please visit our [Guides](https://www.compdf.com/guides/pdf-sdk/java/overview) page.

* For technical assistance, please reach out to our [Technical Support](https://www.compdf.com/support).

* To get more details and an accurate quote, please contact our [Sales Team](https://compdf.com/contact-us).

# Note
We are glad to announce that you can register a ComPDFKit API account for a [free trial]() to process 1000 documents per month for free.