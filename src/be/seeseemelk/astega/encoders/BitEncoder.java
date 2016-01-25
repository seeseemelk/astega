package be.seeseemelk.astega.encoders;

import be.seeseemelk.astega.AstegaSample;

public class BitEncoder implements AstegaEncoder
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
	
	private void writeBit(int b)
	{
		float sample = samples.getRawSample(index);
		
		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFFFFFFE) | (b & 0b1);
		
		sample = Float.intBitsToFloat(data);
		
		samples.setRawSample(index, sample);
		index++;
	}
	
	private void writeHalfNibble(int b)
	{
		writeBit(b);
		writeBit(b >> 1);
	}
	
	private void writeNibble(int b)
	{
		//samples.setRawSample(index++, (float) (b & 0b1111));
		float sample = samples.getRawSample(index);
		
		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFFFFFFE) | (b & 0b1111);
		
		sample = Float.intBitsToFloat(data);
		
		samples.setRawSample(index, sample);
		index++;
		/*writeHalfNibble(b);
		writeHalfNibble(b >> 2);*/
	}
	
	@Override
	public void write(int b)
	{
		/*writeNibble(b);
		writeNibble(b >> 4);*/
		float sample = samples.getRawSample(index);
		
		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFF80000) | (b & 0xFF);
		
		sample = Float.intBitsToFloat(data);
		
		samples.setRawSample(index, sample);
		index++;
	}
}
