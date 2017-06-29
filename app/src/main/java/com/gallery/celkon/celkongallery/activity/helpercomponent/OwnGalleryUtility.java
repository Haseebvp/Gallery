package com.gallery.celkon.celkongallery.activity.helpercomponent;


public class OwnGalleryUtility {
	public static void runOnUIThread(Runnable runnable) {
		runOnUIThread(runnable, 0);
	}

	public static void runOnUIThread(Runnable runnable, long delay) {
		if (delay == 0) {
			App.applicationHandler.post(runnable);
		} else {
			App.applicationHandler.postDelayed(runnable, delay);
		}
	}
}
