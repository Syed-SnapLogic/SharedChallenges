package com.syed.athena.jdbc;

import java.sql.*;

public class Runner {
    public static void main(String s[]) throws Exception {
        Class.forName("com.amazonaws.athena.jdbc.AthenaDriver");
        // ?S3OutputLocation=s3://athena-query-output&s3_staging_dir=s3://athena-query-output
        Connection c = DriverManager.getConnection("jdbc:awsathena://athena.us-west-2.amazonaws.com:443?" +
                        "S3OutputLocation=s3://athena-query-output&" +
                        "s3_staging_dir=s3://athena-query-output",
                "AKIAWAIFKTXJB3OH75DI",
                "smOjPA1v6p3dvHiUt/BT5iuil/J2wPb9xUXs3NqB");
        System.out.println("Connection established with athena");
        try (Statement t = c.createStatement()){
            //createInsertAndRead(t);
            updateAndRead(t);
        } finally {
            c.close();
        }
        System.out.println("successfully completed");
    }

    static void updateAndRead(Statement t) throws  Exception {
        t.executeUpdate("update syed.t1 set name='special' where id=1");
        try (ResultSet rs = t.executeQuery("select * from syed.t1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("\t" + md.getColumnName(i) + " : " + rs.getObject(i));
                }
            }
        }
    }

    static void createInsertAndRead(Statement t) throws Exception {
        t.execute("create table if not exists syed.t1(id int, name varchar(20)) location 's3://athena-query-output/syedt1'");
        System.out.println("table created or already exists");
        t.execute("insert into syed.t1 values (1, 'syed')");
        t.execute("insert into syed.t1 values (2, 'ahmed')");
        try (ResultSet rs = t.executeQuery("select count(*) from syed.t1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("\t" + md.getColumnName(i) + " : " + rs.getObject(i));
                }
            }
        }
        try (ResultSet rs = t.executeQuery("select * from syed.t1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("\t" + md.getColumnName(i) + " : " + rs.getObject(i));
                }
            }
        }
    }
}
