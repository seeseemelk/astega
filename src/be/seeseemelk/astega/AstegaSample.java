package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

public class AstegaSample
{
	private WaveReader wave;
	/*private int framerate;
	private int numChannels;
	private int numFrames;
	private int numSamples;
	private int bitsPerSample;*/
	
	public AstegaSample(File file) throws IOException
	{
		wave = new WaveReader(file);
		
		/*FloatSample floatSamples = SampleLoader.loadFloatSample(file);
		samples = new float[floatSamples.getNumFrames()];
		floatSamples.read(samples);
		
		framerate = (int) floatSamples.getFrameRate();
		numChannels = floatSamples.getChannelsPerFrame();
		numFrames = floatSamples.getNumFrames();
		numSamples = numChannels * numFrames;
		bitsPerSample = 24;*/
	}
	
	public void write(File destination) throws IOException
	{
		wave.flush(destination);
		/*WaveFileWriter recorder = new WaveFileWriter(destination);
		recorder.setBitsPerSample(24);
		recorder.setFrameRate(getFramerate());
		recorder.setSamplesPerFrame(getNumberOfChannels());
		recorder.write(samples);
		recorder.close();*/
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














