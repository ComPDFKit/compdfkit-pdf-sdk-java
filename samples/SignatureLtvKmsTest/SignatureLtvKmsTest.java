import com.compdfkit.core.document.CPDFDocument;
import com.compdfkit.core.signature.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.*;

/** Signature examples. Edit the constants below, then run RunTest.sh without arguments. */
public final class SignatureLtvKmsTest {
    private enum SampleMode { PADES, PROLONG, KMS }

    // RunTest.sh starts Java in this sample's directory, like the other samples.
    private static final String ROOT_DIR = "..";
    // true: AWS KMS signing; false: local PKCS#12 PAdES signing.
    private static final boolean USE_AWS_KMS = false;
    private static final SampleMode SAMPLE_MODE = USE_AWS_KMS ? SampleMode.KMS : SampleMode.PADES;
    private static final String INPUT_PDF = "TestFiles/CommonFivePage.pdf";
    // Used only when running PROLONG on an existing file.
    private static final String SIGNED_INPUT_PDF = "out/SignatureLtvKmsTest/Pades-signed.pdf";
    private static final String OUTPUT_DIR = "out/SignatureLtvKmsTest";

    private static final CPDFPadesOptions.Profile PADES_PROFILE = CPDFPadesOptions.Profile.LT;
    // Reuse the Android demo's signer.p12, test CA and CRL. Run PrepareAndroidTestPki.py
    // on the host once before this mode. False retains the self-contained local PKI.
    private static final boolean USE_ANDROID_TEST_PKI = true;
    // Serve the test CRL for this process; local PKI mode also starts its own TSA.
    // Set false when supplying your own services; this does not apply to KMS.
    private static final boolean USE_LOCAL_TEST_SERVICES = true;
    private static final String LOCAL_PYTHON = "python3";
    private static final String CERTIFICATE_DIR =
            USE_ANDROID_TEST_PKI ? "TestFiles/SignatureLtvAndroid" : "TestFiles/local-certificates";
    private static final String PKCS12_PATH = CERTIFICATE_DIR + "/signer.p12";
    private static final String PKCS12_PASSWORD = USE_ANDROID_TEST_PKI ? "1234" : "test-only";
    private static final String TRUSTED_CERT_PATH =
            CERTIFICATE_DIR + (USE_ANDROID_TEST_PKI ? "/trusted_roots.pem" : "/ca.pem");
    private static final String TSA_URL =
            USE_ANDROID_TEST_PKI ? "http://timestamp.digicert.com" : "http://127.0.0.1:18080/tsa";
    // Override the signer's original LAN CRL URL while producing the PDF in Linux.
    // Android validation reads the embedded CRL and does not fetch this loopback URL.
    private static final String CRL_URL = USE_ANDROID_TEST_PKI ? "http://127.0.0.1:18080/test.crl"
                                                               : "http://127.0.0.1:18080/ca.crl";
    private static final int TSA_TIMEOUT_SECONDS = USE_ANDROID_TEST_PKI ? 30 : 20;
    private static final int TSA_RETRY_COUNT = USE_ANDROID_TEST_PKI ? 1 : 0;
    private static final String TSA_PROXY_URL = "";
    private static final boolean CHECK_REVOCATION = true;
    private static final boolean VISIBLE_SIGNATURE = false;
    private static final String PADES_FIELD_NAME = "PadesSignature";

