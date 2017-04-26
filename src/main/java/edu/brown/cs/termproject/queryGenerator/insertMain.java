package edu.brown.cs.termproject.queryGenerator;

import java.io.IOException;
import java.sql.SQLException;

public class insertMain {

	public static void main(String[] args) throws SQLException, IOException {
		qGenerator qGen = new qGenerator();
		//qGen.insertFile("data/animals.csv", "animal", true);
		//qGen.insertFile("data/actors.csv", "actor", true);
		//qGen.insertFile("data/drinks.csv", "drink", true);
		//qGen.insertFile("data/foods.csv", "food", true);
		//qGen.insertFile("data/animalQs.txt", "checked", false);
		//qGen.insertFile("data/actorQs.txt", "checked", false);
		qGen.insertFile("data/RQS.txt", "checked", false);
		//qGen.fromRelatedQueries("data/poppedUp.csv");
//		qGen.fromRelatedQueries("data/whyDoSome.csv");
//		qGen.fromRelatedQueries("data/whyIsMy.csv");
//		qGen.fromRelatedQueries("data/isItPossible.csv");
//		qGen.fromRelatedQueries("data/iWantTo.csv");

	}

}
