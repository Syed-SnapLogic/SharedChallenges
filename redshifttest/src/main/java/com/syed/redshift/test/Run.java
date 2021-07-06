package com.syed.redshift.test;

import java.sql.Connection;
import java.sql.DriverManager;

public class Run {
    public static void main(String s[]) throws Exception {
        Class.forName("com.amazon.redshift.jdbc42.Driver");
        Connection c = DriverManager.getConnection("jdbc:postgresql://mycluster.com:5439/mydb?tcpKeepAlive=true&OpenSourceSubProtocolOverride=true", "admin", "mypwd123");
        System.out.println("success");
        c.close();
    }
}
