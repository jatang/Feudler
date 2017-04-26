package edu.brown.cs.termproject.scoring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.lang.AutoCloseable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

  public static final Word2VecModel model = new Word2VecModel("", "");

  // vocabulary reference never changes, so multithreaded calls to vocabulary()
  // will not be a problem.
  private ImmutableSet<String> vocabulary;
  private ConcurrentMap<String, WordVector> cache;
  private Connection conn;
  private PreparedStatement statement;
  private ImmutableList<String> stopwords;

  /**
   * Instantiates a word2vec model using the database at the input path.
   *
   * @param path
   *          the path to the database
   */
  public Word2VecModel(String dbPath, String stopwordPath) {
    cache = new ConcurrentHashMap<>();

    try {
      Class.forName("org.sqlite.JDBC");
      String urlToDb = "jdbc:sqlite:" + dbPath;
      conn = DriverManager.getConnection(urlToDb);
      statement = conn
          .prepareStatement("select vector from embeddings where word=?;");

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
      statement.executeQuery(); // Checks that word and vector are fields.

      // TODO: Read in stopwords from file.

    } catch (ClassNotFoundException exception) {
      throw new RuntimeException("Could not find class org.sqlite.JDBC.");
    } catch (SQLException exception) {
      exception.printStackTrace();
      throw new RuntimeException(
          "Non-existent or malformed database at " + dbPath);
    }
  }

  /**
   * Returns the word vector given the input word.
   *
   * @param word
   *          the input word
   * @return the vector, absent if none exists
   */
  public synchronized WordVector vectorOf(String word) {
    if (cache.containsKey(word)) {
      return cache.get(word);
    }

    if (!vocabulary.contains(word)) {
      return new WordVector(word);
    }

    try {
      statement.setString(1, word);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          WordVector vector = new WordVector(word, rs.getString(1));
          cache.put(word, vector);
          return vector;
        } else {
          return new WordVector(word);
        }
      }
    } catch (SQLException exception) {
      exception.printStackTrace();
      return new WordVector(word);
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

  /**
   * Returns a tokenized immutable list from the input string phrase. Converts
   * to lowercase, splits on whitespace, and removes stopwords.
   *
   * @param phrase
   *          the phrase as a string
   * @return an immutable list of the tokens
   */
  public List<WordVector> tokenize(String phrase) {
    String lower = phrase.toLowerCase();
    String[] parts = lower.split("\\s+");
    List<WordVector> nonStopwordEmbeddings = new ArrayList<>();
    for (String part : parts) {
      if (!stopwords.contains(part)) {
        nonStopwordEmbeddings.add(vectorOf(part));
      }
    }
    return ImmutableList.copyOf(nonStopwordEmbeddings);
  }
}
