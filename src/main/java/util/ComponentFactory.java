package util;

import proxy.IProxyCli;
import server.IFileServerCli;
import cli.Shell;
import client.IClientCli;

/**
 * Provides methods for starting an arbitrary amount of various components.
 */
public class ComponentFactory {
	/**
	 * Creates and starts a new client instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IClientCli startClient(Config config, Shell shell) throws Exception {
		return new solution.client.ClientCli(config,shell);
	}

	/**
	 * Creates and starts a new proxy instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IProxyCli startProxy(Config config, Shell shell) throws Exception {
		return new solution.proxy.ProxyCli(config,shell);
	}

	/**
	 * Creates and starts a new file server instance using the provided {@link Config} and {@link Shell}.
	 *
	 * @param config the configuration containing parameters such as connection info
	 * @param shell  the {@code Shell} used for processing commands
	 * @return the created component after starting it successfully
	 * @throws Exception if an exception occurs
	 */
	public IFileServerCli startFileServer(Config config, Shell shell) throws Exception {		
		return new solution.fileserver.FileServerCli1(config,shell);
	}
}
