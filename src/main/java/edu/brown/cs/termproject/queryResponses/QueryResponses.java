package edu.brown.cs.termproject.queryResponses;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.termproject.scoring.Clustering;
import edu.brown.cs.termproject.scoring.Suggestion;

public class QueryResponses {
  public QueryResponses(int id, String query, Clustering<Suggestion> clusters) {
    this.query = query;
    // Should double check that this correctly copies strings so it doesn't
    // matter if you alter the original list.
    this.clusters = clusters;
    this.id = id;
  }

  private final String query;
  private final Clustering<Suggestion> clusters;
  private final int id;

  public String getQuery() {
    return query;
  }

  public Clustering<Suggestion> getResponses() {
    return clusters;
  }

  public int getId() {
    return id;
  }
}
