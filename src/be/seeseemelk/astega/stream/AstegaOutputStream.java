package be.seeseemelk.astega.stream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import be.seeseemelk.astega.AstegaSample;
import be.seeseemelk.astega.coders.AstegaEncoder;

public class AstegaOutputStream extends OutputStream
{
	private AstegaEncoder encoder;
	private AstegaSample samples;
	private File destination;
	private int size;
	
	public AstegaOutputStream(AstegaEncoder encoder, File source, File destination) throws IOException
	{
		this.destination = destination;
		samples = new AstegaSample(source);
		this.encoder = encoder;
		encoder.setSamples(samples);
		encoder.seek(4);
	}
	
	public AstegaSample getSamples()
	{
		return samples;
	}
	
	/**
	 * Get the number of BYTES that can be saved.
	 * @return The number of BYTES that can be saved.
	 */
	public int getSizeLimit()
	{
		return encoder.getSizeLimit() - 4;
	}
	
	/**
	 * Get the amount of bytes that have already been saved
	 * @return The amount of bytes that were saved.
	 */
	public int getSize()
	{
		return size;
	}
	
	public void seek(int b) throws IOException
	{
		if (b < 0)
			throw new IOException("Cannot seek to negative coordinates.");
		else if (b > getSizeLimit())
			throw new IOException("Cannot seek further than size limit.");
		
		encoder.seek(b);
	}
	
	public void writeByte(byte b) throws IOException
	{
		if (size >= getSizeLimit())
			throw new IOException("Reached size limit");
		
		encoder.write(Byte.toUnsignedInt(b));
		
		if (encoder.tell() - 1 > size)
			size++;
	}
	
	public void writeShort(short b) throws IOException
	{
		writeByte((byte) b);
		writeByte((byte) (b >> 8));
	}
	
	public void writeInt(int b) throws IOException
	{
		writeShort((short) b);
		writeShort((short) (b >> 16));
	}
	
	public void writeLong(long b) throws IOException
	{
		writeInt((int) b);
		writeInt((int) (b >> 32));
	}
	
	@Override
	public void write(int b) throws IOException
	{
		writeByte((byte) b);
	}
	
	@Override
	public void flush() throws IOException
	{
		int location = encoder.tell();
		encoder.seek(0);
		writeInt(getSize());
		encoder.seek(location);
		samples.write(destination);
	}
	
	@Override
	public void close() throws IOException
	{
		flush();
		super.close();
	}
}














