package edu.brown.cs.termproject.scoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NearestNeighborsAdder {

  /**
   * Inserts the 10 nearest neighbors for each word in the wiki-100k.txt file.
   *
   * @param args
   *          not used
   */
  public static void main(String[] args) {
    try {
      Class.forName("org.sqlite.JDBC");

      // Connection conn =
      // DriverManager.getConnection("jdbc:sqlite:data/similar_words.sqlite3");
      // PreparedStatement statement = conn
      // .prepareStatement("insert into similar values (?, ?);");

      // Reads vocab from file.
      Set<String> vocab = new HashSet<>();
      Files.lines(Paths.get("data/wiki-100k.txt")).forEach(vocab::add);
      System.out.println("Read needed vocab.");

      Connection embeddingsConn = DriverManager
          .getConnection("jdbc:sqlite:data/embeddings.sqlite3");
      PreparedStatement allWordsStatement = embeddingsConn
          .prepareStatement("select * from embeddings;");
      Map<String, WordVector> allWordVectors = new HashMap<>();
      try (ResultSet results = allWordsStatement.executeQuery()) {
        while (results.next()) {
          // constructor is (word, vectorString)
          String word = results.getString(1);
          allWordVectors.put(word, new WordVector(word, results.getString(2)));
        }
      }
      System.out.println("Read all wordvectors.");

      // Creates a list of cartesians by casting the wordVectors.
      List<Cartesian<Double>> cartesians = new ArrayList<>();
      for (WordVector v : allWordVectors.values()) {
        if (v.getVector().isPresent()) {
          cartesians.add((Cartesian<Double>) v);
        }
      }
      System.out.println("Made casted list.");

      // Instantiates the kdTree.
      KdTree<Double> tree = new KdTree<Double>(cartesians);
      System.out.println("Made tree.");

      11:06pm started
      
      System.out.println(tree.nearestNeighbors(10,
          (Cartesian<Double>) allWordVectors.get("tree")));

    } catch (ClassNotFoundException exception) {
      throw new RuntimeException("Could not find class org.sqlite.JDBC.");
    } catch (SQLException exception) {
      exception.printStackTrace();
      throw new RuntimeException("Non-existent or malformed database.");
    } catch (IOException exception) {
      throw new RuntimeException("Unable to read file.");
    }
  }
}
