package edu.brown.cs.termproject.scoring;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import org.junit.Test;

public class EmbeddedModelTest {

  @Test
  public void testSimple() {
    Word2VecModel model = new EmbeddedModel();
    Optional<Double> result = model.distanceBetween("car", "automobile");
    System.out.println(result.get());
    assertEquals(result.get(), 0.8, 0.001);
  }
}
