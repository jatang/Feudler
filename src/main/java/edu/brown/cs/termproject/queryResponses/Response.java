package edu.brown.cs.termproject.queryResponses;

public class Response {
	public Response(String response, int score) {
		this.response = response;
		this.score = score;
	}
	public String getResponse() {
		return response;
	}
	public int getScore() {
		return score;
	}
	private final String response;
	private final int score;
	
	
}
