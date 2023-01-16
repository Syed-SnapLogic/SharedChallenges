/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syed.test.bouncycastleleakage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author gaian
 */
public class GenKeys {

    private static final String COMMAND = "";

    public static void main(String s[]) {
        int noOfFiles = 2000;
        String path = null;
        for (int i = 1; i <= noOfFiles; i++) {
            System.out.println("Generating " + i + "th file");
            path = "/tmp/" + i + "/";
            try {
                Files.createDirectories(Paths.get(path));
            } catch (FileAlreadyExistsException faee) {
                // NO OP
            } catch (IOException ioe) {
                System.out.println("couldn't complete due to " + ioe.getMessage());
                ioe.printStackTrace();
            }
            path = path + "key.pem";
            try {
                String cmd = "openssl genpkey -algorithm RSA -out " + path + " -aes-256-cbc -pass pass:syedbhai" + i;
                System.out.println("cmd:<" + cmd + ">");
                Process p = Runtime.getRuntime().exec(cmd);
                System.out.println("o/p");
                InputStream is = p.getInputStream();
                if (is.available() > 0) {
                    try ( BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                }
                System.out.println("err");
                is = p.getErrorStream();
                if (is.available() > 0) {
                    try ( BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                }
                p.waitFor();
            } catch (Exception ioe) {
                System.out.println("unable to proceed");
                ioe.printStackTrace();
            }
        }

    }
}
