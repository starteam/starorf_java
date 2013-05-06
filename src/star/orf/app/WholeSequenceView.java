package star.orf.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import star.orf.app.Frames.AminoAcids;
import star.orf.app.Frames.Bases;

public class WholeSequenceView extends JPanel implements EventListener
{
	private static final long serialVersionUID = 1L;

	int minORFLength = 0;

	private final int whole_decoding_height = 5;
	private final int whole_decoding_forward_1 = 1;
	private final int whole_decoding_forward_bottom = whole_decoding_forward_1 + (whole_decoding_height + 1) * 3;

	private final int whole_seq_top = whole_decoding_forward_bottom;
	private final int whole_seq_bottom = whole_seq_top + (int) (whole_decoding_height * 1.75f);

	private final int whole_decoding_backward_top = whole_seq_bottom + 3;

	private final int whole_decoding_backward_bottom = whole_decoding_backward_top + (whole_decoding_height + 1) * 3;
	private final int range_top = whole_decoding_backward_bottom + 3;

	final Color bars = Color.green.darker();
	final Color stop = Frames.end;
	final Color start = Frames.start;

	final int skipStartStops = 15;

	private Model model;
	private Range range;
	private int rangeWidth = 1;

	int getBasesLength()
	{
		if (model != null && model.forwardBases != null)
		{
			return model.forwardBases.length;
		}
		else
		{
			return 0;
		}
	}

