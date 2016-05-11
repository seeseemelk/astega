package be.seeseemelk.astega.coders;

import java.io.File;
import java.io.IOException;

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
	
	private boolean allowDecode = false;
	
	private double[] firstPhases;
	private double[] originalPhases;
	
	public PhaseCoder()
	{
		
	}
	
	public PhaseCoder(File originalFile, int originalSize) throws IOException
	{
		originalSize += 4;
		AstegaSample samples = new AstegaSample(originalFile);
		
		// Load the original phases
		originalPhases = new double[originalSize*8];
		fillArrayWithPhases(originalPhases, samples, 0, originalPhases.length);
		
		allowDecode = true;
	}
	
	private double normalizeAngle(double angle)
	{
	    double newAngle = angle;
	    while (newAngle <= -180.0) newAngle += 360.0;
	    while (newAngle > 180.0) newAngle -= 360.0;
	    return newAngle;
	}

	private void fillArrayWithPhases(double[] array, AstegaSample samples, int start, int size)
	{
		double[] dft = new double[size*2];
		
		int offset = (int) Math.pow(2, samples.getBitsPerSample()-1);
		
		if (size >= (samples.getNumberOfFrames() + start))
		{
			System.err.println("Too many bytes to create phase table");
			System.exit(1);
		}
		
		int end = size+start;
		for (int i = start; i < end; i++)
		{
			dft[i] = ((double) samples.getSample(i, 0)); // / offset;
		}
		
		DoubleFFT_1D fft = new DoubleFFT_1D(size);
		fft.realForwardFull(dft);
		
		double real, imag;
		double phase;
		
		for (int i = 0; i < dft.length; i+=2)
		{
			real = dft[i];
			imag = dft[i+1];
			
			phase = Math.atan2(imag, real);
			array[i/2] = Math.toDegrees(phase);
		}
	}
	
	private void readAllData()
	{
		data = new byte[originalPhases.length / 8];
		
		int dataIndex, bitIndex;
		double deltaPhase;
		
		System.out.println("Original phases length: " + originalPhases.length);
		
		for (int i = 0; i < originalPhases.length; i++)
		{
			dataIndex = i / 8;
			bitIndex = i % 8;
			
			deltaPhase = normalizeAngle(originalPhases[i] - firstPhases[i]);
			//System.out.println(originalPhases[i] + " - " + firstPhases[i] + " = " + deltaPhase);
			System.out.println(deltaPhase);
			if (deltaPhase < 0)
			{
				data[dataIndex] |= 1 << bitIndex;
				//System.out.println("Bit " + bitIndex + ": " + data[dataIndex]);
			}
		}
	}
	
	@Override
	public void setSamples(AstegaSample samples)
	{
		this.samples = samples;
		lastIndex = samples.getNumberOfFrames();
		
		if (allowDecode)
		{
			System.out.println("Calculating first phases");
			firstPhases = new double[originalPhases.length];
			fillArrayWithPhases(firstPhases, samples, 0, originalPhases.length);
			readAllData();
		}
		else
		{
			data = new byte[getSizeLimit()];
		}
	}
	
	@Override
	public int getSizeLimit()
	{
		return lastIndex / 8; 
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
				dft[i] = ((double) samples.getSample(i + segmentOffset, 0) / 2);
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
			//phase += phaseShiftAmount; 
			
			if (getNextBitOfData() == 0)
				phase += phaseShiftAmount;
			else
				phase -= phaseShiftAmount;
			
			amplitude = Math.sqrt(real*real + imag*imag);
			
			//if (segment <= 0)
				//System.out.println("Amplitude at " + i + " = " + amplitude);
			
			//if (amplitude <= 10)
				//amplitude += 10;
			
			//System.out.println(amplitude);
			
			real = Math.cos(phase) * amplitude;
			imag = Math.sin(phase) * amplitude;
			
			//dft[i] = real;
			//dft[i+1] = imag;
		}
		
		// Calculate inverse dft
		fft.realInverse(dft, true);
		
		for (int i = 0; i < dataSize; i+=2)
		{
			double value = clamp(dft[i], -offset, offset-1);
			if (Math.abs(dft[i] - value) > 0.1)
				System.out.println("Value clamped " + i);
			//System.out.println(i + ": " + dft[i]);
			
			if (i+segmentOffset < numFrames)
				samples.setSample((i/2)+segmentOffset, 0, (int) Math.round(value));
		}
	}
	
	@Override
	public void flush()
	{
		System.out.println("Encoding...");
		int segments = (int) Math.ceil((double) samples.getNumberOfFrames() / dataSize);
		
		for (int i = 0; i < segments; i++)
		//for (int i = 0; i < 1; i++)
		{
			flushSegment(i);
		}
			
		System.gc();
		System.out.println("Done");
	}
}























