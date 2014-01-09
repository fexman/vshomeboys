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
	private float[] opRatio; // ratio of operations: up- and downloads and
								// subscriptions
	private int opInterval; // operations per minute
	private float uploadRatio; // ratio new file / exisiting file upload
	private boolean running;

	public ClientSimulator(IClientCli c, String username, int uploads,
			int downloads, int subscriptions, int uploadRatio, FileGenerator f) {

		this.c = c;
		this.f = f;
		running = true;

		if (uploads + downloads + subscriptions == 0) {
			throw new IllegalArgumentException(
					"uploads + downloads + subscriptions must not be 0");
		}

		opRatio = new float[2];
		opRatio[0] = (float) uploads / (uploads + downloads + subscriptions);
		opRatio[1] = 1 - ((float) subscriptions / (uploads + downloads + subscriptions));

		opInterval = 60000 / (uploads + downloads + subscriptions);
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

		float r = random.nextFloat();

		if (r < opRatio[0]) {

			upload();

		} else {

			if (r < opRatio[1]) {

				download();

			} else {

				subscribe();
			}
		}
	}

	private void upload() throws IOException {

		String file;
		
		if (random.nextFloat() < uploadRatio) {

			file = f.getExistingFile();

		} else {

			file = f.getNewFile();
		}
		
		System.out.println("Uploading: " + file);
		c.upload(file);
	}

	private void download() throws IOException {

		Response r = c.list();

		if (r instanceof ListResponse) {

			ListResponse list = (ListResponse) r;
			String[] files = new String[list.getFileNames().size()];
			list.getFileNames().toArray(files);
			String file = files[Math.abs(random.nextInt()) % files.length];

			System.out.println("Downloading: " +  file);
			c.download(file);
		}
	}

	private void subscribe() throws IOException {

		Response r = c.list();

		if (r instanceof ListResponse) {

			ListResponse list = (ListResponse) r;
			String[] files = new String[list.getFileNames().size()];
			list.getFileNames().toArray(files);
			String file = files[Math.abs(random.nextInt()) % files.length];
			int noOfDls = 1 + Math.abs(random.nextInt()) % 10;
			System.out.println("Subscribing to: " +  file + ", number of DLs: " + noOfDls);
			
			c.subscribe(file, noOfDls);
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