package edu.brown.cs.termproject.queryGenerator;

import java.io.IOException;
import java.sql.SQLException;

public class insertMain {

	public static void main(String[] args) throws SQLException, IOException {
		qGenerator qGen = new qGenerator();
		qGen.insertFile("data/animals.csv", "animal", true);
		//qGen.insertFile("data/actors.csv", "actor", false);
		//qGen.insertFile("data/drinks.csv", "drink", false);
		//qGen.fromRelatedQueries("data/howDoI.csv");

	}

}
