package net.geodrop;

/**
 * Entity - quad or or model.
 */
public interface Entity {
  void render(Shader shader);
  Folder select();
  void point(float x, float z);
}
