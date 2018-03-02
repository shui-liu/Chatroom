package test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
private List<Channel> allChannel = new ArrayList<Channel>();
public static void main(String[] args) throws IOException {
    new Server().start();
}

	// 服务器启动
	public void start() throws IOException {
		ServerSocket server = new ServerSocket(6666);
		while (true) {
			Socket client = server.accept();
			Channel channel = new Channel(client);
			allChannel.add(channel);
			new Thread(channel).start();
		}
	}

	// 频道
	private class Channel implements Runnable {
		private String name;
		private DataInputStream dis;
		private DataOutputStream dos;
		private boolean isRunning = true;

		public Channel(Socket client) {
			try {
				dis = new DataInputStream(client.getInputStream());
				dos = new DataOutputStream(client.getOutputStream());
				send("请输入昵称：");
				name = dis.readUTF();
				send("*****欢迎您进入聊天室！*****");
				sendOthers("*****" + name + "进入了聊天室*****");
			} catch (IOException e) {
				CloseUtil.closeAll(dos, dis);
				isRunning = false;
			}
		}

		public String receive() {
			String msg = null;
			try {
				msg = dis.readUTF();
			} catch (IOException e) {
				CloseUtil.closeAll(dis);
				isRunning = false;
				allChannel.remove(this);
				sendOthers("*****" + name + "离开了聊天室*****");
			}
			return msg;
		}

		public void send(String msg) {
			if (null == msg) {
				return;
			}
			try {
				dos.writeUTF(msg);
				dos.flush();
			} catch (IOException e) {
				CloseUtil.closeAll(dos);
				isRunning = false;
				allChannel.remove(this);
				sendOthers("*****" + name + "离开了聊天室*****");
			}
		}

		public void sendOthers(String msg) {
			if (null == msg) {
				return;
			} else if (msg.startsWith("****")) {
				for (Channel temp : allChannel) {
					if (this == temp) {
						continue;
					}
					temp.send(msg);
				}
			} else if (msg.startsWith("@") && msg.indexOf(":") > -1) {
				String name = msg.substring(1, msg.indexOf(":"));
				String content = msg.substring(msg.indexOf(":") + 1);
				for (Channel temp : allChannel) {
					if (temp.name.equals(name) && null != content) {
						temp.send("来自" + this.name + "的悄悄话:" + content);
					}
				}
			} else {
				for (Channel temp : allChannel) {
					if (this == temp) {
						continue;
					}
					temp.send(name + ":" + msg);
				}
			}
		}

		public void run() {
			while (isRunning) {
				String msg = receive();
				sendOthers(msg);
			}
		}
	}
}
