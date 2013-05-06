package star.orf.app;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import star.orf.app.Frames.Bases;

public class Output extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	private TextArea text;
	private JLabel length;
	private JButton copy;
	private JToggleButton shortRepresentation;

	String longAA;
	String shortAA;

	@Override
	public void addNotify()
	{
		super.addNotify();
		setBorder(BorderFactory.createTitledBorder("Putative ORF protein sequence"));
		setLayout(new BorderLayout());
		text = new TextArea("", 80, 2, TextArea.SCROLLBARS_VERTICAL_ONLY);
		text.setRows(2);
		text.setEditable(false);
		add(text);
		Panel panel = new Panel();
		panel.setLayout(new BorderLayout());
		length = new JLabel("<html>&nbsp;</html>");
		copy = new JButton("Copy to clipboard");
		shortRepresentation = new JToggleButton("3 letter code");
		panel.add(BorderLayout.WEST, length);
		Panel panel2 = new Panel();
		//panel2.setLayout(new BorderLayout());
		panel.add(BorderLayout.EAST, panel2);
		panel2.add(copy);
		panel2.add(shortRepresentation);
		add(BorderLayout.SOUTH, panel);
		Main.addListener(this);
		text.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				text.setSelectionStart(0);
				text.setSelectionEnd(text.getText().length());
			}
		});
		copy.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					getToolkit().getSystemClipboard().setContents(new StringSelection(text.getText()), new ClipboardOwner()
					{

						public void lostOwnership(Clipboard clipboard, Transferable contents)
						{

						}
					});
				}
				catch (Throwable t)
				{
					JOptionPane.showMessageDialog(text, "Can not copy to clipboard.\n" + t.getLocalizedMessage());
				}
			}
		});
		shortRepresentation.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				updateText();
				shortRepresentation.setText(shortRepresentation.isSelected() ? "1 letter code" : "3 letter code");
			}
		});
		// this.setMaximumSize(new Dimension(5120, 50));
	}

	public void event(Object evt)
	{
		if (evt instanceof DecodedRange)
		{
			DecodedRange r = (DecodedRange) evt;
			int from = r.r.from;
			int to = r.r.to;
			StringBuilder longName = new StringBuilder();
			StringBuilder shortName = new StringBuilder();

			for (int index = from; index < to; index += 3)
			{
				if (r.aa[index] != null)
				{
					longName.append(r.aa[index]);
					shortName.append(r.aa[index].getShortName());
				}
				else
				{
					longName.append("...");
					shortName.append("...");
				}
				longName.append(' ');
			}
			this.longAA = longName.toString();
			this.shortAA = shortName.toString();
			updateText();
			length.setText(MessageFormat.format("Putative protein is {0} amino acids long.", (to - from) / 3));
			invalidate();
			validate();
			repaint();
		}
		else if (evt instanceof Bases[])
		{
			shortAA = "";
			longAA = "";
			text.setText("");
			text.setSelectionStart(0);
			text.setSelectionEnd(text.getText().length());
			length.setText("Click on the green band above to extract decoded sequence.");
			invalidate();
			validate();
			repaint();

		}
	}

	void updateText()
	{
		text.setText(shortRepresentation.isSelected() ? shortAA : longAA);
		text.setSelectionStart(0);
		text.setSelectionEnd(text.getText().length());
	}
}
