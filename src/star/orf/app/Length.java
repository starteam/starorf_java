package star.orf.app;

import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Length extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	float length = 0;
	JLabel label;

	@Override
	public void addNotify()
	{
		super.addNotify();
		label = new JLabel();
		updateLabel();
		add(label);
		Main.addListener(this);
	}

	public void event(Object evt)
	{
		if (evt instanceof Model)
		{
			length = ((Model) evt).forwardBases.length;
			updateLabel();
		}
	}

	private void updateLabel()
	{
		label.setText(MessageFormat.format("<html><center>Sequence length is<br> {0} bp</html>", length));
	}
}
