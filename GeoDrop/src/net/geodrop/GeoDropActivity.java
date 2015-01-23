package net.geodrop;

import android.app.Activity;
import android.os.Bundle;
import com.google.vrtoolkit.cardboard.CardboardActivity;

public class GeoDropActivity extends CardboardActivity {
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }
}
