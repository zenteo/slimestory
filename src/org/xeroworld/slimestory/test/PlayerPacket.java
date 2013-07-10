package org.xeroworld.slimestory.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.xeroworld.slimestory.net.PacketHandler;
import org.xeroworld.slimestory.net.packet.Packet;

public class PlayerPacket extends Packet {
	private long playerId;
	private float x, y, dx, dy, x0, y0;
	private Calendar lastChange = Calendar.getInstance();
	
	public PlayerPacket() {
	}
	
	public PlayerPacket(int id) {
		super(id);
	}
	
	public PlayerPacket(Packet question) {
		super(question);
	}
	
	public PlayerPacket(PacketHandler callback) {
		super(callback);
	}
	
	public double getTime() {
		Calendar now = Calendar.getInstance();
		return (now.getTimeInMillis() - lastChange.getTimeInMillis())/1000.0;
	}
	
	public void apply() {
		float t = (float)getTime();
		x0 = x0 + dx*t;
		y0 = y0 + dy*t;
	}
	
	public void tick(double deltaTime) {
		float t = (float)getTime();
		x = x0 + dx*t;
		y = y0 + dy*t;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public void setX0(float x) {
		this.x0 = x;
	}

	public void setY0(float y) {
		this.y0 = y;
	}
	
	public void write(DataOutputStream stream) throws IOException {
		super.write(stream);
		stream.writeLong(playerId);
		stream.writeFloat(x0);
		stream.writeFloat(y0);
		stream.writeFloat(dx);
		stream.writeFloat(dy);
		stream.writeLong(lastChange.getTimeInMillis());
	}

	public void read(DataInputStream stream) throws IOException {
		super.read(stream);
		playerId = stream.readLong();
		x0 = x = stream.readFloat();
		y0 = y = stream.readFloat();
		dx = stream.readFloat();
		dy = stream.readFloat();
		lastChange.setTimeInMillis(stream.readLong());
	}

	public float getDx() {
		return dx;
	}

	public void setDx(float dx) {
		this.dx = dx;
		lastChange = Calendar.getInstance();
	}

	public float getDy() {
		return dy;
	}

	public void setDy(float dy) {
		this.dy = dy;
		lastChange = Calendar.getInstance();
	}
}
