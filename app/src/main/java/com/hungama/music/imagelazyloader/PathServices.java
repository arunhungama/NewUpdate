package com.hungama.sdk.imagelazyloader;

import android.content.Context;

class PathServices
{
    private final PathManager _pathManager;

    PathServices(Context context)
    {
        _pathManager = new PathManager(context);
    }

    PathManager getPathManager()
    {
        return _pathManager;
    }

    void refreshPathManager()
    {
        _pathManager.recreateStoragePath();
    }

}
