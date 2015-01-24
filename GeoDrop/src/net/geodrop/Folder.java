package net.geodrop;

import android.content.*;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.util.Log;
import com.dropbox.sync.android.*;
import com.google.vrtoolkit.cardboard.Eye;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stuff
 */
public class Folder implements Entity {

  /**
   * Projection matrix. 
   */
  private static final float[] mProj = new float[16];

  /**
   * View matrix. 
   */
  private static final float[] mView = new float[16];

  /**
   * Model matrix. 
   */
  private static final float[] mModel = new float[16];

  /**
   * List of chilren.
   */
  private List<Entity> children = new ArrayList<>();

  /**
   * Index of the selected child.
   */
  int selected;

  /**
   * True if a child has to be zoomed.
   */
  boolean zoomed;

  /**
   * Dropbox file system
   */
  private final DbxFileSystem dbxFs;

  /**
   * Folder path in the dropbox file system
   */
  private final DbxPath dbxPath;


  public Folder(DbxFileSystem dbxFs, Folder parent, DbxPath dbxPath) {

    this.dbxFs = dbxFs;
    this.dbxPath = dbxPath;

    // Sync folder content
    syncFolderContent();

    // Add the listener
    //dbxFs.addPathListener(new DbxFileSystem.PathListener() {
    //  @Override
    //  public void onPathChange(DbxFileSystem dbxFileSystem, DbxPath dbxPath, Mode mode) {
    //    syncFolderContent();
    //  }
    //}, DbxPath.ROOT, DbxFileSystem.PathListener.Mode.PATH_ONLY);

    if (parent != null) {
      children.add(parent);
    }
  }

  private void syncFolderContent() {
    try {
      List<DbxFileInfo> infos = dbxFs.listFolder(dbxPath);

      for (DbxFileInfo info : infos) {
        Log.i("FS", info.path.toString());
        if (info.isFolder) {
          children.add(new Folder(dbxFs, this, info.path));
        } else if (info.path.getName().endsWith("_model.txt")) {
            children.add(new Model(dbxFs, info.path));
        } else {
          DbxFile file = dbxFs.open(info.path);
          children.add(new Image(BitmapFactory.decodeStream(file.getReadStream())));
          file.close();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void render(Shader shader, Shader unused, Eye eye, float[] model) {
    int i = 0;
    for (Entity child : children) {
      float ang = (float)(i * Math.PI * 2.0 / children.size());
      float dist = selected == i ? (zoomed ? 3.0f : 5.0f) : 7.0f;

      Matrix.setIdentityM(mModel, 0);
      Matrix.translateM(
          mModel, 0,
          (float) Math.sin(ang) * dist,
          0.0f,
          (float) Math.cos(ang) * dist);
      Matrix.rotateM(
          mModel, 0, ang / (float) Math.PI * 180.0f, 0.0f, 1.0f, 0.0f);

      if (i == selected) {
        Matrix.scaleM(mModel, 0, 1.1f, 1.1f, 1.1f);
      }

      if (child instanceof Folder) {
        PictureVR.folderImage.render(shader, unused, eye, mModel);
      } else {
        child.render(shader, unused, eye, mModel);
      }
      ++i;
    }
  }

  public Folder select() {
    if (children.get(selected) instanceof Folder) {
      zoomed = false;
      return (Folder)children.get(selected);
    } else {
      zoomed = !zoomed;
      return null;
    }
  }

  public void point(float x, float z) {
    float ang = (float)Math.atan2(x, -z) + (float)Math.PI;
    if (!zoomed) {
      selected = (int) (ang * children.size() / (2 * Math.PI));
    }
  }
}
