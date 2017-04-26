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
  private final static String dbPath = "data/gFued.sqlite3";
  private static DBConnector db;

  public qGenerator() throws SQLException {
    db = new DBConnector(dbPath);
  }

  public boolean insertQuery(String query) {

    Clustering<Suggestion> suggs = Suggestions
        .getUniqueGoogleSuggestionEndings(query);

    if (suggs.size() < 4) {
      return false;
    }

    QueryResponses qr = new QueryResponses(query, suggs);

    try {
      db.insertQuery(qr);
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean insertCheck(String query, String writeTo) throws IOException {

    // TODO: Make like the other one.

    if (db.containsQuery(query)) {
      return false;
    }
    List<String> suggs = Suggestions.getGoogleSuggestions(query);
    List<String> filteredSuggs = new ArrayList<>();
    for (String sug : suggs) {
      if (sug.startsWith(query.toLowerCase())) {
        filteredSuggs.add(sug);
      }
    }
    suggs = filteredSuggs;
    if (suggs.size() < 4) {
      System.out.println(query);
      System.out.println(false);
      return false;
    }
    for (String s : suggs) {
      System.out.println(s);
    }
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String confirm = r.readLine();
    if (confirm.equals("Y")) {
      boolean output = insertQuery(query);
      System.out.println(output);
      if (output) {
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

  public void insertFile(String filePath, String type, boolean check)
      throws IOException {
    File f = new File(filePath);
    BufferedReader fRead = new BufferedReader(new FileReader(f));
    Consumer<String> con = null;
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

  public void fromAnimal(String animal, boolean check) {
    animal = animal + " ";
    String query1 = "how does ";
    String toAdd;
    Character[] vowels = { 'a', 'e', 'i', 'o', 'u' };
    if (Arrays.asList(vowels).contains(animal.charAt(0))) {
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

  public void deleteInsert(String query) throws IOException {
    System.out.println(query);
    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String line = r.readLine();
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
      insertCheck(toInsert, "data/RQS.txt");
    }
  }

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

  public List<QueryResponses> nRandomQrs(int queryNum) throws SQLException {
    return db.nRandomQueries(queryNum);
  }

}
