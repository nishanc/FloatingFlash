package com.nishan.floatingflash;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 * Created by Nishan Chathuranga on 9/17/2017.
 */

public class FlashService extends Service {
	private WindowManager windowManager;
	private RelativeLayout floaterView, removeView;
	private ImageView floaterImg, removeImg;
	private int x_init_cord, y_init_cord, x_init_margin, y_init_margin;
	private Point szWindow = new Point();
	private boolean isLeft = true;
	Camera camera;
	Camera.Parameters parameters;
	boolean isFlash = false;
	boolean isOn = false;

	@SuppressWarnings("deprecation")

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(Utils.LogTag, "FlashService.onCreate()");
		if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
			camera = Camera.open();
			parameters = camera.getParameters();
			isFlash = true;
		}
//		WindowManager onlockscreen = (WindowManager) getSystemService(WINDOW_SERVICE);
//		LayoutInflater minflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
//		View mView = minflater.inflate(R.layout.floater_circle,null);
//		WindowManager.LayoutParams mlayoutparams = new WindowManager.LayoutParams(
//				ViewGroup.LayoutParams.WRAP_CONTENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT,0,0,
//				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
//				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//		onlockscreen.addView(mView,mlayoutparams);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void handleStart(){
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

		removeView = (RelativeLayout)inflater.inflate(R.layout.remove, null);
		WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		paramRemove.gravity = Gravity.TOP | Gravity.LEFT;

		removeView.setVisibility(View.GONE);
		removeImg = (ImageView)removeView.findViewById(R.id.remove_img);
		windowManager.addView(removeView, paramRemove);


		floaterView = (RelativeLayout) inflater.inflate(R.layout.floater_circle, null);
		floaterImg = (ImageView) floaterView.findViewById(R.id.floater);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			windowManager.getDefaultDisplay().getSize(szWindow);
		} else {
			int w = windowManager.getDefaultDisplay().getWidth();
			int h = windowManager.getDefaultDisplay().getHeight();
			szWindow.set(w, h);
		}

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;
		windowManager.addView(floaterView, params);

		floaterView.setOnTouchListener(new View.OnTouchListener() {
			long time_start = 0, time_end = 0;
			boolean isLongclick = false, inBounded = false;
			int remove_img_width = 0, remove_img_height = 0;

			Handler handler_longClick = new Handler();
			Runnable runnable_longClick = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Log.d(Utils.LogTag, "Into runnable_longClick");

					isLongclick = true;
					removeView.setVisibility(View.VISIBLE);
					floater_longclick();
				}
			};

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) floaterView.getLayoutParams();

				int x_cord = (int) event.getRawX();
				int y_cord = (int) event.getRawY();
				int x_cord_Destination, y_cord_Destination;

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						time_start = System.currentTimeMillis();
						handler_longClick.postDelayed(runnable_longClick, 600);

						remove_img_width = removeImg.getLayoutParams().width;
						remove_img_height = removeImg.getLayoutParams().height;

						x_init_cord = x_cord;
						y_init_cord = y_cord;

						x_init_margin = layoutParams.x;
						y_init_margin = layoutParams.y;

						break;
					case MotionEvent.ACTION_MOVE:
						int x_diff_move = x_cord - x_init_cord;
						int y_diff_move = y_cord - y_init_cord;

						x_cord_Destination = x_init_margin + x_diff_move;
						y_cord_Destination = y_init_margin + y_diff_move;

						if(isLongclick){
							int x_bound_left = szWindow.x / 2 - (int)(remove_img_width * 1.5);
							int x_bound_right = szWindow.x / 2 +  (int)(remove_img_width * 1.5);
							int y_bound_top = szWindow.y - (int)(remove_img_height * 1.5);

							if((x_cord >= x_bound_left && x_cord <= x_bound_right) && y_cord >= y_bound_top){
								inBounded = true;

								int x_cord_remove = (int) ((szWindow.x - (remove_img_height * 1.5)) / 2);
								int y_cord_remove = (int) (szWindow.y - ((remove_img_width * 1.5) + getStatusBarHeight() ));

								if(removeImg.getLayoutParams().height == remove_img_height){
									removeImg.getLayoutParams().height = (int) (remove_img_height * 1.5);
									removeImg.getLayoutParams().width = (int) (remove_img_width * 1.5);

									WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
									param_remove.x = x_cord_remove;
									param_remove.y = y_cord_remove;

									windowManager.updateViewLayout(removeView, param_remove);
								}

								layoutParams.x = x_cord_remove + (Math.abs(removeView.getWidth() - floaterView.getWidth())) / 2;
								layoutParams.y = y_cord_remove + (Math.abs(removeView.getHeight() - floaterView.getHeight())) / 2 ;

								windowManager.updateViewLayout(floaterView, layoutParams);
								break;
							}else{
								inBounded = false;
								removeImg.getLayoutParams().height = remove_img_height;
								removeImg.getLayoutParams().width = remove_img_width;

								WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
								int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
								int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight());

								param_remove.x = x_cord_remove;
								param_remove.y = y_cord_remove;

								windowManager.updateViewLayout(removeView, param_remove);
							}

						}


						layoutParams.x = x_cord_Destination;
						layoutParams.y = y_cord_Destination;

						windowManager.updateViewLayout(floaterView, layoutParams);
						break;
					case MotionEvent.ACTION_UP:
						isLongclick = false;
						removeView.setVisibility(View.GONE);
						removeImg.getLayoutParams().height = remove_img_height;
						removeImg.getLayoutParams().width = remove_img_width;
						handler_longClick.removeCallbacks(runnable_longClick);

						if(inBounded){

							stopService(new Intent(FlashService.this, FlashService.class));
							inBounded = false;
							break;
						}


						int x_diff = x_cord - x_init_cord;
						int y_diff = y_cord - y_init_cord;

						if(Math.abs(x_diff) < 5 && Math.abs(y_diff) < 5){
							time_end = System.currentTimeMillis();
							if((time_end - time_start) < 300){
								floater_click();
							}
						}

						y_cord_Destination = y_init_margin + y_diff;

						int BarHeight =  getStatusBarHeight();
						if (y_cord_Destination < 0) {
							y_cord_Destination = 0;
						} else if (y_cord_Destination + (floaterView.getHeight() + BarHeight) > szWindow.y) {
							y_cord_Destination = szWindow.y - (floaterView.getHeight() + BarHeight );
						}
						layoutParams.y = y_cord_Destination;

						inBounded = false;
						resetPosition(x_cord);

						break;
					default:
						Log.d(Utils.LogTag, "floater View.setOnTouchListener  -> event.getAction() : default");
						break;
				}
				return true;
			}
		});
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);

		if(windowManager == null)
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            int w = windowManager.getDefaultDisplay().getWidth();
            int h = windowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }
		
		WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) floaterView.getLayoutParams();
				
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	Log.d(Utils.LogTag, "FlashService.onConfigurationChanged -> landscap");

	    	if(layoutParams.y + (floaterView.getHeight() + getStatusBarHeight()) > szWindow.y){
	    		layoutParams.y = szWindow.y- (floaterView.getHeight() + getStatusBarHeight());
	    		windowManager.updateViewLayout(floaterView, layoutParams);
	    	}
	    		    	
	    	if(layoutParams.x != 0 && layoutParams.x < szWindow.x){
				resetPosition(szWindow.x);
			}
	    	
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	Log.d(Utils.LogTag, "FlashService.onConfigurationChanged -> portrait");

	    	if(layoutParams.x > szWindow.x){
				resetPosition(szWindow.x);
			}
	    	
	    }
		
	}
	
	private void resetPosition(int x_cord_now) {
		if(x_cord_now <= szWindow.x / 2){
			isLeft = true;
			moveToLeft(x_cord_now);

		} else {
			isLeft = false;
			moveToRight(x_cord_now);

		}

    }
	 private void moveToLeft(final int x_cord_now){
		 	final int x = szWindow.x - x_cord_now;

	        new CountDownTimer(500, 5) {
	        	WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floaterView.getLayoutParams();
	            public void onTick(long t) {
	                long step = (500 - t)/5;
	                mParams.x = 0 - (int)(double)bounceValue(step, x );
	                windowManager.updateViewLayout(floaterView, mParams);
	            }
	            public void onFinish() {
	            	mParams.x = 0;
	                windowManager.updateViewLayout(floaterView, mParams);
	            }
	        }.start();
	 }
	 private  void moveToRight(final int x_cord_now){
	        new CountDownTimer(500, 5) {
	        	WindowManager.LayoutParams mParams = (WindowManager.LayoutParams) floaterView.getLayoutParams();
	            public void onTick(long t) {
	                long step = (500 - t)/5;
	                mParams.x = szWindow.x + (int)(double)bounceValue(step, x_cord_now) - floaterView.getWidth();
	                windowManager.updateViewLayout(floaterView, mParams);
	            }
	            public void onFinish() {
	            	mParams.x = szWindow.x - floaterView.getWidth();
	                windowManager.updateViewLayout(floaterView, mParams);
	            }
	        }.start();
	    }
	 
	 private double bounceValue(long step, long scale){
	        double value = scale * Math.exp(-0.055 * step) * Math.cos(0.08 * step);
	        return value;
	    }
	 
	 private int getStatusBarHeight() {
		int statusBarHeight = (int) Math.ceil(25 * getApplicationContext().getResources().getDisplayMetrics().density);
	    return statusBarHeight;
	}
	
	private void floater_click(){ //Floater click event
		if(isFlash){
			if (!isOn){ //Turn on flash
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				camera.setParameters(parameters);
				camera.startPreview();
				isOn = true;
				Toast.makeText(getApplicationContext(), "Flash Light Turned On", Toast.LENGTH_SHORT).show();
			}
			else{ //Turn off flash
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
				camera.setParameters(parameters);
				camera.stopPreview();
				isOn = false;
				Toast.makeText(getApplicationContext(), "Flash Light Turned Off", Toast.LENGTH_SHORT).show();
			}
		}
		else{ // if flasher is not available
			AlertDialog.Builder builder = new AlertDialog.Builder(FlashService.this);
			builder.setTitle("Error!");
			builder.setMessage("Flash is not available on this device");
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}
	
	private void floater_longclick(){
		Log.d(Utils.LogTag, "Into FlashService.floater_longclick() ");
		
		WindowManager.LayoutParams param_remove = (WindowManager.LayoutParams) removeView.getLayoutParams();
		int x_cord_remove = (szWindow.x - removeView.getWidth()) / 2;
		int y_cord_remove = szWindow.y - (removeView.getHeight() + getStatusBarHeight() );
		
		param_remove.x = x_cord_remove;
		param_remove.y = y_cord_remove;
		
		windowManager.updateViewLayout(removeView, param_remove);
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(Utils.LogTag, "FlashService.onStartCommand() -> startId=" + startId);

		if(intent != null){
			Bundle bd = intent.getExtras();
		}

		if(startId == Service.START_STICKY) {
			handleStart();
			return super.onStartCommand(intent, flags, startId);
		}else{
			return  Service.START_NOT_STICKY;
		}

	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(floaterView != null){
			windowManager.removeView(floaterView);
		}

		if(removeView != null){
			windowManager.removeView(removeView);
		}
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(Utils.LogTag, "FlashService.onBind()");
		return null;
	}
	

}
