package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableList;

//import java.util.stream.Collectors;
import org.junit.Test;

public class ClusteringTest {

  // Currently testing as is being used in the code. (i.e. add is not being
  // called outside the constructor).

  @Test
  public void basicTest() {
    // "another" is a stopword
    // Clustering<Suggestion> clustering = Clustering.newSuggestionClustering(
    // ImmutableList.of("test", "another test"), Word2VecModel.model);
    // // assertEquals(clustering.asList().stream()
    // .map((Suggestion s) -> s.getResponse()).collect(Collectors.toList()),
    // ImmutableList.of("test"));
  }

  @Test
  public void sizeTest() {
    // assertEquals(Clustering.newSuggestionClustering(
    // ImmutableList.of("test", "another test"), Word2VecModel.model).size(),
    // 1);
    assertEquals(Clustering
        .newSuggestionClustering(ImmutableList.of(), Word2VecModel.model)
        .size(), 0);
    assertEquals(Clustering.newSuggestionClustering(ImmutableList.of("", "the"),
        Word2VecModel.model).size(), 0);
    assertEquals(Clustering.newSuggestionClustering(
        ImmutableList.of("faraday", "yemen", "opaque", "discover", "yell"),
        Word2VecModel.model).size(), 5);
    assertEquals(Clustering.newSuggestionClustering(ImmutableList.of("faraday",
        "yemen", "opaque", "discover", "yell", "faraday", "yemen", "shout"),
        Word2VecModel.model).size(), 5);
  }

  @Test
  public void clusterOfTest() {
    assertEquals(
        Clustering
            .newSuggestionClustering(ImmutableList.of("test", "another test"),
                Word2VecModel.model)
            .clusterOf("and test").get().getResponse(),
        "test");

    assertFalse(Clustering
        .newSuggestionClustering(ImmutableList.of(), Word2VecModel.model)
        .clusterOf("some phrase").isPresent());

    assertFalse(Clustering.newSuggestionClustering(ImmutableList.of("", "the"),
        Word2VecModel.model).clusterOf("a long phrase").isPresent());

    assertEquals(
        Clustering
            .newSuggestionClustering(ImmutableList.of("faraday", "yemen",
                "opaque", "discover", "yell"), Word2VecModel.model)
            .clusterOf("yemen").get().getResponse(),
        "yemen");

    assertEquals(
        Clustering
            .newSuggestionClustering(ImmutableList.of("faraday", "yemen",
                "opaque", "discover", "yell"), Word2VecModel.model)
            .clusterOf("find").get().getResponse(),
        "discover");

    assertEquals(Clustering
        .newSuggestionClustering(ImmutableList.of("faraday", "yemen", "opaque",
            "discover", "yell", "shout", "yemen"), Word2VecModel.model)
        .clusterOf("shout").get().getResponse(), "yell");

    assertEquals(Clustering
        .newSuggestionClustering(ImmutableList.of("big", "a big ball",
            "a small ball", "a very small ball"), Word2VecModel.model)
        .clusterOf("bigger").get().getResponse(), "big");

    // assertFalse(Clustering
    // .newSuggestionClustering(ImmutableList.of("big", "a big ball",
    // "a small ball", "a very small ball"), Word2VecModel.model)
    // .clusterOf("large").isPresent());

    assertFalse(Clustering
        .newSuggestionClustering(ImmutableList.of("big", "a big ball",
            "a small ball", "a very small ball"), Word2VecModel.model)
        .clusterOf("grand").isPresent());

    assertEquals(
        Clustering
            .newSuggestionClustering(ImmutableList.of("big", "a big ball",
                "a small ball", "a very small ball"), Word2VecModel.model)
            .clusterOf("a small tiny ball").get().getResponse(),
        "a small ball");
  }

  @Test
  public void testEmbeddingsTest() {
    Word2VecModel model = new Word2VecModel("data/test_embeddings.sqlite3",
        "data/stopwords.txt");
    Clustering<Suggestion> clustering;
    clustering = Clustering.newSuggestionClustering(ImmutableList.of("faraday",
        "yemen", "opaque", "discover", "yell", "faraday", "yemen", "shout"),
        model);
    assertEquals(clustering.size(), 6);
  }

  @Test
  public void someTest() {
    // assertEquals(
    // Clustering
    // .newSuggestionClustering(
    // ImmutableList.of("alcohol", "water", "soda", "tequila", "beer",
    // "rum", "cough syrup", "milk", "vodka", "mentos"),
    // Word2VecModel.model)
    // .asList().stream().map((Suggestion s) -> s.getResponse())
    // .collect(Collectors.toList()),
    // ImmutableList.of("alcohol", "water", "soda", "tequila", "beer",
    // "cough syrup", "milk", "mentos"));
  }
}
