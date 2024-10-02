package com.hungama.sdk.imagelazyloader;

import java.util.List;

interface OnImageReadyListener
{
	void onImageLoadComplete(DiskCache.DiskBitmapLoadInfo info);
	void onImageLoadComplete(ImageDownloader.RemoteBitmapAssetInfo info);
    void onImageLoadComplete(List<ImageDownloader.RemoteBitmapAssetInfo> list);

}
