package edu.brown.cs.termproject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import edu.brown.cs.termproject.networking.Room;
import edu.brown.cs.termproject.networking.ServerSocket;
import edu.brown.cs.termproject.networking.Suggestions;
import edu.brown.cs.termproject.scoring.Suggestion;
import edu.brown.cs.termproject.scoring.Word2VecModel;
import freemarker.template.Configuration;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of the project where execution begins.
 */
public final class Main {

  private String[] args;

  /**
   * The initial method called when execution begins.
   *
   * @param args
   *          An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    Word2VecModel.model.tokenize("the cat");
    runSparkServer(4567);
  }

  private static void runSparkServer(int port) {
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    Spark.webSocket("/connection", ServerSocket.class);
    Spark.get("/", new HomeHandler(), freeMarker);
    Spark.get("/room/:room", new RoomHandler(), freeMarker);

  }

  /**
   * Handle requests to the main page of our website.
   */
  private static class HomeHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      Map<String, String> variables = ImmutableMap.of("roomId", "");
      return new ModelAndView(variables, "room.ftl");
    }
  }

  /**
   * Handle requests to the room page of our website.
   */
  private static class RoomHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {

      String roomId = req.params(":room");

      Map<String, String> variables = ImmutableMap.of("roomId", roomId);
      return new ModelAndView(variables, "room.ftl");
    }
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private static final int INTERNAL_SERVER_ERROR = 500;

  /**
   * A handler to print an Exception as text into the Response.
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(INTERNAL_SERVER_ERROR);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

}
