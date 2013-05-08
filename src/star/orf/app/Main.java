package star.orf.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import star.orf.app.Frames.AminoAcids;
import star.version.VersionChecker;
import utils.Icons;
import utils.OS;
import utils.UIHelpers;

public class Main extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	private boolean showLogo = true;
	private int minDistance = 80;
	private boolean linkAA = true;

	private static Set<EventListener> listeners = new HashSet<EventListener>();

	static void dispatch(final Object event)
	{
		synchronized (listeners)
		{
			for (EventListener e : listeners)
			{
				e.event(event);
			}
		}
	}
	
	static void addListener(EventListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}

	}

	static Timer glassPaneTimer;

	static int waitCounter = 0;

	public static void repaintWindow(Component c)
	{
		while (c.getParent() != null && c != c.getParent())
		{
			c = c.getParent();
		}
		c.repaint();
	}

	public void event(Object evt)
	{
		if (evt instanceof AminoAcids && linkAA)
		{
			try
			{
				String longName = ((AminoAcids) evt).getLongName().replace(' ', '_');
				System.out.println( "EVENT -- " + longName );
//				getAppletContext().showDocument(new URL("http://web.mit.edu/star/orf/linkAA.html?AA=" + longName), "_blank");
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}

	private void setSequence(final java.net.URL url)
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Main.dispatch(new StringBuilder("Loading sequence from URL"));
					BufferedReader r = new BufferedReader(new InputStreamReader(new BufferedInputStream(url.openStream(), 64 * 1024)));
					StringBuilder b = new StringBuilder();
					while (true)
					{
						String line = r.readLine();
						if (line == null)
						{
							break;
						}
						b.append(line);
					}
					Main.dispatch(b);
				}
				catch (Throwable t)
				{
					t.printStackTrace();
					Main.dispatch(new StringBuilder(t.toString()));
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void addNotify()
	{
		UIHelpers.setIcon("/resources/StarORF.png",frame);
		super.addNotify();
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.PAGE_AXIS));
		control.add(new Input());
		control.add(new Statistics(minDistance));
		control.setBorder(BorderFactory.createTitledBorder("Input"));
		getContentPane().add(control);
		JPanel view = new JPanel();
		view.setLayout(new BoxLayout(view, BoxLayout.PAGE_AXIS));
		view.add(new WholeSequenceView());
		view.add(new FrameView());
		view.setBorder(BorderFactory.createTitledBorder("Six frame translation"));
		getContentPane().add(view);
		getContentPane().add(new Output());
		Main.addListener(this);
		open();
	}
	
	JPanel getContentPane()
	{
		return this ;
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if (showLogo)
		{
			Rectangle r = getBounds();
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			String str = "Powered by StarORF";
			int sw = g.getFontMetrics().stringWidth(str);
			int offset = g.getFontMetrics().getHeight();
			int h = offset + 3;
			int w = sw + 30;
			int x0 = r.x + r.width - w;
			g.setColor(UIManager.getColor("Button.background"));
			g.fillRect(r.x + r.width - w, r.y, w, h);
			g.setColor(UIManager.getColor("Button.foreground"));
			g.drawString("Powered by StarORF", x0 + (w - sw) / 2, offset);
			g.drawLine(x0 + (w - sw) / 2, offset + 1, x0 + (w + sw) / 2, offset + 1);
		}
	}

	ImageIcon currentIcon = null;

	void close()
	{
		try
		{
			QuitDialog d = new QuitDialog(frame, "Do you want to quit?");
			System.out.println("Quit dialog done" + d.get());
			int result = d.get();
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			if (result == JOptionPane.YES_OPTION)
			{
				try
				{
					ImageIcon icon = new ImageIcon(new java.net.URL("http://starapp.mit.edu/star/orf/icons/Shutdown.gif"));
					currentIcon = icon;
					frame.dispose();
					System.exit(0);
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(0);
		}
	}

	void open()
	{
		Runnable r = new Runnable()
        {
	        public void run()
	        {
	    		try
	    		{
	    			ImageIcon icon = new ImageIcon(new java.net.URL("http://starapp.mit.edu/star/orf/icons/Startup.gif"));
	    			currentIcon = icon;
	    		}
	    		catch (Throwable e)
	    		{
	    			// e.printStackTrace();
	    		}
		        
	        }
        };
        new Thread(r).start();

	}

	public javax.swing.JMenuBar getMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem search = new JMenuItem("Search");
		Action quitAction = new AbstractAction( "Quit" )
		{			
            private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				frame.setVisible(false);
				frame.dispose();
				try
				{
					Thread.sleep( 300 );
				}
				catch( Exception ex )
				{
					ex.printStackTrace();
				}
				System.exit(0);
			}
		};
		Action aboutAction = new AbstractAction("About") {
            private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, new AboutBox(), "About StarORF", JOptionPane.INFORMATION_MESSAGE, Icons.ABOUT.getIcon());
			}
		};
		JMenuItem quit = new JMenuItem(quitAction);
		JMenuItem about = new JMenuItem(aboutAction);
		menuBar.add(file);
		file.add(about);
		file.add(quit);		
		return menuBar;
	};

	JFrame frame;

	private static void initUI()
	{
		//System.setProperty("swing.aatext", "true");
		if (!OS.isMacOSX())
		{
			UIHelpers.tryNimbus();
		}
		try
		{
			if (UIManager.getLookAndFeelDefaults().getColor("background") == null)
			{
				UIManager.getLookAndFeelDefaults().put("background", java.awt.Color.lightGray);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	final static String PROJECT = "StarORF";

	public static void main(String[] args)
	{
		if (!VersionChecker.processVersionArguments(PROJECT, Version.getProject(), Version.getBuildDate(), args))
		{
			UIHelpers.addTracking(PROJECT);
			initUI();
			final JFrame frame = new JFrame(PROJECT);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.getContentPane().setLayout(new BorderLayout());
			final Main main = new Main();
			main.frame = frame;
			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					main.close();
				}

				@Override
				public void windowClosed(WindowEvent e)
				{
					main.close();
				}
			});
			frame.setJMenuBar(main.getMenuBar());
			frame.getContentPane().add(main);
			frame.pack();
			frame.setVisible(true);
			VersionChecker.invokeLater(PROJECT, Version.getProject(), Version.getBuildDate(), frame);
		}
	}
}
