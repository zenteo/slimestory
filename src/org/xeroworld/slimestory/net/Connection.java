package org.xeroworld.slimestory.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import org.xeroworld.slimestory.Tickable;

public class Connection implements Tickable {
	public static final int STATUS_NONE = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_DISCONNECTED = 2;
	public static final int STATUS_ERROR = 3;
	
	private final PacketManager manager;
	private final Socket socket;
	private int status;
	private int packetCounter;
	private DataOutputStream out;
	private DataInputStream in;
	private HashMap<Integer, PacketHandler> callbacks;
	private LinkedList<Packet> outbox2;
	private LinkedList<Packet> outbox;
	private LinkedList<Packet> inbox;

	public Connection(Socket socket, PacketManager manager) {
		this.socket = socket;
		this.manager = manager;
		this.outbox = new LinkedList<Packet>();
		this.outbox2 = new LinkedList<Packet>();
		this.inbox = new LinkedList<Packet>();
		this.callbacks = new HashMap<Integer, PacketHandler>();
		try {
			this.out = new DataOutputStream(this.socket.getOutputStream());
			this.in = new DataInputStream(this.socket.getInputStream());
		}
		catch (IOException e) {
			status = STATUS_ERROR;
		}
	}
	
	private void switchOutbox() {
		LinkedList<Packet> temp = outbox;
		outbox = outbox2;
		outbox2 = temp;
	}
	
	public void tick(double deltaTime) {
		if (socket.isClosed() || !socket.isConnected()) {
			status = STATUS_DISCONNECTED;
		}
		if (status == STATUS_CONNECTED) {
			try {
				switchOutbox();
				for (Packet packet : outbox2) {
					packet.setId(packetCounter++);
					if (packet.isRequest()) {
						callbacks.put(packet.getId(), packet.getCallback());
					}
					manager.write(packet, out);
				}
				outbox.clear();
				while (in.available() > 0) {
					Packet p = manager.read(in);
					if (p.isResponse()) {
						int packetId = p.getQuestion().getId();
						if (callbacks.containsKey(packetId)) {
							callbacks.get(packetId).handlePacket(this, p);
							callbacks.remove(packetId);
						}
					}
					inbox.add(p);
				}
			}
			catch (IOException e) {
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

	public LinkedList<Packet> getInbox() {
		return inbox;
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

	public void setInbox(LinkedList<Packet> inbox) {
		this.inbox = inbox;
	}

}
