package star.orf.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;

import utils.UIHelpers;

public class Frames extends JComponent implements EventListener, Scrollable
{
	private static final long serialVersionUID = 1L;
	public final static Color start = new Color(96, 96, 192);
	public final static Color end = Color.red;
	final int offset = 5;

	enum Bases
	{
		A('a', 'A', Color.red), U('u', 'U', Color.green), G('g', 'G', Color.yellow), C('c', 'C', Color.orange);

		private char c1, c2;
		private Color color;
		private String toString;

		Bases(char c1, char c2, Color c)
		{
			this.c1 = c1;
			this.c2 = c2;
			this.color = c;
			this.toString = Character.toString(toChar());

		}

		static Bases parse(char c)
		{
			for (Bases b : Bases.values())
			{
				if (c == b.c1 || c == b.c2)
				{
					return b;
				}
			}
			if (c == 't' || c == 'T')
			{
				return U;
			}
			return null;
		}

		Color getColor()
		{
			return color;
		}

		private char toChar()
		{
			return Character.toUpperCase(c1);
		}

		@Override
		public String toString()
		{
			return toString;
		}
	};

	static class Triplet
	{
		private Bases b1, b2, b3;

		public Triplet(String s)
		{
			this.b1 = Bases.parse(s.charAt(0));
			this.b2 = Bases.parse(s.charAt(1));
			this.b3 = Bases.parse(s.charAt(2));
		}

		boolean match(Bases[] bases, int index)
		{
			return (bases[index] == b1 && bases[index + 1] == b2 && bases[index + 2] == b3);
		}
	}

	enum CodonType
	{
		Nonpolar("ffe75f"), Polar("b3dec0"), Basic("bbbfe0"), Acidic("f8b7d3"), START("00ff00"), STOP("ff0000");
		private Color c;

		CodonType(String str)
		{
			this.c = new Color(Integer.parseInt(str.substring(0, 2), 16), Integer.parseInt(str.substring(2, 4), 16), Integer.parseInt(str.substring(4, 6), 16));
		}

		Color getColor()
		{
			return c;
		}
	}

	enum AminoAcids
	{
		Ala("A", "GCU, GCC, GCA, GCG", CodonType.Nonpolar, "Alanine"), //
		Leu("L", "UUA, UUG, CUU, CUC, CUA, CUG", CodonType.Nonpolar, "Leucine"), //
		Arg("R", "CGU, CGC, CGA, CGG, AGA, AGG", CodonType.Polar, "Arginine"), //
		Lys("K", "AAA, AAG", CodonType.Basic, "Lysine"), //
		Asn("N", "AAU, AAC", CodonType.Polar, "Asparagine"), //
		Met("M", "AUG", CodonType.Nonpolar, "Methionine"), //
		Asp("D", "GAU, GAC", CodonType.Nonpolar, "Aspartic acid"), //
		Phe("F", "UUU, UUC", CodonType.Nonpolar, "Phenylalanine"), //
		Cys("C", "UGU, UGC", CodonType.Polar, "Cysteine"), //
		Pro("P", "CCU, CCC, CCA, CCG", CodonType.Nonpolar, "Proline"), //
		Gln("Q", "CAA, CAG", CodonType.Polar, "Glutamine"), //
		Ser("S", "UCU, UCC, UCA, UCG, AGU, AGC", CodonType.Polar, "Serine"), //
		Glu("E", "GAA, GAG", CodonType.Nonpolar, "Glutamic acid"), //
		Thr("T", "ACU, ACC, ACA, ACG", CodonType.Polar, "Threonine"), //
		Gly("G", "GGU, GGC, GGA, GGG", CodonType.Polar, "Glycine"), //
		Trp("W", "UGG", CodonType.Nonpolar, "Tryptophan"), //
		His("H", "CAU, CAC", CodonType.Basic, "Histidine"), //
		Tyr("Y", "UAU, UAC", CodonType.Polar, "Tyrosine"), //
		Ile("I", "AUU, AUC, AUA", CodonType.Nonpolar, "Isoleucine"), //
		Val("V", "GUU, GUC, GUA, GUG", CodonType.Nonpolar, "Valine"), //
		START("START", "AUG", CodonType.START, "Methionine"), //
		STOP("STOP", "UAG, UGA, UAA", CodonType.STOP, "Stop codon") //
		;

