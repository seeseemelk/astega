package be.seeseemelk.astega.encoders;

import be.seeseemelk.astega.AstegaSample;

public interface AstegaEncoder
{
	public int getSizeLimit();
	public void setSamples(AstegaSample samples);
	public void write(int b);
}
