package interaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used to tag a constructor parameter with a 
 * description, in order to generate the field labels ("prompts") when using 
 * {@link Input#readObject}.
 * 
 * Optionally, one may specify a set of possible choices and/or an initial
 * value for the field.
 * 
 * @author Christophe Jacquet
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface InputField {
	/**
	 * The "prompt" for this parameter, to be displayed as a field label.
	 */
	String prompt();
	
	/**
	 * Indicates whether the prompt is really meaningful ({@code false},
	 * default behavior), or just a placeholder to be used only when
	 * necessary ({@code true}). For instance, when using Input.readInt(),
	 * a GUI window would be obliged to render a prompt next to the only
	 * text field; however a console interactor does not need to do it
	 * when there is only one field.
	 */
	boolean dummyPrompt() default false;
	
	/**
	 * The list of possible values for this parameter.
	 */
	String[] choices() default {};
	
	/**
	 * The initial ("default") value of the parameter.
	 */
	String initial() default "";
}