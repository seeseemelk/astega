package be.seeseemelk.astega.stream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import be.seeseemelk.astega.AstegaSample;
import be.seeseemelk.astega.encoders.AstegaEncoder;

public class AstegaOutputStream extends OutputStream
{
	private AstegaEncoder encoder;
	private AstegaSample samples;
	private File destination;
	
	public AstegaOutputStream(AstegaEncoder encoder, File source, File destination) throws IOException
	{
		this.destination = destination;
		samples = new AstegaSample(source);
		this.encoder = encoder;
		encoder.setSamples(samples);
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
		return encoder.getSizeLimit();
	}
	
	public void write(byte b) throws IOException
	{
		encoder.write(Byte.toUnsignedInt(b));
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
		System.out.println("Flushing");
		samples.write(destination);
	}
	
	@Override
	public void close() throws IOException
	{
		flush();
		super.close();
	}
}














