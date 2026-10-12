import com.compdfkit.core.signature.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/** Embedded signature timestamp, independent document timestamp and TSA token requests. */
public final class SignatureTimestampTest {
    private static final String INPUT = "TestFiles/CommonFivePage.pdf";
    private static final String CERTIFICATES = "TestFiles/local-certificates";
    private static final String OUTPUT = "out/SignatureTimestampTest";
    private static final String PASSWORD = "test-only";
    private static final String TSA_URL = "http://127.0.0.1:18080/tsa";

    public static void main(String[] ignored) throws Exception {
        CPDFLicense.init();
        Path samples = Paths.get("..").toAbsolutePath().normalize();
        Path certificates = samples.resolve(CERTIFICATES);
        Path output = samples.resolve(OUTPUT);
        Files.createDirectories(output);
        try (SignatureSessionSupport.LocalTsa service =
                SignatureSessionSupport.LocalTsa.start(samples, certificates)) {
            CPDFSignatureValidationOptions validation = new CPDFSignatureValidationOptions()
                    .setTrustedCertPath(certificates.resolve("ca.pem").toString());
            Path embedded = output.resolve("embedded-timestamp.pdf");
            Files.deleteIfExists(embedded);
            CPDFPadesResult signed = CPDFPadesSigner.sign(samples.resolve(INPUT).toString(),
                    embedded.toString(), null, null, new CPDFPadesOptions()
                            .setProfile(CPDFPadesOptions.Profile.T).setVisible(false)
                            .setCertificate(certificates.resolve("signer.p12").toString(), PASSWORD)
                            .setTsaUrl(TSA_URL).setTsaTransport(10, 0, "")
                            .setTrustedCertPath(certificates.resolve("ca.pem").toString())
                            .setValidationOptions(validation));
            require(signed.isSuccess() && signed.isTargetProfileSatisfied(),
                    "Embedded signature timestamp failed");
            System.out.println("Embedded timestamp: " + signed.getFinalPath());
            System.out.println(signed.getValidationResult());

            CPDFTimestampOptions tsa = new CPDFTimestampOptions()
                    .setTsaUrl(TSA_URL).setTransport(10, 0, "");
            Path document = output.resolve("document-timestamp.pdf");
            Files.deleteIfExists(document);
            CPDFSignatureCreateResult stamped = CPDFTimestampService.addDocumentTimestamp(
                    embedded.toString(), document.toString(), tsa,
                    new CPDFSignatureOptions().setFieldName("DocumentTime")
                            .setValidationOptions(validation));
            require(stamped.isSuccess()
                    && stamped.getValidationResult().getOverallStatus() == CPDFValidationStatus.VALID,
                    "Document timestamp failed");
            System.out.println("Document timestamp: " + stamped.getOutputPath());
            System.out.println(stamped.getValidationResult());

            byte[] bytes = Files.readAllBytes(samples.resolve(INPUT));
            CPDFTimestampResult data = CPDFTimestampService.timestampData(bytes, tsa);
            require(data.isSuccess(), data.getErrorCode() + ": " + data.getErrorMessage());
            Files.write(output.resolve("data-timestamp-token.der"), data.getTokenDer());
            System.out.println("Data timestamp token: " + data.getGenerationTime());
            CPDFTimestampResult digest = CPDFTimestampService.timestampDigest(
                    MessageDigest.getInstance("SHA-256").digest(bytes), tsa);
            require(digest.isSuccess(), digest.getErrorCode() + ": " + digest.getErrorMessage());
            Files.write(output.resolve("digest-timestamp-token.der"), digest.getTokenDer());
            System.out.println("Digest timestamp token: " + digest.getGenerationTime());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
