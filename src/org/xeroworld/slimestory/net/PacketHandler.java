package org.xeroworld.slimestory.net;

import org.xeroworld.slimestory.net.packet.Packet;

public interface PacketHandler {
	public void handlePacket(Connection connection, Packet packet);
}