	@Override
	public void addNotify()
	{
		super.addNotify();
		setPreferredSize(new Dimension(1000, 75));
		setMinimumSize(new Dimension(200, 50));
		Main.addListener(this);

		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Point p = e.getPoint();
				float bpp = 1.0f * getBasesLength() / getWidth();
				Main.dispatch(new Integer((int) ((p.x - getRangeWidth() / 2) * bpp)));
			}

		});

		addMouseMotionListener(new MouseMotionAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Point p = e.getPoint();
				float bpp = 1.0f * getBasesLength() / getWidth();
				Main.dispatch(new Integer((int) ((p.x - getRangeWidth() / 2) * bpp)));
			}
		});
	}

	public void event(Object evt)
	{
		if (evt instanceof Model)
		{
			this.model = (Model) evt;
			this.image = null;
			repaint();
		}
		if (evt instanceof Range)
		{
			this.range = (Range) evt;
			repaint();
		}

	}

	private void paintBases(Graphics g)
	{
		Rectangle r = getBounds();
		if (r.width < getBasesLength())
		{
			float bpp = 1.0f * getBasesLength() / r.width;
			float[] pixel = new float[r.width + 1];
			for (int i = 0; i < getBasesLength(); i++)
			{
				pixel[(int) (i / bpp)] += isGC(model.forwardBases[i]) ? 1 : 0;
			}
			for (int i = 0; i < r.width; i++)
			{
				float v = 1 - Math.min(pixel[i] * 1.0f / bpp, 1);
				g.setColor(new Color(v, v, v));
				g.drawLine(i, whole_seq_top, i, whole_seq_bottom);
			}
		}
		else
		{
			float bpp = 1.0f * getBasesLength() / r.width;
			for (int i = 0; i < getBasesLength(); i++)
			{
				boolean isGC = isGC(model.forwardBases[i]);
				float v = isGC ? 0 : 1;
				g.setColor(new Color(v, v, v));
				g.fillRect((int) (i / bpp), whole_seq_top, (int) (1 / bpp), whole_seq_bottom - whole_seq_top);
			}
		}
	}

	private void paintAAReverse(Graphics g)
	{
		Rectangle r = getBounds();

		Color c1 = new Color(205, 205, 205);
		g.setColor(c1);

		g.fillRect(0, whole_decoding_backward_top + 0 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);
		g.fillRect(0, whole_decoding_backward_top + 1 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);
		g.fillRect(0, whole_decoding_backward_top + 2 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);

		g.setColor(bars);
		float bpp = 1.0f * getBasesLength() / r.width;

		final int bottomReorderFactor = 5 - (model.reverseAA.length % 3);

		for (int offset = 0; offset < 3; offset++)
		{
			int lastStart = 0;
			boolean started = false;
			for (int pos = 0; pos < model.reverseDecodes.length; pos += 3)
			{
				int i = pos + offset;
				int index = model.reverseDecodes.length - i - 1;
				if (index < 0 || index >= model.reverseDecodes.length)
				{
					break;
				}
				if (model.reverseDecodes[index] && !started)
				{
					lastStart = i;
					started = true;
				}
				else if (!model.reverseDecodes[index] && started)
				{
					started = false;
					int xstart = (int) (lastStart / bpp);
					int xend = (int) (i / bpp);
					int width = xend - xstart;
					g.fillRect(xstart, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height + 1, width, whole_decoding_height - 1);
				}
			}
		}

		if (r.width < model.reverseDecodes.length)
		{
			if (getBasesLength() < skipStartStops * r.width)
			{

				for (int i = 0; i < model.reverseDecodes.length; i++)
				{
					int index = model.reverseDecodes.length - i - 1;

					if (model.reverseAA[index] == AminoAcids.STOP)
					{
						int x = (int) (i / bpp);
						g.setColor(stop);
						g.drawLine(x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height + 1, x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height + whole_decoding_height - 1);
					}
					else if (model.reverseAA[index] == AminoAcids.START || model.reverseAA[index] == AminoAcids.Met)
					{
						int x = (int) (i / bpp);
						g.setColor(start);
						g.drawLine(x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height + 1, x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height + whole_decoding_height - 1);
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < model.forwardAA.length; i++)
			{
				int index = model.reverseDecodes.length - i - 1;

				if (model.reverseAA[index] == AminoAcids.STOP)
				{
					int x = (int) (i / bpp);
					g.setColor(stop);
					g.fillRect(x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height, (int) (3 / bpp), 1);
				}
				else if (model.reverseAA[index] == AminoAcids.START || model.reverseAA[index] == AminoAcids.Met)
				{
					int x = (int) (i / bpp);
					g.setColor(start);
					g.fillRect(x, whole_decoding_backward_top + ((i + 1 + bottomReorderFactor) % 3) * whole_decoding_height, (int) (3 / bpp), 1);
				}
			}

		}
	}

	private void paintAAForward(Graphics g)
	{
		Rectangle r = getBounds();

		Color c1 = new Color(205, 205, 205);
		g.setColor(c1);
		g.fillRect(0, whole_decoding_forward_1 + 0 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);
		g.fillRect(0, whole_decoding_forward_1 + 1 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);
		g.fillRect(0, whole_decoding_forward_1 + 2 * whole_decoding_height + 1, r.width, whole_decoding_height - 1);

		g.setColor(bars);
		float bpp = 1.0f * getBasesLength() / r.width;
		for (int offset = 0; offset < 3; offset++)
		{
			int lastStart = 0;
			boolean started = false;
			for (int pos = 0; pos < getBasesLength(); pos += 3)
			{
				int i = pos + offset;
				if (i >= model.forwardDecodes.length)
				{
					break;
				}
				if (model.forwardDecodes[i] && !started)
				{
					lastStart = i;
					started = true;
				}
				else if (!model.forwardDecodes[i] && started)
				{
					started = false;
					int xstart = (int) (lastStart / bpp);
					int xend = (int) (i / bpp);
					int width = xend - xstart;
					g.fillRect(xstart, whole_decoding_forward_1 + (i % 3) * whole_decoding_height + 1, width, whole_decoding_height - 1);
				}
			}
		}

		if (r.width < model.forwardDecodes.length)
		{
			if (getBasesLength() < skipStartStops * r.width)
			{

				for (int i = 0; i < model.forwardAA.length; i++)
				{
					if (model.forwardAA[i] == AminoAcids.STOP)
					{
						int x = (int) (i / bpp);
						g.setColor(stop);
						g.drawLine(x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height + 1, x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height + whole_decoding_height - 1);
					}
					else if (model.forwardAA[i] == AminoAcids.START || model.forwardAA[i] == AminoAcids.Met)
					{
						int x = (int) (i / bpp);
						g.setColor(start);
						g.drawLine(x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height + 1, x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height + whole_decoding_height - 1);
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < model.forwardAA.length; i++)
			{
				if (model.forwardAA[i] == AminoAcids.STOP)
				{
					int x = (int) (i / bpp);
					g.setColor(stop);
					g.fillRect(x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height, (int) (3 / bpp), 1);

				}
				else if (model.forwardAA[i] == AminoAcids.START || model.forwardAA[i] == AminoAcids.Met)
				{
					int x = (int) (i / bpp);
					g.setColor(start);
					g.fillRect(x, whole_decoding_forward_1 + (i % 3) * whole_decoding_height, (int) (3 / bpp), 1);
				}
			}

		}
	}

	private void setRangeWidth(int rangeWidth)
	{
		this.rangeWidth = rangeWidth;
	}

	private int getRangeWidth()
	{
		return rangeWidth;
	}

	private void paintRange(Graphics g)
	{
		Rectangle r = g.getClipBounds();
		if (range != null)
		{
			float bpp = 1.0f * getBasesLength() / r.width;
			if (true)
			{
				Graphics2D g2 = (Graphics2D) (g.create());
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(.1f, .1f, .1f, .3f));
				g2.setStroke(new BasicStroke(2));
				Polygon p = new Polygon();
				int x0 = r.x + (int) (range.from / bpp);
				int x1 = r.x + (int) (range.to / bpp);
				p.addPoint(r.x, r.height);
				p.addPoint(x0, range_top);
				p.addPoint(x1, range_top);
				p.addPoint(r.x + r.width, r.height);
				g2.drawPolygon(p);
				g2.setColor(new Color(.1f, .1f, .1f, .3f));
				int w = (x1 - x0);
				w = w > 1 ? w : 1;
				setRangeWidth(w);
				g2.fillRect(x0, 0, w, range_top);
			}
		}
	}

	BufferedImage image;

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (model != null)
		{
			if (image == null || image.getWidth() != getWidth() || image.getHeight() != getHeight())
			{
				image = null;
			}
			if (image == null)
			{
				image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics ig = image.getGraphics();
				ig.setColor(this.getBackground());
				ig.fillRect(0, 0, getWidth(), getHeight());
				paintBases(ig);
				paintAAForward(ig);
				paintAAReverse(ig);
				ig.dispose();
				image.flush();
			}
			if (image != null)
			{
				g.drawImage(image, 0, 0, Color.white, null);
			}
			else
			{
				paintBases(g);
				paintAAForward(g);
				paintAAReverse(g);

			}
		}
		paintRange(g);
	}

	private boolean isGC(Bases b)
	{
		return b == Bases.G || b == Bases.C;
	}
}
