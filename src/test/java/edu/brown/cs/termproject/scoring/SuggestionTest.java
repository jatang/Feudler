package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class SuggestionTest {

  private Word2VecModel model = Word2VecModel.model;

  @Test
  public void testSimpleGetters() {
    // Really don't need these tests, putting them here as a sanity check.
    assertEquals(new Suggestion(ImmutableList.of(), "", 9).getResponse(), "");
    assertEquals(new Suggestion(ImmutableList.of(), "test", 9).getResponse(),
        "test");
    assertEquals(
        new Suggestion(ImmutableList.of(), "Some string.", 2).getResponse(),
        "Some string.");
    assertEquals(new Suggestion(ImmutableList.of(), ".", 2).getScore(), 2);
    assertEquals(new Suggestion(ImmutableList.of(), "sasdfe.", 5).getScore(),
        5);
  }

  @Test
  public void similarityTest() {
    assertEquals(new Suggestion(model.tokenize("testing"), "", 1)
        .similarity(model.tokenize("testing")), 1.0, 0.00001);
    assertEquals(new Suggestion(model.tokenize("testing the a"), "", 1)
        .similarity(model.tokenize("testing for and")), 1.0, 0.00001);

    // assertEquals(new Suggestion(model.tokenize("test something"), "", 1)
    // .similarity(model.tokenize("testing")), ((1.0 * 3) + 0.0601) / 4,
    // 0.001);

    // assertEquals(new Suggestion(model.tokenize("a big plane"), "", 1)
    // .similarity(model.tokenize("a small plane")), (0.4958 + 1.0) / 2,
    // 0.001);
    // assertEquals(new Suggestion(model.tokenize("the car moved"), "", 1)
    // .similarity(model.tokenize("the automobile drove away")), 0.4686,
    // 0.001);
  }

  @Test
  public void addDoesNothingTest() {
    Suggestion sug;
    List<WordVector> old;

    sug = new Suggestion(model.tokenize("the big airplane"), "the big airplane",
        1);
    old = ImmutableList.copyOf(sug.getVectors());
    sug.add(model.tokenize("the strange airplane"));
    assertEquals(old, sug.getVectors());

    sug = new Suggestion(model.tokenize("what a lovely night"),
        "what a lovely night", 1);
    old = ImmutableList.copyOf(sug.getVectors());
    sug.add(model.tokenize("what a waste of a lovely night"));
    sug.add(model.tokenize("what a lively night"));
    sug.add(model.tokenize("what a lovely night"));
    assertEquals(old, sug.getVectors());
  }

  @Test
  public void badTest() {
    Suggestion su = new Suggestion(model.tokenize("get married"), "", 0);
    Suggestion tu = new Suggestion(model.tokenize("get a passport"), "a", 0);
    assertTrue(su.similarity(tu) < su.similarityThreshold());
  }

  @Test
  public void anotherBadTest() {
    // Suggestion su = new Suggestion(model.tokenize("see"), "", 0);
    // Suggestion tu = new Suggestion(model.tokenize("find my iphone"), "a", 0);
    // System.out.println(su.similarity(tu));
  }
}
