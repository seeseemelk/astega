package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class BitCoder implements AstegaEncoder, AstegaDecoder
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

	private void writeBit(int b)
	{
		float sample = samples.getRawSample(index);

		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFFFFFFE) | (b & 0b1);

		sample = Float.intBitsToFloat(data);

		samples.setRawSample(index, sample);
		index++;
	}

	@SuppressWarnings("unused")
	private void writeHalfNibble(int b)
	{
		writeBit(b);
		writeBit(b >> 1);
	}

	@SuppressWarnings("unused")
	private void writeNibble(int b)
	{
		// samples.setRawSample(index++, (float) (b & 0b1111));
		float sample = samples.getRawSample(index);

		int data = Float.floatToRawIntBits(sample);
		data = (data & 0xFFFFFFFE) | (b & 0b1111);

		sample = Float.intBitsToFloat(data);

		samples.setRawSample(index, sample);
		index++;
		/*
		 * writeHalfNibble(b); writeHalfNibble(b >> 2);
		 */
	}

	@Override
	public void write(int b)
	{
		/*
		 * writeNibble(b); writeNibble(b >> 4);
		 */
		float sample = samples.getRawSample(index);

		//int data = Float.floatToRawIntBits(sample);
		//data = (data & 0xFFFFF00) | (b & 0xFF);
		//data = 0xAAAAAAAA + index * 200;
		float data = 2.1f; //(float) index / lastIndex;

		if (index < 10)
			System.out.println("Index: " + index + ", Written: " + data);
		
		//sample = Float.intBitsToFloat(data);
		//sample = (float) Math.sin((double) index / 10);
		
		samples.setRawSample(index, data);
		//samples.setRawSample(index, sample);
		//System.out.println("Written: " + Integer.toHexString(Float.floatToRawIntBits(samples.getRawSample(index))));
		index++;
	}

	@Override
	public byte read()
	{
		float sample = samples.getRawSample(index++);
		//int data = Float.floatToRawIntBits(sample);
		
		if (index < 10)
			System.out.println("Index: " + (index-1) + ", Read: " + sample);
		
		//System.out.println("Index: " + (index-1) + ", Data: " + Integer.toHexString(data));
		return (byte) ((int) sample & 0xFF);
	}
}










