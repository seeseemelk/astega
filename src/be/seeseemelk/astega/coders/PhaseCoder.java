package be.seeseemelk.astega.coders;

import org.jtransforms.fft.DoubleFFT_1D;

import be.seeseemelk.astega.AstegaSample;

public class PhaseCoder implements AstegaCodec
{
	private int index = 0;
	private int lastIndex;
	private AstegaSample samples;
	private byte[] data;
	private int dataSize = 0;
	private int bitIndex = 0;
	
	@Override
	public void setSamples(AstegaSample samples)
	{
		this.samples = samples;
		lastIndex = samples.getNumberOfSamples();
		data = new byte[getSizeLimit()];
	}
	
	@Override
	public int getSizeLimit()
	{
		return lastIndex / 8; 
	}
	
	@Override
	public void seek(int b)
	{
		index = b * 8;
	}

	@Override
	public int tell()
	{
		return index / 8;
	}
	
	@Override
	public void write(int b)
	{
		data[index++] = (byte) b;
		dataSize = Math.max(index*8, dataSize);
	}
	
	@Override
	public byte read()
	{
		return data[index++];
	}
	
	private double clamp(double value, double min, double max)
	{
		return Math.min(max, Math.max(value, min));
	}
	
	private int getNextBitOfData()
	{
		int index = bitIndex / 8;
		int bitShift = bitIndex % 8;
		
		int value = 0;
		if (index < data.length)
			value = data[index];
		
		int bit = (value >> bitShift) & 1;
		bitIndex++;
		
		return bit;
	}
	
	private void flushSegment(int segment)
	{
		bitIndex = 0;
		double[] dft = new double[dataSize*2];
		
		int offset = (int) Math.pow(2, samples.getBitsPerSample()-1);
		
		int segmentOffset = dataSize * segment;
		int numFrames = samples.getNumberOfFrames();
		
		for (int i = 0; i < dataSize; i++)
		{
			if ((i+segmentOffset) >= numFrames)
				dft[i] = 0;
			else
				dft[i] = ((double) samples.getSample(i + segmentOffset, 0)) / offset;
		}
		
		DoubleFFT_1D fft = new DoubleFFT_1D(dataSize);
		fft.realForwardFull(dft);
		
		double phaseShiftAmount = Math.toDegrees(90);
		
		for (int i = 0; i < dft.length; i+=2)
		{
			double real = dft[i];
			double imag = dft[i+1];
			
			double phase = Math.atan2(imag, real);
			
			if (getNextBitOfData() == 0)
				phase += phaseShiftAmount;
			else
				phase -= phaseShiftAmount;
			
			double amplitude = Math.sqrt(real*real + imag*imag);
			
			real = Math.cos(phase) * amplitude;
			imag = Math.sin(phase) * amplitude;
			
			dft[i] = real;
			dft[i+1] = imag;
		}
		
		// Calculating inverse dft
		fft.realInverse(dft, true);
		
		for (int i = 0; i < dataSize; i++)
		{
			double value = clamp(dft[i]*offset, -offset, offset-1);
			if (i < numFrames)
				samples.setRawSample(i+segmentOffset, (int) value);
		}
		fft = null;
	}
	
	@Override
	public void flush()
	{
		System.out.println("Encoding...");
		int segments = (int) Math.ceil((double) samples.getNumberOfFrames() / dataSize);
		for (int i = 0; i < segments; i++)
			flushSegment(i);
		System.gc();
		System.out.println("Done");
	}
}























