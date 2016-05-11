package be.seeseemelk.astega;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import be.seeseemelk.astega.coders.AstegaCodec;
import be.seeseemelk.astega.coders.AstegaDecoder;
import be.seeseemelk.astega.coders.AstegaEncoder;
import be.seeseemelk.astega.coders.BitCoder1;
import be.seeseemelk.astega.coders.BitCoder2;
import be.seeseemelk.astega.coders.BitCoder4;
import be.seeseemelk.astega.coders.BitCoder8;
import be.seeseemelk.astega.coders.NullCoder;
import be.seeseemelk.astega.coders.ParityCoder;
import be.seeseemelk.astega.coders.PhaseCoder;
import be.seeseemelk.astega.coders.SpreadSpectrum;
import be.seeseemelk.astega.stream.AstegaInputStream;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class AstegaApp
{
	private static String action;
	private static File dataFile;
	private static File inputFile;
	private static File outputFile;
	private static File extraFile;
	private static AstegaCodec codec;
	private static double amountOfNoise = 0;
	private static int dataSize = Integer.MIN_VALUE;
	private static boolean doOverwrite = false;

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
	
	public void printInfo(AstegaDecoder decoder, File input) throws IOException
	{
		System.out.println("File info:");
		
		AstegaSample sample = new AstegaSample(input);
		System.out.println("Bits per sample: " + sample.getBitsPerSample());
		System.out.println("Framerate: " + sample.getFramerate());
		System.out.println("Samplerate: " + sample.getSamplerate());
		System.out.println("Number of channels: " + sample.getNumberOfChannels());
		System.out.println("Number of frames: " + sample.getNumberOfFrames());
		System.out.println("Number of samples: " + sample.getNumberOfSamples());
		
		if (decoder != null)
		{
			System.out.println();
			System.out.println("Decoder info: ");
			
			AstegaInputStream in = new AstegaInputStream(decoder, input);
			if (decoder instanceof AstegaEncoder)
			{
				AstegaEncoder encoder = (AstegaEncoder) decoder;
				int sizeLimit = encoder.getSizeLimit();
				System.out.println("Maximum possible size: " + sizeLimit);
			}
			System.out.println("Number of readable bytes: " + in.getSize());
			in.close();
		}
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

	private void doTest(Tester tester, AstegaEncoder encoder, AstegaDecoder decoder, byte[] data, double noiserate,
			Map<Class<? extends AstegaEncoder>, Integer> read, Map<Class<? extends AstegaEncoder>, Integer> bad)
			throws IOException
	{
		System.out.println("Performing test with " + codec.getClass().getSimpleName());
		tester.setCodecs(encoder, decoder);
		tester.test(data, noiserate);

		if (read != null)
		{
			read.put(encoder.getClass(), tester.getAmountReadInTest());
			bad.put(encoder.getClass(), tester.getAmountBadReadInTest());
		}
	}

	public void test(AstegaCodec codec, File input, File output, File datafile, double noiserate) throws IOException
	{
		Tester tester = new Tester(input, output);
		System.out.println("Generating sample waveform");
		int datalength = tester.createSampleCoverFile();
		System.out.println("Generating sample data");
		
		byte[] data;
		if (datafile != null)
		{
			FileInputStream datain = new FileInputStream(datafile);
			data = new byte[datain.available()];
			datain.read(data);
			datain.close();
		}
		else
			data = tester.createSampleData(datalength);

		if (codec instanceof NullCoder)
		{
			Map<Class<? extends AstegaEncoder>, Integer> totalread = new HashMap<>();
			Map<Class<? extends AstegaEncoder>, Integer> totalbad = new HashMap<>();

			doTest(tester, new BitCoder8(), new BitCoder8(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder4(), new BitCoder4(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder2(), new BitCoder2(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder1(), new BitCoder1(), data, noiserate, totalread, totalbad);
			doTest(tester, new ParityCoder(), new ParityCoder(), data, noiserate, totalread, totalbad);
			doTest(tester, new PhaseCoder(), new PhaseCoder(input, datalength / 8), data, noiserate, totalread, totalbad);

			for (Entry<Class<? extends AstegaEncoder>, Integer> entry : totalread.entrySet())
			{
				int read = entry.getValue();
				int bad = totalbad.get(entry.getKey());
				String classname = entry.getKey().getSimpleName();

				int good = read - bad;
				int goodpercentage = (int) ((double) good / (double) read * 100.0);

				System.out.println(classname + ": " + good + "/" + read + " good (" + goodpercentage + "%)");
			}
		}
		else
		{
			doTest(tester, codec, codec, data, noiserate, null, null);
		}
	}

	private static void checkArgs(String[] args)
	{
		if (args.length > 0)
		{
			action = args[0].toLowerCase();

			int index = 1;
			String arg;
			String larg;
			String codecName = "";
			while (index < args.length)
			{
				arg = args[index++];
				larg = arg.toLowerCase();

				// These don't need any extra parameters
				switch (larg)
				{
					case "bit8":
						codecName = larg;
						break;
					case "bit4":
						codecName = larg;
						break;
					case "bit2":
						codecName = larg;
						break;
					case "bit1":
						codecName = larg;
						break;
					case "parity":
						codecName = larg;
						break;
					case "phase":
						codecName = larg;
						break;
					case "spread":
						codecName = larg;
						break;
					case "-f":
						doOverwrite = true;
						break;
					default:
						// These need 1 extra parameter
						if (args.length > index)
						{
							switch (larg)
							{
								case "-in":
									inputFile = new File(args[index++]);
									break;
								case "-out":
									outputFile = new File(args[index++]);
									break;
								case "-data":
									dataFile = new File(args[index++]);
									break;
								case "-extra":
									extraFile = new File(args[index++]);
									break;
								case "-noise":
									try
									{
										String amountStr = args[index++];
										amountOfNoise = (Double.parseDouble(amountStr) / 100.0);
									}
									catch (NumberFormatException e)
									{
										System.err.println("Malformed number for -noise");
										System.exit(1);
									}
									break;
								case "-size":
									try
									{
										String amountStr = args[index++];
										dataSize = Integer.parseInt(amountStr);
									}
									catch (NumberFormatException e)
									{
										System.err.println("Malformed number for -size");
										System.exit(1);
									}
									break;
								default:
									System.err.println("Unknown parameter " + arg);
									System.exit(1);
									break;
							}
						}
						else
						{
							System.err.println("Missing parameter for " + arg);
							System.exit(1);
						}
				}
			}
			
			// Load codecs
			switch (codecName)
			{
				case "bit8":
					codec = new BitCoder8();
					break;
				case "bit4":
					codec = new BitCoder4();
					break;
				case "bit2":
					codec = new BitCoder2();
					break;
				case "bit1":
					codec = new BitCoder1();
					break;
				case "parity":
					codec = new ParityCoder();
					break;
				case "phase":
					if (hasExtraFileExist())
					{
						needsDataSize();
						try {
							codec = new PhaseCoder(extraFile, dataSize);
						} catch (IOException e) {
							System.err.println("Failed to load extra file for phase coder");
							e.getMessage();
							System.exit(1);
						}
					}
					else
					{
						System.err.println("No extra file given to phase coder");
						System.err.println("Decoding will not be available");
						codec = new PhaseCoder();
					}
					break;
				case "spread":
					codec = new SpreadSpectrum();
					break;
			}
		}
		else
		{
			printUsage();
			System.exit(1);
		}
	}

	public static void printUsage()
	{
		System.out.println("Usage: astega action [codec] [options]");

		System.out.println("Actions:");
		System.out.println(" encode     Encode data in a input and write to output");
		System.out.println(" decode     Decode data from input and write to output");
		System.out.println(" info       Get information from input");
		System.out.println(" sine       Write a sine wave to output");
		System.out.println(" test       Test an encoding, or all encodings if null is used (supports -noise)");
		System.out.println(" img2ewav   Convert an input image file to an encoded output audio file");
		System.out.println(" ewav2img   Convert an encoded input audio file back to an output image");
		System.out.println(" wav2eimg   Convert an input audio file to an encoded output image");
		System.out.println(" eimg2wav   Convert an encoded input image file to an output audio file");

		System.out.println("\nOptions:");
		System.out.println(" -in <file>         Set the input file");
		System.out.println(" -out <file>        Set the output file");
		System.out.println(" -data <file>       Set the data file");
		System.out.println(" -noise <amount>    Set amount of noise for action test");
		System.out.println(" -size <amount>     (phase) How many bytes are saved in the input file");
		System.out.println(" -extra <file>      (phase) Original unencoded wave file");
		System.out.println(" -f                 Overwrites existing files");
		
		System.out.println("\nAvailable codecs:");
		System.out.println(" bit8: Saves data in lowest significant bits");
		System.out.println(" bit4: Saves data in lowest significant bits");
		System.out.println(" bit2: Saves data in lowest significant bits");
		System.out.println(" bit1: Saves data in lowest significant bits");
		System.out.println(" parity: Saves data using parity coding");
		System.out.println(" phase: Saves data using phase coding");
		System.out.println(" spread: Saves data using spread spectrum coding");
		System.out.println(" null: Dummy codec");
	}
	
	private static void needsDataSize()
	{
		if (dataSize <= Double.MIN_VALUE)
		{
			System.err.println("Need -size parameter");
			System.exit(5);
		}
	}
	
	private static void needsInputFile()
	{
		if (inputFile == null)
		{
			System.err.println("No input file given");
			System.exit(2);
		}
	}
	
	private static void needsInputFileExist()
	{
		if (inputFile == null || !inputFile.exists())
		{
			System.err.println("Input file does not exist");
			System.exit(2);
		}
	}
	
	private static boolean hasExtraFileExist()
	{
		return (extraFile != null && extraFile.exists());
	}
	
	private static boolean hasDataFileExist()
	{
		return (dataFile != null && dataFile.exists());
	}
	
	private static void needsDataFileExist()
	{
		if (!hasDataFileExist())
		{
			System.err.println("Data file does not exist");
			System.exit(2);
		}
	}
	
	private static void needsOutputFile()
	{
		if (outputFile == null)
		{
			System.err.println("No output file given");
			System.exit(2);
		}
		else if (outputFile.exists() && !doOverwrite)
		{
			System.err.println("Output file already exists (use -f to overwrite)");
			System.exit(3);
		}
	}
	
	private static void needsCodec()
	{
		if (codec == null)
		{
			System.err.println("No codec selected");
			System.exit(4);
		}
	}

	public static void main(String[] arg)
	{
		try
		{
			checkArgs(arg);
			AstegaApp app = new AstegaApp();

			switch (action)
			{
				case "encode":
					needsInputFileExist();
					needsDataFileExist();
					needsOutputFile();
					needsCodec();
					
					FileInputStream dataStream = new FileInputStream(dataFile);
					app.write(codec, inputFile, outputFile, dataStream);
					dataStream.close();
					break;
				case "decode":
					needsInputFileExist();
					needsOutputFile();
					needsCodec();
					
					FileOutputStream outputStream = new FileOutputStream(outputFile);
					app.read(codec, inputFile, outputStream);
					outputStream.close();
					break;
				case "info":
					needsInputFileExist();
					app.printInfo(codec, inputFile);
					break;
				case "sine":
					needsOutputFile();
					app.createSine(outputFile);
					break;
				case "test":
					needsInputFile();
					needsOutputFile();
					needsCodec();
					app.test(codec, inputFile, outputFile, dataFile, amountOfNoise);
					break;
				case "img2ewav":
					break;
				case "ewav2img":
					break;
				case "wav2eimg":
					break;
				case "eimg2wav":
					break;
				default:
					System.err.println("Unknown action " + action);
					System.exit(1);
					break;
			}
			System.exit(0);
		}
		catch (IOException e)
		{
			System.err.println("IO Exception: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
}