		private ArrayList<Triplet> triples = new ArrayList<Triplet>();
		private String shortName;
		private CodonType codonType;
		private String longName;

		private AminoAcids(String shortName, String decoding, CodonType type, String longName)
		{
			this.shortName = shortName;
			this.longName = longName;
			String[] split = decoding.split(",");
			for (String s : split)
			{
				String d = s.trim();
				triples.add(new Triplet(d));
				codonType = type;
			}
		}

		static AminoAcids decode(Bases[] b, int index)
		{
			for (AminoAcids a : AminoAcids.values())
			{
				for (Triplet t : a.triples)
				{
					if (t.match(b, index))
					{
						return a;
					}
				}
			}
			return null;
		}

		Color getColor()
		{
			return codonType.getColor();
		}

		public String getShortName()
		{
			return shortName;
		}

		public String getLongName()
		{
			return longName;
		}
		
	};

	private final int rows = 12;
	private int rectX = 16;
	private int rectY = 16;
	private Model model;

	@Override
	public void addNotify()
	{
		// TODO Auto-generated method stub
		super.addNotify();
		setMinimumSize(new Dimension(400, 100));
		Main.addListener(this);
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				java.awt.Point p = e.getPoint();
				int row = (p.y / rectY) - 2;
				int bp = (p.x / rectX);
				if (row >= 0)
				{
					int aaIndex = (bp - row) / 3;
					int index = aaIndex * 3 + row;
					if (row < 3)
					{
						if (e.getClickCount() == 2)
						{
							Main.dispatch(model.forwardAA[index]);
							UIHelpers.track("ShowAA/" + model.forwardAA[index].toString() );
						}
						if (model.forwardDecodes[index])
						{
							Range r = findRange(index, model.forwardDecodes);
							Main.dispatch(new DecodedRange(r, model.forwardAA));
							UIHelpers.track("ShowAA/" + model.forwardAA[index] );
						}
					}
					else if (row > 3)
					{
						row = 6 - row;
						int offset = (aaIndex + 2) * 3 - row + 1;
						index = model.reverseAA.length - offset - 1;
						if (e.getClickCount() == 2)
						{
							Main.dispatch(model.reverseAA[index]);
						}
						if (model.reverseDecodes[index])
						{
							Range r = findRange(index, model.reverseDecodes);
							Main.dispatch(new DecodedRange(r, model.reverseAA));
						}
					}

				}
			}
		});
	}

	private Range findRange(int index, boolean[] decodes)
	{
		int from = index;
		while (from >= 0 && decodes[from])
		{
			from -= 3;
		}
		from += 3;
		int to = index;
		while (to < decodes.length && decodes[to])
		{
			to += 3;
		}
		return new Range(from, to);
	}

	private final Color basesBackground = Color.white.darker();

	private void paintBasesForward(Graphics g)
	{
		final int row = 1;
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);
		final int h = g.getFontMetrics().getDescent();
		final Bases[] forwardBases = model.forwardBases;
		final int ww = rectX - 1;
		final int hh = rectY - 1;
		final int w = g.getFontMetrics().charWidth('A');
		final int str_c = (rectX - w) / 2;
		final int str_v = rectY - h;

		Map<Bases,Image> images = new HashMap<Frames.Bases, Image>();
		for( Frames.Bases c : Frames.Bases.values())
		{
			int top = 0;
			int left = 0 ;
			if(! images.containsKey(c))
			{
				Image image = this.createImage(ww, hh);
				Graphics g2 = image.getGraphics() ;
				g2.setColor(basesBackground);
				g2.fillRect(top, left, ww, hh);
				g2.setColor(Color.black);
				g2.drawString(c.toString(), top + str_c, left + str_v);
				g2.dispose();
				image.flush();
				images.put(c, image);				
			}			
		}
		
		for (int i = from; i < to; i++)
		{
			final Bases c = forwardBases[i];
			if (c != null)
			{
				int top = r.x + rectX * (i - from);
				int left = rectY * row;
				g.drawImage(images.get(c), top , left , null ) ;
			}
		}
	}

	private void paintBasesReverse(Graphics g)
	{
		final int row = 9;
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);
		final int h = g.getFontMetrics().getDescent();
		final Bases[] reverseBases = model.reverseBases;
		final int ww = rectX - 1;
		final int hh = rectY - 1;
		final int w = g.getFontMetrics().charWidth('A');
		final int str_c = (rectX - w) / 2;
		final int str_v = rectY - h;

		// make images
		Map<Bases,Image> images = new HashMap<Frames.Bases, Image>();
		for( Frames.Bases c : Frames.Bases.values())
		{
			int top = 0;
			int left = 0 ;
			if(! images.containsKey(c))
			{
				Image image = this.createImage(ww, hh);
				Graphics g2 = image.getGraphics() ;
				g2.setColor(basesBackground);
				g2.fillRect(left, top, ww, hh);
				g2.setColor(Color.black);
				g2.drawString(c.toString(), left + str_c, top + str_v);				
				g2.dispose();
				image.flush();
				images.put(c, image);				
			}			
		}
		
		for (int i = from; i < to; i++)
		{
			Bases c = reverseBases[model.reverseBases.length - i - 1];
			if (c != null)
			{
				final int left = r.x + rectX * (i - from);
				final int top = rectY * row;
				g.drawImage(images.get(c), left,top,null);
			}
		}
	}

	private void paintNumbersTop(Graphics g)
	{
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int from0 = from - (from % 10);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);

		final int rectWidth = rectX - 2;
		final int rectStringY = rectY - 5;
		g.setColor(Color.black);
		for (int i = from0; i < to; i += 10)
		{
			final int left = r.x + rectX * (i - from);
			g.fillRect(left + 1, rectY - 3, rectWidth, 3);
			final String str = Integer.toString(i);
			final int w = g.getFontMetrics().stringWidth(str);
			g.drawString(str, left - (w - rectX) / 2, rectStringY);
		}
	}

	private void paintNumbersBackward(Graphics g)
	{
		final int row = 11;
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int from0 = from - (from % 10);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);

		final int fillRectY = rectY * row - rectY;
		final int rectWidth = rectX - 2;
		final int rectStringY = rectY * row - 2;
		g.setColor(Color.black);
		for (int i = from0; i < to; i += 10)
		{
			final int left = r.x + rectX * (i - from);
			g.fillRect(left + 1, fillRectY, rectWidth, 3);
			final String str = Integer.toString(i);
			final int w = g.getFontMetrics().stringWidth(str);
			g.drawString(str, left - (w - rectX) / 2, rectStringY);
		}
	}

	BufferedImage aaForwardBG = null;
	int aaForwardBGWidth = -1;

	Image getAAForwardBackgroundImage()
	{
		int width = getVisibleRect().width;
		if (aaForwardBG != null && (width != aaForwardBGWidth))
		{
			aaForwardBG = null;
			aaForwardBGWidth = width;
		}
		if (aaForwardBG == null)
		{
			final int right = rectX * 3;
			final Polygon poly = new Polygon();
			poly.addPoint(0, 0);
			poly.addPoint(right, 0);
			poly.addPoint(right + offset, rectY / 2);
			poly.addPoint(right, rectY);
			poly.addPoint(0, rectY);
			poly.addPoint(offset, rectY / 2);

			BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width + rectX * 3 * 9, rectY * 3 + 1);
			Graphics g = image.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setColor(Color.lightGray);

			int drawElements = (width + rectX * 3 * 9) / rectX;
			for (int i = 0; i < drawElements; i++)
			{
				final int left = rectX * i;
				final int top = rectY * (i % 3);

				g.translate(left, top);
				// g.setColor(Color.white);
				// g.fillPolygon(poly);
				g.drawPolygon(poly);
				g.translate(-left, -top);
			}
			g.dispose();
			image.flush();
			aaForwardBG = image;
		}
		return aaForwardBG;
	}

	private void paintAAForward(Graphics g)
	{
		final int row = 2;
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);

		final int h = g.getFontMetrics().getDescent();

		final int right = rectX * 3;
		final Polygon poly = new Polygon();
		poly.addPoint(0, 0);
		poly.addPoint(right, 0);
		poly.addPoint(right + offset, rectY / 2);
		poly.addPoint(right, rectY);
		poly.addPoint(0, rectY);
		poly.addPoint(offset, rectY / 2);

		final Polygon poly2 = new Polygon();
		poly2.addPoint(offset / 2, rectY / 4);
		poly2.addPoint(right + offset / 2, rectY / 4);
		poly2.addPoint(right + offset, rectY / 2);
		poly2.addPoint(right + offset / 2, rectY - rectY / 4);
		poly2.addPoint(offset / 2, rectY - rectY / 4);
		poly2.addPoint(offset, rectY / 2);

		g.drawImage(getAAForwardBackgroundImage(), r.x - (from % 3 + 3) * rectX, row * rectY, null);
		final int loopStart = from - 2 >= 0 ? from - 2 : 0;
		final int loopEnd = to < model.forwardAA.length ? to : model.forwardAA.length;

		final int str_v = rectY - h ;
		AminoAcids[] forwardAA = model.forwardAA;
		boolean[] forwardDecodes = model.forwardDecodes;
		
		Map<AminoAcids,Image> images = new HashMap<Frames.AminoAcids, Image>();
		for (int i = loopStart; i < loopEnd; i++)
		{
			AminoAcids aminoAcid = forwardAA[i];
			if (aminoAcid != null)
			{

				final int left = r.x + rectX * (i - from);
				final int top = rectY * (row + i % 3);

				Color c;
				if (aminoAcid == AminoAcids.START || aminoAcid == AminoAcids.Met)
				{
					c = start;
				}
				else if (aminoAcid == AminoAcids.STOP)
				{
					c = end;
				}
				else
				{
					c = null;
				}
				
				if (c != null)
				{
					g.translate(left, top);
					g.setColor(c);
					g.fillPolygon(poly);
					g.translate(-left, -top);
				}

				if (forwardDecodes[i])
				{
					g.translate(left, top);
					g.setColor(Color.green);
					g.fillPolygon(poly2);
					g.translate(-left, -top);
				}

				if(! images.containsKey( aminoAcid ))
				{
					final int w = g.getFontMetrics().stringWidth(aminoAcid.toString());
					Image img = new BufferedImage(3*rectX,rectY,BufferedImage.TYPE_4BYTE_ABGR);
					Graphics g2 = img.getGraphics();
					g2.setColor(new Color( 1f,1f,1f,0f ) ) ;
					g2.fillRect(0, 0, 4*rectX, rectY ) ;
					g2.setColor(Color.black);
					g2.drawString(aminoAcid.toString(), (3 * rectX ) / 2 - w / 2, str_v );
					g2.dispose();
					img.flush();
					images.put(aminoAcid,img);
				}
				g.drawImage( images.get( aminoAcid) , left , top , null );
			}
		}
	}

	BufferedImage aaReverseBG = null;
	int aaReverseBGWidth = -1;

	Image getAAReverseBackgroundImage()
	{
		aaReverseBG = null;
		int width = getVisibleRect().width;
		if (aaReverseBG != null && (width != aaReverseBGWidth))
		{
			aaReverseBG = null;
			aaReverseBGWidth = width;
		}
		if (aaReverseBG == null)
		{
			final int right = rectX * 3;
			final int bottom = rectY;
			final int top_half = rectY / 2;
			final Polygon poly = new Polygon();

			poly.addPoint(0, 0);
			poly.addPoint(right, 0);
			poly.addPoint(right - offset, top_half);
			poly.addPoint(right, bottom);
			poly.addPoint(0, bottom);
			poly.addPoint(-offset, top_half);

			BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width + rectX * 3 * 9, rectY * 3 + 1);
			Graphics g = image.getGraphics();

			g.setColor(Color.white);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setColor(Color.lightGray);

			int drawElements = (width + rectX * 3 * 9) / rectX;
			AminoAcids[] reverseAA = model.reverseAA;
			final int bottomReorderFactor = 5 - (reverseAA.length % 3);
			for (int i = 0; i < drawElements; i++)
			{
				final int left = rectX * i;
				final int top = rectY * ((i+bottomReorderFactor) % 3);

				g.translate(left, top);
				g.drawPolygon(poly);
				g.translate(-left, -top);
			}
			g.dispose();
			image.flush();
			aaReverseBG = image;
		}
		return aaReverseBG;
	}

	private void paintAAReverse(Graphics g)
	{
		final int row = 6;
		final Rectangle r = g.getClipBounds();
		final int from = Math.min(r.x / rectX, model.forwardBases.length);
		final int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);
		final int h = g.getFontMetrics().getDescent();

		final int right = rectX * 3;
		final int bottom = rectY;
		final int top_half = rectY / 2;
		final Polygon poly = new Polygon();

		poly.addPoint(0, 0);
		poly.addPoint(right, 0);
		poly.addPoint(right - offset, top_half);
		poly.addPoint(right, bottom);
		poly.addPoint(0, bottom);
		poly.addPoint(-offset, top_half);

		final Polygon poly2 = new Polygon();
		poly2.addPoint(-offset / 2, rectY / 4);
		poly2.addPoint(right - offset / 2, rectY / 4);
		poly2.addPoint(+rectX * 3 - offset, top_half);
		poly2.addPoint(right - offset / 2, bottom - rectY / 4);
		poly2.addPoint(-offset / 2, bottom - rectY / 4);
		poly2.addPoint(-offset, top_half);

		g.drawImage(getAAReverseBackgroundImage(), r.x - (from % 3 + 3) * rectX, row * rectY, null);

		final int loopStart = from - 2 >= 0 ? from - 2 : 0;
		final int loopEnd = to < model.forwardAA.length ? to : model.forwardAA.length;
		int indexOffset = model.reverseAA.length;
		AminoAcids[] reverseAA = model.reverseAA;
		boolean[] reverseDecodes = model.reverseDecodes;

		Map<AminoAcids,Image> images = new HashMap<Frames.AminoAcids, Image>();
		final int str_v = rectY - h ;
		final int bottomReorderFactor = 5 - (reverseAA.length % 3);
		for (int i = loopStart; i < loopEnd; i++)
		{
			int index = indexOffset - i - 1;
			AminoAcids aminoAcid = reverseAA[index];

			if (aminoAcid != null)
			{
				final int left = r.x + rectX * (i - 2 - from);
				final int top = rectY * (row + ((i - 2 + bottomReorderFactor) % 3));

				Color c;
				if (aminoAcid == AminoAcids.START || aminoAcid == AminoAcids.Met)
				{
					c = start;
				}
				else if (aminoAcid == AminoAcids.STOP)
				{
					c = end;
				}
				else
				{
					c = null;
				}
				
				if (c != null)
				{
					g.translate(left, top);
					g.setColor(c);
					g.fillPolygon(poly);
					g.translate(-left, -top);
				}

				if (reverseDecodes[index])
				{
					g.translate(left, top);
					g.setColor(Color.green);
					g.fillPolygon(poly2);
					g.translate(-left, -top);
				}
				
				if(! images.containsKey( aminoAcid ))
				{
					final int w = g.getFontMetrics().stringWidth(aminoAcid.toString());
					Image img = new BufferedImage(3*rectX,rectY,BufferedImage.TYPE_4BYTE_ABGR);
					Graphics g2 = img.getGraphics();
					g2.setColor(new Color( 1f,1f,1f,0f ) ) ;
					g2.fillRect(0, 0, 4*rectX, rectY ) ;
					g2.setColor(Color.black);
					g2.drawString(aminoAcid.toString(), (3 * rectX ) / 2 - w / 2, str_v );
					g2.dispose();
					img.flush();
					images.put(aminoAcid,img);
				}
				g.drawImage( images.get( aminoAcid) , left , top , null );				
			}
		}
	}

	private void raiseRange(Graphics g)
	{
		Rectangle r = g.getClipBounds();
		int from = Math.min(r.x / rectX, model.forwardBases.length);
		int to = Math.min((r.x + r.width) / rectX + 1, model.forwardBases.length);
		Main.dispatch(new Range(from, to));
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (model != null)
		{
			paintBasesForward(g);
			paintBasesReverse(g);
			paintNumbersTop(g);
			paintNumbersBackward(g);
			paintAAForward(g);
			paintAAReverse(g);
			raiseRange(g);
		}
	}

	public void event(Object evt)
	{

		if (evt instanceof Model)
		{
			this.model = (Model) evt;
			Dimension d = new Dimension(rectX * model.forwardBases.length, rectY * rows);
			setPreferredSize(new Dimension(d));
			setMinimumSize(new Dimension(d));

			invalidate();
			validate();
			repaint();
		}

		if (evt instanceof Integer)
		{
			int point = (Integer) evt;
			int x = point * rectX;
			Component c = this;
			while (c != null)
			{
				if (c instanceof JViewport)
				{
					((JViewport) c).setViewPosition(new Point(x, 0));
					break;
				}
				c = c.getParent();
			}
		}
	}

	public Dimension getPreferredScrollableViewportSize()
	{

		return new Dimension(600, rectY * rows + rectY);
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return visibleRect.width / 2;
	}

	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return rectX;
	}
}
