package test;

import java.io.IOException;
import java.util.Random;
import message.Response;
import message.response.ListResponse;

import client.IClientCli;

import util.FileGenerator;

public class ClientSimulator implements Runnable {

    private IClientCli c;
    private FileGenerator f;
    private Random random;
    private float opRatio; // ratio upload / download
    private int opInterval;// uploads + downloads = operations per minute
    private float uploadRatio; // ratio new file / exisiting file upload
    private boolean running;

    public ClientSimulator(IClientCli c, String username, int uploads, int downloads, int uploadRatio, FileGenerator f) {

        this.c = c;
        this.f = f;
        running = true;
        opRatio = (float) uploads / downloads;
        opInterval = 60000 / (uploads + downloads);                             
        this.uploadRatio = (float) uploadRatio / 100;
        random = new Random();

        try {

            System.out.println(c.login(username, "12345"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        System.out.println("Starting " + Thread.currentThread());

        while (running) {

            try {

                Thread.sleep(opInterval);
                operation();

            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void operation() throws IOException {

        if (random.nextFloat() < opRatio) {
            
            System.out.println(Thread.currentThread() + " begin upload");
            upload();
            System.out.println(Thread.currentThread() + " upload complete");

        } else {

            System.out.println(Thread.currentThread() + " begin download");
            download();
            System.out.println(Thread.currentThread() + " download complete");
        }
    }

    private void upload() throws IOException {

        if (random.nextFloat() < uploadRatio) {

            c.upload(f.getExistingFile());

        } else {

            c.upload(f.getNewFile());
        }

    }

    private void download() throws IOException {

        Response r = c.list();

        if (r instanceof ListResponse) {

            ListResponse list = (ListResponse) r;
            String[] names = new String[list.getFileNames().size()];
            list.getFileNames().toArray(names);
            c.download(names[Math.abs(random.nextInt()) % names.length]);
        }
    }

    public void shutdown() {

        running = false;

        try {

            Thread.sleep(opInterval);
            c.logout();
            c.exit();
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Shutting down " + Thread.currentThread());
    }
}