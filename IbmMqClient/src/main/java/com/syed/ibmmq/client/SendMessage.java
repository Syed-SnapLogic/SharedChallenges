package com.syed.ibmmq.client;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SendMessage {
    public static void main(String[] args) throws Exception{
        mainNoSsl(args);
        System.out.println("done without ssl\n\n\n");
        Thread.sleep(3000);
        mainSslWithJvmOptions(args);
        System.out.println("done with ssl having jvm options\n\n\n");
        Thread.sleep(3000);
        mainSslWithoutJvmOptions(args);
        System.out.println("done with ssl having no jvm options");
    }

    public static void mainNoSsl(String[] args) throws Exception{
        Properties props = new Properties();
        props.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        props.put(InitialContext.PROVIDER_URL, "file:///Users/gaian/JNDI-Directory");
        InitialContext ctx = new InitialContext(props);
        ConnectionFactory fact = (ConnectionFactory) (ctx.lookup("MyCF1"));
        Connection connection = fact.createConnection("mqm", "syedmqm2025");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = session.createQueue("myfirstqueue");
        MessageProducer producer = session.createProducer(q);
        producer.send(session.createTextMessage("Hello Gaian " + System.nanoTime()));
        System.out.println("msg sent");
        producer.close();
        MessageConsumer consumer = session.createConsumer(q);
        System.out.println("consumer created");
        Message m = consumer.receiveNoWait();
        System.out.println("received message");
        System.out.println(m.getClass().getName());
        if ( m instanceof TextMessage ) {
            System.out.println(((TextMessage) m).getText());
        }
        consumer.close();
        connection.stop();
        connection.close();
    }

    public static void mainSslWithJvmOptions(String[] args) throws Exception{
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        System.setProperty("javax.net.ssl.keyStore", "/Users/gaian/mqssl/sslclient.jks");
        //System.setProperty("javax.net.ssl.keyStore", "/Users/gaian/mqssl/test.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        System.setProperty("javax.net.ssl.trustStore", "/Users/gaian/mqssl/sslclient.jks");
        //System.setProperty("javax.net.ssl.trustStore", "/Users/gaian/mqssl/test.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
        //System.setProperty("jdk.tls.client.cipherSuites", "TLS_RSA_WITH_AES_128_CBC_SHA");
        //System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        Properties props = new Properties();
        //props.put(MQConstants.SSL_CIPHER_SUITE_PROPERTY, "TLS_RSA_WITH_AES_128_CBC_SHA256");
        props.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        props.put(InitialContext.PROVIDER_URL, "file:///Users/gaian/SSL-JNDI-Directory");
        InitialContext ctx = new InitialContext(props);
        ConnectionFactory fact = (ConnectionFactory) (ctx.lookup("SSLCF1"));
        Connection connection = fact.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q = session.createQueue("SSLQ1");
        MessageProducer producer = session.createProducer(q);
        producer.send(session.createTextMessage("Hello World " + System.nanoTime()));
        System.out.println("msg sent");
        producer.close();
        MessageConsumer consumer = session.createConsumer(q);
        System.out.println("consumer created");
        Message m = consumer.receiveNoWait();
        System.out.println("received message");
        System.out.println(m.getClass().getName());
        if ( m instanceof TextMessage ) {
            System.out.println(((TextMessage) m).getText());
        }
        consumer.close();
        connection.stop();
        connection.close();
    }

    public static void mainSslWithoutJvmOptions(String[] args) throws Exception{
        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        String HOSTNAME = "192.168.0.111";
        String QMGRNAME = "SYEDSSLQMJUN2025";
        String CHANNEL = "SSLCHAN1";
        String SSLCIPHERSUITE = "TLS_RSA_WITH_AES_128_CBC_SHA256";

        try {
            Class.forName("com.sun.net.ssl.internal.ssl.Provider");

            System.out.println("JSSE is installed correctly!");

            char[] KSPW = "changeit".toCharArray();

            // instantiate a KeyStore with type JKS
            KeyStore ks = KeyStore.getInstance("JKS");
            // load the contents of the KeyStore
            ks.load(new FileInputStream("/Users/gaian/mqssl/sslclient.jks"), KSPW);
            System.out.println("Number of keys on JKS: "
                    + Integer.toString(ks.size()));

            // Create a keystore object for the truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            // Open our file and read the truststore (no password)
            trustStore.load(new FileInputStream("/Users/gaian/mqssl/sslclient.jks"), KSPW);

            // Create a default trust and key manager
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            // Initialise the managers
            trustManagerFactory.init(trustStore);
            keyManagerFactory.init(ks,KSPW);

            // Get an SSL context.
            // Note: not all providers support all CipherSuites. But the
            // "SSL_RSA_WITH_3DES_EDE_CBC_SHA" CipherSuite is supported on both SunJSSE
            // and IBMJSSE2 providers

            // Accessing available algorithm/protocol in the SunJSSE provider
            // see http://java.sun.com/javase/6/docs/technotes/guides/security/SunProviders.html
            SSLContext sslContext = SSLContext.getInstance("SSLv3");

            // Acessing available algorithm/protocol in the IBMJSSE2 provider
            // see http://www.ibm.com/developerworks/java/jdk/security/142/secguides/jsse2docs/JSSE2RefGuide.html
            // SSLContext sslContext = SSLContext.getInstance("SSL_TLS");
            System.out.println("SSLContext provider: " +
                    sslContext.getProvider().toString());

            // Initialise our SSL context from the key/trust managers
            sslContext.init(keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(), null);

            // Get an SSLSocketFactory to pass to WMQ
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create default MQ connection factory
            MQQueueConnectionFactory factory = new MQQueueConnectionFactory();

            // Customize the factory
            factory.setSSLSocketFactory(sslSocketFactory);
            // Use javac SSLTest.java -Xlint:deprecation
            factory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
            factory.setQueueManager(QMGRNAME);
            factory.setHostName(HOSTNAME);
            factory.setChannel(CHANNEL);
            factory.setPort(2019);
            factory.setSSLFipsRequired(false);
            factory.setSSLCipherSuite(SSLCIPHERSUITE);

            Connection connection = null;
            connection = factory.createQueueConnection("",""); //empty user, pass to avoid MQJMS2013 messages
            connection.start();
            System.out.println("JMS SSL client connection started!");
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue q = session.createQueue("SSLQ1");
            MessageProducer producer = session.createProducer(q);
            producer.send(session.createTextMessage("Hello Snaplogic " + System.nanoTime()));
            System.out.println("msg sent");
            producer.close();
            MessageConsumer consumer = session.createConsumer(q);
            System.out.println("consumer created");
            Message m = consumer.receiveNoWait();
            System.out.println("received message");
            System.out.println(m.getClass().getName());
            if ( m instanceof TextMessage ) {
                System.out.println(((TextMessage) m).getText());
            }
            consumer.close();
            connection.close();
            System.out.println("connection closed!");

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
