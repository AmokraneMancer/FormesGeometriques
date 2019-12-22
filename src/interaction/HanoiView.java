package interaction;
import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * This class allows one to display the evolution of the "Hanoi towers" 
 * problem. To use it one needs to call three methods:
 * <ul>
 *   <li>{@code HanoiView.init(n)} to initialize the problem configuration,
 *   with <em>n</em> rings on tower 0.</li>
 *   <li>{@code HanoiView.moveOneRing(origin, destination)} to move one ring
 *   from an origin tower to a destination tower.</li>
 *   <li>{@code HanoiView.finished()} to indicate that the problem is (meant
 *   to be) solved.</li>
 * </ul>
 * 
 * @author Christophe Jacquet
 *
 */
public class HanoiView {
	/*
	 * General design:
	 *  - rings are numbered from 1 to n, 1 being the smallest
	 *  - for each tower we store the ring numbers in a stack 
	 */
	
	private final static int TOWER_COUNT = 3;
	
	@SuppressWarnings("unchecked")
	private final Stack<Integer>[] tower = new Stack[TOWER_COUNT];
	private final Color[] colors;
	private final int maxRingCount;

	private final JTextPane txtOutput = new JTextPane();
	private final JButton btnStep = new JButton("Next step"),
			btnAuto = new JButton("Automatic"),
			btnQuit = new JButton("Quit");
	private final JButton[] buttons = {this.btnStep, this.btnAuto, this.btnQuit};
	
	private final JFrame frame = new JFrame("Hanoi Towers");
	
	private boolean stepMode = true;
	private boolean nextStep = false;
	
	private static HanoiView instance = null;
	
	private enum TextType {
		ERROR(new Color(230, 0, 0)), SUCCESS(new Color(0, 180, 0)), INFO(Color.BLACK);
		
		private final Color color;
		
		private TextType(Color color) {
			this.color = color;
		}
	}
	
	/**
	 * Creates a new Hanoi setting, with initially a number of rings on tower
	 * number 0.
	 * 
	 * @param ringCount the initial count of rings on tower number 0
	 */
	private HanoiView(int ringCount) {
		if(ringCount < 1) {
			throw new Error("There must be at least one ring in the initial setting");
		}
		
		// create 3 empty towers
		for(int i=0; i<this.tower.length; i++) {
			this.tower[i] = new Stack<Integer>();
		}
		
		// fill the first tower with "ringCount" rings
		for(int i=ringCount; i>=1; i--) {
			this.tower[0].push(i);
		}
		
		this.colors = new Color[ringCount];
		for(int i=0; i<ringCount; i++) {
			this.colors[i] = Color.getHSBColor(((float)i)/ringCount, 1f, .98f);
		}
		
		this.maxRingCount = ringCount;
		
		this.frame.setLayout(new BorderLayout());
		this.frame.add(new TowersPanel(), BorderLayout.CENTER);
		
		JPanel pnlBottom = new JPanel(new BorderLayout());
		JPanel pnlButtons = new JPanel(new FlowLayout());
		
		ActionListener l = new ButtonHandler();
		for(JButton b : this.buttons) {
			pnlButtons.add(b);
			b.addActionListener(l);
		}
		
		this.txtOutput.setContentType("text/html");
		this.txtOutput.setEditable(false);

		JScrollPane scroll = new JScrollPane(this.txtOutput);
		scroll.setPreferredSize(new Dimension(0, 100));

		pnlBottom.add(scroll, BorderLayout.CENTER);
		pnlBottom.add(pnlButtons, BorderLayout.SOUTH);
		
		this.frame.add(pnlBottom, BorderLayout.SOUTH);
		
		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.frame.setVisible(true);
	}
	
