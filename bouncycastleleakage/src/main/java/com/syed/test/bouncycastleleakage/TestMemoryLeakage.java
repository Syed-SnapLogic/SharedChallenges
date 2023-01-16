/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syed.test.bouncycastleleakage;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

/**
 *
 * @author gaian
 */
public class TestMemoryLeakage {

    private static final String HEADER_START = "-----";
    private static final String BEGIN_HEADER_START = HEADER_START + "BEGIN";
    private static final String END_HEADER_START = HEADER_START + "END";

    public static void main(String s[]) {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(bouncyCastleProvider);

        for (int i = 1; i < 2; i++) {
            System.out.println("turn: " + i + "th");
            try {
                String privKeyStrBase64Encoded = Files.readString(Paths.get("/tmp/" + i + "/key.pem"));
                String privateKeyPassphrase = "syed1234";

                if (false) {
                    if (true) {
                        System.out.println("<" + privKeyStrBase64Encoded + ">");
                        privKeyStrBase64Encoded = convertPrivateKeyToMultiline(privKeyStrBase64Encoded);
                        privKeyStrBase64Encoded = privKeyStrBase64Encoded.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "");
                        privKeyStrBase64Encoded = privKeyStrBase64Encoded.replaceAll(System.lineSeparator(), "");
                        privKeyStrBase64Encoded = privKeyStrBase64Encoded.replace("-----END ENCRYPTED PRIVATE KEY-----", "");
                        System.out.println("<" + privKeyStrBase64Encoded + ">");
                        byte[] encryptedPKInfo = Base64.getDecoder().decode(privKeyStrBase64Encoded);
                        EncryptedPrivateKeyInfo ePKInfo = new EncryptedPrivateKeyInfo(encryptedPKInfo);
                        char[] password = privateKeyPassphrase.toCharArray();
                        System.out.println(ePKInfo.getAlgName());
                        Cipher cipher = Cipher.getInstance(ePKInfo.getAlgName()); //Cannot find any provider supporting 1.2.840.113549.1.5.13
                        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
                        // Now create the Key from the PBEKeySpec
                        SecretKeyFactory skFac = SecretKeyFactory.getInstance(ePKInfo.getAlgName());
                        Key pbeKey = skFac.generateSecret(pbeKeySpec);
                        // Extract the iteration count and the salt
                        AlgorithmParameters algParams = ePKInfo.getAlgParameters();
                        cipher.init(Cipher.DECRYPT_MODE, pbeKey, algParams);
                        // Decrypt the encryped private key into a PKCS8EncodedKeySpec
                        KeySpec pkcs8KeySpec = ePKInfo.getKeySpec(cipher);
                        // Now retrieve the RSA Public and private keys by using an
                        // RSA keyfactory.
                        KeyFactory rsaKeyFac = KeyFactory.getInstance("RSA");
                        // First get the private key
                        RSAPrivateCrtKey rsaPriv = (RSAPrivateCrtKey) rsaKeyFac.generatePrivate(pkcs8KeySpec);
                        System.out.println("Private key generated:" + rsaPriv.getClass().getName());
                    } else {
                        System.out.println("using alternate route");
                        String formattedPrivateKey = convertPrivateKeyToMultiline(privKeyStrBase64Encoded);
                        EncryptedPrivateKeyInfo encryptPKInfo = new EncryptedPrivateKeyInfo(formattedPrivateKey.getBytes());
                        Cipher cipher = Cipher.getInstance(encryptPKInfo.getAlgName());
                        PBEKeySpec pbeKeySpec = new PBEKeySpec(privateKeyPassphrase.toCharArray());
                        SecretKeyFactory secFac = SecretKeyFactory.getInstance(encryptPKInfo.getAlgName());
                        Key pbeKey = secFac.generateSecret(pbeKeySpec);
                        AlgorithmParameters algParams = encryptPKInfo.getAlgParameters();
                        cipher.init(Cipher.DECRYPT_MODE, pbeKey, algParams);
                        KeySpec pkcs8KeySpec = encryptPKInfo.getKeySpec(cipher);
                        KeyFactory kf = KeyFactory.getInstance("RSA");
                        PrivateKey pk = kf.generatePrivate(pkcs8KeySpec);
                        System.out.println("private key generated successfully");
                    }
                } else {
                    String formattedPrivateKey = convertPrivateKeyToMultiline(privKeyStrBase64Encoded);
                    System.out.println("formatted the pk");
                    try ( PEMParser pemParser = new PEMParser(new StringReader(formattedPrivateKey))) {
                        PrivateKeyInfo privateKeyInfo;
                        Object pemObject = pemParser.readObject();

                        Security.addProvider(new BouncyCastleProvider());
                        System.out.println("added provicer to the Security instance");

                        if (pemObject == null) {
                            throw new RuntimeException("failed");
                        }
                        org.bouncycastle.openssl.PEMEncryptedKeyPair kp;

                        if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
                            System.out.println("if cond satisfied");
                            PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo
                                    = (PKCS8EncryptedPrivateKeyInfo) pemObject;
                            InputDecryptorProvider provider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                    .setProvider(new BouncyCastleProvider())
                                    .build(privateKeyPassphrase.toCharArray());
                            System.out.println("added provider to the decryptor");
                            privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(provider);
                        } else if (pemObject instanceof PrivateKeyInfo) {
                            throw new RuntimeException("some error");
                        } else {
                            throw new RuntimeException("other");
                        }

                        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                                .setProvider(BouncyCastleProvider.PROVIDER_NAME);
                        PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);
                        System.out.println("PrivateKey instance generated: " + privateKey.getAlgorithm());
                    } catch (Throwable t) {
                        System.out.println("inner try error");
                        t.printStackTrace();
                    }
                }

            } catch (Throwable e) {
                System.out.println("outer retry error");
                e.printStackTrace();
            }
        }
        // Capture the heap dump before existing
        try {
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                    "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap("/tmp/md/heap5.hprof", true);
            System.out.println("done generating the heap dump");
        } catch (Throwable t) {
            System.out.println("error getting heap dump");
            t.printStackTrace();
        }
    }

    public static String convertPrivateKeyToMultiline(String privateKey) {
        if (privateKey == null || privateKey.isEmpty()) {
            throw new IllegalArgumentException("privateKey cannot be blank, empty, or null.");
        }
        // Detect if privateKey is a single-line string, and insert newlines as needed.
        // This method expects the proper formatting of the private key, except for the
        // following: Spaces have been substituted for newline characters, and the key contains
        // no newlines.
        if (privateKey.contains("\n")) {
            return privateKey;
        }
        StringBuilder newPrivateKey = new StringBuilder(privateKey);
        try {
            // Fix begin header
            int startBeginHeaderIndex = newPrivateKey.indexOf(BEGIN_HEADER_START);
            int endBeginHeaderIndex = newPrivateKey.indexOf(HEADER_START, startBeginHeaderIndex
                    + BEGIN_HEADER_START.length()) + HEADER_START.length();
            newPrivateKey.insert(endBeginHeaderIndex, '\n');
            removeSpaces(newPrivateKey, endBeginHeaderIndex + 1);
            // Fix information section.
            int currentIndex = endBeginHeaderIndex + 2;
            boolean foundInfoSection = false;
            while ((currentIndex = newPrivateKey.indexOf(":", currentIndex)) > -1) {
                foundInfoSection = true;
                currentIndex = newPrivateKey.indexOf(" ", currentIndex + 3) + 1;
                newPrivateKey.insert(currentIndex, '\n');
                removeSpaces(newPrivateKey, currentIndex - 1);
            }
            // Get back to the end.
            currentIndex = newPrivateKey.lastIndexOf("\n");
            if (foundInfoSection) {
                currentIndex = newPrivateKey.indexOf(" ", currentIndex + 1) + 1;
                newPrivateKey.insert(currentIndex, '\n');
                removeSpaces(newPrivateKey, currentIndex - 1);
                currentIndex = newPrivateKey.lastIndexOf("\n");
            }
            // Fix base64-encoded key to be formatted to 64 columns.
            int columns = 0;
            do {
                if (++columns > 64) {
                    newPrivateKey.insert(currentIndex + 1, '\n');
                    removeSpaces(newPrivateKey, currentIndex + 2);
                    columns = 0;
                }
            } while (++currentIndex < newPrivateKey.indexOf(END_HEADER_START));

            // Insert newline if less than 64 columns on last part.
            if (columns > 1) {
                newPrivateKey.insert(currentIndex, '\n');
                // We're technically just past the beginning of the end header here.
                removeSpaces(newPrivateKey, currentIndex - 1);
            }

            // Fix end of file.
            newPrivateKey.append('\n');
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("privateKey formatting is invalid");
        }

        return newPrivateKey.toString();
    }

    private static void removeSpaces(StringBuilder sb, int currentIndex) {
        // New lines get converted to spaces when copy-pasted into a pipeline parameter.
        while (sb.charAt(currentIndex) == ' ') {
            sb.deleteCharAt(currentIndex);
        }
    }
}
