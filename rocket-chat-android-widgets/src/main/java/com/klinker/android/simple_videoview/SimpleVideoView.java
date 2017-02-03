/*
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
// ref: https://github.com/klinker24/Android-SimpleVideoView/blob/49ae7b10f6b86b7bd1ddab2223e619c805825067/library/src/main/java/com/klinker/android/simple_videoview/SimpleVideoView.java

package com.klinker.android.simple_videoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.HashMap;
import chat.rocket.android.widget.R;

/**
 * VideoView implementation that simplifies things, fixes aspect ratio, and allows
 * you to specify whether or not you want to overtake the system audio.
 */
public class SimpleVideoView extends RelativeLayout {

  private MediaPlayer mediaPlayer;

  private LinearLayout progressBar;
  private TextureView textureView;
  private Surface surface;


  private VideoPlaybackErrorTracker errorTracker;

  private boolean loop = false;
  private boolean stopSystemAudio = false;
  private boolean muted = false;
  private boolean showSpinner = true;

  private Uri videoUri = null;
  private String cookie = null;

  /**
   * Default constructor
   * @param context context for the activity
   */
  public SimpleVideoView(Context context) {
    super(context);
    init();
  }

  /**
   * Constructor for XML layout
   * @param context activity context
   * @param attrs xml attributes
   */
  public SimpleVideoView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleVideoView, 0, 0);
    loop = a.getBoolean(R.styleable.SimpleVideoView_loop, false);
    stopSystemAudio = a.getBoolean(R.styleable.SimpleVideoView_stopSystemAudio, false);
    muted = a.getBoolean(R.styleable.SimpleVideoView_muted, false);
    showSpinner = a.getBoolean(R.styleable.SimpleVideoView_showSpinner, true);
    a.recycle();

    init();
  }

  /**
   * Initialize the layout for the SimpleVideoView.
   */
  private void init() {
    // add a progress spinner
    progressBar = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.simple_video_view_progress_bar, this, false);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      progressBar.setElevation(6);
    }

    addView(progressBar);

    setGravity(Gravity.CENTER);
  }

  /**
   * Add the SurfaceView to the layout.
   */
  private void addSurfaceView() {
    // disable the spinner if we don't want it
    if (!showSpinner && progressBar.getVisibility() != View.GONE) {
      progressBar.setVisibility(View.GONE);
    }

    final RelativeLayout.LayoutParams surfaceViewParams =
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    textureView = new TextureView(getContext());
    textureView.setLayoutParams(surfaceViewParams);
    addView(textureView, 0);

    textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
      @Override
      public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        surface = new Surface(surfaceTexture);

        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              // this needs to be run on a background thread.
              // set data source can take upwards of 1-2 seconds
              if (TextUtils.isEmpty(cookie)) {
                mediaPlayer.setDataSource(getContext(), videoUri);
              } else {
                mediaPlayer.setDataSource(getContext(), videoUri, new HashMap<String, String>() {
                  {
                    put("Cookie", cookie);
                  }
                });
              }
              mediaPlayer.prepareAsync();
            } catch (Exception e) {
              if (errorTracker != null) {
                errorTracker.onPlaybackError(e);
              }
            }
          }
        }).start();
      }

      @Override
      public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

      }

      @Override
      public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
      }

      @Override
      public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

      }
    });
  }

  /**
   * Prepare to play the media.
   */
  private void prepareMediaPlayer() {
    if (mediaPlayer != null) {
      release();
    }

    // initialize the media player
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(final MediaPlayer mediaPlayer) {
        scalePlayer();

        if (stopSystemAudio) {
          AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
          am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        if (muted) {
          mediaPlayer.setVolume(0, 0);
        }

        if (progressBar.getVisibility() != View.GONE) {
          progressBar.setVisibility(View.GONE);
        }

        try {
          mediaPlayer.setSurface(surface);
          mediaPlayer.start();
        } catch (IllegalArgumentException e) {
          // the surface has already been released
        }
      }
    });

    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        if (loop) {
          mp.seekTo(0);
          mp.start();
        }
      }
    });

    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      /**
       * Called to indicate an error.
       *
       * @param mp      the MediaPlayer the error pertains to
       * @param what    the type of error that has occurred:
       * <ul>
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_UNKNOWN}
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_SERVER_DIED}
       * </ul>
       * @param extra an extra code, specific to the error. Typically
       * implementation dependent.
       * <ul>
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_IO}
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_MALFORMED}
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_UNSUPPORTED}
       * <li>{@link android.media.MediaPlayer.OnErrorListener#MEDIA_ERROR_TIMED_OUT}
       * <li><code>MEDIA_ERROR_SYSTEM (-2147483648)</code> - low-level system error.
       * </ul>
       */
      @Override
      public boolean onError(MediaPlayer mp, int what, int extra) {
        if (errorTracker != null) {
          errorTracker.onPlaybackError(
              new RuntimeException("Error playing video! what code: " + what + ", extra code: " + extra)
          );
        }
        return true;
      }
    });
  }

  /**
   * Adjust the size of the player so it fits on the screen.
   */
  private void scalePlayer() {
    int videoWidth = mediaPlayer.getVideoWidth();
    int videoHeight = mediaPlayer.getVideoHeight();
    float videoProportion = (float) videoWidth / (float) videoHeight;

    float screenProportion = (float) getWidth() / (float) getHeight();
    ViewGroup.LayoutParams lp = textureView.getLayoutParams();

    if (videoProportion > screenProportion) {
      lp.width = getWidth();
      lp.height = (int) ((float) getWidth() / videoProportion);
    } else {
      lp.width = (int) (videoProportion * (float) getHeight());
      lp.height = getHeight();
    }

    textureView.setLayoutParams(lp);
  }

  /**
   * Load the video into the player and initialize the layouts
   *
   * @param videoUrl String url to the video
   */
  public void start(String videoUrl) {
    start(Uri.parse(videoUrl), null);
  }

  /**
   * Load the video into the player and initialize the layouts with Cookie.
   *
   */
  public void start(String videoUrl, String cookie) {
    start(Uri.parse(videoUrl), cookie);
  }

  /**
   * Load the video into the player and initialize the layouts.
   *
   * @param videoUri uri to the video.
   */
  private void start(Uri videoUri, String cookie) {
    this.videoUri = videoUri;
    this.cookie = cookie;

    // we will not load the surface view or anything else until we are given a video.
    // That way, if, say, you wanted to add the simple video view on a list or something,
    // it won't be as intensive. ( == Better performance.)
    if (textureView == null) {
      addSurfaceView();
      prepareMediaPlayer();
    } else {
      prepareMediaPlayer();
    }
  }

  /**
   * Start video playback. Called automatically with the SimpleVideoPlayer#start method
   */
  public void play() {
    if (!mediaPlayer.isPlaying())
      mediaPlayer.start();
  }

  /**
   * Pause video playback
   */
  public void pause() {
    if (mediaPlayer.isPlaying())
      mediaPlayer.pause();
  }

  /**
   * Release the video to stop playback immediately.
   *
   * Should be called when you are leaving the playback activity
   */
  public void release() {
    try {
      mediaPlayer.stop();
      mediaPlayer.release();
    } catch (Exception e) { }

    mediaPlayer = null;
  }

  /**
   * Whether you want the video to loop or not
   *
   * @param shouldLoop
   */
  public void setShouldLoop(boolean shouldLoop) {
    this.loop = shouldLoop;
  }

  /**
   * Whether you want the app to stop the currently playing audio when you start the video
   *
   * @param stopSystemAudio
   */
  public void setStopSystemAudio(boolean stopSystemAudio) {
    this.stopSystemAudio = stopSystemAudio;
  }

  /**
   * Whether or not you want to show the spinner while loading the video
   *
   * @param showSpinner
   */
  public void setShowSpinner(boolean showSpinner) {
    this.showSpinner = showSpinner;
  }

  /**
   * Get whether or not the video is playing
   *
   * @return true if the video is playing, false otherwise
   */
  public boolean isPlaying() {
    try {
      return mediaPlayer != null && mediaPlayer.isPlaying();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Will return a result if there is an error playing the video
   *
   * @param tracker
   */
  public void setErrorTracker(VideoPlaybackErrorTracker tracker) {
    this.errorTracker = tracker;
  }

  public interface VideoPlaybackErrorTracker {
    void onPlaybackError(Exception e);
  }
}