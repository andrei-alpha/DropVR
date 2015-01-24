package net.geodrop;

import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.google.vrtoolkit.cardboard.Eye;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D model. 
 */
public class Model implements Entity {
  int texture;
  int buffer;
  int length;
  
  public Model(DbxFileSystem dbxFs, DbxPath path) {
    List<Float> v = new ArrayList<>();
    List<Float> vn = new ArrayList<>();
    List<Float> vt= new ArrayList<>();
    List<Integer> f = new ArrayList<>();
    
    try {
      // Load the model.
      DbxFile modelFile = dbxFs.open(path);
      BufferedReader modelStream = new BufferedReader(
          new InputStreamReader(new BufferedInputStream(modelFile.getReadStream())));
      
      String line;
      while ((line = modelStream.readLine()) != null) {
        String[] tokens = line.split(" ");
        if (tokens[0].equals("v")) {
          v.add(Float.parseFloat(tokens[1]));
          v.add(Float.parseFloat(tokens[2]));
          v.add(Float.parseFloat(tokens[3]));
        } else if (tokens[0].equals("vt")) {
          vt.add(Float.parseFloat(tokens[1]));
          vt.add(Float.parseFloat(tokens[2]));
        } else if (tokens[0].equals("vn")) {
          vn.add(Float.parseFloat(tokens[1]));
          vn.add(Float.parseFloat(tokens[2]));
          vn.add(Float.parseFloat(tokens[3]));
        } else if (tokens[0].equals("f")) {
          String[] f0 = tokens[1].split("/");
          String[] f1 = tokens[2].split("/");
          String[] f2 = tokens[3].split("/");
          f.add(Integer.parseInt(f0[0]));
          f.add(Integer.parseInt(f0[1]));
          f.add(Integer.parseInt(f0[2]));

          f.add(Integer.parseInt(f1[0]));
          f.add(Integer.parseInt(f1[1]));
          f.add(Integer.parseInt(f1[2]));

          f.add(Integer.parseInt(f2[0]));
          f.add(Integer.parseInt(f2[1]));
          f.add(Integer.parseInt(f2[2]));
          length++;
        }
      }
      modelFile.close();
      
      float[] floats = new float[f.size() / 3 * 8];
      int idx = 0;
      
      for (int i = 0; i < f.size(); i += 3) {
        floats[idx++] = v.get(f.get(i + 0) * 3 + 0 - 3);
        floats[idx++] = v.get(f.get(i + 0) * 3 + 1 - 3);
        floats[idx++] = v.get(f.get(i + 0) * 3 + 2 - 3);

        floats[idx++] = vn.get(f.get(i + 2) * 3 + 0 - 3);
        floats[idx++] = vn.get(f.get(i + 2) * 3 + 1 - 3);
        floats[idx++] = vn.get(f.get(i + 2) * 3 + 2 - 3);
        
        floats[idx++] = vt.get(f.get(i + 1) * 2 + 0 - 2);
        floats[idx++] = 1.0f - vt.get(f.get(i + 1) * 2 + 1 - 2);
      }

      final FloatBuffer jbuffer = (FloatBuffer) ByteBuffer
          .allocateDirect(floats.length * 4)
          .order(ByteOrder.nativeOrder())
          .asFloatBuffer()
          .put(floats)
          .position(0);
      
      final int[] tmp = new int[1];
      GLES20.glGenBuffers(1, tmp, 0);
      buffer = tmp[0];

      GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
      GLES20.glBufferData(
          GLES20.GL_ARRAY_BUFFER, jbuffer.capacity() * 4, jbuffer, GLES20.GL_STATIC_DRAW);
      Log.i("Model", "Uploaded " + jbuffer.capacity());
      
      // Load the texture.
      String name = path.getName();
      name = name.substring(0, name.length() - "_model.txt".length());
          
      DbxFile file = dbxFs.open(new DbxPath(path.getParent(), name + "_texture.png"));
      GLES20.glGenTextures(1, tmp, 0);
      texture = tmp[0];

      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeStream(file.getReadStream()), 0);
      file.close();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private float angle = 0.0f;
  
  @Override
  public void render(Shader unused, Shader shader, Eye eye, float[] model) {
    Matrix.rotateM(model, 0, angle, 0.0f, 1.0f, 0.0f);
    Matrix.scaleM(model, 0, 2.0f, 2.0f, 2.0f);
    angle += 0.3f;
    
    shader.use();
    shader.uniform("u_view", eye.getEyeView());
    shader.uniform("u_proj", eye.getPerspective(0.1f, 100.0f));
    shader.uniform("u_model", model);
    
    int vertex = shader.attrib("in_vertex");
    int normal = shader.attrib("in_normal");
    int uv = shader.attrib("in_uv");

    shader.uniform("u_texture", 0);

    GLES20.glEnableVertexAttribArray(vertex);
    GLES20.glEnableVertexAttribArray(normal);
    GLES20.glEnableVertexAttribArray(uv);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    GLES20.glVertexAttribPointer(vertex, 3, GLES20.GL_FLOAT, false, 32, 0);
    GLES20.glVertexAttribPointer(normal, 3, GLES20.GL_FLOAT, false, 32, 12);
    GLES20.glVertexAttribPointer(uv,     2, GLES20.GL_FLOAT, false, 32, 24);

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, length * 3);

    GLES20.glDisableVertexAttribArray(uv);
    GLES20.glDisableVertexAttribArray(normal);
    GLES20.glDisableVertexAttribArray(vertex);
  }

  @Override
  public Folder select() {
    return null;
  }

  @Override
  public void point(float x, float z) {

  }
}
