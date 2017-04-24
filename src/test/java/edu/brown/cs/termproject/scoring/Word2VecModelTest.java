package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

public class Word2VecModelTest {

  @Test
  public void testVocab() {
    Word2VecModel model = new Word2VecModel("data/test_embeddings.sqlite3");
    assertEquals(model.vocabulary(),
        ImmutableSet.of("real", "great", "ok", "bad", "test", "seven"));
    model.close();
  }

  @Test
  public void testVectorOf() {
    Word2VecModel model = new Word2VecModel("data/test_embeddings.sqlite3");
    WordVector result;

    result = model.vectorOf("great").get();
    assertEquals(result, new WordVector("great", "0.0"));
    assertEquals(result.getVector(), ImmutableList.of(2.2, 1.1));

    // Tests another call.
    result = model.vectorOf("great").get();
    assertEquals(result.getVector(), ImmutableList.of(2.2, 1.1));

    result = model.vectorOf("test").get();
    assertEquals(result.getVector(), ImmutableList.of(2.3, 1.4));

    result = model.vectorOf("seven").get();
    assertEquals(result.getVector(), ImmutableList.of(-912.432, -0.000000177));

    // Checks that this doesn't throw an exception.
    model.vectorOf("ok").get();

    assertFalse(model.vectorOf("Seven").isPresent());
    assertFalse(model.vectorOf("Seven ").isPresent());
    assertFalse(model.vectorOf(" seven").isPresent());
    assertFalse(model.vectorOf("SEVEN").isPresent());
    assertFalse(model.vectorOf("grEAT").isPresent());
    assertFalse(model.vectorOf("grea").isPresent());
    assertFalse(model.vectorOf("reat").isPresent());
    assertFalse(model.vectorOf("      ").isPresent());
    assertFalse(model.vectorOf("\n\n").isPresent());
    assertFalse(model.vectorOf("").isPresent());

    model.close();
  }

  @Test
  public void testCorrectness() {
    Word2VecModel model = new Word2VecModel("data/embeddings.sqlite3");

    // Number might need updating after db is fixed.
    assertEquals(model.vocabulary().size(), 789874);

    WordVector first;
    WordVector second;

    first = model.vectorOf("red").get();
    second = model.vectorOf("blue").get();
    assertEquals(first.similarity(second), 0.72251, 0.0001);

    first = model.vectorOf("train").get();
    second = model.vectorOf("car").get();
    assertEquals(first.similarity(second), 0.34025, 0.0001);

    first = model.vectorOf("train").get();
    second = model.vectorOf("trains").get();
    assertEquals(first.similarity(second), 0.80812, 0.0001);

    first = model.vectorOf("dog").get();
    second = model.vectorOf("doggy").get();
    assertEquals(first.similarity(second), 0.61916, 0.0001);

    first = model.vectorOf("king").get();
    second = model.vectorOf("queen").get();
    assertEquals(first.similarity(second), 0.651095, 0.0001);

    first = model.vectorOf("don't").get();
    second = model.vectorOf("stop").get();
    assertEquals(first.similarity(second), 0.15901, 0.0001);

    first = model.vectorOf("pirate").get();
    second = model.vectorOf("leaf").get();
    assertEquals(first.similarity(second), 0.07423, 0.0001);

    first = model.vectorOf("pirate").get();
    second = model.vectorOf("transformation").get();
    assertEquals(first.similarity(second), -0.006338, 0.0001);

    first = model.vectorOf("yes").get();
    second = model.vectorOf("no").get();
    assertEquals(first.similarity(second), 0.392106, 0.0001);

    model.close();
  }
}
