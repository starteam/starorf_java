package star.orf.app;

import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import star.orf.app.Frames.Bases;

public class GCContent extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	private float content = 0;
	private JLabel label;

	@Override
	public void addNotify()
	{
		super.addNotify();
		label = new JLabel();
		updateLabel();
		add(label);
		Main.addListener(this);
	}

	private void updateLabel()
	{
		label.setText(MessageFormat.format("<html><center>Percentage of GC <br>is {0,number,##} %</center></html>", content));

	}

	public void event(Object evt)
	{
		if (evt instanceof Model)
		{
			Bases[] array = ((Model) evt).forwardBases;

			int gc = 0;
			int len = array.length;
			for (Bases b : array)
			{
				if (b == Bases.G || b == Bases.C)
				{
					gc++;
				}
			}
			content = 100.0f * gc / len;
			updateLabel();
		}
	}
}
