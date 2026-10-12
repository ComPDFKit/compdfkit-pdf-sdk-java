import com.compdfkit.core.signature.CPDFSignatureContainerProvider;
import com.compdfkit.core.signature.CPDFSignatureContainerRequest;
import com.compdfkit.core.signature.CPDFSignatureContainerResult;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Example protocol: POST the binary PDF digest and receive binary CMS DER. */
public final class HttpContainerProvider implements CPDFSignatureContainerProvider {
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;
    private final String endpoint;

    public HttpContainerProvider(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public CPDFSignatureContainerResult signContainer(CPDFSignatureContainerRequest request)
            throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("Accept", "application/pkcs7-signature");
            connection.setRequestProperty("X-Signature-Digest-Algorithm", request.getDigestAlgorithm());
            // Add the real service's authentication here; this loopback demo needs none.
            byte[] digest = request.getDocumentDigest();
            connection.setFixedLengthStreamingMode(digest.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(digest);
            }
            int status = connection.getResponseCode();
            if (status != 200) {
                return CPDFSignatureContainerResult.failure(
                        "service.http.failed", "Container service returned HTTP " + status);
            }
            try (InputStream input = connection.getInputStream();
                    ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (count > MAX_RESPONSE_BYTES - output.size()) {
                        return CPDFSignatureContainerResult.failure(
                                "service.response.too_large", "Container response exceeded the sample limit");
                    }
                    output.write(buffer, 0, count);
                }
                return CPDFSignatureContainerResult.success(output.toByteArray());
            }
        } finally {
            connection.disconnect();
        }
    }
}
