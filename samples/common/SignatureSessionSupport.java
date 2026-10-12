import com.compdfkit.core.signature.CPDFExternalSignPrepareResult;
import com.compdfkit.core.signature.CPDFSignatureDigest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Local test-only signing helpers. Production applications use their external signer. */
public final class SignatureSessionSupport {
    private SignatureSessionSupport() {
    }

    public static byte[] signDigest(Path pkcs12, String password,
            CPDFSignatureDigest digest, byte[] hash) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (java.io.InputStream input = Files.newInputStream(pkcs12)) {
            store.load(input, password.toCharArray());
        }
        PrivateKey key = null;
        Enumeration<String> aliases = store.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (store.isKeyEntry(alias)) {
                key = (PrivateKey) store.getKey(alias, password.toCharArray());
                break;
            }
        }
        if (key == null || !"RSA".equals(key.getAlgorithm())
                || hash.length != digest.getByteLength()) {
            throw new IllegalArgumentException("Matching RSA key and digest required");
        }
        String prefix;
        switch (digest) {
            case SHA384:
                prefix = "3041300d060960864801650304020205000430";
                break;
            case SHA512:
                prefix = "3051300d060960864801650304020305000440";
                break;
            default:
                prefix = "3031300d060960864801650304020105000420";
                break;
        }
        // NONEwithRSA signs the DER DigestInfo without hashing this digest again.
        Signature signer = Signature.getInstance("NONEwithRSA");
        signer.initSign(key);
        signer.update(hex(prefix));
        signer.update(hash);
        return signer.sign();
    }

    public static byte[] container(Path certificates, Path work,
            CPDFExternalSignPrepareResult prepared, byte[] preparedPdf) throws Exception {
        Files.createDirectories(work);
        int firstLength = Math.toIntExact(prepared.getContentsOffset() - 1);
        int tailOffset = Math.toIntExact(prepared.getContentsOffset()
                + 2L * prepared.getContentsSize() + 1);
        if (firstLength < 0 || tailOffset > preparedPdf.length
                || preparedPdf[firstLength] != '<' || preparedPdf[tailOffset - 1] != '>') {
            throw new IllegalArgumentException("Prepared Contents metadata mismatch");
        }
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        content.write(preparedPdf, 0, firstLength);
        content.write(preparedPdf, tailOffset, preparedPdf.length - tailOffset);
        Path covered = work.resolve("covered.bin");
        Path cms = work.resolve("container.der");
        Files.write(covered, content.toByteArray());
        Process process = new ProcessBuilder("openssl", "cms", "-sign", "-binary",
                "-in", covered.toString(), "-signer", certificates.resolve("signer.pem").toString(),
                "-inkey", certificates.resolve("signer.key").toString(),
                "-certfile", certificates.resolve("ca.pem").toString(),
                "-md", prepared.getDigestAlgorithm().toLowerCase(),
                "-outform", "DER", "-out", cms.toString())
                .redirectErrorStream(true).start();
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int size;
        while ((size = process.getInputStream().read(buffer)) >= 0) {
            log.write(buffer, 0, size);
        }
        if (process.waitFor() != 0) {
            throw new IllegalStateException("Local CMS generation failed: " + log.toString("UTF-8"));
        }
        return Files.readAllBytes(cms);
    }

    private static byte[] hex(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int index = 0; index < bytes.length; index++) {
            bytes[index] = (byte) Integer.parseInt(value.substring(index * 2, index * 2 + 2), 16);
        }
        return bytes;
    }

    /** Starts the existing local PKI/TSA service and stops it when the sample finishes. */
    public static final class LocalTsa implements AutoCloseable {
        private final Process process;

        private LocalTsa(Process process) {
            this.process = process;
        }

        public static LocalTsa start(Path samples, Path certificates) throws Exception {
            Process process = new ProcessBuilder("python3",
                    samples.resolve("SignatureLtvKmsTest/LocalSignatureServices.py").toString(),
                    certificates.toString(), "test-only", "http://127.0.0.1:18080/tsa")
                    .redirectError(ProcessBuilder.Redirect.INHERIT).start();
            LocalTsa service = new LocalTsa(process);
            ExecutorService reader = Executors.newSingleThreadExecutor();
            try {
                Future<String> ready = reader.submit(() -> new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8")).readLine());
                if (!"LOCAL_SIGNATURE_SERVICES_READY".equals(ready.get(60, TimeUnit.SECONDS))) {
                    throw new IllegalStateException("Local TSA did not start");
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
            try {
                process.getOutputStream().close();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            } catch (java.io.IOException exception) {
                process.destroyForcibly();
            }
        }
    }
}
