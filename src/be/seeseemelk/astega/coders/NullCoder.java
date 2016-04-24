package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class NullCoder implements AstegaCodec
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	
	@Override
	public void setSamples(AstegaSample samples)
	{
		this.samples = samples;
		lastIndex = samples.getNumberOfSamples();
	}
	
	@Override
	public int getSizeLimit()
	{
		return lastIndex; 
	}
	
	@Override
	public void seek(int b)
	{
		index = b;
	}

	@Override
	public int tell()
	{
		return index;
	}
	
	@Override
	public void write(int b)
	{
		int sample = samples.getRawSample(index);
		sample = (sample & 0xFFFFFF00) | (b & 0xFF);
		samples.setRawSample(index, sample);
		index++;
	}
	
	@Override
	public byte read()
	{
		int sample = samples.getRawSample(index++);
		return (byte) (sample & 0xFF);
	}
	
	@Override
	public void flush()
	{
		
	}
}














