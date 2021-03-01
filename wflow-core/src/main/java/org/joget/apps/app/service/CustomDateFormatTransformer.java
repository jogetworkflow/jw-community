package org.joget.apps.app.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.joget.commons.util.TimeZoneUtil;
import org.simpleframework.xml.transform.Transform;

public class CustomDateFormatTransformer implements Transform<Date>
{
    private DateFormat dateFormat;

    public CustomDateFormatTransformer(){
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S z", Locale.US);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone(TimeZoneUtil.getServerTimeZoneID()));
    }

    @Override
    public Date read(String value) throws Exception
    {
        return dateFormat.parse(value);
    }


    @Override
    public String write(Date value) throws Exception
    {
        return dateFormat.format(value);
    }

}