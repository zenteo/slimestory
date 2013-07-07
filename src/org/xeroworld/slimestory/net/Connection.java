package org.xeroworld.slimestory.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.xeroworld.slimestory.Tickable;
import org.xeroworld.slimestory.net.packet.Packet;
import org.xeroworld.slimestory.net.packet.PingPacket;

public class Connection implements Tickable {
	public static final int STATUS_NONE = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_DISCONNECTED = 2;
	public static final int STATUS_ERROR = 6;
	
	private static final int PING_DELAY = 100;
	private static final int TIMEOUT = 1000;
	
	private final PacketManager manager;
	private final Socket socket;
	private int status = STATUS_CONNECTED;
	private int packetCounter;
	private long lastSent;
	private long lastReceived;
	private DataOutputStream out;
	private DataInputStream in;
	private HashMap<Integer, PacketHandler> callbacks;
	private LinkedList<Packet> outbox2;
	private LinkedList<Packet> outbox;
	private LinkedList<PacketHandler> packetHandlers;

	public Connection(Socket socket, PacketManager manager) {
		this.socket = socket;
		this.manager = manager;
		this.outbox = new LinkedList<Packet>();
		this.outbox2 = new LinkedList<Packet>();
		this.callbacks = new HashMap<Integer, PacketHandler>();
		this.packetHandlers = new LinkedList<PacketHandler>();
		this.lastSent = this.lastReceived = System.currentTimeMillis();
		try {
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.in = new DataInputStream(this.socket.getInputStream());
		}
		catch (IOException e) {
			status = STATUS_ERROR;
		}
	}
	
	public void addPacketHandler(PacketHandler handler) {
		packetHandlers.add(handler);
	}
	
	public void removePacketHandler(PacketHandler handler) {
		packetHandlers.remove(handler);
	}
	
	public void send(Packet packet) {
		outbox.add(packet);
	}
	
	private void switchOutbox() {
		LinkedList<Packet> temp = outbox;
		outbox = outbox2;
		outbox2 = temp;
	}
	
	public void tick(double deltaTime) {
		long now = System.currentTimeMillis();
		if (now - lastReceived >= TIMEOUT) {
			status = STATUS_DISCONNECTED;
		}
		if (socket.isClosed() || !socket.isConnected()) {
			status = STATUS_DISCONNECTED;
		}
		if (status == STATUS_CONNECTED) {
			if (now - lastSent >= PING_DELAY) {
				outbox.add(new PingPacket());
			}
			try {
				if (outbox.size() > 0) {
					switchOutbox();
					for (Packet packet : outbox2) {
						packet.setId(++packetCounter);
						if (packet.isRequest()) {
							callbacks.put(packet.getId(), packet.getCallback());
						}
						manager.write(packet, out);
					}
					outbox.clear();
					lastSent = System.currentTimeMillis();
				}
				out.flush();
				while (in.available() > 0) {
					Packet p = manager.read(in);
					if (!(p instanceof PingPacket)) {
						if (p.isResponse()) {
							int packetId = p.getQuestion().getId();
							if (callbacks.containsKey(packetId)) {
								callbacks.get(packetId).handlePacket(this, p);
								callbacks.remove(packetId);
							}
						}
						for (PacketHandler handler : packetHandlers) {
							handler.handlePacket(this, p);
						}
					}
					lastReceived = System.currentTimeMillis();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				status = STATUS_ERROR;
			}
		}
	}
	
	public int getStatus() {
		return status;
	}
	
	public Socket getSocket() {
		return socket;
	}

	public DataOutputStream getOut() {
		return out;
	}

	public DataInputStream getIn() {
		return in;
	}

	public LinkedList<Packet> getOutbox() {
		return outbox;
	}

	public void setOut(DataOutputStream out) {
		this.out = out;
	}

	public void setIn(DataInputStream in) {
		this.in = in;
	}

	public void setOutbox(LinkedList<Packet> outbox) {
		this.outbox = outbox;
	}
}
