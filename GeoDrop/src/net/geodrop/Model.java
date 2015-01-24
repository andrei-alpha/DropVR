package net.geodrop;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Some 3D model.
 */
public class Model implements Entity{
  private static final float[] QUAD = new float[] {
      -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
      1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
      -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,

      -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
      1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
      1.0f,  1.0f, 0.0f, 1.0f, 1.0f
  };

  private static final FloatBuffer QUAD_BUFFER = ByteBuffer
      .allocateDirect(QUAD.length * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .put(QUAD);
  
  private float rot = 0.0f;
  
  public float getRot() {
    return rot;
  }

  public void render(Shader shader) {
    int vertex = shader.attrib("in_vertex");
    int uv = shader.attrib("in_uv");

    GLES20.glEnableVertexAttribArray(vertex);
    GLES20.glEnableVertexAttribArray(uv);

    QUAD_BUFFER.position(0);
    GLES20.glVertexAttribPointer(vertex, 3, GLES20.GL_FLOAT, false, 20, QUAD_BUFFER);
    QUAD_BUFFER.position(12);
    GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 20, QUAD_BUFFER);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

    GLES20.glDisableVertexAttribArray(uv);
    GLES20.glDisableVertexAttribArray(vertex);
    
    rot += 0.1f;
  }
}
