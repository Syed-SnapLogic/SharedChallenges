package com.syed.test.db.performance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Insert extends Thread {
    private String jdbcUrl;
    private String user;
    private String passwd;
    private String driver;
    private String db;

    public Insert(String jdbcUrl, String user, String passwd, String driver, String db) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.passwd = passwd;
        this.driver = driver;
        this.db = db;
    }

    public void run() {
        long start = System.currentTimeMillis();
        Connection c = null;
        PreparedStatement ps = null;
        try {
            Class.forName(driver);
            c = DriverManager.getConnection(jdbcUrl, user, passwd);
            System.out.println("connection obtained successfully for the db " + db);
            ps = c.prepareStatement("insert into syedperftest values (?, ?)");
            int batchSize = 0;
            int batchId = 1;
            for (int i = 1; i < 100001; i++) {
                ps.setInt(1, i);
                ps.setString(2, "abcdefghijklmnopqrstuvwxyz-" + i);
                ps.addBatch();
                batchSize++;
                if (batchSize == 500) {
                    batchSize = 0;
                    ps.executeBatch();
                    System.out.println("completed inserting batch-" + (batchId++) +
                            " for the db " + db);
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("completed successfully for the db " + db + " in " +
                    (end - start) + " millisecs");
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable t1) {

            }
            if (c != null) {
                try {
                    c.close();
                } catch (Throwable t2) {

                }
            }
        }
    }
}
