package be.seeseemelk.astega;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WaveChunk
{
	private String id;
	private RandomAccessFile channel;
	private int location;
	private int size;
	private byte[] data;
	
	public WaveChunk(RandomAccessFile channel, int location) throws IOException
	{
		this.channel = channel;
		this.location = location;
		
		id = readId();
		size = readSize();
		
		seek(8);
		data = new byte[getSize()];
		channel.readFully(data);
	}
	
	private String readId() throws IOException
	{
		seek(0);
		byte[] data = new byte[4];
		channel.readFully(data);
		return new String(data);
	}
	
	private int readSize() throws IOException
	{
		seek(4);
		byte[] data = new byte[4];
		channel.readFully(data);
		return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}
	
	private void seek(int index) throws IOException
	{
		channel.seek(getLocation() + index);
	}
	
	public void flush(RandomAccessFile channel) throws IOException
	{
		seek(0);
		writeChars(channel, getId());
		int size = getSize();
		channel.writeByte(size & 0xFF);
		channel.writeByte((size >> 8) & 0xFF);
		channel.writeByte((size >> 16) & 0xFF);
		channel.writeByte((size >> 24) & 0xFF);
		
		channel.write(data);
	}
	
	private void writeChars(RandomAccessFile channel, String string) throws IOException
	{
		byte[] text = string.getBytes();
		for (byte chr : text)
		{
			channel.writeByte(chr);
		}
	}

	public int getLocation()
	{
		return location;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public String getId()
	{
		return id;
	}
	
	public byte readByte(int index)
	{
		return data[index];
	}
	
	public short readShort(int index)
	{
		int high = Byte.toUnsignedInt(data[index]);
		int low = Byte.toUnsignedInt(data[index + 1]) << 8;
		return (short) (high | low);
	}
	
	public int readInt(int index)
	{
		int high = Short.toUnsignedInt(readShort(index));
		int low = Short.toUnsignedInt(readShort(index + 2)) << 16;
		return (int) (high | low);
	}
	
	public void read(int index, byte[] dest)
	{
		System.arraycopy(data, index, dest, 0, dest.length);
	}

	public void write(int index, byte value)
	{
		data[index] = value;
	}
	
	public void write(int index, short value)
	{
		write(index, (byte) value);
		write(index+1, (byte) (value >> 8));
	}
	
	public void write(int index, int value)
	{
		write(index, (short) value);
		write(index+2, (short) (value >> 16));
	}
}










