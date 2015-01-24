package net.geodrop;

/**
 * Entity - quad or or model.
 */
public interface Entity {
  void render(Shader shader);
  float getRot();
}
