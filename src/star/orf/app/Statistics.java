package star.orf.app;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.swingworker.SwingWorker;

import utils.UIHelpers;

public class Statistics extends JPanel
{
	private static final long serialVersionUID = 1L;

	public Statistics(int minValue)
	{
		this.minValue = minValue;
	}

	JButton rc = new JButton("Reverse complement");
	JButton field = new JButton();
	JLabel length = new JLabel();
	int minValue;

	@Override
	public void addNotify()
	{
		super.addNotify();
		// setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setLayout(new GridLayout(1, 5));
		add(new Length());
		add(new GCContent());
		add(length);
		JPanel p2 = new JPanel();
		p2.add(field);
		p2.add(rc);
		add(p2);
//		JPanel p1 = new JPanel();
//		
//		add(p1);
		rc.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				Main.dispatch(new ReverseComplement());
				UIHelpers.track("ReverseComplement");
			}
		});
		field.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object ret = JOptionPane.showInputDialog(field, "Please enter new minimum distance between stop codons to call ORF", minValue);
				if (ret != null)
				{
					updateMinValue(Integer.parseInt(ret.toString()));
				}
			}
		});
		updateMinValue(minValue);
	}

	void updateMinValue(int value)
	{
		field.setText(MessageFormat.format("Change ORF length", value));
		length.setText(MessageFormat.format("<html><center>Current minimal <br>ORF length is {0}bp</center></html>", value));
		minValue = value;
		Main.dispatch(this);
		UIHelpers.track( "SetORFLength/" + value );
	}

	public int getMinValue()
	{
		return minValue;
	}
}
