package org.xeroworld.slimestory.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.xeroworld.slimestory.net.Connection;
import org.xeroworld.slimestory.net.PacketHandler;
import org.xeroworld.slimestory.net.PacketManager;
import org.xeroworld.slimestory.net.packet.Packet;

public class TestGameClient extends JFrame implements ActionListener, PacketHandler, KeyListener {
	private PlayerPacket player;
	private HashMap<Long, PlayerPacket> players = new HashMap<Long, PlayerPacket>();
	private BufferedImage backBuffer = null;
	private PacketManager packetManager;
	private Connection connection = null;
	private Timer timer;
	private long lastTick;
	private boolean[] inputs = new boolean[4];
	
	public TestGameClient() {
		setTitle("Test game");
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		addKeyListener(this);
		
		packetManager = new PacketManager();
		packetManager.registerPacket(PlayerPacket.class);
		timer = new Timer(0, this);
	}
	
	public boolean connect() {
		try {
			connection = new Connection(new Socket("127.0.0.1", 5340), packetManager);
			connection.addPacketHandler(this);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void start() {
		lastTick = System.nanoTime();
		timer.start();
	}
	
	public void stop() {
		timer.stop();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		long now = System.nanoTime();
		double deltaTime = (now-lastTick)/1.0E9;
		lastTick = now;
		if (connection != null) {
			connection.tick(deltaTime);
		}
		float speed = 100.0f;
		if (player != null) {
			float dx = 0;
			float dy = 0;
			if (inputs[0]) { //LEFT
				dx -= speed;
			}
			if (inputs[1]) { //UP
				dy -= speed;
			}
			if (inputs[2]) { //RIGHT
				dx += speed;
			}
			if (inputs[3]) { //DOWN
				dy += speed;
			}
			if (dx != player.getDx() || dy != player.getDy()) {
				player.apply();
				player.setDx(dx);
				player.setDy(dy);
				connection.send(player);
			}
			player.tick(deltaTime);
		}
		Iterator<Entry<Long, PlayerPacket>> it = players.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, PlayerPacket> e = it.next();
			e.getValue().tick(deltaTime);
		}
		repaint();
	}
	
	@Override
	public void paint(Graphics graphics) {
		if (backBuffer == null || backBuffer.getWidth() != getWidth() || backBuffer.getHeight() != getHeight()) {
			backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		}
		Graphics2D g = (Graphics2D)backBuffer.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
		g.translate(backBuffer.getWidth()/2, backBuffer.getHeight()/2);
		g.setColor(Color.BLUE);
		g.drawOval(-5, -5, 10, 10);
		g.setColor(Color.RED);
		if (player != null) {
			g.translate(-player.getX(), -player.getY());
		}
		Iterator<Entry<Long, PlayerPacket>> it = players.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, PlayerPacket> entry = it.next();
			PlayerPacket p = entry.getValue();
			AffineTransform saved = g.getTransform();
			g.translate(p.getX(), p.getY());
			g.drawOval(-5, -5, 10, 10);
			g.setTransform(saved);
		}
		g = (Graphics2D)graphics;
		g.drawImage(backBuffer, 0, 0, null);
	}
	
	public static void main(String[] args) {
		TestGameClient client = new TestGameClient();
		if (client.connect()) {
			client.setVisible(true);
			client.start();
		}
		
	}

	@Override
	public void handlePacket(Connection connection, Packet packet) {
		if (packet instanceof PlayerPacket) {
			PlayerPacket p = (PlayerPacket)packet;
			if (player == null) {
				player = p;
			}
			else {
				players.put(p.getPlayerId(), p);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() >= KeyEvent.VK_LEFT && e.getKeyCode() <=  KeyEvent.VK_DOWN) {
			inputs[e.getKeyCode()-KeyEvent.VK_LEFT] = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() >= KeyEvent.VK_LEFT && e.getKeyCode() <=  KeyEvent.VK_DOWN) {
			inputs[e.getKeyCode()-KeyEvent.VK_LEFT] = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
}
