package edu.brown.cs.termproject.queryGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import edu.brown.cs.termproject.database.DBConnector;
import edu.brown.cs.termproject.networking.Suggestions;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.queryResponses.Response;

public class qGenerator {
 private final static String dbPath = "data/gFued.sqlite3";
 private static DBConnector db; 
	
 public qGenerator() throws SQLException {
	db = new DBConnector(dbPath);
 }
	public void fromAnimal(String animal) {
		animal = animal + " ";
	   String query1 = "how does ";
	   String toAdd;
	Character[] vowels = {'a', 'e', 'i', 'o', 'u'};
	   if (Arrays.asList(vowels).contains(animal.charAt(0))) {
             toAdd = "an ";
             query1 += toAdd;
           }
            else {
            toAdd = "a ";
            query1 += toAdd;
            }
       query1 += animal; 
       
       String query2 = "why does " + toAdd + animal;
       String query3 = "is " + toAdd + animal;
       String query4 = "how can " + toAdd + animal;
       
       String[] queries = {query1, query2, query3, query4};
       for (String query : queries) {
    	   List<String> suggs = Suggestions.getGoogleSuggestions(query);
    	   List<Response> reses = new ArrayList<Response>();
    	   List<String> filteredSuggs = new ArrayList<>();
    	   for (String sug : suggs) {
    		   if (sug.startsWith(query)) {
    			   filteredSuggs.add(sug);
    		   }
    	   }
    	   suggs = filteredSuggs;
    	   if (suggs.size() < 6) {
    		   continue;
    	   }
    	   for (int i = 0; i < suggs.size(); i++) {
    		   reses.add(new Response(suggs.get(i).substring(query.length()), i+1));
    	   }
    	   QueryResponses qr = new QueryResponses(query, reses);
    	   try {
			db.insertQuery(qr);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			continue;
		}
       }
	}
	
	public List<QueryResponses> nRandomQrs(int queryNum) throws SQLException {
		return db.nRandomQueries(queryNum);
	}
	
	
}
