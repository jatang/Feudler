package edu.brown.cs.termproject.networking;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import edu.brown.cs.termproject.Main;
import edu.brown.cs.termproject.scoring.Clustering;
import edu.brown.cs.termproject.scoring.Suggestion;
import edu.brown.cs.termproject.scoring.Word2VecModel;

/**
 * A class representing methods of getting suggestions.
 */
public class Suggestions {

  private static final Gson GSON = new Gson();
  private static final String GOOGLE_SUGGESTIONS = "http://suggestqueries.google.com/complete/search";

  /**
   * A getter for suggestions from Google.
   *
   * @param query
   *          A String to query autocorrection suggestions for.
   * @return Returns a List of String representing all the suggestions for
   *         query.
   */
  public static List<String> getGoogleSuggestions(String query) {
    if (query == null || query.equals("")) {
      return Collections.emptyList();
    }

    JsonArray jsonSuggestions = null;
    URIBuilder builder;
    try {
      builder = new URIBuilder(GOOGLE_SUGGESTIONS);
      builder.setParameter("hl", "en");
      builder.setParameter("q", query);
      builder.setParameter("jsonp", "");
      builder.setParameter("client", "youtube");

      HttpGet request = new HttpGet(builder.build());
      HttpClient client = HttpClients.createDefault();
      HttpResponse response = client.execute(request);

      String readJsonp = EntityUtils.toString(response.getEntity());
      String readJson = readJsonp.substring(readJsonp.indexOf("(") + 1,
          readJsonp.lastIndexOf(")"));

      jsonSuggestions = GSON.fromJson(readJson, JsonArray.class).get(1)
          .getAsJsonArray();

    } catch (URISyntaxException e) {
      return Collections.emptyList();
    } catch (ClientProtocolException e) {
      return Collections.emptyList();
    } catch (IOException e) {
      return Collections.emptyList();
    }

    List<String> res = new ArrayList<>();
    jsonSuggestions.forEach(
        (element) -> res.add(element.getAsJsonArray().get(0).getAsString()));

    return res;
  }

  /**
   * A getter for unique suggestions from Google.
   *
   * @param query
   *          A String to query autocorrection suggestions for.
   * @return Returns a List of unique String representing all the suggestions
   *         for query starting with query.
   */
  private static List<String> getUniqueGoogleSuggestions(String query) {
    List<String> suggestions = getGoogleSuggestions(query);
    List<String> updatedSuggestions = new ArrayList<>();

    for (String suggestion : suggestions) {
      if (suggestion.startsWith(query.toLowerCase())) {
        updatedSuggestions.add(suggestion);
      }
    }

    // Still need to remove similar queries
    // Ex. (The dog): The dog barks, The dog barks a lot
    // But not (My dog likes ): the butter, the watermelon
    return updatedSuggestions;
  }

  /**
   * A getter for unique endings of suggestions from Google.
   *
   * @param query
   *          A String to query autocorrection suggestions for.
   * @return Returns a List of unique String representing all the endings of
   *         suggestions for query. Includes the word / letters in each ending
   *         if there is not a space at the end of the query.
   */
  public static Clustering<Suggestion> getUniqueGoogleSuggestionEndings(
      String query) {
    List<String> suggestions = getUniqueGoogleSuggestions(query);

    int i = query.lastIndexOf(' ');
    suggestions.forEach((s) -> s.substring(i + 1, s.length()));

    return Clustering.newSuggestionClustering(suggestions, Word2VecModel.model);
  }

}
