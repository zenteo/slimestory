package org.xeroworld.slimestory.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.xeroworld.slimestory.net.PacketHandler;

public class Packet {
	private int id;
	private Packet question;
	private PacketHandler callback;
	
	public Packet() {
	}
	
	public Packet(int id) {
		this.id = id;
	}
	
	public Packet(Packet question) {
		this.question = question;
	}
	
	public Packet(PacketHandler callback) {
		this.callback = callback;
	}
	
	public void write(DataOutputStream stream) throws IOException {
		if (question != null) {
			stream.writeInt(-id);
			stream.writeInt(question.getId());
		}
		else {
			stream.writeInt(id);
		}
	}

	public void read(DataInputStream stream) throws IOException {
		id = stream.readInt();
		if (id < 0) {
			id *= -1;
			question = new Packet(stream.readInt());
		}
	}
	
	public boolean isRequest() {
		return callback != null;
	}
	
	public boolean isResponse() {
		return question != null;
	}
	
	public Packet getQuestion() {
		return question;
	}
	
	public PacketHandler getCallback() {
		return callback;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Packet) {
			return ((Packet)other).id == id;
		}
		return false;
	}
}
