import java.sql.SQLException;
import java.util.List;

import edu.brown.cs.termproject.networking.Suggestions;
import edu.brown.cs.termproject.queryGenerator.qGenerator;
import edu.brown.cs.termproject.queryResponses.QueryResponses;
import edu.brown.cs.termproject.queryResponses.Response;


//I just use this when I want to try things out in a main.
public class Fiddle {
	public static final void main(String[] args) throws SQLException {
		List<QueryResponses> qr1 = new qGenerator().nRandomQrs(1);
		System.out.println(qr1.get(0).getQuery());
		for (Response res : qr1.get(0).getResponses()) {
			System.out.println(res.getScore());
			System.out.println(res.getResponse());
		}
		
	}
}
