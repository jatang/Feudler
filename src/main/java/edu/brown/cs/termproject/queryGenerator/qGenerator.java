package edu.brown.cs.termproject.queryGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import edu.brown.cs.swaxman1.reader.CsvReader;
import edu.brown.cs.swaxman1.reader.InvalidFormatException;
import edu.brown.cs.termproject.database.DBConnector;
import edu.brown.cs.termproject.networking.Suggestions;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.queryResponses.Response;
import edu.brown.cs.termproject.scoring.Clustering;
import edu.brown.cs.termproject.scoring.Suggestion;

public class qGenerator {
  private final static String dbPath = "data/gFeud.sqlite3";
  private static DBConnector db;

  public qGenerator() throws SQLException {
    // TODO: Close this at somet point
    db = new DBConnector(dbPath);
  }

  /**
   * Inserts a query and its answers into the database, not prompting user or
   * checking to make sure the query is good
   * 
   * @param query
   *          - The Query to add to the database
   * @return - True if input successfully, false if not put into database
   */
  public boolean insertQuery(String query) {
    // This will get the google suggestion endings that aren't too similar
    Clustering<Suggestion> suggs = Suggestions
        .getUniqueGoogleSuggestionEndings(query);
    // We only want to insert to database if there are enough answers
    if (suggs.size() < 4) {
      return false;
    }
    // building our QR to put into the database's insertQuery method
    QueryResponses qr = new QueryResponses(0, query, suggs);

    try {
      db.insertQuery(qr);
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * Used for server to make sure a custom query has enough answers to be used
   * in game. Takes in a query (as a string) and returns boolean corresponding
   * to whether there are enough answers
   * 
   * @param query
   *          - The custom query the user inputted
   * @return - boolean corresponding to whether there are enough answers
   */
  public boolean isValidQuery(String query) {
    Clustering<Suggestion> suggs = Suggestions
        .getUniqueGoogleSuggestionEndings(query);
    if (suggs.size() < 4) {
      return false;
    }
    return true;
  }

  /**
   * Similar to the insertQuery method, but allows for a lot more checks to take
   * place. First, in System.out, the method prints out the answers to the
   * queries. The user then quickly scans them over and determines if this is a
   * good enough question to use. If it is, they press Y and it's inserted to
   * the database if not, they can type anything else they want and it's not
   * added.
   * 
   * The WriteTo functionality was for easy extensibility while we work on the
   * project: We easily might add new columns or change the format of what we
   * store, (and we did), so having a few methods that will automatically take
   * in these files and repopulate the database instead of having to dump the
   * queries from the db into a file and use that ( which would lose some data,
   * like where the queries came from), this made things simpler.
   * 
   * 
   * @param query
   *          - The query to put into the database.
   * @param writeTo
   *          - A file to write the selected queries to, in addition to the
   *          database
   * @return - boolean corresponding to whether or not the query was added
   * @throws IOException
   *           - If writing to file doesn't work
   */
  public boolean insertCheck(String query, String writeTo) throws IOException {
    // if the db has the query, don't bother printing answers, just return
    // false.
    if (db.containsQuery(query)) {
      return false;
    }
    Clustering<Suggestion> suggs = Suggestions
        .getUniqueGoogleSuggestionEndings(query);
    // similarly, if there's fewer than 4, don't show the answers. Show the
    // query so I know
    // what was skipped over.
    if (suggs.size() < 4) {
      System.out.println(query);
      System.out.println(false);
      return false;
    }
    List<Suggestion> filteredSuggs = suggs.asList();
    // print answers
    for (Suggestion s : filteredSuggs) {
      System.out.println(s.getResponse());
    }
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String confirm = r.readLine();
    // if it's good, put it into database and print out whether
    // the insert was succesful
    if (confirm.equals("Y")) {
      boolean output = insertQuery(query);
      System.out.println(output);
      if (output) {
        // if succesful, store in our given file
        FileWriter fw = new FileWriter(writeTo, true);
        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
        pw.println(query);
        pw.flush();
        pw.close();
      }
      return output;
    }
    return false;
  }

  /**
   * takes a file of things (animals for instance) and a string representing
   * what insert category function should be used, as well as a boolean
   * representing if we want insertQuery or insertCheck to be used. Adds the
   * things to the databse using the specified category function
   * 
   * @param filePath
   *          - Path to the file in question
   * @param type
   *          - type of category function to use
   * @param check
   *          - whether or not to have user check the queries
   * @throws IOException
   *           - if reading from file goes badly
   */
  public void insertFile(String filePath, String type, boolean check)
      throws IOException {
    File f = new File(filePath);
    BufferedReader fRead = new BufferedReader(new FileReader(f));
    Consumer<String> con = null;
    // Here are the aforementioned "types"
    switch (type) {
      case "animal":
        con = s -> fromAnimal(s, check);
        break;
      case "drink":
        con = s -> fromDrink(s, check);
        break;
      case "actor":
        con = s -> fromActor(s, check);
        break;
      case "food":
        con = s -> fromFood(s, check);
        break;
      // This next case is for lists of queries that we know are good and should
      // be entered
      case "checked":
        con = s -> insertQuery(s);
        break;
    }
    if (con == null) {
      System.out.println("Didn't enter valid function type.");
      return;
    } else {
      String line = fRead.readLine();
      while (line != null) {
        con.accept(line);
        line = fRead.readLine();
      }
    }
  }

  /**
   * Takes in the name of an animal, adds some queries about it to the database
   * 
   * @param animal
   *          - singular name of animal
   * @param check
   *          - whether or not to have user check the query
   */
  public void fromAnimal(String animal, boolean check) {
    animal = animal + " ";
    String query1 = "how does ";
    String toAdd;
    Character[] vowels = { 'a', 'e', 'i', 'o', 'u' };
    if (Arrays.asList(vowels).contains(animal.charAt(0))) {
      // could mess up once in a while, but users type this in wrong a lot,
      // so not too big of a deal
      toAdd = "an ";
      query1 += toAdd;
    } else {
      toAdd = "a ";
      query1 += toAdd;
    }
    query1 += animal;

    String query2 = "why does " + toAdd + animal;
    String query3 = "is " + toAdd + animal;
    String query4 = "how can " + toAdd + animal;
    String query5 = "is my " + animal;

    String[] queries = { query1, query2, query3, query4, query5 };
    for (String query : queries) {
      try {
        if (check) {
          insertCheck(query, "data/animalQs.txt");
        } else {
          insertQuery(query);
        }
      } catch (IOException e) {
        System.out.println("fromAnimal: " + e.getMessage());
      }
    }
  }

  /**
   * Inserts a few queries about an actor into the database
   * 
   * @param actor
   *          - actor name
   * @param check
   *          - whether the user should check the query
   */
  public void fromActor(String actor, boolean check) {
    actor += " ";
    String query1 = actor + "is ";
    String query2 = "does " + actor;
    String query3 = actor + "in ";
    try {
      if (check) {
        String writeTo = "data/actorQs.txt";
        insertCheck(query1, writeTo);
        insertCheck(query2, writeTo);
        insertCheck(query3, writeTo);
      } else {
        insertQuery(query1);
        insertQuery(query2);
        insertQuery(query3);
      }
    } catch (IOException e) {
      System.out.println("fromActor: " + e.getMessage());
    }
  }

  /**
   * Inserts a few queries about a food into the database
   * 
   * @param food
   *          - the food name
   * @param check
   *          - whether the user should check the query
   */
  public void fromFood(String food, boolean check) {
    food += " ";
    String query1 = "can you eat " + food + "with ";
    String query2 = food + "mixed with ";
    String query3 = "is " + food;
    try {
      if (check) {
        String writeTo = "data/foodQs.txt";
        insertCheck(query1, writeTo);
        insertCheck(query2, writeTo);
        insertCheck(query3, writeTo);
      } else {
        insertQuery(query1);
        insertQuery(query2);
        insertQuery(query3);
      }
    } catch (IOException e) {
      System.out.println("fromFood: " + e.getMessage());
    }
  }

  /**
   * Inserts a few queries about a drink into the database
   * 
   * @param drink
   *          - the drink
   * @param check
   *          - whether the user should check the query
   */
  public void fromDrink(String drink, boolean check) {
    drink += " ";
    String query1 = "drinking " + drink + "makes me ";
    String query2 = "drinking " + drink + "with ";
    String query3 = drink + "mixed with ";
    String query4 = "can you drink " + drink;
    try {
      if (check) {
        String writeTo = "data/drinkQs.txt";
        insertCheck(query1, writeTo);
        insertCheck(query2, writeTo);
        insertCheck(query3, writeTo);
        insertCheck(query4, writeTo);
      } else {
        insertQuery(query1);
        insertQuery(query2);
        insertQuery(query3);
        insertQuery(query4);
      }
    } catch (IOException e) {
      System.out.println("fromDrink: " + e.getMessage());
    }
  }

  /**
   * Mainly to be used with google top trends related queries. Takes in a string
   * like "why do I feel so happy today", prints it out to user, and lets user
   * enter a comma separated string of integers representing which words to keep
   * to make a query. For instance if you type in "3,5", you'd be selecting the
   * queries "why do I" and "why do I feel so"
   * 
   * The method will then call insertCheck on these subqueries and you can
   * insert the ones you like (called delete insert because it inserts queries
   * by deleting words from the original)
   * 
   * @param query
   * @throws IOException
   */
  public void deleteInsert(String query) throws IOException {
    System.out.println(query);
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String line = r.readLine();
    // If you don't want any of the queries, just type N
    if (line.equals("N")) {
      return;
    }
    String[] toDel = line.split(",");
    String[] brokenUp = query.split(" ");
    for (String s : toDel) {
      int wordNum = Integer.parseInt(s);
      String toInsert = "";
      for (int i = 0; i < wordNum; i++) {
        toInsert += brokenUp[i] + " ";
      }
      // Inserts the queries into the RelatedQueries text file,
      // found at the path below
      insertCheck(toInsert, "data/RQS.txt");
    }
  }

  /**
   * Takes in a path to a CSV generated by a google trends relatedQueries
   * download and calls insertDelete on all of its top searched queries
   * 
   * @param filePath
   *          - Path to the csv file
   */
  public void fromRelatedQueries(String filePath) {
    try {
      CsvReader csv = new CsvReader(filePath);
      String[][] records = csv.getRecords();
      int i = 0;
      for (; i < records.length; i++) {
        if (records[i][0].equals("TOP")) {
          i++;
          break;
        }
      }
      List<String> queries = new ArrayList<>();
      for (; i < records.length; i++) {
        if (records[i][0].equals("") || records[i][0].equals("RISING")) {
          break;
        }
        queries.add(records[i][0]);
      }
      for (String quer : queries) {
        deleteInsert(quer);
      }

    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    } catch (InvalidFormatException e) {
      System.out.println(e.getMessage());
      return;
    }
  }

  // TODO: Make the main that adds things in this file. Make everything private
  // but this function.

  /**
   * Returns queryNum random queries from the database as a list.
   * 
   * @param queryNum
   *          - Number of queries to return
   * @return - The list of the random queries.
   * @throws SQLException
   *           - If the SQL statement returns an error
   */
  public List<QueryResponses> nRandomQrs(int queryNum) throws SQLException {
    return db.nRandomQueries(queryNum);
  }

}
