package be.seeseemelk.astega;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import be.seeseemelk.astega.stream.AstegaInputStream;
import be.seeseemelk.astega.stream.AstegaOutputStream;

public class AstegaApp
{
	private static String action;
	private static File dataFile;
	private static File inputFile;
	private static File outputFile;
	private static AstegaCodec codec;
	private static double amountOfNoise;
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

	private void doTest(Tester tester, AstegaCodec codec, byte[] data, double noiserate,
			Map<Class<? extends AstegaCodec>, Integer> read, Map<Class<? extends AstegaCodec>, Integer> bad)
			throws IOException
	{
		System.out.println("Performing test with " + codec.getClass().getSimpleName());
		tester.setCodecs(codec, codec);
		tester.test(data, noiserate);

		if (read != null)
		{
			read.put(codec.getClass(), tester.getAmountReadInTest());
			bad.put(codec.getClass(), tester.getAmountBadReadInTest());
		}
	}

	public void test(AstegaCodec codec, File input, File output, double noiserate) throws IOException
	{
		Tester tester = new Tester(input, output);
		System.out.println("Generating sample waveform");
		int datalength = tester.createSampleCoverFile();
		System.out.println("Generating sample data");
		byte[] data = tester.createSampleData(datalength);

		if (codec instanceof NullCoder)
		{
			Map<Class<? extends AstegaCodec>, Integer> totalread = new HashMap<>();
			Map<Class<? extends AstegaCodec>, Integer> totalbad = new HashMap<>();

			doTest(tester, new BitCoder8(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder4(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder2(), data, noiserate, totalread, totalbad);
			doTest(tester, new BitCoder1(), data, noiserate, totalread, totalbad);
			doTest(tester, new ParityCoder(), data, noiserate, totalread, totalbad);
			doTest(tester, new PhaseCoder(), data, noiserate, totalread, totalbad);

			for (Entry<Class<? extends AstegaCodec>, Integer> entry : totalread.entrySet())
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
			doTest(tester, codec, data, noiserate, null, null);
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
			while (index < args.length)
			{
				arg = args[index++];
				larg = arg.toLowerCase();

				// These don't need any extra parameters
				switch (larg)
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
						codec = new PhaseCoder();
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
								case "-amount":
									try
									{
										String amountStr = args[index++];
										amountOfNoise = Double.parseDouble(amountStr);
									}
									catch (NumberFormatException e)
									{
										System.err.println("Malformed number for -noise");
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
		System.out.println(" test       Test an encoding, or all encodings if null is used");
		System.out.println(" noisetest  Read file from input, add -noise amount of noise, write to output");
		System.out.println(" img2ewav   Convert an input image file to an encoded output audio file");
		System.out.println(" ewav2img   Convert an encoded input audio file back to an output image");
		System.out.println(" wav2eimg   Convert an input audio file to an encoded output image");
		System.out.println(" eimg2wav   Convert an encoded input image file to an output audio file");

		System.out.println("\nOptions:");
		System.out.println(" -in <input>        Set the input file");
		System.out.println(" -out <output>      Set the output file");
		System.out.println(" -data <data>       Set the data file");
		System.out.println(" -noise <amount>    Set amount of noise for noisetest");
		System.out.println(" -f                 Overwrites existing files");

		System.out.println("\nAvailable codecs:");
		System.out.println(" bit8: Saves data in lowest significant bits");
		System.out.println(" bit4: Saves data in lowest significant bits");
		System.out.println(" bit2: Saves data in lowest significant bits");
		System.out.println(" bit1: Saves data in lowest significant bits");
		System.out.println(" parity: Saves data using parity coding");
		System.out.println(" phase: Saves data using phase coding");
		System.out.println(" null: Doesn't do anything");

		/*
		 * System.out.println("Usage: astega <codec> <action>\n");
		 * System.out.println("Actions:"); System.out.println(
		 * "encode <data> <cover> <output>     Encode data in a cover file");
		 * System.out.println(
		 * "decode <cover> <output>            Decode data from a file");
		 * System.out.println(
		 * "info <cover>                       Get information from a file");
		 * System.out.println(
		 * "sine <output>                      Create a sine wave");
		 * System.out.println(
		 * "test <input> <output>              Test an encoding, or all encodings if null is used"
		 * ); System.out.println(
		 * "noisetest <input> <output> <rate>  Test an encoding with noise added"
		 * ); System.out.println(
		 * "img2ewav <input> <output>          Convert an image file to an encoded audio file"
		 * ); System.out.println(
		 * "ewav2img <input> <output> [width height] Convert an encoded audio file back to an image"
		 * ); System.out.println(
		 * "wav2eimg <input> <output>          Convert an audio file to an encoded image"
		 * ); System.out.println(
		 * "eimg2wav <input> <output>          Convert an encoded image file to an audio file"
		 * ); System.out.println("\nAvailable codecs:"); System.out.println(
		 * "bit8: Saves data in lowest significant bits"); System.out.println(
		 * "bit4: Saves data in lowest significant bits"); System.out.println(
		 * "bit2: Saves data in lowest significant bits"); System.out.println(
		 * "bit1: Sa7ves data in lowest significant bits"); System.out.println(
		 * "parity: Saves data using parity coding"); System.out.println(
		 * "phase: Saves data using phase coding"); System.out.println(
		 * "null: Truncates samples and stores a byte in each sample");
		 */
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
					if (inputFile != null && inputFile.exists())
					{
						if (dataFile != null && dataFile.exists())
						{
							if (outputFile != null && (!outputFile.exists() || doOverwrite))
							{
								if (codec != null)
								{
									FileInputStream dataStream = new FileInputStream(dataFile);
									app.write(codec, inputFile, outputFile, dataStream);
									dataStream.close();
								}
								else
								{
									System.err.println("No codec selected");
									System.exit(4);
								}
							}
							else
							{
								System.err.println("Output file already exists (use -f to overwrite)");
								System.exit(3);
							}
						}
						else
						{
							System.err.println("Data file does not exist");
							System.exit(2);
						}
					}
					else
					{
						System.err.println("Input file does not exist");
						System.exit(2);
					}
					break;
				case "decode":
					if (inputFile != null && inputFile.exists())
					{
						if (outputFile != null && (!outputFile.exists() || doOverwrite))
						{
							if (codec != null)
							{
								FileOutputStream outputStream = new FileOutputStream(outputFile);
								app.read(codec, inputFile, outputStream);
								outputStream.close();
							}
							else
							{
								System.err.println("No codec selected");
								System.exit(4);
							}
						}
						else
						{
							System.err.println("Output file already exists (use -f to overwrite)");
							System.exit(3);
						}
					}
					else
					{
						System.err.println("Input file does not exist");
						System.exit(2);
					}
					break;
				case "info":
					break;
				case "sine":
					break;
				case "test":
					break;
				case "noisetest":
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

			/*
			 * if (arg.length > 1) { String codecName = arg[0]; String action =
			 * arg[1];
			 * 
			 * AstegaCodec codec;
			 * 
			 * // Get the right codec switch (codecName) { case "bit8": codec =
			 * new BitCoder8(); break; case "bit4": codec = new BitCoder4();
			 * break; case "bit2": codec = new BitCoder2(); break; case "bit1":
			 * codec = new BitCoder1(); break; case "parity": codec = new
			 * ParityCoder(); break; case "phase": codec = new PhaseCoder();
			 * break; case "null": codec = new NullCoder(); break; default:
			 * printUsage(); return; }
			 * 
			 * AstegaApp app = new AstegaApp();
			 * 
			 * // Perform the action switch (action.toLowerCase()) { case
			 * "encode": if (arg.length > 4) {
			 * 
			 * } else printUsage(); break; case "decode": if (arg.length > 3) {
			 * File input = new File(arg[2]); File output = new File(arg[3]);
			 * FileOutputStream outputStream = new FileOutputStream(output);
			 * app.read(codec, input, outputStream); outputStream.close(); }
			 * else printUsage(); break; case "info": if (arg.length > 2) { File
			 * input = new File(arg[2]); app.size(codec, input); } else
			 * printUsage(); break; case "sine": if (arg.length > 2) { File
			 * output = new File(arg[2]); app.createSine(output); } else
			 * printUsage(); break; case "test": if (arg.length > 3) { File
			 * input = new File(arg[2]); File output = new File(arg[3]);
			 * app.test(codec, input, output, 0); } else printUsage(); break;
			 * case "noisetest": if (arg.length > 4) { File input = new
			 * File(arg[2]); File output = new File(arg[3]); double noiserate =
			 * Double.parseDouble(arg[4]) / 100.0; app.test(codec, input,
			 * output, noiserate); } else printUsage(); break; case "img2ewav":
			 * if (arg.length > 3) { File input = new File(arg[2]); File output
			 * = new File(arg[3]); Convertor.convertImageToEncodedAudio(input,
			 * output, true); } else printUsage(); break; case "ewav2img": if
			 * (arg.length > 5) { File input = new File(arg[2]); File output =
			 * new File(arg[3]); int width = Integer.parseInt(arg[4]); int
			 * height = Integer.parseInt(arg[5]);
			 * Convertor.convertEncodedAudioToImage(input, output, width,
			 * height, true); } else if (arg.length > 3) { File input = new
			 * File(arg[2]); File output = new File(arg[3]);
			 * Convertor.convertEncodedAudioToImage(input, output, true); } else
			 * printUsage(); break; case "wav2eimg": if (arg.length > 3) { File
			 * input = new File(arg[2]); File output = new File(arg[3]);
			 * Convertor.convertAudioToEncodedImage(input, output); } else
			 * printUsage(); break; case "eimg2wav": if (arg.length > 3) { File
			 * input = new File(arg[2]); File output = new File(arg[3]);
			 * Convertor.convertEncodedImageToAudio(input, output); } else
			 * printUsage(); break; default: printUsage(); } } else {
			 * printUsage(); }
			 * 
			 * /*System.out.println("Starting...");
			 * 
			 * File inputFile = new File("cirice.wav"); File outputFile = new
			 * File("cirice_coded.wav"); File dataFile = new File("data.bin");
			 * 
			 * BufferedInputStream dataInput = new BufferedInputStream(new
			 * FileInputStream(dataFile)); AstegaApp app = new AstegaApp();
			 * AstegaEncoder encoder = new BitCoder(); app.write(encoder,
			 * inputFile, outputFile, dataInput);
			 */
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
