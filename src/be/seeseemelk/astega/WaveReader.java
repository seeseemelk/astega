package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class WaveReader implements AutoCloseable
{
	private File file;
	private RandomAccessFile input;
	private String signature;
	private int size;
	private String format;
	private Map<String, WaveChunk> chunks = new HashMap<>();
	private WaveChunk formatChunk;
	private WaveChunk dataChunk;
	
	public WaveReader(File file) throws IOException
	{
		this.file = file;
		input = new RandomAccessFile(file, "rw");
		
		signature = readSignature();
		size = readSize();
		format = readFormat();
		
		if (!getSignature().equals("RIFF"))
			throw new IOException("Invalid RIFF signature (Expected RIFF, found " + getSignature() + ")");
		
		readChunks();
		
		formatChunk = getChunk("fmt ");
		dataChunk = getChunk("data");
		
		if (getAudioFormat() != 1)
			throw new IOException("Unsupported audio format");
	}
	
	public void flush(RandomAccessFile channel) throws IOException
	{
		channel.seek(0);
		channel.writeChars(getSignature());
		int size = getSize();
		channel.writeByte(size & 0xFF);
		channel.writeByte((size >> 8) & 0xFF);
		channel.writeByte((size >> 16) & 0xFF);
		channel.writeByte((size >> 24) & 0xFF);
		channel.writeChars(getFormat());
		
		for (WaveChunk chunk : chunks.values())
		{
			chunk.flush(channel);
		}
	}
	
	public void flush(File file) throws IOException
	{
		try (RandomAccessFile channel = new RandomAccessFile(file, "rw"))
		{
			flush(channel);
		}
	}
	
	public void flush() throws IOException
	{
		flush(input);
	}
	
	public void close() throws IOException
	{
		flush();
		input.close();
	}
	
	public File getFile()
	{
		return file;
	}
	
	private String readSignature() throws IOException
	{
		byte[] data = new byte[4];
		input.seek(0);
		input.readFully(data);
		return new String(data);
	}
	
	private int readSize() throws IOException
	{
		byte[] data = new byte[4];
		input.seek(4);
		input.readFully(data);
		size = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
		return size;
	}
	
	private String readFormat() throws IOException
	{
		byte[] data = new byte[4];
		input.seek(8);
		input.readFully(data);
		return new String(data);
	}
	
	private void readChunks() throws IOException
	{
		int location = 12;
		int maxLocation = 4 + getSize();
		
		while (location <= maxLocation)
		{
			WaveChunk chunk = new WaveChunk(input, location);
			System.out.println("Found chunk '" + chunk.getId() + "' (size: " + chunk.getSize() + ")");
			chunks.put(chunk.getId(), chunk);
			location += 8 + chunk.getSize();
		}
	}
	
	public WaveChunk getChunk(String name)
	{
		return chunks.get(name);
	}
	
	public String getSignature()
	{
		return signature;
	}
	
	public int getSize()
	{
		return size;
	}
	
	public String getFormat()
	{
		return format;
	}
	
	public int getAudioFormat()
	{
		return formatChunk.readShort(0);
	}

	public int getNumberOfChannels()
	{
		return (int) formatChunk.readShort(2);
	}
	
	public int getSampleRate()
	{
		return formatChunk.readInt(4);
	}
	
	public int getByteRate()
	{
		return formatChunk.readInt(8);
	}
	
	public int getBlockAlign()
	{
		return formatChunk.readShort(12);
	}
	
	public int getBitsPerSample()
	{
		return (int) formatChunk.readShort(14);
	}
	
	public int getBytesPerSample()
	{
		return getBitsPerSample() / 8;
	}
	
	public int getNumberOfSamples()
	{
		return dataChunk.getSize() / getBytesPerSample();
	}
	
	public int getSample(int index)
	{
		switch (getBitsPerSample())
		{
			case 8:
				return (int) dataChunk.readByte(index);
			case 16:
				index *= 2;
				return (int) dataChunk.readShort(index);
			case 24:
				index *= 3;
				int low = dataChunk.readShort(index);
				int high = dataChunk.readByte(index + 2) << 16;
				return (low | high);
			case 32:
				index *= 4;
				return (int) dataChunk.readInt(index);
			default:
				throw new RuntimeException("Cannot work with " + getBitsPerSample() + " bits per sample.");
		}
	}
	
	/**
	 * Writes a sample to the file.
	 * It will drop any bits that aren't saved in the samples.
	 * @param index The number of the sample to modify
	 * @param value The value to save
	 */
	public void setSample(int index, int value)
	{
		switch (getBitsPerSample())
		{
			case 8:
				dataChunk.write(index, (byte) value);
				break;
			case 16:
				index *= 2;
				dataChunk.write(index, (short) value);
				break;
			case 24:
				index *= 3;
				dataChunk.write(index, (short) value);
				dataChunk.write(index+1, (byte) (value >> 16));
				break;
			case 32:
				index *= 4;
				dataChunk.write(index, value);
				break;
			default:
				throw new RuntimeException("Cannot work with " + getBitsPerSample() + " bits per sample.");
		}
	}
}



































