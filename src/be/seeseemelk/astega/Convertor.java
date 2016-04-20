package be.seeseemelk.astega;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Convertor
{
	public static void convertImageToEncodedAudio(File imagefile, File audiofile, boolean singleChannel) throws IOException
	{
		BufferedImage image = ImageIO.read(imagefile);
		
		int datasize = image.getWidth() * image.getHeight() * 3;
		int samplerate = 44100;
		int bitspersample = 8;
		
		int[] data = new int[datasize/3]; 
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
		
		AstegaSample out;
		
		if (singleChannel)
			out = new AstegaSample(audiofile, 1, samplerate, bitspersample, datasize+8);
		else
			out = new AstegaSample(audiofile, 3, samplerate, bitspersample, (datasize+8)/3);
		
		for (int i = 0; i < data.length; i++)
		{
			int color = data[i];
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			out.setRawSample((i*3)+8, r);
			out.setRawSample((i*3)+9, g);
			out.setRawSample((i*3)+10, b);
		}
		
		out.setRawSample(0, image.getWidth());
		out.setRawSample(1, image.getWidth() >> 8);
		out.setRawSample(2, image.getWidth() >> 16);
		out.setRawSample(3, image.getWidth() >> 24);
		
		out.setRawSample(4, image.getHeight());
		out.setRawSample(5, image.getHeight() >> 8);
		out.setRawSample(6, image.getHeight() >> 16);
		out.setRawSample(7, image.getHeight() >> 24);
		
		out.write(audiofile);
	}
	
	public static void convertEncodedAudioToImage(File audiofile, File imagefile, int width, int height, boolean singleChannel) throws IOException
	{
		AstegaSample in = new AstegaSample(audiofile);
		
		if (width < 0)
			width = in.getRawSample(0) | (in.getRawSample(1) << 8) | (in.getRawSample(2) << 16) | (in.getRawSample(3) << 24);
		
		if (height < 0)
			height = in.getRawSample(4) | (in.getRawSample(5) << 8) | (in.getRawSample(6) << 16) | (in.getRawSample(7) << 24);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		int datasize = image.getWidth() * image.getHeight() * 3;
		int[] data = new int[datasize/3]; 
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
		
		
		for (int i = 0; i < datasize; i+=3)
		{
			int a = 0xFF;
			int r = in.getRawSample(i+8);
			int g = in.getRawSample(i+9);
			int b = in.getRawSample(i+10);
			int color = (a << 24) | (r << 16) | (g << 8) | (b);
			data[i/3] = color;
		}
		
		image.setRGB(0, 0, width, height, data, 0, width);
		ImageIO.write(image, "png", imagefile);
	}
	
	public static void convertEncodedAudioToImage(File audiofile, File imagefile, boolean singleChannel) throws IOException
	{
		convertEncodedAudioToImage(audiofile, imagefile, -1, -1, singleChannel);
	}
	
	private static int encImageSetDataByte(int[] data, int index, int value)
	{
		value = value & 0xFF;
		
		int pixelnum = index / 3;
		int channelnum = index % 3;
		
		int color = data[pixelnum];
		int shift = 8 * channelnum;
		int mask = 255 << shift;
		color = (color & ~mask) | (value << shift);
		
		data[pixelnum] = color;
		
		return index+1;
	}
	
	private static int encImageSetDataShort(int[] data, int index, int value)
	{
		index = encImageSetDataByte(data, index, value);
		index = encImageSetDataByte(data, index, (value >> 8));
		return index;
	}
	
	private static int encImageSetDataInt(int[] data, int index, int value)
	{
		index = encImageSetDataShort(data, index, value);
		index = encImageSetDataShort(data, index, (value >> 16));
		return index;
	}
	
	public static void convertAudioToEncodedImage(File audiofile, File imagefile) throws IOException
	{
		AstegaSample in = new AstegaSample(audiofile);
		
		int numsamples = in.getNumberOfSamples();
		int bytespersample = in.getBitsPerSample() / 8;
		int numbytes = numsamples * bytespersample + 16;
		
		double side = Math.ceil(Math.sqrt(Math.ceil((double)(numbytes)/3)));
		int width = (int) side;
		int height = (int) side;
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int[] data = new int[width * height];
		image.getRGB(0, 0, width, height, data, 0, width);
		
		int ii = 0;
		
		ii = encImageSetDataInt(data, ii, numsamples);
		ii = encImageSetDataInt(data, ii, in.getNumberOfChannels());
		ii = encImageSetDataInt(data, ii, in.getBitsPerSample());
		ii = encImageSetDataInt(data, ii, in.getSamplerate());
				
		for (int i = 0; i < numsamples; i++)
		{
			int sample = in.getRawSample(i);
			if (bytespersample >= 1)
				ii = encImageSetDataByte(data, ii, sample);
			if (bytespersample >= 2)
				ii = encImageSetDataByte(data, ii, sample>>8);
			if (bytespersample >= 3)
				ii = encImageSetDataByte(data, ii, sample>>16);
			if (bytespersample >= 4)
				ii = encImageSetDataByte(data, ii, sample>>24);
		}
		
		int area = width * height * 3;
		System.out.println("Need to fill " + (area-numbytes) + " bytes");
		for (int i = numbytes; i < area; i++)
		{
			ii = encImageSetDataByte(data, ii, data[i-numbytes]);
			//ii = encImageSetDataByte(data, ii, 0xFF0000);
		}
		
		image.setRGB(0, 0, width, height, data, 0, width);
		ImageIO.write(image, "png", imagefile);
	}
	
	private static int encImageReadByte(int[] data, int index)
	{
		int pixelnum = index / 3;
		int channelnum = index % 3;
		
		int color = data[pixelnum];
		int shift = 8 * channelnum;
		int mask = 255 << shift;
		int value = (color & mask) >> shift;
		
		return value;
	}
	
	private static int encImageReadShort(int[] data, int index)
	{
		int value = encImageReadByte(data, index);
		value |= encImageReadByte(data, index+1) << 8;
		return value;
	}
	
	private static int encImageReadInt(int[] data, int index)
	{
		int value = encImageReadShort(data, index);
		value |= encImageReadShort(data, index+2) << 16;
		return value;
	}
	
	public static void convertEncodedImageToAudio(File imagefile, File audiofile) throws IOException
	{
		BufferedImage image = ImageIO.read(imagefile);
		
		int datasize = image.getWidth() * image.getHeight();
		
		int[] data = new int[datasize]; 
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());
		
		int ii = 0;
		
		int numsamples = encImageReadInt(data, ii); ii+=4;
		int numchannels = encImageReadInt(data, ii); ii+=4;
		int bitspersample = encImageReadInt(data, ii); ii+=4;
		int bytespersample = bitspersample/8;
		int samplerate = encImageReadInt(data, ii); ii+=4;
		int encodeddatasize = numsamples*bytespersample + 16;
		
		//System.out.println("Number of samples: " + numsamples);
		
		AstegaSample out = new AstegaSample(audiofile, numchannels, samplerate, bitspersample, numsamples/numchannels);
		
		int ai = 0;
		for (int i = ii; i < encodeddatasize; i++)
		{
			int sample = 0;
			sample = encImageReadByte(data, i);
			if (bytespersample > 1)
				sample |= encImageReadByte(data, ++i) << 8;
			if (bytespersample > 2)
				sample |= encImageReadByte(data, ++i) << 16;
			if (bytespersample > 3)
				sample |= encImageReadByte(data, ++i) << 24;
			out.setRawSample(ai++, sample);
			if ((i % 100) == 0)
				System.out.println((double)i/encodeddatasize*100);
		}
		
		out.write(audiofile);
	}
}






















