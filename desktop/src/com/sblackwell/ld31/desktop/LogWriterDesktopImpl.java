package com.sblackwell.ld31.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.sblackwell.ld31.utils.L;
import com.sblackwell.ld31.utils.LogWriter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogWriterDesktopImpl implements LogWriter {
    private final String MESG_FORMAT;
    private SimpleDateFormat dateFormat;
    private Date date;

    public LogWriterDesktopImpl() {
        MESG_FORMAT = "%s - %s: %s";
        String stampFormat = "HH:mm:ss.SSS";
        dateFormat = new SimpleDateFormat(stampFormat);
        date = new Date();
    }

    public void write(String tag, String msg) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement ste = null;
        boolean pastLogger = false;
        for(int i = 0; i < elements.length; i++) {
            if(!pastLogger && elements[i].getClassName().equals(L.class.getCanonicalName())) {
                pastLogger = true;
                continue;
            }
            if(pastLogger && !elements[i].getClassName().equals(L.class.getCanonicalName())) {
                ste = elements[i];
                break;
            }
        }

        date.setTime(TimeUtils.millis());
        String className = (ste == null) ? "UNKNOWN:??" : (ste.getFileName() + ":" + ste.getLineNumber());
        String timeStamp = dateFormat.format(date);
        String fullMsg = String.format(MESG_FORMAT, timeStamp, className, msg);
        if(Gdx.app != null) {
            Gdx.app.log(tag, fullMsg);
        } else {
            System.out.println(tag + ": " + fullMsg);
        }
    }
}
