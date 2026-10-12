# Digital signatures on Java

`SignatureTest` demonstrates visible and invisible digital signatures, certificate
generation and import, certificate trust, signature verification, and signature
removal. Initialize the SDK through the shared `samples/common/CPDFLicense.java`
loader before using the signing APIs.

## Invisible signature

An invisible signature has no visible signature text, image, or logo on a PDF page.
It is embedded as a PDF signature form field with a Widget annotation whose
rectangle is `[0, 0, 0, 0]`. This follows the PDF specification's representation of
an invisible signature field. The field value refers to the signature dictionary,
which stores the signature in `/Contents` and defines the signed bytes in
`/ByteRange`. A PDF reader can inspect and verify the signature through its
signature panel even though no signature appearance is displayed on the page.

For the PDF field and annotation representation, see section 1.2 of the
[PDF Association's Digital Signatures technical note](https://pdfa.org/wp-content/uploads/2011/08/tn0006_digital_signatures_in_pdfa-1_2008-03-141.pdf).

## Run the sample

1. Place the SDK JAR in `samples/lib` and configure the shared sample license in
   `samples/common/license_key.txt`. Use a Java runtime supported by the SDK and
   the native libraries for that operating system.
2. From `samples/SignatureTest`, run `./RunTest.sh`. The input is
   `samples/TestFiles/CommonFivePage.pdf`.
3. The sample first creates the PKCS#12 identity
   `samples/out/SignatureTest/certificate/Certificate.pfx`. The invisible-signature
   example imports that file using the fixed password `ComPDFKit`, checks the
   password and certificate validity, and prints the certificate subject.
4. The example explicitly trusts its generated test certificate in
   `samples/SignatureTest/trusted/invisible-demo.pem`. This is a local test trust
   decision; it does not change Acrobat's or the operating system's trust store.
5. The example creates `InvisibleSignature`, sets
   `widget.setRect(new RectF(0, 0, 0, 0))`, and calls `writeSignature()` without
   configuring a visible appearance. It saves
   `samples/out/SignatureTest/digitalSignature/Invisible_Signed.pdf`.
6. It reopens that exact output and checks the signature count, signer
   certificates, cryptographic integrity, document modification permissions,
   current certificate validity, and certificate-chain trust. A failed check
   throws an exception.

The invisible-signature example should print:

```text
Invisible signature certificate imported: true
Invisible signature creation: true
Invisible signature signer 0 certificate within validity period: true
Invisible signature signer 0 certificate chain trusted: true
Invisible signature verification: true
Invisible signature document permissions valid: true
Invisible signature basic verification passed: true
Invisible signature revocation/LTV checks: not performed
```

The basic result combines integrity, document permissions, certificate validity,
and chain trust. It does not establish revocation status, trusted signing time,
or an LT/LTA profile. Use `SignatureLtvKmsTest` and the developer guide's LTV
section when those checks are required.

## Import an existing signing certificate

Edit the fixed constants in `SignatureTest.java`; no additional command-line
parameters are needed:

- `INVISIBLE_CERTIFICATE_PATH`: the existing `.p12` or `.pfx` file, relative to
  the `samples` directory, or an absolute path. It must contain the private key,
  the signer certificate, and any required intermediate certificates.
- `INVISIBLE_CERTIFICATE_PASSWORD`: the PKCS#12 file's password.
- `INVISIBLE_TRUST_DIRECTORY`: a directory containing independently approved
  trusted certificates in PEM format, relative to `samples`, or an absolute path.
- `TRUST_INVISIBLE_DEMO_CERTIFICATE`: set to `false` for an existing identity.
  The sample's automatic trust step is only for the self-signed test identity it
  generates.

`checkPKCS12Password()` and `getX509ByPKCS12Cert()` read the supplied identity.
Importing an identity does not make it trusted. `verifyGetChain()` checks the
certificate embedded in the signed PDF against the configured trust directory,
and `isExpired()` checks whether it is within its current validity period.

In Acrobat, open the output's signature panel to inspect the invisible signature.
The page itself displays no signature appearance. An untrusted-certificate
message in Acrobat reflects that application's trust settings and is separate
from whether the signature is visible.
