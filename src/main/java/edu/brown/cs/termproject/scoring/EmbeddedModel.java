package edu.brown.cs.termproject.scoring;

import com.google.common.base.Optional;
import java.util.Set;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * Uses a word2vec model by embedding python and using the gensim library. Note
 * that instantiating one of these may take up to four minutes.
 *
 * @author asekula
 */
public class EmbeddedModel implements Word2VecModel {

  private PythonInterpreter interpreter;
  private PyObject model;

  /**
   * Initializes a model using embedded python, with the file located at
   * data/GoogleNews-vectors-negative300.bin. Overload this constructor to allow
   * freedom to choose the file name (functionality not here because it's not
   * necessary).
   */
  public EmbeddedModel() {
    interpreter = new PythonInterpreter();
    interpreter.exec("from gensim.models.word2vec import Word2Vec");
    model = interpreter.eval(
        "Word2Vec.load_word2vec_format('GoogleNews-vectors-negative300.bin', binary=True)");
  }

  @Override
  public Optional<Double> distanceBetween(String word1, String word2) {
    PyObject result = model.invoke("similarity", interpreter.eval(word1),
        interpreter.eval(word2));
    return Optional.of(Double.parseDouble(result.toString()));
  }

  @Override
  public Optional<WordVector> vectorOf(String word) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<String> vocabulary() {
    // TODO Auto-generated method stub
    return null;
  }
}
