package be.seeseemelk.astega;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Convertor
{
	public static void convertImageToAudio(File imagefile, File audiofile, boolean singleChannel) throws IOException
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
	
	public static void convertAudioToImage(File audiofile, File imagefile, int width, int height, boolean singleChannel) throws IOException
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
	
	public static void convertAudioToImage(File audiofile, File imagefile, boolean singleChannel) throws IOException
	{
		convertAudioToImage(audiofile, imagefile, -1, -1, singleChannel);
	}
}
