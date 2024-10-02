package com.hungama.sdk.imagelazyloader;

import android.content.Context;

class HandledErrorTracker
{
    private static HandledErrorTracker _instance = new HandledErrorTracker();
    private Context _context;

    private HandledErrorTracker()
    {

    }

    void init(Context context)
    {
        if(_context == null)
        {
            _context = context;
        }
    }

    static synchronized HandledErrorTracker getHandledErrorTracker()
    {
        if(_instance == null)
        {
            _instance = new HandledErrorTracker();
        }
        return _instance;
    }

    void registerThrowable(Throwable tr)
    {

    }
}
