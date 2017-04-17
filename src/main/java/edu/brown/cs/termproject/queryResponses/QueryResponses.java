package edu.brown.cs.termproject.queryResponses;

import java.util.ArrayList;
import java.util.List;

public class QueryResponses {
	public QueryResponses(String query, List<Response> responses) {
		this.query = query;
		//Should double check that this correctly copies strings so it doesn't matter if you alter the original list.
		this.responses = new ArrayList<>(responses);
	}
	private final String query;
	private final List<Response> responses;
	
	public String getQuery() {
		return query;
	}
	public List<Response> getResponses() {
		return new ArrayList<>(responses);
	}
}
