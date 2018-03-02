package test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static class Send implements Runnable {
		private BufferedReader console;
		private DataOutputStream dos;
		private boolean isRunning = true;

		public Send(Socket client) {
			console = new BufferedReader(new InputStreamReader(System.in));
			try {
				dos = new DataOutputStream(client.getOutputStream());
			} catch (IOException e) {
				isRunning = false;
				CloseUtil.closeAll(dos, console);
			}
		}

		public void run() {
			while (isRunning) {
				String msg = null;
				try {
					msg = console.readLine();
				} catch (IOException e) {
					msg = null;
				}
				try {
					if (null != msg) {
						dos.writeUTF(msg);
						dos.flush();
					}
				} catch (IOException e) {
					isRunning = false;
					CloseUtil.closeAll(dos, console);
				}
			}
		}
	}

	public static class Receive implements Runnable {
		private DataInputStream dis;
		private boolean isRunning = true;

		public Receive(Socket client) {
			try {
				dis = new DataInputStream(client.getInputStream());
			} catch (IOException e) {
				isRunning = false;
				CloseUtil.closeAll(dis);
			}
		}

		public void run() {
			while (isRunning) {
				String msg = null;
				try {
					msg = dis.readUTF();
				} catch (IOException e) {
					isRunning = false;
					CloseUtil.closeAll(dis);
				}
				System.out.println(msg);
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket client = new Socket("myLaptop", 6666);
		new Thread(new Send(client)).start();
		new Thread(new Receive(client)).start();
	}
}
