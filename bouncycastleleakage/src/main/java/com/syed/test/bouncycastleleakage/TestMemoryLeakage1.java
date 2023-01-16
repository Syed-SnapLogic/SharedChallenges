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
import java.security.PrivateKey;
import java.security.Security;

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
public class TestMemoryLeakage1 {
    private static final int NO_THREADS = 1200;
    private static final String HEADER_START = "-----";
    private static final String BEGIN_HEADER_START = HEADER_START + "BEGIN";
    private static final String END_HEADER_START = HEADER_START + "END";
    private static final BouncyCastleProvider BC = new BouncyCastleProvider();

    public static void main(String s[]) {
        // Capture the heap dump before executing
        try {
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                    "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap("/tmp/md/heapb4.hprof", true);
            System.out.println("done generating the heap dump b4");
        } catch (Throwable t) {
            System.out.println("error getting heap dump");
            t.printStackTrace();
        }
        
        Security.addProvider(BC);
        Thread[] ts = new Thread[NO_THREADS];

        for (int i = 0; i < NO_THREADS; i++) {
            System.out.println("turn: " + i + "th");
            try {
                ts[i] = new MyThread(i+1);
                ts[i].start();
            } catch (Throwable e) {
                System.out.println("outer retry error");
                e.printStackTrace();
            }
        }
        for (int i = 0; i < NO_THREADS; i++) {
            try {
                ts[i].join();
            } catch (Throwable t) {
                System.out.println("join error");
                t.printStackTrace();
            }
        }
        // Capture the heap dump after executing
        try {
            HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                    "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap("/tmp/md/heapafter.hprof", true);
            System.out.println("done generating the heap dump after");
        } catch (Throwable t) {
            System.out.println("error getting heap dump");
            t.printStackTrace();
        }
        System.out.println("all good sir, bye");
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

    static class MyThread extends Thread {

        int i;

        MyThread(int i) {
            this.i = i;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                String file = "/tmp/" + i + "/key.pem";
                String privKeyStrBase64Encoded = Files.readString(Paths.get(file));
                String privateKeyPassphrase = "syedbhai" + i;

                String formattedPrivateKey = convertPrivateKeyToMultiline(privKeyStrBase64Encoded);
                System.out.println("pk formatted");
                try ( PEMParser pemParser = new PEMParser(new StringReader(formattedPrivateKey))) {
                    PrivateKeyInfo privateKeyInfo;
                    Object pemObject = pemParser.readObject();

                    if (pemObject == null) {
                        throw new RuntimeException("failed in thread " + i);
                    }
                    System.out.println(pemObject.getClass().getName());

                    if (pemObject instanceof PKCS8EncryptedPrivateKeyInfo) {
                        System.out.println("if cond satisfied");
                        PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo
                                = (PKCS8EncryptedPrivateKeyInfo) pemObject;
                        InputDecryptorProvider provider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                //.setProvider(new BouncyCastleProvider())
                                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                                .build(privateKeyPassphrase.toCharArray());
                        System.out.println("added provider to the decryptor in thread " + i);
                        privateKeyInfo = encryptedPrivateKeyInfo.decryptPrivateKeyInfo(provider);
                    } else if (pemObject instanceof PrivateKeyInfo) {
                        throw new RuntimeException("some error in thread " + i);
                    } else {
                        throw new RuntimeException("other in thread " + i);
                    }

                    JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME);
                    PrivateKey privateKey = converter.getPrivateKey(privateKeyInfo);
                    System.out.println("PrivateKey instance generated in " + i
                            + "th thread: " + privateKey.getAlgorithm());
                } catch (Throwable t) {
                    System.out.println("inner try error in thread " + i);
                    t.printStackTrace();
                }
            } catch (Throwable t) {
                System.out.println("error in thread:" + i);
                t.printStackTrace();
            }
        }
    }
}
