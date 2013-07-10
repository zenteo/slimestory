package org.xeroworld.slimestory.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;


public class PingPacket extends Packet {
	private Calendar time;
	private Calendar receiveTime;
	
	public PingPacket() {
		time = Calendar.getInstance();
	}
	
	public int getPing() {
		if (receiveTime == null) {
			return -1;
		}
		return (int)(receiveTime.getTimeInMillis()-time.getTimeInMillis());
	}
	
	@Override
	public void write(DataOutputStream stream) throws IOException {
		stream.writeLong(time.getTimeInMillis());
	}

	@Override
	public void read(DataInputStream stream) throws IOException {
		time.setTimeInMillis(stream.readLong());
		receiveTime = Calendar.getInstance();
	}
}
