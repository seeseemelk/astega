package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

public class AstegaSample
{
	private WaveReader wave;
	
	public AstegaSample(File file) throws IOException
	{
		wave = new WaveReader(file);
	}
	
	public AstegaSample(File file, int numchannels, int samplerate, int bitspersample, int size) throws IOException
	{
		wave = new WaveReader(file, numchannels, samplerate, bitspersample, size);
	}
	
	public void write(File destination) throws IOException
	{
		wave.flush(destination);
	}
	
	public int getRawSample(int index)
	{
		//return samples[index];
		return wave.getSample(index);
	}
	
	public void setRawSample(int index, int value)
	{
		//samples[index] = value;
		wave.setSample(index, value);
	}
	
	public int getSample(int index, int channel)
	{
		index *= getNumberOfChannels();
		return getRawSample(index + channel);
	}
	
	public void setSample(int index, int channel, int value)
	{
		index *= getNumberOfChannels();
		setRawSample(index + channel, value);
	}
	
	public int getNumberOfChannels()
	{
		return wave.getNumberOfChannels();
	}
	
	public int getNumberOfFrames()
	{
		return getNumberOfSamples() / getNumberOfChannels();
	}
	
	public int getNumberOfSamples()
	{
		return wave.getNumberOfSamples();
	}
	
	public int getFramerate()
	{
		return wave.getSampleRate() / getNumberOfChannels();
	}
	
	public int getBitsPerSample()
	{
		return wave.getBitsPerSample();
	}
}














