package net.geodrop;

import com.google.vrtoolkit.cardboard.Eye;

/**
 * Entity - quad or or model.
 */
public interface Entity {
  void render(Shader shader, Shader unused, Eye eye, float[] model);
  Folder select();
  void point(float x, float z);
}
