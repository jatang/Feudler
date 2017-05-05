package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
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

    // assertFalse(model.vectorOf("Seven").getVector().isPresent());
    assertFalse(model.vectorOf("Seven ").getVector().isPresent());
    // assertFalse(model.vectorOf(" seven").getVector().isPresent());
    assertFalse(model.vectorOf("SEVEN").getVector().isPresent());
    assertFalse(model.vectorOf("grEAT").getVector().isPresent());
    // assertFalse(model.vectorOf("grea").getVector().isPresent());
    // assertFalse(model.vectorOf("reat").getVector().isPresent());
    assertFalse(model.vectorOf("      ").getVector().isPresent());
    assertFalse(model.vectorOf("\n\n").getVector().isPresent());
    assertFalse(model.vectorOf("").getVector().isPresent());

    model.close();
  }

  @Test
  public void testCorrectness() {
    Word2VecModel model = Word2VecModel.model;

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
    // assertEquals(first.similarity(second), 1.0, 0.0001);

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
  }

  @Test
  public void tokenizeStopWordsTest() {
    Word2VecModel model = Word2VecModel.model;

    assertEquals(model.tokenize("the"), Collections.emptyList());
    assertEquals(model.tokenize("the a and"), Collections.emptyList());
    assertEquals(model.tokenize("the big cat"), model.tokenize("big cat"));
    assertEquals(model.tokenize("ThE BAG cAT"), model.tokenize("bag cat"));
    assertEquals(model.tokenize("across the large place"),
        model.tokenize("across large place"));
  }

  @Test
  public void tokenizeWhitespaceAndPunctuationTest() {
    Word2VecModel model = Word2VecModel.model;

    assertEquals(model.tokenize("red?"),
        ImmutableList.of(model.vectorOf("red?")));
    assertEquals(
        model.tokenize(
            "my biggest \t\t\npet\npeeve\t\tis\tredundant\t\nwhitespace!"),
        model.tokenize("my biggest pet peeve is redundant whitespace!"));

    // Checks that this doesn't crash anything.
    model.tokenize("ß∂ƒ©˙ ∆µ∆˙©ƒ∆®¥†˙µ ˜©∫ƒ∂ßƒ∂© ˙∆≤˚ ˙©ƒ∂© ƒç©√˜∫");

    assertEquals(model.tokenize("punc. tuation"),
        ImmutableList.of(model.vectorOf("punc."), model.vectorOf("tuation")));
    assertEquals(model.tokenize("regular input"),
        ImmutableList.of(model.vectorOf("regular"), model.vectorOf("input")));
  }

  @Test
  public void stopwordInclusionExclusionTest() {
    assertTrue(Word2VecModel.model.getStopwords()
        .containsAll(ImmutableSet.of("and", "for", "the", "a", "from")));
    assertFalse(Word2VecModel.model.getStopwords().containsAll(ImmutableSet.of(
        "want", "big", "small", "old", "work", "ate", "new", "year", "good")));
  }

  @Test
  public void emptyTokenizeTest() {
    assertTrue(Word2VecModel.model.tokenize("").isEmpty());
    assertTrue(Word2VecModel.model.tokenize("the").isEmpty());
  }
}
