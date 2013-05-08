package star.orf.app;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;
import utils.UIHelpers;

public class QuitDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    private String url = "http://www.surveymonkey.com/s/ZXFQ6ZC";
    private int ret = JOptionPane.CANCEL_OPTION ;
	
	public QuitDialog(Frame parent, String title)
	{
		super(parent, title, true);
		final QuitDialog self = this ;
		final JButton quit = new JButton("Quit");
		quit.setMnemonic('Q');
		quit.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				self.ret = JOptionPane.YES_OPTION ;
				self.setVisible(false);
				self.dispose();
			}
		});
		final JButton cancel = new JButton("Cancel");
		cancel.setMnemonic('C');
		cancel.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				self.ret = JOptionPane.CANCEL_OPTION ;
				self.setVisible(false);
				self.dispose();
			}
		});
		final JLabel thankyou = new JLabel("Thank you for your participation! A new web browser window will open.");
		thankyou.setVisible(false);
		final JButton feedback = new JButton("Take Survey");
		feedback.setMnemonic('F');
		feedback.addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				thankyou.setVisible(true);
				feedback.setVisible(false);
				UIHelpers.openWebBrowser(url);
			}
		});


		setLayout(new MigLayout("", "[][]"));
		add(new JLabel("<html><font size=+1>Thank you for using StarORF</font></html>"), "wrap,span 2, center");
		JLabel textArea = new JLabel("<html><center>This survey will take less than 2 minutes to complete.<br> Your input regarding this software will greatly improve its usability in the classroom.</center></html>");
		add(textArea, "span 2, growy 3, wrap ");
		add(feedback, "wrap, span 2, center");
		add(thankyou, "wrap, span 2, center");
		add( new JLabel(" "), "wrap");
		add(new JLabel(title), "wrap, span 2, center");
		add(quit, "center, sg sg1, tag ok");
		add(cancel, "center, sg sg1, tag cancel");
		UIHelpers.centerOnParent(this);
		pack();
		setVisible(true);

	}
	
	public int get()
	{
		return ret ;
	}

}
