package com.syed.netbeans.mac1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ScanSnapv4Repo {
    private static final String DEPENDENCY_TO_BE_SCANNED_FOR = "jxml";
    private static final String FILE_PATH_HOLDING_TREE = "/tmp/syed.tree";
    private static final String[] ALL_SNAPPACKS = new String[] {
            "activedirectory",
            "amazonsns",
            "amazonsqs",
            "anaplan",
            "apisuite",
            "autosync",
            "azureactivedirectory",
            "azureservicebus",
            "binary",
            "birst",
            "box",
            "confluentkafka",
            "coupa",
            "datacatalog",
            "db/alloydb",
            "db/azuresql",
            "db/azuresynapsesql",
            "db/cassandra",
            "db/databricks",
            "db/elt",
            "db/hive",
            "db/jdbc",
            "db/mongo",
            "db/mysql",
            "db/netezza",
            "db/oracle",
            "db/postgres",
            "db/redshift",
            "db/saphana",
            "db/snowflake",
            "db/sqlserver",
            "db/teradata",
            "dynamics365forsales",
            "dynamodb",
            "eloqua",
            "email",
            "exchange",
            "expensify",
            "facebook",
            "flow",
            "foursquare",
            "google/analytics",
            "google/analytics4",
            "google/bigquery",
            "google/directory",
            "google/spreadsheet",
            "hadoop",
            "jira",
            "jms",
            "jwt",
            "ldap",
            "marketo",
            "microsoft/businesscentral",
            "microsoft/msdynamics365fo",
            "microsoft/exchangeonline",
            "microsoft/onedrive",
            "microsoft/teams",
            "mlanalytics",
            "mlcore",
            "mldatapreparation",
            "mlnlp",
            "msdynamicsax",
            "netsuite",
            "openair",
            "openapi",
            "policies",
            "rabbitmq",
            "reltiosnapconnector",
            "rest",
            "s3snap",
            "salesforce",
            "sap",
            "script",
            "servicenow",
            "shopify",
            "slack",
            "snaplogicmetadata",
            "soap",
            "splunk",
            "sumologic",
            "tableau9",
            "test",
            "transform",
            "twitter",
            "vertica",
            "workday",
            "workdayprism",
            "xactly",
            "zuora"
    };
    private static final String SNAP_V4_LOCATION = "/Users/syedgaian/SyedsMacWorld/7_Work/Snap_v4";
    private static final String MATCHING_PATTERN = "\n[INFO] +- com.snaplogic:" + DEPENDENCY_TO_BE_SCANNED_FOR + ":jar:34.0:compile";

    public static void main(String[] args) throws Exception {
        for (String snappack : ALL_SNAPPACKS) {
            System.out.println("processing: " + snappack);
            String snappackLocation = SNAP_V4_LOCATION;
            if (!snappackLocation.endsWith(File.separator)) {
                snappackLocation = snappackLocation + File.separator;
            }
            snappackLocation = snappackLocation + snappack;
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("sh", "-c",
                    "/Users/syedgaian/SyedsMacWorld/6_Setup/2_Maven/apache-maven-3.8.6/bin/mvn -f " +
                            snappackLocation + " dependency:tree > " + FILE_PATH_HOLDING_TREE);

            try {
                Process process = processBuilder.start();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                process.waitFor();
                String allContents = Files.readString(Paths.get(FILE_PATH_HOLDING_TREE));
                if (allContents.contains(MATCHING_PATTERN)) {
                    System.out.println("snappack impacted: " + snappack);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
