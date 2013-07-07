package org.xeroworld.slimestory.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.xeroworld.slimestory.net.PacketHandler;

public class MessagePacket extends Packet {
	private int message;
	
	public MessagePacket() {
		
	}
	
	public MessagePacket(int message) {
		this.message = message;
	}
	
	public MessagePacket(Packet question, int message) {
		super(question);
		this.message = message;
	}
	
	public MessagePacket(int message, PacketHandler callback) {
		super(callback);
		this.message = message;
	}
	
	@Override
	public void write(DataOutputStream stream) throws IOException {
		super.write(stream);
		stream.writeInt(message);
	}

	public void read(DataInputStream stream) throws IOException {
		super.read(stream);
		message = stream.readInt();
	}
	
	public int getMessage() {
		return message;
	}

	public void setMessage(int message) {
		this.message = message;
	}
}
