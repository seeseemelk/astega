package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public interface AstegaDecoder
{
	/**
	 * Set the samples that the decoder should work with.
	 * @param samples The samples that the decoder should use.
	 */
	public void setSamples(AstegaSample samples);

	/**
	 * Seek to the location the decoder should read from next.
	 * @param i The location to seek to.
	 */
	public void seek(int i);

	/**
	 * Get the location the decoder would read next.
	 * @return The decoder's next read location.
	 */
	public int tell();

	/**
	 * Read a byte from the samples.
	 * @return A byte of read data.
	 */
	public byte read();

}
