# Java 3.2.0 sample setup

## Dependencies and license

Keep the release layout: each example is directly under `samples`, the SDK is
`lib/compdfkit-3.2.0.jar` relative to this directory, and shared source files
are in `common`. Run each `RunTest.sh` from its own example directory.

No SDK license is distributed. Set `COMPDFKIT_LICENSE_FILE` to an absolute
path to your own local license file, or create `common/license_key.txt`
locally. Both approaches use the existing `CPDFLicense` helper. The helper
also accepts the Java system property `compdfkit.license.file`.

License files, private keys, PKCS#12/PFX files, complete distribution ZIPs,
generated output, and OS metadata are excluded from this repository.
`legal.txt` in the repository root contains legal notices, not an activation key.

## Signing prerequisites

Signing examples are supplied as source code, not as preconfigured signing
identities. Never add customer certificates with private keys or production
credentials to the repository. Public certificates alone cannot perform signing.

The pre-generated contents of these local fixture directories are not included:

- `TestFiles/local-certificates`
- `TestFiles/SignatureContainer`
- `TestFiles/SignatureLtvAndroid`

The entire directories are ignored so that newly generated keys, matching
certificates, and local CA state stay together and are not committed. Do not
restore only part of an old fixture set: its certificates may not match newly
generated keys, and the local test service validates its managed directory.

Before running a signing example, review its constants, select the intended
mode, and prepare the matching local prerequisites:

- `SignatureTest` includes certificate-generation examples and an
  invisible-signature example that can use a locally generated or supplied
  PKCS#12/PFX file. Follow its own `README.md` and configured certificate path.
- `SignatureLtvKmsTest/LocalSignatureServices.py` supplies local test PKI and
  loopback timestamp/CRL services for the examples that start it. The helper
  requires Python 3 and an OpenSSL executable. Generated private material
  stays in the ignored local fixture directories.
- `SignatureLtvKmsTest/PrepareAndroidTestPki.py` imports an existing local test
  PKI; it does not create one. Configure its source path before use.
  The Java example currently sets `USE_ANDROID_TEST_PKI = true`, so this
  external fixture set is required by default. To use its self-contained local
  PKI mode instead, set that constant to `false` and keep
  `USE_LOCAL_TEST_SERVICES = true` before running the example.
- `SignatureContainerTest/LocalContainerService.py` requires matching local
  `signer.key`, `signer.der`, and `ca.der` files. Supply or generate a complete
  test identity before using the local container service.
- Timestamp, two-phase, LTV, and KMS examples must use the matching test PKI
  and service configuration selected in their Java source. AWS KMS mode
  additionally requires your own authorized AWS configuration; no cloud
  credentials are distributed.

Do not assume that compiling a signing example proves end-to-end signing or
validation. Runtime verification requires an appropriate SDK license and
the selected local or external signing services.
