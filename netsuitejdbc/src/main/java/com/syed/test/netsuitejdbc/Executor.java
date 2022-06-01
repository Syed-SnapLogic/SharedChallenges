/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syed.test.netsuitejdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 *
 * @author gaian
 */
public class Executor {

    public static void main(String str[]) throws Exception {
        System.out.println(System.getProperty("java.class.path"));
        boolean cdata = false;
        if (str.length > 0) {
            for (int i = 0; i < str.length; i++) {
                if (str[i].equalsIgnoreCase("cdata")) {
                    cdata = true;
                    break;
                }
            }
        }
        Connection c = null;
        String query;
        try {
            if (cdata) {
                System.out.println("Trying cdata netsuite jdbc driver");
                query = "SELECT * FROM AccountingPeriod";
                Class.forName("cdata.jdbc.netsuite.NetSuiteDriver");
                /*Connection c = DriverManager.getConnection(
                    "jdbc:cdata:netsuite:Account Id=TSTDRV877548;Role Id=3;Version=2019.1", "ops_test@snaplogic.com", "Pwd");*/
                c = DriverManager.getConnection(
                        "jdbc:netsuite:AuthScheme=\"Token\";AccountId=\"TSTDRV877548\";"
                        + "OAuthClientId=\"CID\";"
                        + "OAuthClientSecret=\"CS\";"
                        + "OAuthAccessToken=\"T\";"
                        + "OAuthAccessTokenSecret=\"TS\";");
            } else {
                System.out.println("Trying other netsuite jdbc driver");
                query = "SELECT * from ACCOUNTING_PERIODS";
                Class.forName("com.netsuite.jdbc.openaccess.OpenAccessDriver");
                c = DriverManager.getConnection("jdbc:ns://tstdrv877548.connect.api.netsuite.com:1708;"
                        + "ServerDataSource=NetSuite.com;Encrypted=1;"
                        + "CustomProperties=(AccountID=TSTDRV877548;RoleID=3);", "ops_test@snaplogic.com", "Pwd");
            }
            int rand = 1 + (int) ((Math.random() * 100));
            if (rand % 2 == 0) {
                query += " LIMIT 50";
            }
            //System.out.println("Query: " + query);
            System.out.println("target db: " + c.getMetaData().getDatabaseProductName());
            if (false) {
                try ( Statement s = c.createStatement()) {
                    try ( ResultSet rs = s.executeQuery("select * from sys_tables")) {
                        ResultSetMetaData md = rs.getMetaData();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            System.out.println("    col: " + md.getColumnName(i));
                        }
                        while (rs.next()) {
                            System.out.println("\t" + rs.getString("SchemaName") + "."
                                    + rs.getString("TableName"));
                        }
                    }
                }
            }
            System.out.println("Query: select * from SuiteTalk.SyedsCustRec1 limit 50");
            try ( Statement s = c.createStatement()) {
                try ( ResultSet rs = s.executeQuery("select * from SuiteTalk.SyedsCustRec1 limit 50")) { // select * from SuiteTalk.SyedsCustRec1 limit 50
                    ResultSetMetaData md = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            System.out.println("\t" + md.getColumnName(i) + " : " + rs.getString(md.getColumnName(i)));
                        }
                    }
                }
            }

        } finally {
            if (c != null) {
                c.close();
            }
        }
        System.out.println("done");
    }
}
