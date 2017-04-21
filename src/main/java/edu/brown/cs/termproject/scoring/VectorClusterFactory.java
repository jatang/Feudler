package edu.brown.cs.termproject.scoring;

class VectorClusterFactory implements ClusterFactory<VectorCluster> {

  @Override
  public VectorCluster newInstance(String phrase) {
    return new VectorCluster(phrase);
  }
}
