package org.xeroworld.slimestory.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xeroworld.slimestory.net.packet.MessagePacket;
import org.xeroworld.slimestory.net.packet.Packet;
import org.xeroworld.slimestory.net.packet.PingPacket;

public class PacketManager {
	private ArrayList<Class<? extends Packet>> list;
	private HashMap<Class<? extends Packet>, Integer> map;
	
	public PacketManager() {
		list = new ArrayList<Class<? extends Packet>>();
		map = new HashMap<Class<? extends Packet>, Integer>();
		registerDefaults();
	}
	
	private void registerDefaults() {
		registerPacket(PingPacket.class);
		registerPacket(MessagePacket.class);
	}
	
	public void registerPacket(Class<? extends Packet> c) {
		map.put(c, list.size());
		list.add(c);
	}
	
	public void write(Packet packet, DataOutputStream stream) throws IOException {
		int i = map.get(packet.getClass());
		stream.writeByte(i);
		packet.write(stream);
	}
	
	public Packet read(DataInputStream stream) throws IOException {
		int i = stream.readByte();
		Class<? extends Packet> c = list.get(i);
		try {
			Packet ret = c.newInstance();
			ret.read(stream);
			return ret;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
