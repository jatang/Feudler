package edu.brown.cs.termproject.scoring;

import java.util.List;

public class Guess implements Cluster {

  public Guess(List<WordVector> vectors, String originalPhrase) {
    // TODO
  }

  // TODO: Add another constructor that can make a guess from the database
  // entries. Also figure out how to store these in the database.

  @Override
  public boolean contains(List<WordVector> other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void add(List<WordVector> other) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getResponse() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getScore() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double similarity(List<WordVector> other) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public double similarityThreshold() {
    // TODO Auto-generated method stub
    return 0;
  }
}
