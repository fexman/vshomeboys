package test;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import proxy.IProxyCli;
import cli.Shell;
import cli.TestInputStream;
import server.IFileServerCli;
import util.Cleanup;
import util.ComponentFactory;
import util.Config;
import util.FileGenerator;
import util.Util;

/**
 * ########## ruLTSS (rulez) - Ruffman's Long Term Stress Simulator ##########
 * 
 * Set and check test properties at {@link src/test/resources/loadtest.properties}}
 */
public class LoadSimulator {

    public static void main(String[] args) throws Exception {

        Config confClient = new Config("client");
        Config confTest = new Config("loadtest");
        ArrayList<ClientSimulator> clients = new ArrayList<ClientSimulator>();
        ExecutorService threadpool = Executors.newCachedThreadPool();
        TestInputStream testInput = new TestInputStream();
        ComponentFactory comp = new ComponentFactory();

        int duration = confTest.getInt("duration") * 1000; // sec to ms-sec
        int noOfClients = confTest.getInt("clients");
        int uploads = confTest.getInt("uploadsPerMin");
        int downloads = confTest.getInt("downloadsPerMin");
        int filesize = confTest.getInt("fileSizeKB");
        int ratio = confTest.getInt("overwriteRatio");

        FileGenerator f = new FileGenerator(filesize);
        Util.addUsers(noOfClients);

        // Start Proxy

        IProxyCli proxy = comp.startProxy(new Config("proxy"), new Shell("proxy", System.out, testInput));
        IFileServerCli fs1 = comp.startFileServer(new Config("fs1"), new Shell("fs1", System.out, testInput));
        IFileServerCli fs2 = comp.startFileServer(new Config("fs2"), new Shell("fs2", System.out, testInput));
        IFileServerCli fs3 = comp.startFileServer(new Config("fs3"), new Shell("fs3", System.out, testInput));
        IFileServerCli fs4 = comp.startFileServer(new Config("fs4"), new Shell("fs4", System.out, testInput));

        for (int i = 0; i < noOfClients; i++) {

            ClientSimulator s = new ClientSimulator(comp.startClient(confClient, new Shell("client", System.out,
                    testInput)), "u" + i, uploads, downloads, ratio, f);
            clients.add(s);
            threadpool.execute(s);
        }

        Thread.sleep(duration);

        // Shutting down all clients

        for (ClientSimulator c : clients) {
            c.shutdown();
        }

        proxy.exit();
        fs1.exit();
        fs2.exit();
        fs3.exit();
        fs4.exit();
        testInput.close();
        threadpool.shutdown();
        Cleanup.cleanup();
    }
}