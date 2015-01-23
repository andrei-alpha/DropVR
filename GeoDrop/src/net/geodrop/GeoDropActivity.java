package net.geodrop;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import com.google.vrtoolkit.cardboard.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.IOException;

public class GeoDropActivity 
    extends CardboardActivity 
    implements CardboardView.StereoRenderer 
{

  /**
   *  App key & secret
   */
  final static private String APP_KEY = "pm7uj5ni8got89w";
  final static private String APP_SECRET = "b4ln0k265szoal4";
  final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

  /**
   * API object
   */
  private DropboxAPI<AndroidAuthSession> mDBApi;

  /**
   * 3D cardboardView.
   */
  CardboardView cardboardView;

  /**
   * 2D image shader. 
   */
  Shader image2DShader;
  
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialise API stuff
    AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
    AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
    mDBApi = new DropboxAPI<AndroidAuthSession>(session);

    // Authenticate
    mDBApi.getSession().startOAuth2Authentication(GeoDropActivity.this);

    // Create the cardboard cardboardView.
    cardboardView = new CardboardView(this);
    setContentView(cardboardView);
    cardboardView.setRenderer(this);
    setCardboardView(cardboardView);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (mDBApi.getSession().authenticationSuccessful()) {
      try {
        mDBApi.getSession().finishAuthentication();
        String accessToken = mDBApi.getSession().getOAuth2AccessToken();
      } catch (IllegalStateException e) {
        Log.i("DbAuthLog", "Error authenticating", e);
      }
    }
  }

  /**
   * Sets up GLES stuff.
   *  
   * @param eglConfig EGL configuration.
   */
  @Override
  public void onSurfaceCreated(EGLConfig eglConfig) {
    GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
    
    // Load shaders.
    try {
      image2DShader = new Shader("image2DShader");
      image2DShader.load(Shader.Type.VERT, getResources().openRawResource(R.raw.image2d_frag));
      image2DShader.load(Shader.Type.FRAG, getResources().openRawResource(R.raw.image2d_vert));
      image2DShader.link();
    } catch (IOException e) {
      Log.i("CardBox", "Cannot read 'image2DShader': " + e.toString());
      finish();
    }
  }


  /**
   * Called when a new frame starts. Should update the application state.
   *
   * @param headTransform Head transform information.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    
  }

  /**
   * Draws a frame for a single eye.
   *
   * @param eye
   */
  @Override
  public void onDrawEye(Eye eye) {
    
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
