package edu.brown.cs.swaxman1.reader;

/**
 * Simple class for invalid csv Format exceptions.
 *
 * @author swaxman1
 *
 */
public class InvalidFormatException extends Exception {

  /**
   * It complained, so I added this!
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor with message.
   *
   * @param message - exception message
   */
  public InvalidFormatException(String message) {
    this.message = message;
  }
  
  @Override
  public String getMessage() {
	  return message;
  }

  private String message;

  @Override
  public String toString() {
    return this.message;
  }
}
