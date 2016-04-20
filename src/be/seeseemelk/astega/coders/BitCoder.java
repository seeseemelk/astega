package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public class BitCoder implements AstegaCodec
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	private int DATA_BITS;
	
	public BitCoder(int dataBits)
	{
		if (dataBits == 8 || dataBits == 4 || dataBits == 2 || dataBits == 1)
			DATA_BITS = dataBits;
		else
			throw new RuntimeException("Invalid number of data bits");
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
	
	public int getDataBits()
	{
		return DATA_BITS;
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
	
	private int readBit()
	{
		int bit = samples.getRawSample(index++);
		return bit & 1;
	}
	
	private int readHalfNibble()
	{
		if (DATA_BITS == 2)
		{
			int nibble = samples.getRawSample(index++);
			return nibble & 0b11;
		}
		else
		{
			int least = readBit();
			int most = readBit();
			return (most << 1) | (least);
		}
	}
	
	private int readNibble()
	{
		if (DATA_BITS == 4)
		{
			int nibble = samples.getRawSample(index++);
			return nibble & 0b1111;
		}
		else
		{
			int least = readHalfNibble();
			int most = readHalfNibble();
			return (most << 2) | (least);
		}
	}
	
	private int readByte()
	{
		if (DATA_BITS == 8)
		{
			int sample = samples.getRawSample(index++);
			return (sample & 0xFF);
		}
		else
		{
			int least = readNibble();
			int most = readNibble();
			return (most << 4) | (least);
		}
	}
	
	@Override
	public byte read()
	{
		return (byte) readByte();
	}
}














