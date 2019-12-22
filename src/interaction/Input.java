package interaction;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * This class provides services for reading user input easily.
 * 
 * <p>The values may be simple values ({@code readInt()}, {@code readDouble()},
 * etc.) or object values ({@code readObject}).</p>
 * 
 * <p>The inputs may be done textually in the console (by default) or
 * graphically.</p>
 * 
 * @author Christophe Jacquet
 *
 */
public class Input {
	// By default in console mode
	private static boolean graphicalMode = false;
	
	/**
	 * Switch to graphical mode for subsequent inputs.
	 */
	public static void switchToGraphicalMode() {
		graphicalMode = true;
	}
	
	/**
	 * Switch to textual mode (in the console) for subsequent inputs.
	 */
	public static void switchToConsoleMode() {
		graphicalMode = false;
	}
	
	private static class UserField {
		public final Class<?> type;
		public final String prompt;
		public final boolean dummyPrompt;
		public Object value;
		public final String[] choices;
		public final String initialValue;
		
		public UserField(Class<?> type, String prompt, boolean dummyPrompt, String[] choices, String initialValue) {
			this.type = type;
			this.prompt = prompt;
			this.dummyPrompt = dummyPrompt;
			this.choices = choices;
			this.initialValue = initialValue;
		}
		
		public String toString() {
			return "<" + this.prompt + ": " + this.type.getCanonicalName() + ">";
		}

		public boolean parse(String txt) {
			try {
				if(this.type == int.class || this.type == Integer.class) {
					this.value = Integer.parseInt(txt);
				} else if(this.type == long.class || this.type == Long.class) {
					this.value = Long.parseLong(txt);
				} else if(this.type == double.class || this.type == Double.class) {
					this.value = Double.parseDouble(txt);
				} else if(this.type == float.class || this.type == Float.class) {
					this.value = Float.parseFloat(txt);
				} else if(this.type == char.class || this.type == Character.class) {
					if(txt.length() != 1) throw new Exception("Not a single character");
					this.value = txt.charAt(0);
				} else if(this.type == boolean.class || this.type == Boolean.class) {
					// cannot use Boolean.parseBoolean() here because it
					// considers any non-"true" value is false
					if("true".equalsIgnoreCase(txt)) {
						this.value = true;
					} else if("false".equalsIgnoreCase(txt)) {
						this.value = false;
					} else {
						throw new Exception("Bad boolean value");
					}
				} else if(this.type == String.class) {
					if(this.choices.length > 0 && ! oneOfChoicesIgnoreCase(txt)) {
						throw new Exception("Value is not one of the possible choices");
					}
					this.value = txt;
				} else {
					this.value = txt;
				}
				return true;
			} catch(Exception exc) {
				return false;
			}
		}

		
		private boolean oneOfChoicesIgnoreCase(String v) {
			for(String c : this.choices) {
				if(c.equalsIgnoreCase(v)) return true;
			}
			return false;
		}
	}
	

