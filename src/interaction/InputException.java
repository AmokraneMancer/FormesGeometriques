package interaction;

/**
 * This exception is reported when an error occurs while using one of
 * {@link interaction.Input}'s services. 
 * 
 * @author Christophe Jacquet
 *
 */
@SuppressWarnings("serial")
class InputException extends RuntimeException {
	InputException(String message) {
		super(message);
	}
	
	InputException(Exception e) {
		super(e);
	}
}