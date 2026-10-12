import com.compdfkit.core.signature.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Complete external CMS container example. All sample settings are fixed below. */
public final class SignatureContainerTest {
    private static final String ROOT_DIR = "..";
    private static final String INPUT_PDF = "TestFiles/CommonFivePage.pdf";
    private static final String OUTPUT_DIR = "out/SignatureContainerTest";
    private static final String CERTIFICATE_DIR = "TestFiles/SignatureContainer";
    private static final String TRUSTED_CERT_PATH = CERTIFICATE_DIR + "/ca.pem";
    private static final boolean USE_LOCAL_TEST_SERVICE = true;
    private static final int LOCAL_SERVICE_PORT = 18081;
    private static final String SERVICE_URL = "http://127.0.0.1:18081/cms";
    private static final String LOCAL_PYTHON = "python3";
    private static final String FIELD_NAME = "ExternalContainerSignature";
    private static final int CONTENTS_SIZE = 65536;

    static {
        CPDFLicense.init();
    }

    public static void main(String[] args) throws Exception {
        Path root = new File(ROOT_DIR).getCanonicalFile().toPath();
        Files.createDirectories(root.resolve(OUTPUT_DIR));
        try (LocalService service = LocalService.start(root)) {
            CPDFSignatureValidationOptions validation = new CPDFSignatureValidationOptions()
                    .setTrustedCertPath(root.resolve(TRUSTED_CERT_PATH).toString())
                    .setCheckRevocation(true);
            CPDFSignatureOptions options = new CPDFSignatureOptions()
                    .setFieldName(FIELD_NAME)
                    .setVisible(false)
                    .setEstimatedContentsSize(CONTENTS_SIZE)
                    .setValidationOptions(validation);
            Path output = nextOutput(root);
            CPDFSignatureCreateResult result = CPDFExternalSignatureSigner.signWithContainer(
                    root.resolve(INPUT_PDF).toString(), output.toString(), options,
                    new HttpContainerProvider(SERVICE_URL));
            System.out.println("Creation success=" + result.isSuccess());
            System.out.println("Signed PDF=" + result.getOutputPath());
            System.out.println("Errors=" + Arrays.toString(result.getErrors()));
            System.out.println("Warnings=" + Arrays.toString(result.getWarnings()));
            System.out.println("Post-sign validation=" + result.getValidationResult());
            if (!result.isSuccess()) {
                throw new IllegalStateException("External container signing failed; inspect the errors");
            }

            // Independently reopen the published PDF, as in the Android Sign Test flow.
            CPDFSignatureValidationResult verified =
                    CPDFSignatureValidator.validate(result.getOutputPath(), validation);
            System.out.println("Published PDF validation=" + verified);
            boolean integrityValid = false;
            for (CPDFSignatureValidationReport report : verified.getReports()) {
                System.out.println(report);
                System.out.println("LTV enabled=" + report.isLtvEnabled());
                for (CPDFValidationReportItem item : report.getItems()) {
                    System.out.println(item);
                }
                integrityValid |= report.getFieldName().equals(FIELD_NAME)
                        && report.hasItem("integrity.valid");
            }
            if (!integrityValid) {
                throw new IllegalStateException("Published signature integrity did not pass verification");
            }
            System.out.println("The local service creates an ordinary detached signature without a TSA.");
            System.out.println("An INDETERMINATE LTV report does not establish LT/LTA; inspect every check.");
            System.out.println("Trust certificate=" + root.resolve(TRUSTED_CERT_PATH));
        }
    }

    private static Path nextOutput(Path root) {
        Path directory = root.resolve(OUTPUT_DIR);
        Path output = directory.resolve("Container-signed.pdf");
        int index = 1;
        while (Files.exists(output)) {
            output = directory.resolve("Container-" + index++ + "-signed.pdf");
        }
        return output;
    }

    private static final class LocalService implements AutoCloseable {
        private final Process process;

        private LocalService(Process process) {
            this.process = process;
        }

        static LocalService start(Path root) throws Exception {
            if (!USE_LOCAL_TEST_SERVICE) {
                return new LocalService(null);
            }
            Process process = new ProcessBuilder(LOCAL_PYTHON,
                    new File("LocalContainerService.py").getAbsolutePath(),
                    root.resolve(CERTIFICATE_DIR).toString(), Integer.toString(LOCAL_SERVICE_PORT))
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            LocalService service = new LocalService(process);
            ExecutorService reader = Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "sample-container-startup");
                thread.setDaemon(true);
                return thread;
            });
            try {
                Future<String> ready = reader.submit(() -> {
                    BufferedReader output = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), "UTF-8"));
                    return output.readLine();
                });
                if (!"LOCAL_CONTAINER_SERVICE_READY".equals(ready.get(60, TimeUnit.SECONDS))) {
                    throw new IOException("Local container service failed; check Python 3, OpenSSL and port 18081");
                }
                return service;
            } catch (Exception exception) {
                service.close();
                throw exception;
            } finally {
                reader.shutdownNow();
            }
        }

        @Override
        public void close() {
            if (process == null) {
                return;
            }
            try {
                process.getOutputStream().close();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            } catch (IOException exception) {
                process.destroyForcibly();
            }
        }
    }
}
