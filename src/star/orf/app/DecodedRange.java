package star.orf.app;

import star.orf.app.Frames.AminoAcids;

public class DecodedRange
{
	Range r;
	AminoAcids[] aa;

	public DecodedRange(Range r, AminoAcids[] aa)
	{
		this.r = r;
		this.aa = aa;
	}
}
