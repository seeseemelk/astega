package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class BitCoder implements AstegaEncoder, AstegaDecoder
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	private int DATA_BITS = 8;
	
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
		index = b;
	}

	@Override
	public int tell()
	{
		return index;
	}
	
	private void writeBit(int b)
	{
		int sample = samples.getRawSample(index);
		sample = (sample & 0xFFFFFFFE) | (b & 1);
		samples.setRawSample(index, sample);
		index++;
	}
	
	private void writeHalfNibble(int b)
	{
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
		/*writeNibble(b);
		writeNibble(b >> 4);*/
		
		/*float sample = samples.getRawSample(index);
		
		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFF80000) | (b & 0xFF);
		
		sample = Float.intBitsToFloat(data);
		
		samples.setRawSample(index, sample);
		index++;*/
		
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
		
		if (index < 10)
			System.out.println("Index: " + (index-1) + ", Read: " + sample);
		
		//System.out.println("Index: " + (index-1) + ", Data: " + Integer.toHexString(data));
		return (byte) (sample & 0xFF);
	}
}














