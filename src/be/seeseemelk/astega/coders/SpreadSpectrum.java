package be.seeseemelk.astega.coders;

import org.jtransforms.fft.DoubleFFT_1D;

import be.seeseemelk.astega.AstegaSample;

public class SpreadSpectrum implements AstegaCodec
{
	private int multiplier = 4;
	private double freqsPerData = 10;
	private int bitsPerData = 2;
	
	private int index = 0;
	private AstegaSample samples;
	private byte[] data;
	private double[] dft;
	private DoubleFFT_1D fft;
	
	private void calculateDFT()
	{
		System.out.println("Preparing calculation of DFT");
		System.out.println("Alocating dft (size: " + (Double.BYTES * samples.getNumberOfFrames() * 2) + ")");
		dft = new double[samples.getNumberOfFrames() * 2];
		System.out.println("Alocating data (size: " + (Byte.BYTES * samples.getNumberOfFrames()) + ")");
		data = new byte[samples.getNumberOfFrames()];
		System.out.println("Copying data");
		
		for (int i = 0; i < samples.getNumberOfFrames(); i++)
			dft[i] = samples.getSample(i, 0);
		
		System.out.println("Calculating DFT");
		fft.realForwardFull(dft);
		System.out.println("Finished calculating DFT");
		
		double real, imag, amplitude;
		
		int mask = (int) Math.pow(2, bitsPerData) - 1;
		int bits;
		
		int dataIndex, bitIndex;
		
		for (int a = 0; a < (dft.length - freqsPerData); a += freqsPerData)
		{
			amplitude = 0;
			for (int b = 0; b < freqsPerData; b++)
			{
				int i = a + b;
				real = dft[i];
				imag = dft[i+1];
				amplitude += Math.sqrt(real*real + imag*imag);
			}
			amplitude /= freqsPerData;
			
			dataIndex = (int) (a / freqsPerData / (8-bitsPerData));
			bitIndex = (int) ((a / freqsPerData) % (8-bitsPerData));
			bits = (int) amplitude & mask;
			
			// Write bits
			data[dataIndex] |= bits << bitIndex;
		}
	}
	
	@Override
	public void setSamples(AstegaSample samples)
	{
		this.samples = samples;
		fft = new DoubleFFT_1D(samples.getNumberOfFrames());
		calculateDFT();
	}
	
	@Override
	public int getSizeLimit()
	{
		return (int) (samples.getNumberOfFrames() / bitsPerData / freqsPerData); 
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
		data[index++] = (byte) b;
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
	
	private void writeByte(int location, byte value)
	{
		int mask = (int) Math.pow(2, bitsPerData) - 1;
		
		double real, imag, phase, amplitude;
		
		for (int i = 0; i < bitsPerData; i += bitsPerData)
		{
			real = dft[i];
			imag = dft[i+1];
			
			phase = Math.atan2(imag, real);
			amplitude = Math.sqrt(real*real + imag*imag);
			
			/*amplitude = (int) amplitude & (~mask);
			amplitude = (int) amplitude | ((value >> bitsPerData) & mask);*/
			amplitude *= 0.01;
			
			real = Math.cos(phase) * amplitude;
			imag = Math.sin(phase) * amplitude;
			
			dft[i] = real;
			dft[i+1] = imag;
		}
	}
	
	private void flushSegment(int segment)
	{
		
		
		/*bitIndex = 0;
		double[] dft = new double[dataSize*2];
		
		int offset = (int) Math.pow(2, samples.getBitsPerSample()-1);
		
		int segmentOffset = dataSize * segment;
		int numFrames = samples.getNumberOfFrames();
		
		for (int i = 0; i < dataSize; i++)
		{
			if ((i+segmentOffset) >= numFrames)
				dft[i] = 0;
			else
				dft[i] = ((double) samples.getSample(i + segmentOffset, 0));
		}
		
		DoubleFFT_1D fft = new DoubleFFT_1D(dataSize);
		fft.realForwardFull(dft);
		
		double phaseShiftAmount = Math.toRadians(90.0);
		
		double real, imag;
		double phase, amplitude;
		
		for (int i = 0; i < dft.length; i+=2)
		{
			real = dft[i];
			imag = dft[i+1];
			
			phase = Math.atan2(imag, real);
			
			if (getNextBitOfData() == 0)
				phase += phaseShiftAmount;
			else
				phase -= phaseShiftAmount;
			
			//if (segment <= 0)
				//System.out.println("Phase at " + i + " = " + phase);
			
			amplitude = Math.sqrt(real*real + imag*imag);
			
			real = Math.cos(phase) * amplitude;
			imag = Math.sin(phase) * amplitude;
			
			dft[i] = real;
			dft[i+1] = imag;
		}
		
		// Calculate inverse dft
		fft.realInverse(dft, true);
		
		for (int i = 0; i < dataSize; i++)
		{
			double value = clamp(dft[i], -offset, offset-1);
			if (i+segmentOffset < numFrames)
				samples.setSample(i+segmentOffset, 0, (int) Math.round(value));
		}*/
	}
	
	@Override
	public void flush()
	{
		System.out.println("Flusing");
		for (int i = 0; i < data.length; i++)
		{
			writeByte(i, data[i]);
		}
		
		fft.realInverse(dft, true);
		
		int offset = (int) Math.pow(2, samples.getBitsPerSample()-1);
		
		for (int i = 0; i < dft.length/2; i++)
		{
			double value = clamp(dft[i], -offset, offset-1);
			samples.setSample(i, 0, (int) Math.round(value));
		}
		
		/*System.out.println("Encoding...");
		int segments = (int) Math.ceil((double) samples.getNumberOfFrames() / dataSize);
		
		for (int i = 0; i < segments; i++)
		{
			flushSegment(i);
		}
			
		System.gc();
		System.out.println("Done");*/
	}
}























