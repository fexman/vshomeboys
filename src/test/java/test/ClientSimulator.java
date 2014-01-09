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

		if (uploads == 0 || downloads == 0 || subscriptions == 0) {
			throw new IllegalArgumentException(
					"uploads, downloads and subscriptions must not be 0");
		}

		opRatio = new float[2];

		opRatio[0] = uploads <= (downloads + subscriptions) ? (float) uploads
				/ (downloads + subscriptions) : (float) 1
				- ((downloads + subscriptions) / uploads);
		opRatio[1] = subscriptions <= (uploads + downloads) ? (float) 1
				- (subscriptions / (uploads + downloads))
				: (float) (uploads + downloads / subscriptions);

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

			System.out.println(Thread.currentThread() + " begin upload");
			upload();
			System.out.println(Thread.currentThread() + " upload complete");

		} else {

			if (r < opRatio[1]) {

				System.out.println(Thread.currentThread() + " begin download");
				download();
				System.out.println(Thread.currentThread()
						+ " download complete");

			} else {

				System.out.println(Thread.currentThread()
						+ " begin subscription");
				subscribe();
				System.out.println(Thread.currentThread()
						+ " subscription complete");
			}
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

	private void subscribe() throws IOException {

		Response r = c.list();

		if (r instanceof ListResponse) {

			ListResponse list = (ListResponse) r;
			String[] names = new String[list.getFileNames().size()];
			list.getFileNames().toArray(names);
			c.subscribe(names[Math.abs(random.nextInt()) % names.length],
					Math.abs(random.nextInt() % 10));
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