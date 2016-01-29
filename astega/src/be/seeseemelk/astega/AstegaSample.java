package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

import com.jsyn.data.FloatSample;
import com.jsyn.util.SampleLoader;
import com.jsyn.util.WaveFileWriter;
import com.sun.media.sound.WaveFileReader;

public class AstegaSample
{
	private float[] samples;
	private int framerate;
	private int numChannels;
	private int numFrames;
	private int numSamples;
	private int bitsPerSample;
	
	public AstegaSample(File file) throws IOException
	{
		FloatSample floatSamples = SampleLoader.loadFloatSample(file);
		samples = new float[floatSamples.getNumFrames()];
		floatSamples.read(samples);
		
		framerate = (int) floatSamples.getFrameRate();
		numChannels = floatSamples.getChannelsPerFrame();
		numFrames = floatSamples.getNumFrames();
		numSamples = numChannels * numFrames;
		bitsPerSample = 16;
	}
	
	public void write(File destination) throws IOException
	{
		WaveFileWriter recorder = new WaveFileWriter(destination);
		recorder.setBitsPerSample(24);
		recorder.setFrameRate(getFramerate());
		recorder.setSamplesPerFrame(getNumberOfChannels());
		recorder.write(samples);
		recorder.close();
	}
	
	public float getRawSample(int index)
	{
		return samples[index];
	}
	
	public void setRawSample(int index, float value)
	{
		samples[index] = value;
	}
	
	public float getSample(int index, int channel)
	{
		index *= getNumberOfChannels();
		return getRawSample(index + channel);
	}
	
	public void setSample(int index, int channel, float value)
	{
		index *= getNumberOfChannels();
		setRawSample(index + channel, value);
	}
	
	public int getNumberOfChannels()
	{
		return numChannels;
	}
	
	public int getNumberOfFrames()
	{
		return numFrames;
	}
	
	public int getNumberOfSamples()
	{
		return numSamples;
	}
	
	public int getFramerate()
	{
		return framerate;
	}
	
	public int getBitsPerSample()
	{
		return bitsPerSample;
	}
}














