package net.geodrop;

import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * GLES Shader class.  
 */
public class Shader {
  /**
   * Enumeration of supported shader types. 
   */
  public static enum Type {
    VERT(GLES20.GL_VERTEX_SHADER),
    FRAG(GLES20.GL_FRAGMENT_SHADER);

    /**
     * Shader type. 
     */
    private final int type;

    /**
     * Creates a new shader type. 
     *  
     * @param type GLES20 Shader type.
     */
    private Type(int type) {
      this.type = type;
    }

    /**
     * Returns the shader type. 
     */
    public int getType() {
      return type;
    }
  }

  /**
   * Name of the shader program. 
   */
  final String name;

  /**
   * List of shaders. 
   */
  final List<Integer> shaders = new ArrayList<Integer>();
  
  /**
   * Shader program.
   */
  int prog;
  
  /**
   * Creates an empty shader program.
   */
  public Shader(String name) {
    this.name = name;
    this.prog = GLES20.glCreateProgram();
  }

  /**
   * Loads a shader source.
   */
  public void load(Type type, InputStream source) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(source));
    StringBuilder sb = new StringBuilder();
    
    for (String line = ""; line != null; line = reader.readLine()) {
      sb.append(line).append('\n');
    }    
    
    int shader = GLES20.glCreateShader(type.getType());
    shaders.add(shader);
    
    GLES20.glShaderSource(shader, sb.toString());
    GLES20.glCompileShader(shader);

  }

  /**
   * Links the shader. 
   */
  public void link() {
  
  }

  /**
   * Frees the shader. 
   */
  public void close() {
    for (int shader : shaders) {
      GLES20.glDetachShader(prog, shader);
      GLES20.glDeleteShader(shader);
    }
    
    GLES20.glDeleteProgram(prog);
  }
}