	/**
	 * "Reads" an object: asks the user to provide the arguments of one
	 * designated constructor of a given class to create an instance of
	 * this class.
	 * 
	 * <p>
	 * The constructor used is the so-called "input constructor":
	 * if the class only has one constructor, this one is used; otherwise,
	 * the programmer must explicitly tag one of the constructor (chosen at
	 * their discretion) with the InputConstructor annotation.
	 * </p>
	 * 
	 * <p>
	 * The programmer may also annotate the parameters of the input
	 * constructor with the InputField annotation. This allows one
	 * to specify:
	 * </p>
	 * <ul>
	 *   <li>a label/title for the parameter, to be used as a prompt for
	 *   user input,</li>
	 *   <li>optionally, a list of possible choices,</li>
	 *   <li>optionally, a default value.</li>
	 * </ul> 
	 * 
	 * @param message a message to be displayed, prompting for user input
	 * @param type a Class object representing the type of the object to be
	 * read (such an object is generally obtained by writing MyClass.class)
	 * @return a new object of the specified class, created by calling the
	 * input constructor onto user-provided values
	 *  
	 * @see InputConstructor
	 * @see InputField
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readObject(String message, Class<T> type) {
		Constructor<?> inputConstructor = null;
		
		// Browse all constructors
		Constructor<?>[] constructors = type.getConstructors();
		for(Constructor<?> c : constructors) {
			if(c.isAnnotationPresent(InputConstructor.class)) {
				// If one input constructor found then store it and raise an exception if it
				// is not the only one. (A class must not have more than one input
				// constructor)
				if(inputConstructor != null) {
					throw new InputException("The class " + type.getSimpleName() + 
							" has more than one input constructor, which is not permitted.");
				} else {
					inputConstructor = c;
				}
			}
		}
		
		// If no explicit input constructor found, and if the class has only one constructor,
		// then use it
		if(inputConstructor == null && constructors.length == 1) {
			inputConstructor = constructors[0];
		}
		
		// Otherwise, raise an exception, because it is not allowed
		if(inputConstructor == null) throw new InputException("The class " + 
				type.getSimpleName() + ", which has " + constructors.length + 
				" constructors, MUST explicitly declare one of them as the input constructor.");
		
		// Now, we have identified the constructor!
		assert inputConstructor != null;
		
		Class<?>[] parameterTypes = inputConstructor.getParameterTypes();
		Annotation[][] parameterAnnotations = inputConstructor.getParameterAnnotations();
		
		String[] parameterNames = Paranamer.lookupParameterNames(inputConstructor, false);
		
		UserField[] inputFields = new UserField[parameterTypes.length];
		for(int i=0; i<parameterTypes.length; i++) {
			// By default, use the type as a prompt
			String prompt = parameterTypes[i].getSimpleName();
			
			// parameter name: available if class compiled with the "debug" option
			if(parameterNames.length > i) prompt = parameterNames[i];
			String[] choices = {};
			String initialValue = "";
			boolean dummyPrompt = false;
			
			// Try to find a "field" annotation
			for(Annotation a : parameterAnnotations[i]) {
				if(a.annotationType() == InputField.class) {
					prompt = ((InputField)a).prompt();
					choices = ((InputField)a).choices();
					initialValue = ((InputField)a).initial();
					dummyPrompt = ((InputField)a).dummyPrompt();
					break;
				}
			}
			
			inputFields[i] = new UserField(parameterTypes[i], prompt, dummyPrompt, choices, initialValue);
		}
		
		// show the input window, and return null if it is canceled
		InputReader reader = graphicalMode ?
				new Window(message, inputFields) : 
				new ConsoleReader(message, inputFields);
		boolean result = reader.readInput();
		reader.dispose();
		
		if(! result) {
			return null;
		}
		
		// otherwise construct a new object
		Object[] values = new Object[inputFields.length];
		for(int i=0; i<values.length; i++) {
			values[i] = inputFields[i].value;
		}
		
		try {
			return (T) inputConstructor.newInstance(values);
		} catch (Exception e) {
			throw new InputException(e);
		}
	}
	
	
	/**
	 * Reads an integer from the user.
	 * 
	 * @param message a prompting message
	 * @return the integer read
	 */
	public static int readInt(final String message) {
		SimpleIntReader reader = readObject(message, SimpleIntReader.class);
		return reader.value;
	}
	
	
	/**
	 * Reads an integer from the user.
	 * 
	 * @return the integer read
	 */
	public static int readInt() {
		return readInt("Enter a value");
	}
	
	
	/**
	 * Reads a double from the user.
	 * 
	 * @param message a prompting message
	 * @return the double read
	 */
	public static double readDouble(final String message) {
		SimpleDoubleReader reader = readObject(message, SimpleDoubleReader.class);
		return reader.value;
	}
	
	
	/**
	 * Reads a double from the user.
	 * 
	 * @return the double read
	 */
	public static double readDouble() {
		return readDouble("Enter a value");
	}
	
	
	/**
	 * Reads a string from the user.
	 * 
	 * @param message a prompting message
	 * @return the string read
	 */
	public static String readString(final String message) {
		SimpleStringReader reader = readObject(message, SimpleStringReader.class);
		return reader.value;
	}
	
	
	/**
	 * Reads a string from the user.
	 * 
	 * @return the string read
	 */
	public static String readString() {
		return readString("Enter a value");
	}
	
	
	/**
	 * Reads a character from the user.
	 * 
	 * @param message a prompting message
	 * @return the character read
	 */
	public static char readChar(final String message) {
		SimpleCharacterReader reader = readObject(message, SimpleCharacterReader.class);
		return reader.value;
	}
	
	
	/**
	 * Reads a character from the user.
	 * 
	 * @return the character read
	 */
	public static char readChar() {
		return readChar("Enter a value");
	}
	
	
	/**
	 * Reads a boolean from the user.
	 * 
	 * @param message a prompting message
	 * @return the boolean read
	 */
	public static boolean readBoolean(final String message) {
		SimpleBooleanReader reader = readObject(message, SimpleBooleanReader.class);
		return reader.value;
	}
	
	
	/**
	 * Reads a boolean from the user.
	 * 
	 * @return the boolean read
	 */
	public static boolean readBoolean() {
		return readBoolean("Enter a value");
	}
	

