package net.geodrop;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * GLES quad.
 */
public class Image implements Entity {
  private static final float[] QUAD = new float[] {
    -1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
    -1.0f,  1.0f, 0.0f, 1.0f, 0.0f,
     1.0f, -1.0f, 0.0f, 0.0f, 1.0f,

    -1.0f,  1.0f, 0.0f, 1.0f, 0.0f,
     1.0f,  1.0f, 0.0f, 0.0f, 0.0f,
     1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
  };

  private static final FloatBuffer QUAD_BUFFER = (FloatBuffer)ByteBuffer
      .allocateDirect(QUAD.length * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .put(QUAD)
      .position(0);
  
  private int texture;
  
  private int buffer;

  public Image(Bitmap bitmap) {
    final int[] tmp = new int[1];
    GLES20.glGenTextures(1, tmp, 0);
    texture = tmp[0];

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

    GLES20.glGenBuffers(1, tmp, 0);
    buffer = tmp[0];
    
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    GLES20.glBufferData(
        GLES20.GL_ARRAY_BUFFER, QUAD_BUFFER.capacity() * 4, QUAD_BUFFER, GLES20.GL_STATIC_DRAW);
  }

  @Override
  public void render(Shader shader) {
    int vertex = shader.attrib("in_vertex");
    int uv = shader.attrib("in_uv");
    
    shader.uniform("u_texture", 0);
    
    GLES20.glEnableVertexAttribArray(vertex);
    GLES20.glEnableVertexAttribArray(uv);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    GLES20.glVertexAttribPointer(vertex, 3, GLES20.GL_FLOAT, false, 20, 0);
    GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 20, 12);
    
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    
    GLES20.glDisableVertexAttribArray(uv);
    GLES20.glDisableVertexAttribArray(vertex);
  }
  
  public Folder select() {
    return null;
  }

  public void point(float x, float z) {
  }
}
