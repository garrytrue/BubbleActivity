package course.labs.GraphicsLab;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BubbleActivity extends Activity {

	// These variables are for testing purposes
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final int MENU_STILL = Menu.FIRST;
	private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
	private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;

	private static final String TAG = "Graphics";
	private static final String TAG2 = "Mygraphics";

	// Main view
	private RelativeLayout mFrame;

	// Bubble image
	private Bitmap mBitmap;

	// Display dimensions
	private int mDisplayWidth, mDisplayHeight;

	// Sound variables

	// AudioManager
	private AudioManager mAudioManager;
	// SoundPool
	private SoundPool mSoundPool;
	// ID for the bubble popping sound
	private int mSoundID;
	// Audio volume
	private float mStreamVolume;

	// Gesture Detector
	private GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG2, "onCreate_start");

		setContentView(R.layout.main);

		// Set up user interface
		mFrame = (RelativeLayout) findViewById(R.id.frame);

		// Load basic bubble Bitmap
		mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.b64);
		Log.i(TAG2, "onCreate_end");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG2, "onResume_start");
		// Manage bubble popping sound
		// Use AudioManager.STREAM_MUSIC as stream type

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		mStreamVolume = (float) mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC)
				/ mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// make a new SoundPool, allowing up to 10 streams
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		// set a SoundPool OnLoadCompletedListener that calls
		// setupGestureDetector()
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool mSoundPool, int sampleId,
					int status) {
				// Auto-generated method stub
				Log.i(TAG2, "onResume_SounPollLoad");
				if (status == 0) {
					setupGestureDetector();
					Log.i(TAG2, "onResume_SoundPollLoadComplite");
				}

			}
		});

		// load the sound from res/raw/bubble_pop.wav
		mSoundID = mSoundPool.load(this, R.raw.bubble_pop, 1);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Get the size of the display so this view knows where borders is
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

		}
	}

	// Set up GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,

		new GestureDetector.SimpleOnGestureListener() {

			// If a fling gesture starts on a BubbleView then change the
			// BubbleView's velocity

			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {

				// Implement onFling actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

				Log.i(TAG2, "Inside onFling");
				for (int i = 0; i < mFrame.getChildCount(); i++) {
					BubbleView child = (BubbleView) mFrame.getChildAt(i);
					if (child.intersects(event1.getRawX(), event1.getRawY())) {
						child.deflect(velocityX, velocityY);
						return true;
					}

				}

				return false;

			}

			// If a single tap intersects a BubbleView, then pop the BubbleView
			// Otherwise, create a new BubbleView at the tap's location and add
			// it to mFrame.

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {

				// Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method

				// MY TAG
				float eventX = event.getRawX();
				float eventY = event.getRawY();
				String str_evenXY = String.valueOf(eventX) + " "
						+ String.valueOf(eventY);
				Log.i(TAG2, "onSingleTapConfirmed" + str_evenXY);
				Log.i(TAG2,
						"Child in mFrame "
								+ String.valueOf(mFrame.getChildCount()));

				// another realization is work

				float rawX = event.getRawX();
				float rawY = event.getRawY();
				boolean bubbleFound = false;
				for (int i = 0; i < mFrame.getChildCount(); i++) {
					BubbleView currBubbleView = (BubbleView) mFrame
							.getChildAt(i);
					if (currBubbleView.intersects(rawX, rawY)) {
						currBubbleView.stop(true);
						bubbleFound = true;
						break;
					}
				}
				if (!bubbleFound) {
					BubbleView newBubbleView = new BubbleView(mFrame
							.getContext(), rawX, rawY);
					mFrame.addView(newBubbleView);
					newBubbleView.start();
				}

				return false;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// delegate the touch to the gestureDetector
		if (mGestureDetector != null)
			return mGestureDetector.onTouchEvent(event);

		return false;

	}

	@Override
	protected void onPause() {

		// - Release all SoundPool resources
		if (mSoundPool != null) {
			mSoundPool.unload(mSoundID);
			mSoundPool.release();
			mSoundPool = null;
		}

		super.onPause();
	}

	// BubbleView is a View that displays a bubble.
	// This class handles animating, drawing, popping amongst other actions.
	// A new BubbleView is created for each bubble on the display

	private class BubbleView extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mScaledBitmapWidth;
		private Bitmap mScaledBitmap;

		// location, speed and direction of the bubble
		private float mXPos, mYPos, mDx, mDy;
		private long mRotate, mDRotate;

		public BubbleView(Context context, float x, float y) {
			super(context);
			log("Creating Bubble at: x:" + x + " y:" + y);

			// Create a new random number generator to
			// randomize size, rotation, speed and direction
			Random r = new Random();

			// Creates the bubble bitmap for this BubbleView
			createScaledBitmap(r);

			// Adjust position to center the bubble under user's finger
			mXPos = x - mScaledBitmapWidth / 2;
			mYPos = y - mScaledBitmapWidth / 2;

			// Set the BubbleView's speed and direction
			setSpeedAndDirection(r);

			// Set the BubbleView's rotation
			setRotation(r);

			mPainter.setAntiAlias(true);

		}

		private void setRotation(Random r) {

			if (speedMode == RANDOM) {

				// set rotation in range [1..3]
				mDRotate = r.nextInt(3) + 1;

			} else {

				mDRotate = 0;

			}
		}

		private void setSpeedAndDirection(Random r) {

			// Used by test cases
			switch (speedMode) {

			case SINGLE:

				// Fixed speed
				mDx = 10;
				mDy = 10;
				break;

			case STILL:

				// No speed
				mDx = 0;
				mDy = 0;
				break;

			default:

				// Set movement direction and speed
				// Limit movement speed in the x and y
				// direction to [-3..3].
				mDx = r.nextInt(7) - 3;
				mDy = r.nextInt(7) - 3;
			}
		}

		private void createScaledBitmap(Random r) {

			if (speedMode != RANDOM) {

				mScaledBitmapWidth = BITMAP_SIZE * 3;

			} else {

				// set scaled bitmap size in range [1..3] * BITMAP_SIZE
				mScaledBitmapWidth = (r.nextInt(3) + 1) * BITMAP_SIZE;

			}

			// create the scaled bitmap using size set above
			mScaledBitmap = Bitmap.createScaledBitmap(mBitmap,
					mScaledBitmapWidth, mScaledBitmapWidth, true);
		}

		// Start moving the BubbleView & updating the display
		private void start() {

			// Creates a WorkerThread
			ScheduledExecutorService executor = Executors
					.newScheduledThreadPool(1);

			// Execute the run() in Worker Thread every REFRESH_RATE
			// milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					// implement movement logic.
					// Each time this method is run the BubbleView should
					// move one step. If the BubbleView exits the display,
					// stop the BubbleView's Worker Thread.
					// Otherwise, request that the BubbleView be redrawn.
					// if(moveWhileOnScreen())
					if (moveWhileOnScreen()) {
						stop(true);
						mMoverFuture.cancel(true);
					}

					BubbleView.this.postInvalidate();

				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}

		private synchronized boolean intersects(float x, float y) {

			// Return true if the BubbleView intersects position (x,y)
			Log.i(TAG2, "Inside intersect() ");
			Log.i(TAG2, "current_X= " + String.valueOf(x) + " " + "current_Y= "
					+ String.valueOf(y));
			Log.i(TAG2, "mXPos " + String.valueOf(mXPos) + " " + "mYPos "
					+ String.valueOf(mYPos));
			Log.i(TAG2,
					"mScaleBitmapWith= " + String.valueOf(mScaledBitmapWidth));

			if ((x >= mXPos)
					&& (x <= mXPos + mScaledBitmap.getWidth() / 2)
					&& ((y >= mYPos) && (y <= mYPos + mScaledBitmap.getHeight()
							/ 2))) {

				Log.i(TAG2, "Intersect return true");
				return true;

			}

			else {
				Log.i(TAG2, "Intersect return false");
				return false;
			}

		}

		// Cancel the Bubble's movement
		// Remove Bubble from mFrame
		// Play pop sound if the BubbleView was popped

		private void stop(final boolean popped) {

			if (null != mMoverFuture && mMoverFuture.cancel(true)) {

				// This work will be performed on the UI Thread

				mFrame.post(new Runnable() {
					@Override
					public void run() {

						//
						// if(BubbleView.this.isOutOfView())
						mFrame.removeView(BubbleView.this);

						if (popped) {
							log("Pop!");

							// - If the bubble was popped by user,
							// play the popping sound
							mSoundPool.play(mSoundID, mStreamVolume,
									mStreamVolume, 1, 0, 1);

						}

						log("Bubble removed from view!");

					}
				});
			}
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) {
			log("velocity X:" + velocityX + " velocity Y:" + velocityY);

			// set mDx and mDy to be the new velocities divided by the
			// REFRESH_RATE

			mDx = velocityX / REFRESH_RATE;
			mDy = velocityY / REFRESH_RATE;

		}

		// Draw the Bubble at its current location
		@Override
		protected synchronized void onDraw(Canvas canvas) {

			// save the canvas
			canvas.save();

			// increase the rotation of the original image by mDRotate

			mRotate += mDRotate;

			// Rotate the canvas by current rotation

			canvas.rotate(mRotate, mXPos, mYPos);

			// draw the bitmap at it's new location

			canvas.drawBitmap(mBitmap, mXPos, mYPos, mPainter);

			// restore the canvas
			canvas.restore();
		}

		private synchronized boolean moveWhileOnScreen() {

			// Move the BubbleView
			// Returns true if the BubbleView has exited the screen
			if (isOutOfView()) {

				return true;
			} else {
				mXPos += mDx;
				mYPos += mDy;

			}
			return false;

		}

		private boolean isOutOfView() {

			// Return true if the BubbleView has exited the screen
			if ((mXPos - mScaledBitmapWidth / 2 >= mDisplayWidth)
					|| (mXPos < 0)
					|| (mYPos - mScaledBitmapWidth / 2 >= mDisplayHeight)
					|| (mYPos < 0)) {
				Log.i(TAG2, "isOutOfView return true");
				// Log.i(TAG2,
				// "mXPos "+String.valueOf(mXPos)+" "+"mYPos "+String.valueOf(mYPos));

				return true;
			}

			return false;

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_STILL, Menu.NONE, "Still Mode");
		menu.add(Menu.NONE, MENU_SINGLE_SPEED, Menu.NONE, "Single Speed Mode");
		menu.add(Menu.NONE, MENU_RANDOM_SPEED, Menu.NONE, "Random Speed Mode");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_STILL:
			speedMode = STILL;
			return true;
		case MENU_SINGLE_SPEED:
			speedMode = SINGLE;
			return true;
		case MENU_RANDOM_SPEED:
			speedMode = RANDOM;
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static void log(String message) {
		Log.i(TAG, message);
	}
}