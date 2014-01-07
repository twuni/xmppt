package org.twuni.xmppt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer extends Thread {

	public static class Worker {

		private Socket socket;

		public final void attach( Socket socket ) throws IOException {
			this.socket = socket;
			onAttach( socket );
		}

		public final void detach() throws IOException {
			socket = null;
			onDetach();
		}

		public boolean isAttached() {
			return socket != null;
		}

		/**
		 * @param socket
		 * @throws IOException
		 */
		protected void onAttach( Socket socket ) throws IOException {
			// By default, do nothing.
		}

		/**
		 * @throws IOException
		 */
		protected void onDetach() throws IOException {
			// By default, do nothing.
		}

	}

	private final ServerSocket server;
	private final Worker worker;

	public TestServer( ServerSocket server, Worker worker ) {
		this.server = server;
		this.worker = worker;
	}

	@Override
	public void run() {
		try {
			while( !isInterrupted() && server.isBound() && !server.isClosed() ) {
				worker.attach( server.accept() );
			}
			worker.detach();
		} catch( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

}
