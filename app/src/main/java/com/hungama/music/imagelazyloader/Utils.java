package com.hungama.sdk.imagelazyloader;

class Utils
{
    static String getKeyFroUrl(String downloadUrl, String extention)
    {
        String key;

        try
        {
            key = downloadUrl.replace("http://", "");
            key = key.replace("https://", "");
            key = key.replaceAll("www", "");
            key = key.replaceAll(":", "");
            key = key.replaceAll("/", "");

            key = key.replaceAll(".jpg", "");
            key = key.replaceAll(".png", "");
            key = key.replaceAll(".jpeg", "");
            key = key.replaceAll(".bmp", "");
            key = key.replaceAll(".gif", "");

            key = key.replaceAll("\\.", "");
            key = key.replaceAll("\\?", "");
            key = key.replaceAll("=", "");
            key = key.replaceAll("&", "");
            key = key.replaceAll("_", "");
            key = key.replaceAll("-", "");
        }
        catch (Exception ex)
        {
            HandledErrorTracker.getHandledErrorTracker().registerThrowable(ex);
            key = downloadUrl.replaceAll("/", "");
        }

        key = key + extention;
        return key;
    }
}
