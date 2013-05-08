package star.orf.app;

public class Range
{
	Range(int from, int to)
	{
		this.from = from;
		this.to = to;
	}

	int from;
	int to;

	boolean contains(int i)
	{
		return i >= from && i <= to;
	}

	@Override
	public String toString()
	{
		return from + " - " + to;
	}
}
