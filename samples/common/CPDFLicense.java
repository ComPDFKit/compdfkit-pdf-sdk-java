import com.compdfkit.core.internal.Constants;
import com.compdfkit.core.internal.NativeKMPDFKit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Shared license loader for every sample under ComPDFKit_java/samples.
 *
 * <p>The license key lives in one common file (samples/common/license_key.txt)
 * so it only has to be updated in a single place. Each sample calls
 * {@link #init()} from its static initializer instead of hard-coding the key.
 *
 * <p>The license file is located by, in order:
 * <ol>
 *   <li>the {@code compdfkit.license.file} system property,</li>
 *   <li>the {@code COMPDFKIT_LICENSE_FILE} environment variable,</li>
 *   <li>walking up from the working directory, checking both {@code
 *       license_key.txt} and {@code common/license_key.txt} at each level
 *       (RunTest.sh runs from &lt;sample&gt;, so samples/common is picked
 *       up automatically).</li>
 * </ol>
 */
public final class CPDFLicense {

    public static final String LICENSE_FILE_NAME = "license_key.txt";
    private static final String COMMON_DIR_NAME = "common";

    private static final String LICENSE_FILE_PROPERTY = "compdfkit.license.file";
    private static final String LICENSE_FILE_ENV = "COMPDFKIT_LICENSE_FILE";
    private static final int MAX_PARENT_LEVELS = 8;

    private static boolean initialized = false;

    private CPDFLicense() {
    }

    /**
     * Initializes the SDK with the key read from the common license file.
     * Repeated calls are ignored.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        try {
            NativeKMPDFKit.initialize(
                    readLicenseKey(), "", "ComPDFKit", Constants.LIB_DIR_NAME);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }

    /** Reads the license key from the common license file. */
    public static String readLicenseKey() {
        File file = locateLicenseFile();
        String content;
        try {
            content = new String(
                    Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read license file: " + file, e);
        }
        String key = stripComments(content);
        if (key.isEmpty()) {
            throw new IllegalStateException(
                    "License file contains no license key: " + file);
        }
        return key;
    }

    /** Drops blank and '#' comment lines, then joins the rest. */
    private static String stripComments(String content) {
        StringBuilder key = new StringBuilder();
        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            key.append(trimmed);
        }
        return key.toString();
    }

    private static File locateLicenseFile() {
        String override = System.getProperty(LICENSE_FILE_PROPERTY);
        if (override == null || override.trim().isEmpty()) {
            override = System.getenv(LICENSE_FILE_ENV);
        }
        if (override != null && !override.trim().isEmpty()) {
            File file = new File(override.trim());
            if (!file.isFile()) {
                throw new IllegalStateException("License file not found: " + file);
            }
            return file;
        }

        File dir = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        for (int level = 0; dir != null && level <= MAX_PARENT_LEVELS; level++) {
            File candidate = new File(dir, LICENSE_FILE_NAME);
            if (candidate.isFile()) {
                return candidate;
            }
            File commonCandidate =
                    new File(new File(dir, COMMON_DIR_NAME), LICENSE_FILE_NAME);
            if (commonCandidate.isFile()) {
                return commonCandidate;
            }
            dir = dir.getParentFile();
        }
        throw new IllegalStateException(
                "Cannot find " + LICENSE_FILE_NAME + " in "
                        + System.getProperty("user.dir")
                        + " or any parent directory. Set -D"
                        + LICENSE_FILE_PROPERTY + "=<path> or the "
                        + LICENSE_FILE_ENV + " environment variable.");
    }
}
