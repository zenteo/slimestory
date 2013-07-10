package org.xeroworld.slimestory.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.xeroworld.slimestory.net.Connection;
import org.xeroworld.slimestory.net.packet.Packet;
import org.xeroworld.slimestory.server.Server;

public class TestGameServer extends Server {
	private HashMap<Connection, PlayerPacket> players = new HashMap<Connection, PlayerPacket>();
	
	public TestGameServer(int port) {
		super(port);
		getPacketManager().registerPacket(PlayerPacket.class);
	}

	@Override
	public void handlePacket(Connection connection, Packet packet) {
		if (packet instanceof PlayerPacket) {
			PlayerPacket player = (PlayerPacket)packet;
			Iterator<Entry<Connection, PlayerPacket>> it = players.entrySet().iterator();
			players.put(connection, player);
			while (it.hasNext()) {
				Entry<Connection, PlayerPacket> e = it.next();
				if (e.getKey() != connection) {
					e.getKey().send(player);
				}
			}
		}
	}
	
	@Override
	public void tick(Connection connection, double deltaTime) {
		if (players.get(connection) != null) {
			players.get(connection).tick(deltaTime);
		}
	}
	
	public static void main(String[] args) {
		Server server = new TestGameServer(5340);
		server.start();
	}

	@Override
	public void handleConnection(Connection connection) {
		PlayerPacket player = new PlayerPacket();
		player.setPlayerId(System.nanoTime());
		player.setX0((float)Math.random()*100 - 50);
		player.setY0((float)Math.random()*100 - 50);
		player.setDx(0.0f);
		player.setDy(0.0f);
		connection.send(player);
		Iterator<Entry<Connection, PlayerPacket>> it = players.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Connection, PlayerPacket> e = it.next();
			e.getKey().send(player);
			connection.send(e.getValue());
		}
		players.put(connection, player);
	}

	@Override
	public void handleDisconnection(Connection connection) {
		players.remove(connection);
	}
}
