package be.seeseemelk.astega.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import be.seeseemelk.astega.AstegaSample;
import be.seeseemelk.astega.coders.AstegaDecoder;

public class AstegaInputStream extends InputStream
{
	private AstegaDecoder decoder;
	private AstegaSample samples;
	private int size = -1;
	
	public AstegaInputStream(AstegaDecoder decoder, File source) throws IOException
	{
		samples = new AstegaSample(source);
		this.decoder = decoder;
		decoder.setSamples(samples);
		decoder.seek(4);
	}
	
	public AstegaSample getSamples()
	{
		return samples;
	}
	
	/**
	 * Get the number of BYTES that can be saved.
	 * @return The number of BYTES that can be saved.
	 * @throws IOException 
	 */
	public int getSize() throws IOException
	{
		if (size == -1)
		{
			int location = decoder.tell();
			decoder.seek(0);
			size = readInt();
			decoder.seek(location);
		}
		
		return size;
	}
	
	public void seek(int b) throws IOException
	{
		if (b < 0)
			throw new IOException("Cannot seek to negative coordinates.");
		else if (b > getSize())
			throw new IOException("Cannot seek further than size limit.");
		
		decoder.seek(b);
	}
	
	public byte readByte() throws IOException
	{
		byte data = decoder.read();
		//System.out.println("Byte read: " + Byte.toUnsignedInt(data));
		return data;
	}
	
	public short readShort() throws IOException
	{
		byte low = readByte();
		byte high = readByte();
		short value = (short) ((Byte.toUnsignedInt(high) << 8) | Byte.toUnsignedInt(low));
		return value;
	}
	
	public int readInt() throws IOException
	{
		short low = readShort();
		short high = readShort();
		int value = (int) ((Short.toUnsignedInt(high) << 16) | Short.toUnsignedInt(low));
		return value;
	}
	
	public long readLong() throws IOException
	{
		int low = readInt();
		int high = readInt();
		long value = (long) ((Integer.toUnsignedLong(high) << 32) | Integer.toUnsignedLong(low));
		return value;
	}

	@Override
	public int read() throws IOException {
		return readByte();
	}
}














