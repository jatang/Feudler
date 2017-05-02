package edu.brown.cs.termproject.queryGenerator;

import java.io.IOException;
import java.sql.SQLException;

public class insertMain {

  public static void main(String[] args) throws SQLException, IOException {
    qGenerator qGen = new qGenerator();
    qGen.insertFile("data/RQS.txt", "checked", false);
    qGen.insertFile("data/actorQs.txt", "checked", false);
    qGen.insertFile("data/animalQs.txt", "checked", false);
    qGen.insertFile("data/foodQs.txt", "checked", false);
    qGen.insertFile("data/drinkQs.txt", "checked", false);
  }
}
