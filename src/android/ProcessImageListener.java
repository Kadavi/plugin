package org.schoolsfirstfcu.mobile.plugin.checkcapture;

public interface ProcessImageListener {
	void onStarted();
	void onCompleted(String imageData);
}

