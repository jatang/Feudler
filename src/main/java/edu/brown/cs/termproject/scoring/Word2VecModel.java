package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.lang.AutoCloseable;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  public static final Word2VecModel model = new Word2VecModel(
      "data/embeddings.sqlite3", "data/stopwords.txt");

  // vocabulary reference never changes, so concurrent calls to vocabulary()
  // will not be a problem.
  private ImmutableSet<String> vocabulary;
  private ConcurrentMap<String, WordVector> cache;
  private Connection embeddingConn;
  private PreparedStatement embeddingStatement;
  private ImmutableSet<String> stopwords;
  private ImmutableSet<String> naughtyWords;

  /**
   * Overloaded constructor for old code.
   *
   * @param dbPath
   *          the path to the embeddings db
   * @param stopwordPath
   *          the path to the stopwords file
   */
  public Word2VecModel(String dbPath, String stopwordPath) {
    this(dbPath, stopwordPath, "data/naughty_words.txt");
  }

  /**
   * Instantiates a word2vec model using the database at the input path.
   *
   * @param dbPath
   *          the path to the database
   * @param stopwordPath
   *          the path to the stopwords file
   * @param naughtyWordPath
   *          the path to the naughty words file
   */
  public Word2VecModel(String dbPath, String stopwordPath,
      String naughtyWordPath) {
    cache = new ConcurrentHashMap<>();

    try {
      Class.forName("org.sqlite.JDBC");
      String urlToDb = "jdbc:sqlite:" + dbPath;
      embeddingConn = DriverManager.getConnection(urlToDb);
      embeddingStatement = embeddingConn
          .prepareStatement("select vector from embeddings where word=?;");

      // Creates the word vocabulary.
      PreparedStatement allWordsStatement = embeddingConn
          .prepareStatement("select word from embeddings;");
      try (ResultSet results = allWordsStatement.executeQuery()) {
        Set<String> vocab = new HashSet<>();
        while (results.next()) {
          vocab.add(results.getString(1));
        }
        vocabulary = ImmutableSet.copyOf(vocab);
      }

      embeddingStatement.setString(1, "a");
      embeddingStatement.executeQuery(); // Checks that word/vector are fields.

      // Reads stopwords from file.
      Set<String> temporaryStopwords = new HashSet<>();
      Files.lines(Paths.get(stopwordPath)).forEach(temporaryStopwords::add);
      stopwords = ImmutableSet.copyOf(temporaryStopwords);

      // Reads naughty words from file.
      Set<String> temporaryNaughtyWords = new HashSet<>();
      Files.lines(Paths.get(naughtyWordPath))
          .forEach(temporaryNaughtyWords::add);
      naughtyWords = ImmutableSet.copyOf(temporaryNaughtyWords);

    } catch (ClassNotFoundException exception) {
      throw new RuntimeException("Could not find class org.sqlite.JDBC.");
    } catch (SQLException exception) {
      exception.printStackTrace();
      throw new RuntimeException(
          "Non-existent or malformed database at " + dbPath);
    } catch (IOException exception) {
      throw new RuntimeException(
          "Unable to read stopword file at " + stopwordPath);
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
      Optional<String> fixed = LedCorrector.fix(vocabulary, word);
      if (fixed.isPresent()) {
        assert (vocabulary.contains(fixed.get()));
        return vectorOf(fixed.get());
      }

      // else give up and return a wordvector of just the word
      return new WordVector(word);
    }

    try {
      embeddingStatement.setString(1, word);
      try (ResultSet rs = embeddingStatement.executeQuery()) {
        if (rs.next()) {
          WordVector vector = new WordVector(word, rs.getString(1));
          cache.put(word, vector);
          rs.close();
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
      embeddingStatement.close();
      embeddingConn.close();
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
      if (!stopwords.contains(part) && !part.isEmpty()) {
        nonStopwordEmbeddings.add(vectorOf(part));
      }
    }
    return ImmutableList.copyOf(nonStopwordEmbeddings);
  }

  /**
   * Gets the stopwords of the model.
   * 
   * @return an immutable set of words
   */
  public ImmutableSet<String> getStopwords() {
    return stopwords;
  }

  /**
   * Gets the model's naughty words. Used in meta mode, to prevent storing dirty
   * answers.
   *
   * @return a set of naughty words
   */
  public ImmutableSet<String> getNaughtyWords() {
    return naughtyWords;
  }
}
