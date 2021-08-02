package com.syed.test.db.performance;

import java.io.FileInputStream;
import java.util.Scanner;

public class Executor {
    public static void main(String t[]) throws Exception {
        if (t.length < 1) {
            System.out.println("Usage: java Executor <path of file containing creds>");
            return;
        }
        try (FileInputStream fis = new FileInputStream(t[0]);
             Scanner s = new Scanner(fis)) {
            System.out.println("Enter credentials for oracle db:");
            System.out.print("\thost: ");
            String oracleHost = s.nextLine();
            System.out.print("\tuser: ");
            String oracleUser = s.nextLine();
            System.out.print("\tpasswd: ");
            String oraclePasswd = s.nextLine();
            System.out.print("\tdb: ");
            String oracleDb = s.nextLine();
            System.out.println("\nEnter credentials for redshift db:");
            System.out.print("\thost: ");
            String rsHost = s.nextLine();
            System.out.print("\tuser: ");
            String rsUser = s.nextLine();
            System.out.print("\tpasswd: ");
            String rsPasswd = s.nextLine();
            System.out.print("\tdb: ");
            String rsDb = s.nextLine();
            System.out.println("Enter credentials for mysql db:");
            System.out.print("\thost: ");
            String msHost = s.nextLine();
            System.out.print("\tuser: ");
            String msUser = s.nextLine();
            System.out.print("\tpasswd: ");
            String msPasswd = s.nextLine();
            System.out.print("\tdb: ");
            String msDb = s.nextLine();
            Thread oracleInsert = new Insert("jdbc:oracle:thin:@" + oracleHost + ":1521/" + oracleDb,
                    oracleUser, oraclePasswd, "oracle.jdbc.driver.OracleDriver", "oracle");
            Thread mysqlInsert = new Insert("jdbc:mysql://" + msHost + ":3306/" + msDb, msUser,
                    msPasswd, "com.mysql.cj.jdbc.Driver", "mysql");
            Thread redshiftInsert = new Insert("jdbc:redshift://" + rsHost + ":5439/" + rsDb +
                    "?tcpKeepAlive=true", rsUser, rsPasswd, "com.amazon.redshift.jdbc42.Driver",
                    "redshift");
            oracleInsert.start();
            mysqlInsert.start();
            redshiftInsert.start();
            oracleInsert.join();
            mysqlInsert.join();
            redshiftInsert.join();
            System.out.println("\n\ncompleted testing all the three dbs");
        }
    }
}
