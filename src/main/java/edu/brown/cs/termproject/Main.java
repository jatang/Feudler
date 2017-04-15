package edu.brown.cs.termproject;

import edu.brown.cs.termproject.networking.Suggestions;

/**
 * The Main class of the project where execution begins.
 */
public final class Main {

  private String[] args;

  /**
   * The initial method called when execution begins.
   *
   * @param args
   *          An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {

    for (String s : Suggestions.getGoogleSuggestions("My dog likes b")) {
      System.out.println(s);
    }

  }

  private static void runSparkServer() {

  }

}
