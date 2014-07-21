package org.schoolsfirstfcu.mobile.plugin.checkcapture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;

public class CropMarks extends View {
	private Path path = new Path();
	private Paint paint = new Paint();
	private DashPathEffect pathEffect;
	private Point topLeftPts;
	private Point topRightPts;
	private Point bottomLeftPts;
	private Point bottomRightPts;
	
	public CropMarks(Context context) {
		super(context);
	}

	public void draw(int pixelWidth, int pixelHeight, int frameBorderSize, int headerHeight) {
		pathEffect = new DashPathEffect(new float[] { 10, 10 }, 0);
		topLeftPts = new Point(pixelsToDp(frameBorderSize), pixelsToDp(frameBorderSize));
		topRightPts = new Point(pixelWidth - pixelsToDp(headerHeight) - pixelsToDp(frameBorderSize), pixelsToDp(frameBorderSize));
		bottomLeftPts = new Point(pixelsToDp(frameBorderSize), pixelHeight - pixelsToDp(frameBorderSize) * 2);
		bottomRightPts = new Point(pixelWidth - pixelsToDp(headerHeight) - pixelsToDp(frameBorderSize), pixelHeight - pixelsToDp(frameBorderSize) * 2);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		int borderLength = pixelsToDp(35);
		paint.setColor(0xFF2D4452);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(4);
		paint.setPathEffect(pathEffect);

		path.moveTo(topLeftPts.x, topLeftPts.y + borderLength);
		path.lineTo(topLeftPts.x, topLeftPts.y);
		path.lineTo(topLeftPts.x + borderLength, topLeftPts.y);

		path.moveTo(topRightPts.x - borderLength, topRightPts.y);
		path.lineTo(topRightPts.x, topRightPts.y);
		path.lineTo(topRightPts.x, topRightPts.y + borderLength);

		path.moveTo(bottomRightPts.x, bottomRightPts.y - borderLength);
		path.lineTo(bottomRightPts.x, bottomRightPts.y);
		path.lineTo(bottomRightPts.x - borderLength, bottomRightPts.y);

		path.moveTo(bottomLeftPts.x + borderLength, bottomLeftPts.y);
		path.lineTo(bottomLeftPts.x, bottomLeftPts.y);
		path.lineTo(bottomLeftPts.x, bottomLeftPts.y - borderLength);

		canvas.drawPath(path, paint);
	}
	private int pixelsToDp(int pixels) {
		float density = getResources().getDisplayMetrics().density;
		return Math.round(pixels * density);
	}
}