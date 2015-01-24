package net.geodrop;

import android.content.Context;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dropbox.sync.android.*;

import com.google.vrtoolkit.cardboard.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PictureVR extends CardboardActivity implements CardboardView.StereoRenderer 
{
  /**
   *  App key
   */
  final static private String APP_KEY = "mwssyal2r1n22uo";

  /**
   * App secret.
   */
  final static private String APP_SECRET = "g1umz9hg1zz1w5k";

  /**
   * Custom request ID. 
   */
  static final int REQUEST_LINK_TO_DBX = 0xFF;

  /**
   * API object
   */
  private DbxAccountManager mDbxAcctMgr;

  /**
   * Dropbox file system
   */
  DbxFileSystem dbxFs;

  /**
   * 3D cardboardView.
   */
  private CardboardView cardboardView;

  /**
   * Vibrator.
   */
  private Vibrator vibrator;

  /**
   * 2D image shader. 
   */
  private Shader image2DShader;
  /**
   * 2D image shader.
   */
  private Shader image3DShader;
  
  /**
   * Root folder. 
   */
  private Folder root;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);

    // Create the cardboard cardboardView.
    cardboardView = new CardboardView(this);
    setContentView(cardboardView);
    cardboardView.setRenderer(this);
    setCardboardView(cardboardView);
    
    // Access devices.
    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    
    // Dropbox sync.
    if (!mDbxAcctMgr.hasLinkedAccount()) {
      mDbxAcctMgr.startLink(this, REQUEST_LINK_TO_DBX);
    } else {
      initFileSystem();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_LINK_TO_DBX) {
      if (resultCode == Activity.RESULT_OK) {
        initFileSystem();
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
  
  private void initFileSystem() {
    try {
      dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
    } catch (IOException e) {
      e.printStackTrace();
      finish();
    }    
  }

  /**
   * Sets up GLES stuff.
   *  
   * @param eglConfig EGL configuration.
   */
  @Override
  public void onSurfaceCreated(EGLConfig eglConfig) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    
    // Load shaders.
    try {
      image2DShader = new Shader("image2DShader");
      image2DShader.load(Shader.Type.FRAG, getResources().openRawResource(R.raw.image2d_frag));
      image2DShader.load(Shader.Type.VERT, getResources().openRawResource(R.raw.image2d_vert));
      image2DShader.link();
      
      image3DShader = new Shader("image3DShader");
      image3DShader.load(Shader.Type.FRAG, getResources().openRawResource(R.raw.model3d_frag));
      image3DShader.load(Shader.Type.VERT, getResources().openRawResource(R.raw.model3d_vert));
      image3DShader.link();
    } catch (IOException e) {
      Log.i("CardBox", "Cannot read 'image2DShader': " + e.toString());
      finish();
    }
  }
  
  /**
   * Called when the magnet is pulled.
   */
  @Override
  public void onCardboardTrigger() {
    vibrator.vibrate(70);
    if (root != null) {
      Folder child = root.select();
      if (child != null) {
        root = child;
      }
    }
  }
  
  static Image folderImage;
  
  /**
   * Called when a new frame starts. Should update the application state.
   *
   * @param headTransform Head transform information.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    try {
      if (root == null) {
        Log.i("FS", "Creating home folder.");
        root = new Folder(dbxFs, null, DbxPath.ROOT);
        Log.i("FS", "Created home folder.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      finish();
    }
    
    if (folderImage == null) {
      folderImage = new Image(BitmapFactory.decodeResource(getResources(), R.drawable.folder));
    }
    
    final float[] forward = new float[3];
    headTransform.getForwardVector(forward, 0);
    if (root != null) {
      root.point(forward[0], forward[2]);
    }
  }

  /**
   * Draws a frame for a single eye.
   *
   * @param eye
   */
  @Override
  public void onDrawEye(Eye eye) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (root != null) {
      root.render(image2DShader, image3DShader, eye, null);
    }
  }


  /**
   * Unused.
   *
   * @param viewport Viewport.
   */
  @Override
  public void onFinishFrame(Viewport viewport) {
  }

  /**
   * Unused
   *
   * @param width Width of the screen.
   * @param height Height of the screen.
   */
  @Override
  public void onSurfaceChanged(int width, int height) {
    Log.i("GeoDrop", "OnSurfaceChanged :" + width + "x" + height);
  }
  
  /**
   * Unused
   */
  @Override
  public void onRendererShutdown() {
    image2DShader.close();
  }
}
