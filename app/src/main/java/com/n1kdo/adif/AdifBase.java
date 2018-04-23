package com.n1kdo.adif;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * base class for ADIF records.
 * see http://www.adif.org/304/ADIF_304.htm
 * @author n1kdo
 */
@SuppressLint("DefaultLocale")
abstract class AdifBase
{
    @SuppressLint("SimpleDateFormat")
	static final SimpleDateFormat DATE_FORMATTER_8 = new SimpleDateFormat("yyyyMMdd");
    @SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat DATE_FORMATTER_14 = new SimpleDateFormat("yyyyMMddhhmmss");
    @SuppressLint("SimpleDateFormat")
	static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    static
    {
		DATE_FORMATTER_8.setTimeZone(TimeZone.getTimeZone("GMT"));
		DATE_FORMATTER_14.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
	static String safe(String s)
    {
        return s == null ? "" : s;
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
			System.out.println(line);
			e.printStackTrace();
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
			date = DATE_FORMATTER_8.parse(data);
		}
		catch (Exception e)
		{
			System.err.println("could not parse date "+ data);
			// empty catch -- will return null
		}
		return date;
	}
	
	static Date getDate14(String data)
	{
		Date date = null;
		try
		{
			date = DATE_FORMATTER_14.parse(data);
		}
		catch (Exception e)
		{
			System.err.println("could not parse date "+ data);
			// empty catch -- will return null
		}
		return date;
	}
	
	static Date getDate(String data)
	{
		Date date = null;
		try
		{
			date = DATE_FORMATTER.parse(data);
		}
		catch (Exception e)
		{
			System.err.println("could not parse date "+ data);
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