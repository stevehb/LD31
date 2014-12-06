package com.sblackwell.ld31.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import com.sblackwell.ld31.utils.LogWriter;

import java.util.Date;

public class LogWriterGwtImpl implements LogWriter {
    private DateTimeFormat dateFormat;
    private Date date;

    public LogWriterGwtImpl() {
        String stampFormat = "HH:mm:ss.SSS";
        DefaultDateTimeFormatInfo info = new DefaultDateTimeFormatInfo();
        dateFormat  = new DateTimeFormat(stampFormat, info) {};  // <= trick here
        date = new Date();
    }

    public void write(String tag, String msg) {
        date.setTime(TimeUtils.millis());
        String timeStamp = dateFormat.format(date);
        String fullMsg = tag + " - " + timeStamp + ": " + msg;
        if(Gdx.app != null) {
            Gdx.app.log(tag, fullMsg);
        } else {
            System.out.println(tag + ": " + fullMsg);
        }
    }
}
