package edu.brown.cs.termproject.scoring;

interface Cluster {

  public boolean contains(String phrase);

  public void add(String phrase);
}
