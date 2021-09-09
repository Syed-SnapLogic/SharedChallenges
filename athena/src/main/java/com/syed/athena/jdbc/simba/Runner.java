package com.syed.athena.jdbc.simba;

import java.sql.*;

public class Runner {
    public static void main(String s[]) throws Exception {
        Class.forName("com.simba.universal.jdbc42.Driver");
        // ?S3OutputLocation=s3://athena-query-output&s3_staging_dir=s3://athena-query-output
        Connection c = DriverManager.getConnection("jdbc:awsathena://User=AKIAWAIFKTXJB3OH75DI;" +
                "Password=smOjPA1v6p3dvHiUt/BT5iuil/J2wPb9xUXs3NqB;" +
                "S3OutputLocation=s3://athena-query-output;" +
                "AwsRegion=us-west-2");
        System.out.println("Connection established with athena");
        try (Statement t = c.createStatement()){
            createInsertAndRead(t);
            //updateAndRead(t);
            executeBatch(t);
            readTable(t);
        } finally {
            c.close();
        }
        System.out.println("successfully completed");
    }

    public static void main_old(String s[]) throws Exception {
        Class.forName("com.simba.athena.jdbc42.Driver");
        // ?S3OutputLocation=s3://athena-query-output&s3_staging_dir=s3://athena-query-output
        Connection c = DriverManager.getConnection("jdbc:awsathena://User=AKIAWAIFKTXJB3OH75DI;" +
                "Password=smOjPA1v6p3dvHiUt/BT5iuil/J2wPb9xUXs3NqB;" +
                "S3OutputLocation=s3://athena-query-output;" +
                "AwsRegion=us-west-2");
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
        t.execute("update syed.t1 set name='special' where id=1");
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
        t.execute("create table if not exists syed.simbat1(id int, name varchar(20)) location 's3://athena-query-output/syedsimbat1'");
        System.out.println("table created or already exists");
        t.execute("insert into syed.simbat1 values (3, 'syed')");
        t.execute("insert into syed.simbat1 values (4, 'ahmed')");
        try (ResultSet rs = t.executeQuery("select count(*) from syed.simbat1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("\t" + md.getColumnName(i) + " : " + rs.getObject(i));
                }
            }
        }
        try (ResultSet rs = t.executeQuery("select * from syed.simbat1")) {
            ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.println("\t" + md.getColumnName(i) + " : " + rs.getObject(i));
                }
            }
        }
    }

    static void executeBatch(Statement t) throws Exception {
        t.addBatch("insert into syed.simbat1 values (7, 'syed7'");
        t.addBatch("insert into syed.simbat1 values (8, 'syed8'");
        t.executeBatch();
        System.out.println("successful execution of the batch");
    }

    static void readTable(Statement t) throws  Exception {
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
