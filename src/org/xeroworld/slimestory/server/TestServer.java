package org.xeroworld.slimestory.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.xeroworld.slimestory.net.Connection;
import org.xeroworld.slimestory.net.PacketHandler;
import org.xeroworld.slimestory.net.PacketManager;
import org.xeroworld.slimestory.net.packet.MessagePacket;
import org.xeroworld.slimestory.net.packet.Packet;

public class TestServer {
	public static void main(String[] args) {
		try {
			PacketManager packetManager = new PacketManager();
			Socket socket = new Socket("127.0.0.1", 5340);
			Connection c = new Connection(socket, packetManager);
			for (int i = 0; i < 1000; i++) {
				c.send(new MessagePacket(i, new PacketHandler() {
					@Override
					public void handlePacket(Connection connection, Packet packet) {
						MessagePacket answer = (MessagePacket)packet;
						System.out.println("YAY! IT WORKS! " + answer.getMessage());
					}
				}));
			}
			while (true) {
				c.tick(10);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
