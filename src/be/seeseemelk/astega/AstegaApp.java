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
	
	public static void printUsage()
	{
		System.out.println("Usage: astega <codec> <action>\n");
		System.out.println("Actions:");
		System.out.println("encode <data> <input> <output>");
		System.out.println("decode <input> <output>");
		System.out.println("\nAvailable codecs:");
		System.out.println("bit: Saves data in lowest significant bits");
	}
	
	
	public static void main(String[] arg)
	{
		try
		{
			if (arg.length > 1)
			{
				String action = arg[0];
				String codecName = arg[1]; 
				
				AstegaCodec codec;
				
				// Get the right codec
				switch (codecName)
				{
					case "bit":
						codec = new BitCoder();
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
							File input = new File(arg[3]);
							File output = new File(arg[4]);
							FileOutputStream outputStream = new FileOutputStream(output);
							app.read(codec, input, outputStream);
							outputStream.close();
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





























