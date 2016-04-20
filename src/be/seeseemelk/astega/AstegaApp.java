package be.seeseemelk.astega;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import be.seeseemelk.astega.coders.AstegaCodec;
import be.seeseemelk.astega.coders.AstegaDecoder;
import be.seeseemelk.astega.coders.AstegaEncoder;
import be.seeseemelk.astega.coders.BitCoder;
import be.seeseemelk.astega.coders.NullCoder;
import be.seeseemelk.astega.coders.ParityCoder;
import be.seeseemelk.astega.stream.AstegaInputStream;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class AstegaApp
{
	public void write(AstegaEncoder encoder, File input, File output, InputStream data) throws IOException
	{
		AstegaOutputStream out = new AstegaOutputStream(encoder, input, output);
		System.out.println("Size limit: " + out.getSizeLimit() + " bytes");
		System.out.println("Encoding data...");
		
		int read;
		while ((read = data.read()) != -1)
			out.write(read);
		
		System.out.println("Amount of bytes encoded: " + out.getSize());
		
		out.close();
		System.out.println("Data encoded!");
	}
	
	public void read(AstegaDecoder decoder, File input, OutputStream output) throws IOException
	{
		AstegaInputStream in = new AstegaInputStream(decoder, input);
		System.out.println("Number of bytes to load: " + in.getSize());
		System.out.println("Decoding data...");
		
		int read;
		while ((read = in.read()) != -1)
			output.write(read);
		
		in.close();
		
		System.out.println("Data decoded");
	}
	
	public void size(AstegaEncoder encoder, File input) throws IOException
	{
		AstegaOutputStream out = new AstegaOutputStream(encoder, input, null);
		int maxsize = out.getSizeLimit();
		System.out.println("Maximum size: " + maxsize + " bytes");
		System.out.println("Maximum size: " + (int) (maxsize / 1024) + " kilobytes");
		System.out.println("Maximum size: " + (int) (maxsize / 1024 / 1024) + " megabytes");
		System.out.println("Currently saved size: " + out.getSize());
		out.close();
	}
	
	public void createSine(File output) throws IOException
	{
		int length = 44100 * 10;
		AstegaSample out = new AstegaSample(output, 1, 44100, 16, length);
		for (int i = 0; i < length; i++)
		{
			double x = (double) i;
			double y = (Math.sin(x / 20.0)) * Math.pow(2.0, 15);
			
			int value = (int) y;
			
			out.setRawSample(i, value);
		}
		out.write(output);
	}
	
	public static void printUsage()
	{
		System.out.println("Usage: astega <codec> <action>\n");
		System.out.println("Actions:");
		System.out.println("encode <data> <cover> <output>     Encode data in a cover file");
		System.out.println("decode <cover> <output>            Decode data from a file");
		System.out.println("info <cover>                       Get information from a file");
		System.out.println("sine <output>                      Create a sine wave");
		System.out.println("img2ewav <input> <output>          Convert an image file to an encoded audio file");
		System.out.println("ewav2img <input> <output> [width height] Convert an encoded audio file back to an image");
		System.out.println("wav2eimg <input> <output>          Convert an audio file to an encoded image");
		System.out.println("eimg2wav <input> <output>          Convert an encoded image file to an audio file");
		System.out.println("\nAvailable codecs:");
		System.out.println("bit8: Saves data in lowest significant bits");
		System.out.println("bit4: Saves data in lowest significant bits");
		System.out.println("bit2: Saves data in lowest significant bits");
		System.out.println("bit1: Saves data in lowest significant bits");
		System.out.println("parity: Saves data using parity coding");
		System.out.println("null: Truncates samples and stores a byte in each sample");
	}
	
	
	public static void main(String[] arg)
	{
		try
		{
			if (arg.length > 1)
			{
				String codecName = arg[0];
				String action = arg[1];
				
				AstegaCodec codec;
				
				// Get the right codec
				switch (codecName)
				{
					case "bit8":
						codec = new BitCoder(8);
						break;
					case "bit4":
						codec = new BitCoder(4);
						break;
					case "bit2":
						codec = new BitCoder(2);
						break;
					case "bit1":
						codec = new BitCoder(1);
						break;
					case "parity":
						codec = new ParityCoder();
						break;
					case "null":
						codec = new NullCoder();
						break;
					default:
						printUsage();
						return;
				}
				
				AstegaApp app = new AstegaApp();
				
				// Perform the action
				switch (action.toLowerCase())
				{
					case "encode":
						if (arg.length > 4)
						{
							File data = new File(arg[2]);
							File input = new File(arg[3]);
							File output = new File(arg[4]);
							BufferedInputStream dataStream = new BufferedInputStream(new FileInputStream(data));
							app.write(codec, input, output, dataStream);
							dataStream.close();
						}
						else
							printUsage();
						break;
					case "decode":
						if (arg.length > 3)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							FileOutputStream outputStream = new FileOutputStream(output);
							app.read(codec, input, outputStream);
							outputStream.close();
						}
						else
							printUsage();
						break;
					case "info":
						if (arg.length > 2)
						{
							File input = new File(arg[2]);
							app.size(codec, input);
						}
						else
							printUsage();
						break;
					case "sine":
						if (arg.length > 2)
						{
							File output = new File(arg[2]);
							app.createSine(output);
						}
						else
							printUsage();
						break;
					case "img2ewav":
						if (arg.length > 3)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							Convertor.convertImageToEncodedAudio(input, output, true);
						}
						else
							printUsage();
						break;
					case "ewav2img":
						if (arg.length > 5)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							int width = Integer.parseInt(arg[4]);
							int height = Integer.parseInt(arg[5]);
							Convertor.convertEncodedAudioToImage(input, output, width, height, true);
						}
						else if (arg.length > 3)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							Convertor.convertEncodedAudioToImage(input, output, true);
						}
						else
							printUsage();
						break;
					case "wav2eimg":
						if (arg.length > 3)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							Convertor.convertAudioToEncodedImage(input, output);
						}
						else
							printUsage();
						break;
					case "eimg2wav":
						if (arg.length > 3)
						{
							File input = new File(arg[2]);
							File output = new File(arg[3]);
							Convertor.convertEncodedImageToAudio(input, output);
						}
						else
							printUsage();
						break;
					default:
						printUsage();
				}
			}
			else
			{
				printUsage();
			}
			
			/*System.out.println("Starting...");
			
			File inputFile = new File("cirice.wav");
			File outputFile = new File("cirice_coded.wav");
			File dataFile = new File("data.bin");
			
			BufferedInputStream dataInput = new BufferedInputStream(new FileInputStream(dataFile));
			AstegaApp app = new AstegaApp();
			AstegaEncoder encoder = new BitCoder();
			app.write(encoder, inputFile, outputFile, dataInput);*/
		}
		catch (IOException e)
		{
			System.err.println("IO Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}




























