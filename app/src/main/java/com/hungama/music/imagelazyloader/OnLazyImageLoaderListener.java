package com.hungama.sdk.imagelazyloader;

import android.graphics.Bitmap;

public interface OnLazyImageLoaderListener
{
	void onImageLoadSuccess(String downloadUrl, Bitmap bm);
	void onImageLoadFailed(String downloadUrl, String errorMesg);
}
