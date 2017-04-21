package edu.brown.cs.termproject.scoring;

import java.util.List;
import java.util.Set;

public class VectorClustering extends Clustering<VectorCluster> {

  public VectorClustering(List<String> phrases) {
    super(phrases, new VectorClusterFactory());
  }

  public VectorClustering(Set<VectorCluster> clusters) {
    super(clusters, new VectorClusterFactory());
  }
}