	/**
	 * Moves the topmost ring of one tower to another tower.
	 * 
	 * @param originTower number of the origin tower (starting from 1)
	 * @param destinationTower number of the destination tower (starting from 1)
	 */
	private void moveOneRing_(int originTower, int destinationTower) {
		boolean sleep = false;
		originTower--;
		destinationTower--;
		
		synchronized(this) {
			if(this.stepMode) {
				while(! this.nextStep) {
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				this.nextStep = false;
			} else {
				sleep = true;
			}
		}
		
		if(sleep) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		synchronized(this) {
			String prefix = (originTower+1) + " \u2192 " + (destinationTower+1) + ": ";
			
			if(originTower < 0 || originTower >= TOWER_COUNT) {
				error(prefix + "non-existing origin tower");
				return;
			}
			
			if(destinationTower < 0 || destinationTower >= TOWER_COUNT) {
				error(prefix + "non-existing destination tower");
				return;
			}
			
			if(originTower == destinationTower) {
				error(prefix + "origin and destination towers cannot be the same");
				return;
			}
			
			if(this.tower[originTower].size() == 0) {
				error(prefix + "cannot move one ring from empty origin tower");
				return;
			}
			
			int ring = this.tower[originTower].peek();
			
			if(this.tower[destinationTower].size() > 0 && ring > this.tower[destinationTower].peek()) {
				error(prefix + "cannot move ring of size " + ring + " onto smaller ring (size " + this.tower[destinationTower].peek() + ")");
				return;
			}
			
			// at this point, we know the move is possible: do it!
			this.tower[originTower].pop();
			this.tower[destinationTower].push(ring);
			
			addMessage(TextType.INFO, prefix + "moving ring " + ring);
		}
		
		this.frame.repaint();
	}
	
	private void finished_() {
		if(this.tower[TOWER_COUNT-1].size() == this.maxRingCount) {
			addMessage(TextType.SUCCESS, "Solution valid. Congratulations!");
			disableButtons();	
		} else {
			error("Solution invalid");
		}
	}
	
	private void error(String message) {
		addMessage(TextType.ERROR, message);
		disableButtons();
		throw new Error(message);
	}
	
	private void addMessage(TextType type, String message) {
		Document d = this.txtOutput.getDocument();
		this.txtOutput.setCaretPosition(d.getLength());

		{
			SimpleAttributeSet sas = new SimpleAttributeSet();
			StyleConstants.setForeground(sas, type.color);
			addTextToEnd(d, message + "\n", sas);
		}
	}
	
	private void addTextToEnd(Document d, String text, AttributeSet as) {
		try {
			d.insertString(d.getLength(), text, as);
		} catch (BadLocationException e) {}		
	}
	
	private void disableButtons() {
		this.btnStep.setEnabled(false);
		this.btnAuto.setEnabled(false);
	}
	
	
	/**
	 * Initializes the problem configuration, with the given number of rings on
	 * Tower 0 (the leftmost one).
	 * 
	 * @param ringCount the number of rings to be added to Tower 0
	 */
	public static void init(int ringCount) {
		instance = new HanoiView(ringCount);
	}
	

	/**
	 * Moves one ring from an origin tower to a destination tower.
	 * 
	 * This method checks that the move is possible, and raises an error
	 * otherwise, with an explanatory message. Reasons for this method to
	 * fail include specifying an incorrect tower index, trying to move a
	 * ring from an empty tower, specifying the same tower for both origin
	 * and destination, or trying to move a larger ring onto a smaller one.
	 * 
	 * @param originTower the origin tower
	 * @param destinationTower the destination tower
	 */
	public static void moveOneRing(int originTower, int destinationTower) {
		if(instance == null) {
			throw new Error("You must call init before calling moveOneRing");
		} else {
			instance.moveOneRing_(originTower, destinationTower);
		}
	}

	
	/**
	 * Declares the problem solved.
	 * 
	 * This method raises an error if all the rings are not located on the
	 * rightmost tower (Tower 2).
	 */
	public static void finished() {
		if(instance == null) {
			throw new Error("You must call init before calling finished");
		} else {
			instance.finished_();
		}
	}


	private class ButtonHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			synchronized(HanoiView.this) {
			
				if(source == HanoiView.this.btnStep) {
					HanoiView.this.nextStep = true;
					HanoiView.this.notifyAll();
				} else if(source == HanoiView.this.btnAuto) {
					HanoiView.this.stepMode = ! HanoiView.this.stepMode;
					HanoiView.this.btnStep.setEnabled(HanoiView.this.stepMode);
					if(! HanoiView.this.stepMode) {
						HanoiView.this.nextStep = true;
						HanoiView.this.notifyAll();
					}
				} else if(source == HanoiView.this.btnQuit) {
					System.exit(0);
				}
			
			}
		}		
	}
	
	@SuppressWarnings("serial")
	private class TowersPanel extends JPanel {
		private static final int MIN_GAP = 20;
		
		public TowersPanel() {
			setPreferredSize(new Dimension(600, 240));
		}
		
		
		public void paintComponent(Graphics g) {
			int oneThird = getWidth() / TOWER_COUNT;
			int height = getHeight();
			int largestRingSize = 2 * oneThird / 3;
			if(oneThird - largestRingSize < MIN_GAP) largestRingSize = oneThird - MIN_GAP;
			
			int ringHeight = (height - 2 * MIN_GAP) / HanoiView.this.maxRingCount;
			int ringSizeDecrement = HanoiView.this.maxRingCount > 1 ?
					(largestRingSize - ringHeight) / (HanoiView.this.maxRingCount - 1)
					: 0;

			Graphics2D g2d = (Graphics2D) g;
			
			// change the origin of the canvas
			g2d.translate(0, height);
			g2d.scale(1, -1);
			final AffineTransform transf = g2d.getTransform();

			final FontMetrics m = g.getFontMetrics();
			final int lineHeight = m.getMaxAscent();
			
			synchronized(HanoiView.this) {

				for(int towerNum = 0; towerNum < TOWER_COUNT; towerNum++) {
					g.setColor(Color.DARK_GRAY);
					
					final int midX = oneThird/2 + towerNum * oneThird;
					
					g.fillRect(midX-1, MIN_GAP, 3, height - 3*MIN_GAP/2);
					
					String label = Integer.toString(towerNum+1);
					g2d.translate(midX - g.getFontMetrics().stringWidth(label)/2, 5);
					g2d.scale(1, -1);
					g.drawString(label, 0, 0);
					g2d.setTransform(transf);

					int y = MIN_GAP;
					for(int ring : HanoiView.this.tower[towerNum]) {
						int ringSize = largestRingSize - ringSizeDecrement * (HanoiView.this.maxRingCount - ring);
						int x = midX - ringSize/2;
						
						g.setColor(HanoiView.this.colors[ring-1]);
						g.fillRect(x, y, ringSize, ringHeight-2);

						g.setColor(Color.WHITE);
						String number = Integer.toString(ring);

						if(ringHeight - 2 > lineHeight) {
							int ypos = (ringHeight - lineHeight) / 2 + 1;
							
							g2d.translate(midX - g.getFontMetrics().stringWidth(number)/2, y+ypos);
							g2d.scale(1, -1);
							g.drawString(number, 0, 0);
							g2d.setTransform(transf);
						}
						
						g.setColor(Color.BLACK);
						g.drawRect(x, y, ringSize, ringHeight-2);
						
						y += ringHeight;
					}
				}
			
			}
		}
	}
}