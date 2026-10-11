package com.n1kdo.adif;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * base class for ADIF records.
 * see <a href="https://www.adif.org/317/ADIF_317.htm">...</a>
 * @author n1kdo
 */
@SuppressLint("DefaultLocale")
abstract class AdifBase
{
    private static final String TAG = "AdifBase";

    // SimpleDateFormat is NOT thread-safe, and these formatters are used from the main
    // thread, background services, and async tasks. Give each thread its own instance.
    private static SimpleDateFormat gmtFormatter(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    }

	static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER_8 =
            ThreadLocal.withInitial(() -> gmtFormatter("yyyyMMdd"));
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER_14 =
            ThreadLocal.withInitial(() -> gmtFormatter("yyyyMMddhhmmss"));
	static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER =
            ThreadLocal.withInitial(() -> gmtFormatter("yyyy-MM-dd hh:mm:ss"));
    
	static String safe(String s)
    {
        return s == null ? "" : s;
    }

    static Date safe_date(Date d) {
    	return d == null ? new Date(0) : d;
	}

    static String [] parseAdifLine(String line)
	{
		int lbp, cp, rbp;
		try
		{
			line = line.trim();
			lbp = line.indexOf('<');
			cp  = line.indexOf(':', lbp);
			rbp = line.indexOf('>', cp);
			if (lbp != -1 && cp != -1 && rbp != -1)
			{
				String fn = line.substring(lbp+1, cp);
				int dl = Integer.parseInt(line.substring(cp+1, rbp));
				String data = line.substring(rbp+1, rbp+1+dl);
				return  new String[] {fn, data};
			}
			if (lbp != -1 && rbp != -1)
			{
				return new String [] { line.substring(lbp+1, rbp) };
			}
			//noinspection ZeroLengthArrayAllocation,
			return new String[]{};
		}
		catch (Exception e)
		{
			Log.w(TAG, "could not parse line: " + line);
			//e.printStackTrace();
			//noinspection ZeroLengthArrayAllocation
			return new String[]{};
		}
	}
	
	static boolean getBoolean(String data)
	{
		return data.toUpperCase().startsWith("Y");
	}
	
	static int getInt(String data)
	{
		int i;
		try
		{
			i = Integer.parseInt(data);
		}
		catch (NumberFormatException e)
		{
			i =-1;
		}
		return i;
	}
	
	static Date getDate8(String data)
	{
		Date date = null;
		try
		{
			date = DATE_FORMATTER_8.get().parse(data);
		}
		catch (Exception e)
		{
			Log.w(TAG, "could not parse date " + data);
			// empty catch -- will return null
		}
		return date;
	}
	
	/** @noinspection unused*/
    static Date getDate14(String data)
	{
		Date date = null;
		try
		{
			date = DATE_FORMATTER_14.get().parse(data);
		}
		catch (Exception e)
		{
			Log.w(TAG, "could not parse date " + data);
			// empty catch -- will return null
		}
		return date;
	}
	
	static Date getDate(String data)
	{
		Date date = null;
		try
		{
			date = DATE_FORMATTER.get().parse(data);
		}
		catch (Exception e)
		{
			Log.w(TAG, "could not parse date " + data);
			// empty catch -- will return null
		}
		return date;
	}
	
	static String deCapitalize(String s)
	{
		char [] chars = s.toCharArray();
		char c;
		boolean firstLetter = true;
		for (int i=0; i<chars.length; i++)
		{
			if (firstLetter)
			{
				firstLetter = false;
				continue;
			}
			c = chars[i];
			if (c == ' ')
			{
				firstLetter = true;
				continue;
			}
			chars[i] = Character.toLowerCase(c);
		}
		return new String(chars);
	}
	
	static String [] getStringArray(String s)
	{
	    return s.split(",");
	}
}