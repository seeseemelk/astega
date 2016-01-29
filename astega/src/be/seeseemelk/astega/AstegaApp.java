package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

import be.seeseemelk.astega.coders.AstegaDecoder;
import be.seeseemelk.astega.coders.AstegaEncoder;
import be.seeseemelk.astega.coders.BitCoder;
import be.seeseemelk.astega.stream.AstegaInputStream;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class AstegaApp
{
	public static boolean write(File inputFile, File outputFile)
	{
		System.out.println("Performing write test...");
		try
		{
			AstegaEncoder encoder = new BitCoder();
			AstegaOutputStream out = new AstegaOutputStream(encoder, inputFile, outputFile);
			System.out.println("Size limit: " + out.getSizeLimit() + " bytes");
			System.out.println("Encoding data...");
			
			int bytes = 0;
			/*for (int i = 0; i < (out.getSizeLimit() / 4) - 1; i++)
			{
				out.write(i);
				bytes += 4;
			}*/
			for (int i = 0; i < 666666; i++)
			{
				out.write(i);
				bytes += 4;
			}
			out.close();
			System.out.println("Wrote " + bytes + " bytes");
			System.out.println("Data encoded!");
			return true;
		}
		catch (IOException e)
		{
			System.err.println("IO Exception: " + e.getMessage());
		}
		return false;
	}
	
	public static boolean read(File inputFile)
	{
		System.out.println("Performing read test...");
		try
		{
			AstegaDecoder decoder = new BitCoder();
			AstegaInputStream in = new AstegaInputStream(decoder, inputFile);
			
			int size = in.getSize();
			System.out.println("Amount of data available: " + size);
			
			for (int i = 0; i < 100; i++)
			{
				String data = Byte.toString(in.readByte());
				//System.out.println(data);
			}
			
			in.close();
			
			System.out.println("Read test performed!");
			return true;
		}
		catch (IOException e)
		{
			System.err.println("IO Exception: " + e.getMessage());
		}
		return false;
	}
	
	public static void main(String[] arg)
	{
		System.out.println("Starting...");
		String inputFileName = "cirice.wav";
		String outputFileName = "cirice_coded.wav";
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName) ;
		if (write(inputFile, outputFile))
		{
			read(outputFile);
		}
	}
}








