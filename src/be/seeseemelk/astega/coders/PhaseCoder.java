package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class PhaseCoder implements AstegaCodec
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	private int DATA_BITS = 1;
	
	public PhaseCoder()
	{
		
	}
	
	@Override
	public void setSamples(AstegaSample samples)
	{
		this.samples = samples;
		lastIndex = samples.getNumberOfSamples();
		
		System.out.println("Saving " + DATA_BITS + " bits per sample");
	}
	
	@Override
	public int getSizeLimit()
	{
		return lastIndex / (8 / DATA_BITS); 
	}
	
	@Override
	public void seek(int b)
	{
		index = b * (8 / DATA_BITS);
	}

	@Override
	public int tell()
	{
		return index / (8 / DATA_BITS);
	}
	
	private void writeBit(int b)
	{
		b &= 0b1;
		int sample = samples.getRawSample(index);
		sample = (sample & 0xFFFFFFFE) | (b & 1);
		samples.setRawSample(index, sample);
		index++;
	}
	
	private void writeHalfNibble(int b)
	{
		b &= 0b11;
		if (DATA_BITS == 2)
		{
			int sample = samples.getRawSample(index);
			sample = (sample & 0xFFFFFFFC) | (b & 3);
			samples.setRawSample(index, sample);
			index++;
		}
		else
		{
			writeBit(b);
			writeBit(b >> 1);
		}
	}
	
	private void writeNibble(int b)
	{
		b &= 0b1111;
		if (DATA_BITS == 4)
		{
			int sample = samples.getRawSample(index);
			sample = (sample & 0xFFFFFFF0) | (b & 0x0F);
			samples.setRawSample(index, sample);
			index++;
		}
		else
		{
			writeHalfNibble(b);
			writeHalfNibble(b >> 2);
		}
	}
	
	@Override
	public void write(int b)
	{
		if (DATA_BITS == 8)
		{
			int sample = samples.getRawSample(index);
			sample = (sample & 0xFFFFFF00) | (b & 0xFF);
			samples.setRawSample(index, sample);
			index++;
		}
		else
		{
			writeNibble(b);
			writeNibble(b >> 4);
		}
	}
	
	@Override
	public byte read()
	{
		int sample = samples.getRawSample(index++);
		return (byte) (sample & 0xFF);
	}
}













