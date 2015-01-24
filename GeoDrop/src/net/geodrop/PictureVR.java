package net.geodrop;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import com.google.vrtoolkit.cardboard.*;

import javax.microedition.khronos.egl.EGLConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PictureVR
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
   * Array of images to display. 
   */
  private List<Entity> entities;

  /**
   * Projection matrix. 
   */
  private final float[] mProj = new float[16];

  /**
   * View matrix. 
   */
  private final float[] mView = new float[16];

  /**
   * Model matrix. 
   */
  private final float[] mModel = new float[] {
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1
  };

  /**
   * Index of the selected image. 
   */
  private int selected = 0;

  /**
   * True if the current item was zoomed. 
   */
  private boolean zoomed = false;

  /**
   * Java Shit 
   */
  private final BitmapFactory.Options options;
  
  public PictureVR() {
    options = new BitmapFactory.Options();
    options.inScaled = false;
  }
  
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
    //mDBApi.getSession().startOAuth2Authentication(GeoDropActivity.this);

    // Create the cardboard cardboardView.
    cardboardView = new CardboardView(this);
    setContentView(cardboardView);
    cardboardView.setRenderer(this);
    setCardboardView(cardboardView);
    
    // Access devices.
    vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
  }

  @Override
  protected void onResume() {
    super.onResume();

    /*if (mDBApi.getSession().authenticationSuccessful()) {
      try {
        mDBApi.getSession().finishAuthentication();
        String accessToken = mDBApi.getSession().getOAuth2AccessToken();
      } catch (IllegalStateException e) {
        Log.i("DbAuthLog", "Error authenticating", e);
      }
    }*/
  }

  /**
   * Sets up GLES stuff.
   *  
   * @param eglConfig EGL configuration.
   */
  @Override
  public void onSurfaceCreated(EGLConfig eglConfig) {
    GLES20.glClearColor(0.0f, 0.49f, 0.89f, 1.0f);
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    GLES20.glEnable(GLES20.GL_CULL_FACE);
    GLES20.glCullFace(GLES20.GL_BACK);
    GLES20.glFrontFace(GLES20.GL_CCW);
    
    // Load shaders.
    try {
      image2DShader = new Shader("image2DShader");
      image2DShader.load(Shader.Type.FRAG, getResources().openRawResource(R.raw.image2d_frag));
      image2DShader.load(Shader.Type.VERT, getResources().openRawResource(R.raw.image2d_vert));
      image2DShader.link();
    } catch (IOException e) {
      Log.i("CardBox", "Cannot read 'image2DShader': " + e.toString());
      finish();
    }

    // Initialise the list of files to view.
    entities = new ArrayList<>();
    for (int i = 0; i < 24; ++i) {
      entities.add(new Quad(BitmapFactory.decodeResource(getResources(), R.drawable.cat, options)));
    }
    for (int i = 0; i < 24; ++i) {
      entities.add(new Model());
    }
  }

  /**
   * Called when a new frame starts. Should update the application state.
   *
   * @param headTransform Head transform information.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {
    final float[] forward = new float[3];
    headTransform.getForwardVector(forward, 0);
    int third = entities.size() / 3;
    
    float ang = (float)Math.atan2(forward[0], -forward[2]) + (float)Math.PI;
    int newSelected = (int)(ang * third / (2 * Math.PI)) + third;
    
    if (forward[1] > 0.2) {
      newSelected -= third;
    } else if (forward[1] < -0.2) {
      newSelected += third;
    }
    
    if (!zoomed) {
      selected = newSelected;
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
    
    image2DShader.use();
    image2DShader.uniform("u_view", eye.getEyeView());
    image2DShader.uniform("u_proj", eye.getPerspective(0.1f, 100.0f));
    
    int i = 0;
    for (Entity quad : entities) {
      float ang = i * (float)Math.PI * 2.0f / (entities.size() / 3);
      float dist = i == selected ? (zoomed ? 3.0f : 5.0f) : 7.0f;
      
      Matrix.setIdentityM(mModel, 0);
      Matrix.translateM(
          mModel, 0,
          (float) Math.sin(ang) * dist,
          (i == selected && zoomed) ? 0.0f : ((float) (i / (entities.size() / 3)) * 3 - 3),
          (float) Math.cos(ang) * dist);
      Matrix.rotateM(
          mModel, 0, quad.getRot() + ang / (float)Math.PI * 180.0f, 0.0f, 1.0f, 0.0f);
      
      if (i == selected) {
        Matrix.scaleM(mModel, 0, 1.1f, 1.1f, 1.1f);
      }
      
      image2DShader.uniform("u_model", mModel);
      quad.render(image2DShader);
      
      ++i;
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
  
  @Override
  public void onCardboardTrigger() {
    vibrator.vibrate(70);
    zoomed = !zoomed;
  }
  
  /**
   * Unused
   */
  @Override
  public void onRendererShutdown() {
    image2DShader.close();
  }
}
