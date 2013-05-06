package star.orf.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.Arrays;

import javax.swing.JPanel;

import star.orf.app.Frames.AminoAcids;
import star.orf.app.Frames.Bases;

public class Input extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	private TextArea text;
	private int minLength = 100;

	@Override
	public void addNotify()
	{
		super.addNotify();
		setLayout(new BorderLayout());

		final String str = "Enter DNA sequence here.";
		text = new TextArea(str, 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
		text.setMaximumSize(new Dimension(5120, 80));
		text.setMinimumSize(new Dimension(200, 80));
		text.setSelectionStart(0);
		text.setSelectionEnd(text.getText().length());
		text.addTextListener(new TextListener()
		{
			
			boolean skip = true ;
			public void textValueChanged(TextEvent e)
			{
				String str = text.getText() ;
				if( !( skip && str.length() == 0 ) )
				{
					parse( str ) ;
				}
				skip = false ;
			}
		});
		text.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mousePressed(MouseEvent e)
			{
				if (str.equals(text.getText()))
				{
					text.setText("");
				}
			}
		});
		add(text);
		Main.addListener(this);
		parse( "" ) ;
	}

	public void event(Object evt)
	{
		if (evt instanceof ReverseComplement)
		{
			text.setText(reverseComplement(text.getText()));
		}
		if (evt instanceof Statistics)
		{
			minLength = ((Statistics) evt).getMinValue();
			parse(text.getText());
		}
		if (evt instanceof StringBuilder)
		{
			text.setText(evt.toString());
		}
	}

	void parse(String str)
	{
		Model model = new Model();
		model.forwardBases = parseBases(str);
		model.forwardAA = decode(model.forwardBases);
		model.forwardDecodes = match(model.forwardAA, minLength);
		String rstr = reverseComplement(str);
		model.reverseBases = parseBases(rstr);
		model.reverseAA = decode(model.reverseBases);
		model.reverseDecodes = match(model.reverseAA, minLength);
		Main.dispatch(model);

	}

	String reverseComplement(String str)
	{
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = str.length(); i != 0;)
		{
			i--;
			sb.append(complement(str.charAt(i)));
		}
		return sb.toString();
	}

	char complement(char C)
	{
		Bases b = Bases.parse(C);
		if (b == Bases.A)
			return 'u';
		if (b == Bases.U)
			return 'a';
		if (b == Bases.C)
			return 'g';
		if (b == Bases.G)
			return 'c';
		return ' ';
	}

	Bases[] parseBases(String str)
	{
		try
		{
			int length = str.length();
			Bases[] chars = new Bases[length];
			int index = 0;
			for (int i = 0; i < str.length(); i++)
			{
				Bases b = Bases.parse(str.charAt(i));
				if (b != null)
				{
					chars[index++] = b;
				}
			}
			Bases[] evt = new Bases[index];
			System.arraycopy(chars, 0, evt, 0, index);
			return evt;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	private AminoAcids[] decode(Bases[] chars)
	{
		AminoAcids[] aa;
		if (chars.length >= 3)
		{
			aa = new AminoAcids[chars.length];
			for (int i = 0; i < chars.length - 2; i++)
			{
				aa[i] = AminoAcids.decode(chars, i);
			}
		}
		else
		{
			aa = new AminoAcids[0];
		}
		return aa;
	}

	private boolean[] match(AminoAcids[] aa, int minLength)
	{
		boolean[] decodes = new boolean[aa.length];
		int[] lastStop = new int[3];
		boolean foundStop[] = new boolean[3] ;
		Arrays.fill(decodes, false);
		Arrays.fill(foundStop,false);
		Arrays.fill(lastStop, Integer.MAX_VALUE);
		for (int offset = 0; offset < 3; offset++)
		{
			for (int position = 0; position < aa.length; position += 3)
			{
				int index = position + offset;
				if (index >= aa.length)
				{
					continue;
				}
				if (aa[index] == AminoAcids.STOP)
				{
					if (index - lastStop[offset] > 3 * minLength)
					{
						for (int p = lastStop[offset] + 3; p < index; p += 3)
						{
							decodes[p] = true;
						}
					}
					lastStop[offset] = index;
					foundStop[offset] = true ;
				}
				else if (!foundStop[offset] && (aa[index] == AminoAcids.START || aa[index] == AminoAcids.Met))
				{
					lastStop[offset] = Math.max(index-3,0);
					foundStop[offset] = true ;
				}
			}
		}
		return decodes;
	}
}
