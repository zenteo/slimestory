package org.xeroworld.slimestory.net;

public interface PacketHandler {
	public void handlePacket(Connection connection, Packet packet);
}