    // Set real AWS values here. KMS mode does not start the local TSA/CRL service.
    private static final String KMS_ENDPOINT = "https://kms.us-east-1.amazonaws.com";
    private static final String KMS_REGION = "us-east-1";
    private static final String KMS_KEY_ID = "REPLACE_WITH_KMS_KEY_ID";
    private static final String KMS_ACCESS_KEY_ID = "REPLACE_WITH_ACCESS_KEY_ID";
    private static final String KMS_SECRET_ACCESS_KEY = "REPLACE_WITH_SECRET_ACCESS_KEY";
    private static final String KMS_SESSION_TOKEN =
            ""; // Nonempty tokens are currently unsupported.
    private static final String KMS_SIGNING_ALGORITHM = "RSASSA_PKCS1_V1_5_SHA_256";
    // The signer certificate's public key must match KMS_KEY_ID. The local PADES
    // demonstration certificate uses a different private key and cannot be used here.
    private static final String KMS_CERTIFICATE_DIR = "TestFiles/AwsKms";
    private static final String KMS_SIGNER_CERT_PATH = KMS_CERTIFICATE_DIR + "/signer.der";
    private static final String KMS_ISSUER_CERT_PATH =
            KMS_CERTIFICATE_DIR + "/ca.der"; // Empty means no supplied issuer.
    private static final String KMS_TRUSTED_CERT_PATH = KMS_CERTIFICATE_DIR + "/ca.pem";
    private static final String KMS_FIELD_NAME = "KmsSignature";

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) throws Exception {
        printHead("SignatureLtvKms");
        System.out.println("Selected mode: " + SAMPLE_MODE);
        String rootDir = new File(ROOT_DIR).getCanonicalPath();
        Files.createDirectories(new File(rootDir, OUTPUT_DIR).toPath());
        try (LocalServices services = LocalServices.start(rootDir)) {
            switch (SAMPLE_MODE) {
                case PADES:
                    requireFile(path(rootDir, TRUSTED_CERT_PATH), "Trust certificate");
                    requireFile(path(rootDir, INPUT_PDF), "Input PDF");
                    requireFile(path(rootDir, PKCS12_PATH), "PKCS12 certificate");
                    String signedPdf = signPades(rootDir);
                    prolongLtv(rootDir, signedPdf);
                    break;
                case PROLONG:
                    requireFile(path(rootDir, TRUSTED_CERT_PATH), "Trust certificate");
                    prolongLtv(rootDir, path(rootDir, SIGNED_INPUT_PDF));
                    break;
                case KMS:
                    requireFile(path(rootDir, INPUT_PDF), "Input PDF");
                    signWithKms(rootDir);
                    break;
            }
        }
        printFooter();
    }

    private static String path(String rootDir, String relativePath) {
        return new File(rootDir, relativePath).getAbsolutePath();
    }

    private static CPDFSignatureValidationOptions validationOptions(String rootDir) {
        return new CPDFSignatureValidationOptions()
                .setTrustedCertPath(path(rootDir, TRUSTED_CERT_PATH))
                .setCheckRevocation(CHECK_REVOCATION);
    }

    private static CPDFPadesOptions padesOptions(String rootDir) {
        return new CPDFPadesOptions()
                .setProfile(PADES_PROFILE)
                .setFieldName(PADES_FIELD_NAME)
                .setVisible(VISIBLE_SIGNATURE)
                .setTrustedCertPath(path(rootDir, TRUSTED_CERT_PATH))
                .setTsaUrl(TSA_URL)
                .setTsaTransport(TSA_TIMEOUT_SECONDS, TSA_RETRY_COUNT, TSA_PROXY_URL)
                .setRevocationUrls(null, new String[] {CRL_URL})
                .setValidationOptions(validationOptions(rootDir));
    }

    /** Sample 1: Sign and validate, then return this run's signed stage for the LTV example. */
    private static String signPades(String rootDir) throws IOException {
        printDividingLine();
        System.out.println("Sample 1: Sign PAdES " + PADES_PROFILE);
        CPDFPadesOptions options =
                padesOptions(rootDir).setCertificate(path(rootDir, PKCS12_PATH), PKCS12_PASSWORD);
        String prefix = outputPrefix(rootDir, "Pades");
        CPDFPadesResult result = CPDFPadesSigner.sign(path(rootDir, INPUT_PDF),
                prefix + "-signed.pdf",
                prefix + "-lt.pdf",
                prefix + "-lta.pdf",
                options);
        printPadesResult(result);
        verifyPublishedPades(rootDir, result);
        return result.getSignedPath();
    }

    /** Sample 2: Add LTV material to the selected signed PDF and validate the final output. */
    private static void prolongLtv(String rootDir, String signedPdf) throws IOException {
        requireFile(signedPdf, "Signed input PDF (run PADES first)");
        printDividingLine();
        System.out.println("Sample 2: Prolong LTV");
        System.out.println("Signed input=" + signedPdf);
        CPDFPadesOptions options = padesOptions(rootDir).setAppendDocumentTimestamp(
                PADES_PROFILE == CPDFPadesOptions.Profile.LTA);
        String prefix = outputPrefix(rootDir, "Prolong");
        CPDFPadesResult result = CPDFPadesSigner.prolongLtv(
                signedPdf, prefix + "-lt.pdf", prefix + "-lta.pdf", options);
        printPadesResult(result);
        verifyPublishedPades(rootDir, result);
    }

    /** Reopen the saved PDF with the Android demo's validation settings. */
    private static void verifyPublishedPades(String rootDir, CPDFPadesResult result)
            throws IOException {
        String pdf = result.getFinalPath();
        String trustBundle = pdf.substring(0, pdf.length() - 4) + "-trusted_roots.pem";
        Files.copy(new File(path(rootDir, TRUSTED_CERT_PATH)).toPath(),
                new File(trustBundle).toPath());
        System.out.println("Android verification trust bundle=" + trustBundle);
        System.out.println(
                "Use this bundle as /storage/emulated/0/Download/trusted_roots.pem in the Android demo.");
        CPDFSignatureValidationResult validation = verifySignedPdf(pdf, trustBundle);
        if (!validation.isSuccess()) {
            throw new IllegalStateException(
                    "Saved PDF did not pass validation with the Android demo's options; inspect the report");
        }
    }

    /** Immediately reopen the signed output and run Android Sign Test's rich validation. */
    private static CPDFSignatureValidationResult verifySignedPdf(
            String pdf, String trustedCertPath) {
        printDividingLine();
        System.out.println("===== post-sign rich validation (Android Sign Test) =====");
        System.out.println("PDF=" + pdf);
        System.out.println("Trusted certificates=" + trustedCertPath);
        requireFile(pdf, "Signed PDF");
        requireFile(trustedCertPath, "Verification trust bundle");
        CPDFDocument document = new CPDFDocument();
        try {
            document.open(pdf);
            if (!document.isValid()) {
                throw new IllegalStateException("Cannot reopen signed PDF for validation: " + pdf);
            }
            CPDFSignatureValidationOptions options = new CPDFSignatureValidationOptions()
                                                             .setTrustedCertPath(trustedCertPath)
                                                             .setCheckRevocation(CHECK_REVOCATION);
            CPDFSignatureValidationResult validation =
                    CPDFSignatureValidator.validate(document, options);
            printValidationResult(validation);
            for (int i = 0; i < document.getSignatureCount(); i++) {
                CPDFSignature signature = document.getPdfSignature(i);
                if (signature != null) {
                    System.out.println("signature " + i
                            + " timestampSource=" + signature.getTimestampSource());
                }
            }
            return validation;
        } finally {
            document.close();
        }
    }

    /** Sample 3: Sign a PDF with AWS KMS and report the independent validation result. */
    private static void signWithKms(String rootDir) throws Exception {
        printDividingLine();
        System.out.println("Sample 3: Sign with AWS KMS");
        if (KMS_KEY_ID.startsWith("REPLACE_") || KMS_ACCESS_KEY_ID.startsWith("REPLACE_")
                || KMS_SECRET_ACCESS_KEY.startsWith("REPLACE_")) {
            throw new IllegalStateException(
                    "Set KMS_REGION, KMS_ENDPOINT, KMS_KEY_ID, KMS_ACCESS_KEY_ID and KMS_SECRET_ACCESS_KEY in SignatureLtvKmsTest.java to real AWS values; also configure the matching KMS certificate paths. No AWS request was sent.");
        }
        requireFile(path(rootDir, KMS_TRUSTED_CERT_PATH), "KMS trust certificate");
        requireFile(path(rootDir, KMS_SIGNER_CERT_PATH), "KMS signer certificate");
        if (!KMS_ISSUER_CERT_PATH.isEmpty()) {
            requireFile(path(rootDir, KMS_ISSUER_CERT_PATH), "KMS issuer certificate");
        }
        byte[] certificate = Files.readAllBytes(new File(rootDir, KMS_SIGNER_CERT_PATH).toPath());
        byte[][] chain = KMS_ISSUER_CERT_PATH.isEmpty()
                ? null
                : new byte[][] {
                          Files.readAllBytes(new File(rootDir, KMS_ISSUER_CERT_PATH).toPath())};
        CPDFAwsKmsConfig kms =
                new CPDFAwsKmsConfig()
                        .setEndpoint(KMS_ENDPOINT)
                        .setRegion(KMS_REGION)
                        .setKeyId(KMS_KEY_ID)
                        .setCredentials(KMS_ACCESS_KEY_ID, KMS_SECRET_ACCESS_KEY, KMS_SESSION_TOKEN)
                        .setSigningAlgorithm(KMS_SIGNING_ALGORITHM)
                        .setCertificate(certificate, chain);
        CPDFSignatureCreateResult result = CPDFAwsKmsSigner.sign(path(rootDir, INPUT_PDF),
                outputPrefix(rootDir, "Kms") + ".pdf",
                new CPDFSignatureOptions()
                        .setFieldName(KMS_FIELD_NAME)
                        .setValidationOptions(
                                new CPDFSignatureValidationOptions()
                                        .setTrustedCertPath(path(rootDir, KMS_TRUSTED_CERT_PATH))
                                        .setCheckRevocation(CHECK_REVOCATION)),
                kms);
        System.out.println(
                "Sign success=" + result.isSuccess() + " output=" + result.getOutputPath());
        for (CPDFValidationReportItem item : result.getErrors()) {
            System.out.println(item);
        }
        printValidationResult(result.getValidationResult());
        if (!result.isSuccess()) {
            throw new IllegalStateException("KMS signature failed; inspect diagnostic codes above");
        }
        // Ordinary KMS signatures need not satisfy PAdES-LT; report the actual verdict.
        verifySignedPdf(result.getOutputPath(), path(rootDir, KMS_TRUSTED_CERT_PATH));
    }

    private static void requireFile(String filename, String description) {
        if (!Files.isRegularFile(new File(filename).toPath())
                || !Files.isReadable(new File(filename).toPath())) {
            throw new IllegalStateException(description + " is missing or unreadable: " + filename);
        }
    }

    // Preserve earlier results when the sample is run repeatedly.
    private static String outputPrefix(String rootDir, String name) {
        for (int number = 1;; number++) {
            String prefix =
                    path(rootDir, OUTPUT_DIR + "/" + name + (number == 1 ? "" : "-" + number));
            boolean exists = false;
            for (String suffix : new String[] {".pdf", "-signed.pdf", "-lt.pdf", "-lta.pdf"}) {
                exists |= Files.exists(new File(prefix + suffix).toPath(),
                        java.nio.file.LinkOption.NOFOLLOW_LINKS);
            }
            if (!exists) {
                return prefix;
            }
        }
    }

    /** Owns only this sample's local process; existing services are never stopped. */
    private static final class LocalServices implements AutoCloseable {
        private final Process process;

        private LocalServices(Process process) {
            this.process = process;
        }

        static LocalServices start(String rootDir) throws Exception {
            if (!USE_LOCAL_TEST_SERVICES || SAMPLE_MODE == SampleMode.KMS) {
                return new LocalServices(null);
            }
            ProcessBuilder command;
            if (USE_ANDROID_TEST_PKI) {
                requireFile(path(rootDir, PKCS12_PATH),
                        "Android test signer (run PrepareAndroidTestPki.py on the host first)");
                requireFile(path(rootDir, TRUSTED_CERT_PATH), "Android test trust bundle");
                System.out.println("Using Android demo PKI and TSA: " + TSA_URL);
                System.out.println("Starting local test CRL service: " + CRL_URL);
                command = new ProcessBuilder(LOCAL_PYTHON,
                        new File("AndroidTestCrlService.py").getAbsolutePath(),
                        path(rootDir, CERTIFICATE_DIR),
                        CRL_URL);
            } else {
                System.out.println(
                        "Preparing local test certificates and TSA/CRL service: " + TSA_URL);
                command = new ProcessBuilder(LOCAL_PYTHON,
                        new File("LocalSignatureServices.py").getAbsolutePath(),
                        path(rootDir, CERTIFICATE_DIR),
                        PKCS12_PASSWORD,
                        TSA_URL);
            }
            Process process = command.redirectError(ProcessBuilder.Redirect.INHERIT).start();
            LocalServices services = new LocalServices(process);
            ExecutorService reader = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "sample-tsa-startup");
                thread.setDaemon(true);
                return thread;
            });
            try {
                Future<String> ready = reader.submit(() -> {
                    BufferedReader output = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "UTF-8"));
                    return output.readLine();
                });
                if (!"LOCAL_SIGNATURE_SERVICES_READY".equals(ready.get(60, TimeUnit.SECONDS))) {
                    throw new IOException(
                            "Test service setup failed; see the diagnostic above. Python 3 and OpenSSL are required; port 18080 must be free.");
                }
                System.out.println("Local test certificates: " + path(rootDir, CERTIFICATE_DIR));
                return services;
            } catch (Exception error) {
                services.close();
                throw error;
            } finally {
                reader.shutdownNow();
            }
        }

        public void close() {
            if (process == null) {
                return;
            }
            try {
                // EOF requests a graceful shutdown; also works if the parent JVM exits.
                process.getOutputStream().close();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException error) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            } catch (IOException error) {
                process.destroyForcibly();
            }
        }
    }

    private static void printPadesResult(CPDFPadesResult result) {
        System.out.println("Process success=" + result.isSuccess()
                + " target satisfied=" + result.isTargetProfileSatisfied());
        System.out.println("Last completed output=" + result.getFinalPath());
        System.out.println("Errors=" + Arrays.toString(result.getErrors()));
        System.out.println("Warnings=" + Arrays.toString(result.getWarnings()));
        printValidationResult(result.getValidationResult());
        if (!result.isSuccess() || !result.isTargetProfileSatisfied()) {
            throw new IllegalStateException(
                    "Requested profile was not fully verified; inspect the report");
        }
    }

    /** Same per-signature diagnostics as the Android demo's rich validation panel. */
    private static void printValidationResult(CPDFSignatureValidationResult result) {
        System.out.println(
                "success=" + result.isSuccess() + " overall=" + result.getOverallStatus());
        for (CPDFValidationReportItem error : result.getErrors()) {
            System.out.println("error: " + error);
        }
        for (CPDFValidationReportItem warning : result.getWarnings()) {
            System.out.println("warning: " + warning);
        }
        for (CPDFSignatureValidationReport report : result.getReports()) {
            System.out.println("\nfield=" + report.getFieldName());
            System.out.println("overall=" + report.getOverallStatus());
            System.out.println("subFilter=" + report.getSubFilter()
                    + " digest=" + report.getDigestAlgorithm());
            System.out.println("coversWholeDocument=" + report.coversWholeDocument());
            System.out.println("embedded timestamp=" + report.hasEmbeddedTimestamp());
            System.out.println("LTV enabled=" + report.isLtvEnabled());
            for (CPDFValidationReportItem item : report.getItems()) {
                System.out.println("  " + item);
            }
        }
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
}
