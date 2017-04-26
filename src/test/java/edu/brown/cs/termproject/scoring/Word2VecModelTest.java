package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

public class Word2VecModelTest {

  @Test
  public void testVocab() {
    Word2VecModel model = new Word2VecModel("data/test_embeddings.sqlite3",
        "data/stopwords.txt");
    assertEquals(model.vocabulary(),
        ImmutableSet.of("real", "great", "ok", "bad", "test", "seven"));
    model.close();
  }

  @Test
  public void testVectorOf() {
    Word2VecModel model = new Word2VecModel("data/test_embeddings.sqlite3",
        "data/stopwords.txt");
    WordVector result;

    result = model.vectorOf("great");
    assertEquals(result, new WordVector("great", "0.0"));
    assertEquals(result.getVector().get(), ImmutableList.of(2.2, 1.1));

    // Tests another call.
    result = model.vectorOf("great");
    assertEquals(result.getVector().get(), ImmutableList.of(2.2, 1.1));

    result = model.vectorOf("test");
    assertEquals(result.getVector().get(), ImmutableList.of(2.3, 1.4));

    result = model.vectorOf("seven");
    assertEquals(result.getVector().get(),
        ImmutableList.of(-912.432, -0.000000177));

    // Checks that this doesn't throw an exception.
    model.vectorOf("ok");

    assertFalse(model.vectorOf("Seven").getVector().isPresent());
    assertFalse(model.vectorOf("Seven ").getVector().isPresent());
    assertFalse(model.vectorOf(" seven").getVector().isPresent());
    assertFalse(model.vectorOf("SEVEN").getVector().isPresent());
    assertFalse(model.vectorOf("grEAT").getVector().isPresent());
    assertFalse(model.vectorOf("grea").getVector().isPresent());
    assertFalse(model.vectorOf("reat").getVector().isPresent());
    assertFalse(model.vectorOf("      ").getVector().isPresent());
    assertFalse(model.vectorOf("\n\n").getVector().isPresent());
    assertFalse(model.vectorOf("").getVector().isPresent());

    model.close();
  }

  @Test
  public void testCorrectness() {
    Word2VecModel model = new Word2VecModel("data/embeddings.sqlite3",
        "data/stopwords.txt");

    // Number might need updating after db is fixed.
    assertEquals(model.vocabulary().size(), 714265);

    WordVector first;
    WordVector second;

    first = model.vectorOf("red");
    second = model.vectorOf("blue");
    assertEquals(first.similarity(second), 0.72251, 0.0001);

    first = model.vectorOf("train");
    second = model.vectorOf("car");
    assertEquals(first.similarity(second), 0.34025, 0.0001);

    first = model.vectorOf("train");
    second = model.vectorOf("trains");
    assertEquals(first.similarity(second), 0.80812, 0.0001);

    first = model.vectorOf("dog");
    second = model.vectorOf("doggy");
    assertEquals(first.similarity(second), 0.61916, 0.0001);

    first = model.vectorOf("king");
    second = model.vectorOf("queen");
    assertEquals(first.similarity(second), 0.651095, 0.0001);

    first = model.vectorOf("don't");
    second = model.vectorOf("stop");
    assertEquals(first.similarity(second), 0.15901, 0.0001);

    first = model.vectorOf("pirate");
    second = model.vectorOf("leaf");
    assertEquals(first.similarity(second), 0.07423, 0.0001);

    first = model.vectorOf("pirate");
    second = model.vectorOf("transformation");
    assertEquals(first.similarity(second), -0.006338, 0.0001);

    first = model.vectorOf("yes");
    second = model.vectorOf("no");
    assertEquals(first.similarity(second), 0.392106, 0.0001);

    model.close();
  }
}
