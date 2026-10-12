import com.compdfkit.core.signature.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/** Fixed-configuration two-phase signing sample with optional document timestamping. */
public final class SignatureTwoPhaseTest {
    private enum Phase {
        PREPARE,
        FILL,
        BOTH
    }

    private static final Phase PHASE = Phase.BOTH;
    private static final CPDFExternalSignPrepareOptions.HashMode HASH_MODE =
            CPDFExternalSignPrepareOptions.HashMode.SIGNED_ATTRIBUTES;
    private static final CPDFSignatureDigest DIGEST = CPDFSignatureDigest.SHA256;
    private static final boolean USE_COMPLETE_CONTAINER = false;
    private static final boolean APPEND_DOCUMENT_TIMESTAMP = true;
    private static final String INPUT = "TestFiles/CommonFivePage.pdf";
    private static final String CERTIFICATES = "TestFiles/local-certificates";
    private static final String OUTPUT = "out/SignatureTwoPhaseTest";
    private static final String TSA_URL = "http://127.0.0.1:18080/tsa";
    private static final String PASSWORD = "test-only";

    public static void main(String[] ignored) throws Exception {
        CPDFLicense.init();
        Path samples = Paths.get("..").toAbsolutePath().normalize();
        Path certificates = samples.resolve(CERTIFICATES);
        Path output = samples.resolve(OUTPUT);
        Files.createDirectories(output);
        if (USE_COMPLETE_CONTAINER
                && HASH_MODE != CPDFExternalSignPrepareOptions.HashMode.DOCUMENT_DIGEST) {
            throw new IllegalArgumentException("Complete containers use DOCUMENT_DIGEST");
        }
        try (SignatureSessionSupport.LocalTsa service =
                SignatureSessionSupport.LocalTsa.start(samples, certificates)) {
            Path prepared = output.resolve("prepared.pdf");
            Path sessionFile = output.resolve("session.txt");
            Path payloadFile = output.resolve("signature-payload.bin");
            Path signed = output.resolve("signed.pdf");
            Path timestamped = output.resolve("timestamped.pdf");
            CPDFExternalSignPrepareResult preparation = null;
            if (PHASE != Phase.FILL) {
                Files.deleteIfExists(prepared);
                Files.deleteIfExists(sessionFile);
                preparation = CPDFExternalSignSession.prepare(samples.resolve(INPUT).toString(),
                        prepared.toString(), new CPDFExternalSignPrepareOptions()
                                .setFieldName("TwoPhaseSignature")
                                .setHashMode(HASH_MODE)
                                .setDigestAlgorithm(DIGEST)
                                .setCertificate(Files.readAllBytes(certificates.resolve("signer.der")),
                                        new byte[][] {Files.readAllBytes(certificates.resolve("ca.der"))}));
                require(preparation.isSuccess(),
                        preparation.getErrorCode() + ": " + preparation.getErrorMessage());
                CPDFExternalSignSession.saveSession(sessionFile.toString(), preparation.getSession());
                Files.write(output.resolve("hash-to-sign.bin"), preparation.getHashToSign());
                System.out.println("Prepared PDF: " + preparation.getPreparedPath());
                System.out.println("Session: " + sessionFile);
                System.out.println("Hash mode: " + HASH_MODE + ", digest: " + preparation.getDigestAlgorithm());
                System.out.println("Hash to sign (Base64): "
                        + Base64.getEncoder().encodeToString(preparation.getHashToSign()));
                if (PHASE == Phase.PREPARE) {
                    System.out.println("Prepare complete; persist this transaction and obtain its external signature.");
                    return;
                }
                byte[] payload = USE_COMPLETE_CONTAINER
                        ? SignatureSessionSupport.container(certificates, output.resolve("cms-work"),
                                preparation, Files.readAllBytes(prepared))
                        : SignatureSessionSupport.signDigest(certificates.resolve("signer.p12"),
                                PASSWORD, DIGEST, preparation.getHashToSign());
                Files.write(payloadFile, payload);
            }

            Files.deleteIfExists(signed);
            String session = CPDFExternalSignSession.loadSession(sessionFile.toString());
            CPDFSignatureValidationOptions validation = new CPDFSignatureValidationOptions()
                    .setTrustedCertPath(certificates.resolve("ca.pem").toString());
            CPDFExternalSignFillOptions fillOptions = new CPDFExternalSignFillOptions()
                    .setCertificate(Files.readAllBytes(certificates.resolve("signer.der")),
                            new byte[][] {Files.readAllBytes(certificates.resolve("ca.der"))})
                    .setValidationOptions(validation);
            CPDFExternalSignFillResult filled = USE_COMPLETE_CONTAINER
                    ? CPDFExternalSignSession.fillWithContainer(session, prepared.toString(),
                            signed.toString(), Files.readAllBytes(payloadFile), fillOptions)
                    : CPDFExternalSignSession.fillWithSignatureValue(session, prepared.toString(),
                            signed.toString(), Files.readAllBytes(payloadFile), fillOptions);
            require(filled.isSuccess(), filled.getErrorCode() + ": " + filled.getErrorMessage());
            System.out.println("Fill success: " + filled.getOutputPath());
            System.out.println(filled.getValidationResult());
            require(filled.getValidationResult().getReports().length == 1
                    && filled.getValidationResult().getReports()[0].hasItem("integrity.valid"),
                    "External signature verification failed");
            if (APPEND_DOCUMENT_TIMESTAMP) {
                Files.deleteIfExists(timestamped);
                CPDFSignatureCreateResult timestamp = CPDFTimestampService.addDocumentTimestamp(
                        signed.toString(), timestamped.toString(),
                        new CPDFTimestampOptions().setTsaUrl(TSA_URL).setTransport(10, 0, ""),
                        new CPDFSignatureOptions().setFieldName("TwoPhaseDocumentTime")
                                .setValidationOptions(validation));
                require(timestamp.isSuccess(), "Document timestamp creation failed");
                System.out.println("Document timestamp: " + timestamp.getOutputPath());
                System.out.println(timestamp.getValidationResult());
                require(timestamp.getValidationResult().getOverallStatus() == CPDFValidationStatus.VALID,
                        "Document timestamp did not validate");
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
