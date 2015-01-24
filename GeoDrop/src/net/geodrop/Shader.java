package net.geodrop;

import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  final List<Integer> shaders = new ArrayList<>();

  /**
   * Mapping of uniforms. 
   */
  final Map<String, Integer> uniforms = new HashMap<>();

  /**
   * Mapping of attributes. 
   */
  final Map<String, Integer> attributes = new HashMap<>();
  
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
    
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
    if (compileStatus[0] == GLES20.GL_FALSE) {
      throw new IllegalArgumentException(GLES20.glGetShaderInfoLog(shader));
    }
  }

  /**
   * Links the shader. 
   */
  public void link() {
    int maxLength;
    
    for (int shader : shaders) {
      GLES20.glAttachShader(prog, shader);
    }
    GLES20.glLinkProgram(prog);
    
    final int[] temp = new int[1];
    GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, temp, 0);
    if (temp[0] == GLES20.GL_FALSE) {
      throw new IllegalArgumentException(GLES20.glGetProgramInfoLog(prog));
    }

    GLES20.glGetProgramiv(prog, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, temp, 0);
    maxLength = temp[0];    
    GLES20.glGetProgramiv(prog, GLES20.GL_ACTIVE_UNIFORMS, temp, 0);
    for (int i = 0; i < temp[0]; ++i) {
      final byte[] name = new byte[20];
      final int[] type = new int[1];
      final int[] size = new int[1];
      final int[] length = new int[1];
          
      GLES20.glGetActiveUniform(prog, i, maxLength, length, 0, size, 0, type, 0, name, 0);
      final String unifName = new String(name).substring(0, length[0]);
      uniforms.put(unifName, GLES20.glGetUniformLocation(prog, unifName));
    }

    GLES20.glGetProgramiv(prog, GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, temp, 0);
     maxLength = temp[0];
    GLES20.glGetProgramiv(prog, GLES20.GL_ACTIVE_ATTRIBUTES, temp, 0);
    for (int i = 0; i < temp[0]; ++i) {
      final byte[] name = new byte[20];
      final int[] type = new int[1];
      final int[] size = new int[1];
      final int[] length = new int[1];

      GLES20.glGetActiveAttrib(prog, i, maxLength, length, 0, size, 0, type, 0, name, 0);
      final String attribName = new String(name).substring(0, length[0]);
      attributes.put(attribName, GLES20.glGetAttribLocation(prog, attribName));
    }
  }

  /**
   * Frees the shader. 
   */
  public void close() {
    for (int shader : shaders) {
      GLES20.glDetachShader(prog, shader);
      GLES20.glDeleteShader(shader);
    }
    
    if (prog != 0) {
      GLES20.glDeleteProgram(prog);
    }
  }

  /**
   * Binds the program to the context. 
   */
  public void use() {
    GLES20.glUseProgram(prog);
  }

  /**
   * Sets the value of a uniform matrix. 
   */
  public void uniform(String name, float[] value) {
    if (uniforms.containsKey(name)) {
      GLES20.glUniformMatrix4fv(uniforms.get(name), 1, false, value, 0);
    }
  }
  
  /**
   * Sets the value of a uniform matrix. 
   */
  public void uniform(String name, int value) {
    if (uniforms.containsKey(name)) {
      GLES20.glUniform1i(uniforms.get(name), value);
    }
  }


  /**
   * Retrieves an attribute location. 
   */
  public int attrib(String name) {
    return attributes.get(name);
  }
}
