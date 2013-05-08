package star.orf.app;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class FrameView extends JPanel
{
	private static final long serialVersionUID = 1L;

	@Override
	public void addNotify()
	{
		super.addNotify();
		setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(new Frames());
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		setMinimumSize(new Dimension(400, 16 * 12));
		add(pane);
	}
}
