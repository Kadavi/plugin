package org.schoolsfirstfcu.mobile.plugin.checkcapture;

import java.io.ByteArrayOutputStream;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

public class ProcessImageTask extends AsyncTask<byte[], Void, String> {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private ProcessImageListener callbackListener;
	private int picWidth;
	private int picHeight;
	private int picQuality;
	
	public ProcessImageTask(int width, int height, int quality, ProcessImageListener callback) {
		picWidth = width;
		picHeight = height;
		picQuality = quality;
		callbackListener = callback;
	}
	@Override
	protected void onPreExecute() {
		if (callbackListener != null) {
			callbackListener.onStarted();
		}
	}
	@Override
	protected String doInBackground(byte[]... jpegData) {
		try {
			Bitmap scaleBitmap = getScaledBitmap(jpegData[0], picWidth, picHeight);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			scaleBitmap.compress(Bitmap.CompressFormat.JPEG, picQuality, stream);
			byte[] byteArray = stream.toByteArray();

			String imageData = Base64.encodeToString(byteArray, Base64.DEFAULT);
			
			return imageData;
		} catch (Exception ex) {
			Log.e(TAG, "Failed to take picture.", ex);
		}
		return null;
	}
	@Override
	protected void onPostExecute(String imageData){
		if (callbackListener != null) {
			callbackListener.onCompleted(imageData);
		}
	}
	
	private Bitmap getScaledBitmap(byte[] jpegData, int targetWidth, int targetHeight) {
		
		if (targetWidth <= 0 && targetHeight <= 0) {
			return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
		}

		// get dimensions of image without scaling
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

		// decode image as close to requested scale as possible
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
		Bitmap bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

		// set missing width/height based on aspect ratio
		float aspectRatio = ((float) options.outHeight) / options.outWidth;
		if (targetWidth > 0 && targetHeight <= 0) {
			targetHeight = Math.round(targetWidth * aspectRatio);
		} else if (targetWidth <= 0 && targetHeight > 0) {
			targetWidth = Math.round(targetHeight / aspectRatio);
		}

		// make sure we also
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
	}
	private int calculateInSampleSize(BitmapFactory.Options options, int requestedWidth, int requestedHeight) {
		int originalHeight = options.outHeight;
		int originalWidth = options.outWidth;
		int inSampleSize = 1;
		if (originalHeight > requestedHeight || originalWidth > requestedWidth) {
			int halfHeight = originalHeight / 2;
			int halfWidth = originalWidth / 2;
			while ((halfHeight / inSampleSize) > requestedHeight && (halfWidth / inSampleSize) > requestedWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
}