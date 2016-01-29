package be.seeseemelk.astega.coders;

import be.seeseemelk.astega.AstegaSample;

public interface AstegaEncoder
{
	/**
	 * Get the number of BYTES that can be saved.
	 * @return The number of BYTES that can be saved.
	 */
	public int getSizeLimit();
	
	/**
	 * Set the AstegaSample object that the data should be written to.
	 * @param samples The AstegaSample object to write to.
	 */
	public void setSamples(AstegaSample samples);
	
	/**
	 * Write a byte.
	 * @param b The byte to write.
	 */
	public void write(int b);
	
	/**
	 * Seek to a byte.
	 * @param b The byte to seek to.
	 */
	public void seek(int b);
	
	/**
	 * Get the byte the encoder would write to next.
	 * @return The next byte to write to
	 */
	public int tell();
}
