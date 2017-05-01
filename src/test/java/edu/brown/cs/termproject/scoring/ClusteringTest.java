package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.stream.Collectors;
import org.junit.Test;

public class ClusteringTest {

  private Word2VecModel m = Word2VecModel.model;

  @Test
  public void basicTest() {
    // "another" is a stopword
    Clustering<Suggestion> c = Clustering
        .newSuggestionClustering(ImmutableList.of("test", "another test"), m);
    assertEquals(c.asList().stream().map((Suggestion s) -> s.getResponse())
        .collect(Collectors.toList()), ImmutableList.of("test"));
  }

}
