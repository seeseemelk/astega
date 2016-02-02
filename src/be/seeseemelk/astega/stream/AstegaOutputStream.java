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
	
	public void seek(int b) throws IOException
	{
		if (b < 0)
			throw new IOException("Cannot seek to negative coordinates.");
		else if (b > getSizeLimit())
			throw new IOException("Cannot seek further than size limit.");
		
		encoder.seek(b);
	}
	
	public void write(byte b) throws IOException
	{
		if (size >= getSizeLimit())
			throw new IOException("Reached size limit");
		
		encoder.write(Byte.toUnsignedInt(b));
		
		if (encoder.tell() - 1 > size)
			size++;
	}
	
	public void write(short b) throws IOException
	{
		write((byte) b);
		write((byte) (b >> 8));
	}
	
	@Override
	public void write(int b) throws IOException
	{
		write((short) b);
		write((short) (b >> 16));
	}
	
	public void write(long b) throws IOException
	{
		write((int) b);
		write((int) (b >> 32));
	}
	
	@Override
	public void flush() throws IOException
	{
		int location = encoder.tell();
		encoder.seek(0);
		write((int) size);
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














