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
	
	/**
	 * Read a chunk from a file
	 * @param channel
	 * @param location
	 * @throws IOException
	 */
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
	
	/**
	 * Create a new chunk to a file
	 * @param channel
	 * @param location
	 * @param name
	 * @throws IOException
	 */
	public WaveChunk(RandomAccessFile channel, int location, String name, int size) throws IOException
	{
		this.channel = channel;
		this.location = location;
		
		id = name;
		this.size = size;
		
		data = new byte[getSize()];
	}
	
	/**
	 * Create a new chunk to a file
	 * @param channel
	 * @param location
	 * @param name
	 * @throws IOException
	 */
	public WaveChunk(RandomAccessFile channel, int location, String name) throws IOException
	{
		this(channel, location, name, 0);
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
		int low = Byte.toUnsignedInt(data[index]);
		int high = Byte.toUnsignedInt(data[index + 1]) << 8;
		return (short) (low | high);
	}
	
	public int readInt(int index)
	{
		int low = Short.toUnsignedInt(readShort(index));
		int high = Short.toUnsignedInt(readShort(index + 2)) << 16;
		return (int) (low | high);
	}
	
	public void read(int index, byte[] dest)
	{
		System.arraycopy(data, index, dest, 0, dest.length);
	}

	public void writeByte(int index, int value)
	{
		if (index >= data.length)
		{
			byte[] newdata = new byte[index+1];
			for (int i = 0; i < data.length; i++)
				newdata[i] = data[i];
			data = newdata;
		}
		data[index] = (byte) (value & 0xFF);
	}
	
	public void writeShort(int index, int value)
	{
		writeByte(index, value);
		writeByte(index+1, (value >> 8));
	}
	
	public void writeInt(int index, int value)
	{
		writeShort(index, value);
		writeShort(index+2, (value >> 16));
	}
}










