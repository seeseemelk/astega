package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class ParityCoder implements AstegaCodec
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	private int DATA_BITS = 1;
	
	public ParityCoder()
	{
		/*if (dataBits == 8 || dataBits == 4 || dataBits == 2 || dataBits == 1)
			DATA_BITS = dataBits;
		else
			throw new RuntimeException("Invalid number of data bits");*/
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
	
	private boolean hasEvenParity(int sample)
	{
		return Integer.bitCount(sample) % 2 == 0;
	}
	
	private void writeBit(int b)
	{
		b &= 0b1;
		int sample = samples.getRawSample(index);
		
		if (hasEvenParity(sample) != (b == 1))
			sample ^= 1;
		
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
			writeBit(b & 1);
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
			writeHalfNibble(b & 0b11);
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
	
	public int readBit()
	{
		int sample = samples.getRawSample(index);
		if (index < 40)
			System.out.println("Index: " + index + " value: " + sample);
		index++;
		return hasEvenParity(sample) == true ? 1 : 0;
	}
	
	public int readHalfNibble()
	{
		int low = readBit();
		int high = readBit();
		return (low) | (high << 1);
	}
	
	public int readNibble()
	{
		int low = readHalfNibble();
		int high = readHalfNibble();
		return (low) | (high << 2);
	}
	
	public int readByte()
	{
		int low = readNibble();
		int high = readNibble();
		return (low) | (high << 4);
	}
	
	public int readShort()
	{
		int low = readNibble();
		int high = readNibble();
		return (low) | (high << 8);
	}
	
	public int readInt()
	{
		int low = readNibble();
		int high = readNibble();
		return (low) | (high << 16);
	}
	
	@Override
	public byte read()
	{
		return (byte) readByte();
	}
}














