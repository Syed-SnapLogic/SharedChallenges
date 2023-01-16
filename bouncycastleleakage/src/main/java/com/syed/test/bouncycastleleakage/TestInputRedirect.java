/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syed.test.bouncycastleleakage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 * @author gaian
 */
public class TestInputRedirect {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("/tmp/1/in.txt");
        System.setIn(fis);
        Process p = Runtime.getRuntime().exec("/bin/sh -c openssl");
        try (PrintStream ps = new PrintStream(p.getOutputStream())) {
            ps.println("genpkey -algorithm RSA -out /tmp/1/key.pem -aes-256-cbc");
        }
        System.out.println("op");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        System.out.println("err");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        p.waitFor();
        System.out.println("done");
    }
    
}
