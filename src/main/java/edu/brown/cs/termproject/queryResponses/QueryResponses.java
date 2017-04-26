package edu.brown.cs.termproject.queryResponses;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.termproject.scoring.Clustering;
import edu.brown.cs.termproject.scoring.Suggestion;

public class QueryResponses {
  public QueryResponses(String query, Clustering<Suggestion> clusters) {
    this.query = query;
    // Should double check that this correctly copies strings so it doesn't
    // matter if you alter the original list.
    this.clusters = clusters;
  }

  private final String query;
  private final Clustering<Suggestion> clusters;

  public String getQuery() {
    return query;
  }

  public Clustering<Suggestion> getResponses() {
    return clusters;
  }
}
