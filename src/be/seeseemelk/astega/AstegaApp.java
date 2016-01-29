package be.seeseemelk.astega;

import java.io.File;
import java.io.IOException;

import be.seeseemelk.astega.coders.AstegaEncoder;
import be.seeseemelk.astega.coders.BitCoder;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class AstegaApp
{
	public static void main(String[] arg)
	{
		System.out.println("Starting...");
		String inputFileName = "cirice.wav";
		String outputFileName = "cirice_coded.wav";
		File inputFile = new File(inputFileName);
		File outputFile = new File(outputFileName) ;
		
		try
		{
			//WaveReader wave = new WaveReader(inputFile);
			AstegaEncoder encoder = new BitCoder();
			AstegaOutputStream out = new AstegaOutputStream(encoder, inputFile, outputFile);
			System.out.println("Size limit: " + out.getSizeLimit() + " bytes");
			System.out.println("Encoding data...");
			for (int i = 0; i < (out.getSizeLimit() / 4); i++)
			{
				out.write(i);
			}
			out.close();
			System.out.println("Data encoded!");
		}
		catch (IOException e)
		{
			System.err.println("IO Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