	/**
	 * Returns the number of seconds since midnight.
	 * 
	 * @return the number of seconds since midnight
	 */
	public static int secondsSinceMidnight() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
	}
	
	private interface InputReader {
		public boolean readInput();
		public void dispose();
	}
	
	@SuppressWarnings("serial")
	private static class Window extends JFrame implements ActionListener, InputReader {
		private final Semaphore semValidated = new Semaphore(0);
		private final JButton btnOK, btnCancel;
		private final Interactor[] interactor;
		private boolean validateOK = false;
		
		public Window(String prompt, UserField[] fields) {
			super(prompt);
			
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			setLayout(new BorderLayout());
			
			this.btnOK = new JButton("OK");
			this.btnCancel = new JButton("Cancel");

			// set up the "main panel":
			// the area containing the labels and fields
			
			SpringLayout layout = new SpringLayout();
			JPanel pnlMain = new JPanel(layout);
			
			this.interactor = new Interactor[fields.length];

			for(int i=0; i<fields.length; i++) {
				this.interactor[i] = Interactor.forInputField(this, fields[i]);
				JLabel lbl = new JLabel(fields[i].prompt);
				pnlMain.add(lbl);
				pnlMain.add(this.interactor[i]);
			}
			
			SpringUtilities.makeCompactGrid(pnlMain, fields.length, 2, 5, 5, 5, 10);
			
			add(pnlMain, BorderLayout.CENTER);
			
			JPanel pnlButtons = new JPanel(new FlowLayout());
			this.btnOK.addActionListener(this);
			this.btnCancel.addActionListener(this);
			pnlButtons.add(this.btnOK);
			pnlButtons.add(this.btnCancel);
			add(pnlButtons, BorderLayout.SOUTH);
			
			// default button
			getRootPane().setDefaultButton(this.btnOK);
			
			pack();
			setLocationRelativeTo(null);
			setResizable(false);
			
			this.updateOK();
		}
		
		public boolean readInput() {
			setVisible(true);
			this.semValidated.acquireUninterruptibly();
			return this.validateOK;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == this.btnOK) {
				for(Interactor i : this.interactor) {
					if(! i.isOK()) return;
					// Can validate the dialog only if all fields are OK
				}
				this.validateOK = true;
			}
			this.semValidated.release();
		}

		public void updateOK() {
			boolean allOK = true;
			
			pack();
			
			for(Interactor i : this.interactor) {
				if(i != null && ! i.isOK()) {
					allOK = false;
					break;
				}
			}
			
			this.btnOK.setEnabled(allOK);
		}
	}
	
	@SuppressWarnings("serial")
	private static abstract class Interactor extends JPanel {
		protected final UserField inputField;
		protected final Window window;
		private boolean ok = false;
		
		public Interactor(Window window, UserField inputField) {
			super(new BorderLayout());
			this.inputField = inputField;
			this.window = window;
		}
		
		protected void setSizes() {
			Dimension prefDim = getPreferredSize();
			setPreferredSize(new Dimension(Math.max(prefDim.width, 300), prefDim.height));
		}
		
		public static Interactor forInputField(Window window, UserField inputField) {
			if(inputField.type == String.class && inputField.choices.length > 0) {
				return new MultiInteractor(window, inputField);
			}
			if(inputField.type == String.class ||
					inputField.type == int.class || inputField.type == Integer.class ||
					inputField.type == long.class || inputField.type == Long.class ||
					inputField.type == double.class || inputField.type == Double.class ||
					inputField.type == float.class || inputField.type == Float.class ||
					inputField.type == char.class || inputField.type == Character.class) {
				return new TextInteractor(window, inputField);
			} else if(inputField.type == boolean.class || inputField.type == Boolean.class) {
				return new BooleanInteractor(window, inputField);
			} else throw new InputException("Type " + inputField.type.getCanonicalName() + " not handled.");
		}
		
		public final boolean isOK() {
			return this.ok;
		}
		
		protected void setOK(boolean ok) {
			this.ok = ok;
			this.window.updateOK();
		}
	}
	
	@SuppressWarnings("serial")
	private static abstract class ValidatedInteractor extends Interactor {
		private final JLabel lblOK = new JLabel("???");
		
		protected ValidatedInteractor(Window window, UserField inputField) {
			super(window, inputField);
			Font font = this.lblOK.getFont();
			Font myFont = new Font(font.getName(), font.getStyle(), 5*font.getSize()/6);
			this.lblOK.setFont(myFont);
		}
		
		protected void setup(JComponent cmpInteract) {
			this.add(cmpInteract, BorderLayout.NORTH);
			this.add(this.lblOK, BorderLayout.SOUTH);
		}
		
		protected void setOK() {
			setMessage(null);
			setOK(true);
		}
		
		protected void setError(String message) {
			setMessage(message);
			setOK(false);
		}
		
		private void setMessage(String message) {
			StringBuilder b = new StringBuilder("<html><i>").append(this.inputField.type.getSimpleName()).append(" - ");
			if(message == null) {
				b.append("<font color=#00AA00>OK</font>");
			} else {
				b.append("<font color=#AA0000>").append(message).append("</font>");
			}
			b.append("</html>");
			this.lblOK.setText(b.toString());
		}
	}
	

	@SuppressWarnings("serial")
	private static class TextInteractor extends ValidatedInteractor implements DocumentListener {
		private JTextField text = new JTextField();
		
		public TextInteractor(Window window, UserField inputField) {
			super(window, inputField);
			setup(this.text);
			this.text.setText(inputField.initialValue);
			this.text.getDocument().addDocumentListener(this);
			checkText();
			setSizes();
		}

		private void checkText() {
			String txt = this.text.getText();

			if(this.inputField.type == String.class) {
				this.inputField.value = txt;
				setOK();
			} else {
				if(txt.trim().length() == 0) {
					setError("Empty");
				} else {
					if(this.inputField.parse(txt)) {
						setOK();
					} else {
						setError("Wrong format");
					}
				}
			}
		}

		@Override
		public void changedUpdate(DocumentEvent evt) {
			checkText();
		}
		

		@Override
		public void insertUpdate(DocumentEvent evt) {
			checkText();
		}

		@Override
		public void removeUpdate(DocumentEvent evt) {
			checkText();
		}
	}
	
	@SuppressWarnings("serial")
	private static class BooleanInteractor extends Interactor implements ChangeListener {
		private final JCheckBox checkBox;
		
		public BooleanInteractor(Window window, UserField inputField) {
			super(window, inputField);
			
			this.checkBox = new JCheckBox();
			this.checkBox.setSelected(Boolean.parseBoolean(inputField.initialValue));
			add(this.checkBox, BorderLayout.CENTER);
			this.checkBox.addChangeListener(this);
			checkValue();
			setOK(true);
			setSizes();
		}

		@Override
		public void stateChanged(ChangeEvent evt) {
			checkValue();
		}
		
		private void checkValue() {
			this.inputField.value = this.checkBox.isSelected();
		}
	}
	
	@SuppressWarnings("serial")
	private static class MultiInteractor extends Interactor implements ActionListener {
		private final JComboBox combo;
		
		public MultiInteractor(Window window, UserField inputField) {
			super(window, inputField);
			
			this.combo = new JComboBox(inputField.choices);
			
			// try to find the default value in the "choices" array
			for(int i=0; i<inputField.choices.length; i++) {
				if(inputField.initialValue != null && 
						inputField.initialValue.equals(inputField.choices[i])) {
					this.combo.setSelectedIndex(i);
					break;
				}
			}

			add(this.combo, BorderLayout.CENTER);
			this.combo.addActionListener(this);
			checkValue();
			setOK(true);
			setSizes();
		}
		
		private void checkValue() {
			this.inputField.value = this.combo.getSelectedItem();
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			checkValue();
		}
	}
	
	private static class SimpleReader<T> {
		public final T value;
		
		public SimpleReader(@InputField(prompt="Value", dummyPrompt=true) T value) {
			this.value = value;
		}
	}
	
	private static final class SimpleIntReader extends SimpleReader<Integer> {
		public SimpleIntReader(@InputField(prompt="Value", dummyPrompt=true) Integer value) {
			super(value);
		}
	}
	
	private static final class SimpleDoubleReader extends SimpleReader<Double> {
		public SimpleDoubleReader(@InputField(prompt="Value", dummyPrompt=true) Double value) {
			super(value);
		}
	}
	
	private static final class SimpleStringReader extends SimpleReader<String> {
		public SimpleStringReader(@InputField(prompt="Value", dummyPrompt=true) String value) {
			super(value);
		}
	}
	
	private static final class SimpleCharacterReader extends SimpleReader<Character> {
		public SimpleCharacterReader(@InputField(prompt="Value", dummyPrompt=true) Character value) {
			super(value);
		}
	}
	
	private static final class SimpleBooleanReader extends SimpleReader<Boolean> {
		public SimpleBooleanReader(@InputField(prompt="Value", dummyPrompt=true) Boolean value) {
			super(value);
		}
	}
	
	private static class ConsoleReader implements InputReader {
		private final UserField[] fields;
		private final String prompt;

		public ConsoleReader(String prompt, UserField[] fields) {
			this.fields = fields;
			this.prompt = prompt;
		}

		@Override
		public boolean readInput() {
			// Explicitly print prompts for individual fields only if there
			// are more than 1 field OR if there is one field whose prompt
			// is non-dummy. Otherwise we can use a somewhat simplified
			// interaction scheme (useful for readInt, readDouble, etc.).
			boolean printPrompts = this.fields.length != 1 || !this.fields[0].dummyPrompt
					|| this.prompt == null || this.prompt.length() == 0;
			
			if(this.prompt != null && this.prompt.length() > 0) {
				System.out.print(this.prompt);
				if(printPrompts) {
					System.out.println();
				}
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			try {
				for(UserField field : this.fields) {
					boolean inputOK;
					
					do {
						String prompt = (printPrompts ? field.prompt : "") + " (" + field.type.getSimpleName();
						if(field.choices.length > 0) {
							prompt += ", choices: " + Arrays.toString(field.choices);
						}
						prompt += "): ";
						System.out.print(prompt);
						String txt = reader.readLine();

						inputOK = field.parse(txt);
						if(!inputOK) {
							System.out.println("Wrong format. Please type value again.");
							// if we do not print the individual prompt of the only field, then
							// it's a good idea to print the general prompt here
							if(! printPrompts) System.out.print(this.prompt);
						}
					} while(!inputOK);
				}
			} catch (IOException e) {
				return false;
			}

			return true;
		}

		@Override
		public void dispose() {
			// nothing to do -- just to mimic JFrame's dispose method
		}
	}
}