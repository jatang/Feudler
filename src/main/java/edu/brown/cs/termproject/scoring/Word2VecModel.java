package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.lang.AutoCloseable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * This class opens a connection, makes a prepared statement, and caches the
 * data when it reads it in. All it needs to check in the database is what the
 * vector of the input word is. This class is thread-safe, multiple threads can
 * access the data.
 *
 * @author asekula
 */
public class Word2VecModel implements AutoCloseable {

  // vocabulary reference never changes, so multithreaded calls to vocabulary()
  // will not be a problem.
  private ImmutableSet<String> vocabulary;
  private ConcurrentMap<String, WordVector> cache;
  private Connection conn;
  private PreparedStatement statement;

  /**
   * Instantiates a word2vec model using the database at the input path.
   *
   * @param path
   *          the path to the database
   */
  public Word2VecModel(String path) {
    try {
      Class.forName("org.sqlite.JDBC");
      String urlToDb = "jdbc:sqlite:" + path;
      conn = DriverManager.getConnection(urlToDb);
      statement = conn
          .prepareStatement("select vector from embeddings where word='?';");

      // Creates the word vocabulary.
      PreparedStatement allWordsStatement = conn
          .prepareStatement("select word from embeddings;");
      try (ResultSet results = allWordsStatement.executeQuery()) {
        Set<String> vocab = new HashSet<>();
        while (results.next()) {
          vocab.add(results.getString(1));
        }
        vocabulary = ImmutableSet.copyOf(vocab);
      }

      statement.setString(1, "a");
      statement.executeQuery(); // Checks that vector is a field.

    } catch (ClassNotFoundException exception) {
      throw new RuntimeException("Could not find class org.sqlite.JDBC.");
    } catch (SQLException exception) {
      exception.printStackTrace();
      throw new RuntimeException(
          "Non-existent or malformed database at " + path);
    }
  }

  /**
   * Returns the word vector given the input word.
   *
   * @param word
   *          the input word
   * @return the vector, absent if none exists
   */
  public synchronized Optional<WordVector> vectorOf(String word) {
    if (cache.containsKey(word)) {
      return Optional.of(cache.get(word));
    }

    if (!vocabulary.contains(word)) {
      return Optional.absent();
    }

    try {
      statement.setString(1, word);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          WordVector vector = new WordVector(rs.getString(1));
          cache.put(word, vector);
          return Optional.of(vector);
        } else {
          return Optional.absent();
        }
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
      return Optional.absent();
    }
  }

  /**
   * The vocabulary of the model.
   *
   * @return a set of all the words in the model
   */
  public ImmutableSet<String> vocabulary() {
    return vocabulary;
  }

  @Override
  public void close() {
    try {
      statement.close();
      conn.close();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }
}
