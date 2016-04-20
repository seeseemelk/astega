package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

import be.seeseemelk.astega.coders.AstegaDecoder;
import be.seeseemelk.astega.coders.AstegaEncoder;
import be.seeseemelk.astega.stream.AstegaInputStream;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class Tester
{
	private AstegaEncoder encoder;
	private AstegaDecoder decoder;
	private File inputFile;
	private File outputFile;
	private int lastRead;
	private int lastBad;
	private int lastBadPercentage;
	
	public Tester(File input, File output)
	{
		inputFile = input;
		outputFile = output;
	}
	
	public void setCodecs(AstegaEncoder encoder, AstegaDecoder decoder)
	{
		this.encoder = encoder;
		this.decoder = decoder;
	}
	
	private void applyNoise(double rate) throws IOException
	{
		AstegaSample sample = new AstegaSample(outputFile);
		
		int length = sample.getNumberOfSamples();
		
		double noiseconstant = Math.pow(2.0, (double) sample.getBitsPerSample()-1.0) * rate;
		
		System.out.println("Skipping first 32 bytes in case of headers");
		
		for (int i = 32; i < length; i++)
		{
			int value = sample.getRawSample(i);
			double noise = (Math.random()-0.5) * noiseconstant;
			value += (int) noise;
			sample.setRawSample(i, value);
		}
		
		sample.write(outputFile);
	}
	
	public void test(byte[] data, double noiserate) throws IOException
	{
		AstegaOutputStream output = new AstegaOutputStream(encoder, inputFile, outputFile);
		
		int sizelimit = output.getSizeLimit();
		
		if (data.length > sizelimit)
		{
			System.err.println("Input sample data size is too large (data: " + data.length + "; max: " + sizelimit + ")");
			System.err.println("Will continue without excess data.");
		}
		
		int limit = Math.min(data.length, sizelimit);
		
		//System.out.println("Saving data...");
		for (int i = 0; i < limit; i++)
		{
			output.writeByte(data[i]);
		}
		
		output.close();
		//System.out.println("Done saving data");
		
		if (noiserate > 0)
		{
			System.out.println("Applying noise");
			applyNoise(noiserate);
		}
		
		//System.out.println("Loading data");
		AstegaInputStream input = new AstegaInputStream(decoder, outputFile);
		int savedbytes = input.getSize();
		if (savedbytes != limit)
		{
			System.err.println("Number of bytes that can be read does not equal number of bytes that were saved (written: " + limit + "; readable: " + savedbytes + ")");
			if (savedbytes > limit)
				System.err.println("Header most likely corrupted, will continue anyway");
			else
				System.err.println("Will continue with available data");
		}
		
		limit = Math.min(savedbytes, limit);
		
		//byte[] read = new byte[limit];
		int read = 0;
		int bad = 0;
		for (int i = 0; i < limit; i++)
		{
			byte value = input.readByte();
			read++;
			if (value != data[i])
			{
				if (bad < 10)
					System.err.println("Read data incorrect: got " + value + ", expected " + data[i]);
				bad++;
			}
		}
		
		input.close();
		
		int good = read - bad;
		int badPercentage = (int) ((double) bad / (double) read * 100.0);
		int goodPercentage = (int) ((double) good / (double) read * 100.0);
		
		lastRead = read;
		lastBad = bad;
		lastBadPercentage = badPercentage;
		
		if (read <= 0)
		{
			System.err.println("Test completed, however 0 bytes were read!");
		}
		else
		{
			System.out.println("Test completed:");
			System.out.println("Number of bytes read: " + read);
			System.out.println("Number of bytes bad: " + bad + " (" + badPercentage + "%)");
			
			String status;
			if (bad > 0)
			{
				if (badPercentage <= 5)
					status = "ECC REQUIRED";
				else if (badPercentage <= 50)
					status = "DATA DAMAGED";
				else if (badPercentage <= 90)
					status = "DATA HEAVILY DAMAGED";
				else
					status = "UNRECOVERABLE";
			}
			else
				status = "GOOD";
			System.out.println("End result: " + status + " [ " + good + "/" + read + " (" + goodPercentage + "% good) ]");
		}
	}
	
	public int getAmountReadInTest()
	{
		return lastRead;
	}
	
	public int getAmountBadReadInTest()
	{
		return lastBad;
	}
	
	public int getAmountBadPercentage()
	{
		return lastBadPercentage;
	}
	
	public int createSampleCoverFile() throws IOException
	{
		int samplerate = 44100;
		int bitspersample = 16;
		int bytespersample = bitspersample / 8;
		
		int sinelength = samplerate * 5;
		int silentlength = samplerate * 5;
		int randomlength = samplerate * 5;
		
		int length = sinelength + silentlength + randomlength;
		
		AstegaSample out = new AstegaSample(inputFile, 1, samplerate, bitspersample, length);
		
		int sampleindex = 0;
		
		// Save sine wave
		for (int i = 0; i < sinelength; i++)
		{
			int value = (int) ((Math.sin((double) i / 20.0)) * Math.pow(2.0, 15));
			out.setRawSample(sampleindex++, value);
		}
		
		// Save silent wave
		for (int i = 0; i < silentlength; i++)
		{
			int value = (int) (Math.pow(2.0, 15) - 1);
			out.setRawSample(sampleindex++, value);
		}
		
		// Save random wave
		for (int i = 0; i < randomlength; i++)
		{
			int value = (int) ((Math.random() - 0.5) * Math.pow(2.0, 16));
			out.setRawSample(sampleindex++, value);
		}
		
		out.write(inputFile);
		
		return bytespersample * length;
	}
	
	public byte[] createSampleData(int length)
	{
		byte[] data = new byte[length];
		
		for (int i = 0; i < length; i++)
		{
			data[i] = (byte) (Math.random() * 256);
		}
		
		return data;
	}
}



















