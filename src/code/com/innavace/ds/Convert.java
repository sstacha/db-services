package com.innavace.ds;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: sstacha
 * Date: 6/21/13
 * Time: 12:17 PM
 * Handles basic data conversions from string to another type
 */
public class Convert {
    public static Logger log = Logger.getLogger(Convert.class);
    private static final String HEXES = "0123456789ABCDEF";

    // ---------------------------------------------------------- toDate -
    /**
     *  Attempts to parse a <code>String</code> to a <code>java.util.Date</code>
     *  datatype.
     *
     *@param	text_date		generic string to be converted to Date
     *@param    parser          how to format the date
     *@return				    Date value converted
     */
    public static synchronized Date parseDate(String text_date, SimpleDateFormat parser)
    {
        Date theDate = null;
        //log.debug("date string is: " + text_date);
        if (parser != null)
        {
            log.debug("format mask is: " + parser.toPattern());
            try {theDate = parser.parse(text_date);}
            catch (Exception ex) { log.debug("exception when attempting to convert: " + ex.getMessage()); }
        }
        return theDate;
    }
    public static synchronized Date parseDate(String text_date, DateFormat parser)
    {
        Date theDate = null;
        //log.finest("date string is: " + text_date);
        if (parser != null)
        {
            try {theDate = parser.parse(text_date);}
            catch (Exception ex) { log.debug("exception when attempting to convert: " + ex.getMessage()); }
        }
        return theDate;
    }

    // ---------------------------------------------------------- toDate -
    /**
     *  Converts an <code>Object</code> to a <code>java.util.Date</code>
     *  datatype.
     *
     *@param	object		generic object to be converted to Date
     *@return				Date value converted
     */
    public static synchronized Date toDate(Object object)
    {
        //log.finest("converting to a date...");
        Date value = null;
        String string = "";
        SimpleDateFormat sdf = new SimpleDateFormat();
        //DateFormat df = null;

        if (object != null)
        {
            if (object instanceof Date)
                return (Date)object;

            if (object instanceof String)
            {
                //log.finest("object is a string...");
                // try and make it a date type if we have a value
                if (((String)object).length() > 0)
                {
                    string = (String)object;
                    //log.finest("starting string is: " + string);

                    // try the basic formats first
                    if (value == null)
                        value = parseDate(string, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
                    if (value == null)
                        value = parseDate(string, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT));
                    if (value == null)
                        value = parseDate(string, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT));
                    if (value == null)
                        value = parseDate(string, DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL));
                    // try some of the common custom formats: NOTE: try in most specific to least specific to prevent errors
                    sdf.applyPattern("EEE MMM d HH:mm:ss z yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd'T'HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HHmmss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HHmmss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss.SZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd'T'HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HHmmssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HHmmssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ssZ");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd'T'HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HHmmss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss.S");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd'T'HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMMdd'T'HHmmss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd'T'HHmmss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MMM-dd HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd-MMM-yyyy HH:mm:ss");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd-MMM-yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd MMM yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd/MM/yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd/MM/yy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d/MM/yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d/M/yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d/M/yy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd.MM.yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("dd.MM.yy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d.MM.yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d.M.yyyy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("d.M.yy");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyy.MM.dd");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyy-MM-dd");
                    if (value == null)
                        value = parseDate(string, sdf);
                    sdf.applyPattern("yyyyMMdd");
                    if (value == null)
                        value = parseDate(string, sdf);
                    if (value == null)
                        log.debug("attempted to parse date string but didn't find a match!");
                }
            }

            if (object instanceof Number)
            {
                try
                {
                    log.debug("attempting to convert to long");
                    long long_value = ((Number)object).longValue();
                    value = new Date(long_value);
                }
                catch (Exception ex)
                {
                    log.error("Exception occured converting Number <"	+ object.toString()	+ "> to a Date");
                }
            }
        }

        return value;
    }

    // ---------------------------------------------------------- toBool -
    /**
    *  Converts to a <code>boolean</code> value.
    *
    *@param	object		generic object to be converted to int
    *@return			boolean value converted
    */
    public static synchronized boolean toBool(Object object)
    {
        boolean value = false;
        boolean set = false;
        String string;

        int integer;

        log.debug("attempting to convert generic object to a boolean value...");
        log.debug("object: " + object);

        if (object != null)
        {
            if (object instanceof String)
            {
                log.debug("is instance of String...");
                // try and make it an integer type if we have a value
                if (((String)object).length() > 0)
                {
                    string = (String)object;
                    try
                    {
                        if (string.equalsIgnoreCase("YES") || string.equalsIgnoreCase("TRUE") || string.equalsIgnoreCase("Y") || string.equalsIgnoreCase("T"))
                        {
                            value = true;
                            set = true;
                        }
                        if (string.equalsIgnoreCase("NO") || string.equalsIgnoreCase("FALSE") || string.equalsIgnoreCase("N") || string.equalsIgnoreCase("F"))
                        {
                            value = false;
                            set = true;
                        }
                        // note: setting the object back to an integer will cause the number part next to fire
                        if (!set)
                            object = new Integer((String)object);
                    }
                    catch (NumberFormatException nfe)
                    {
                        log.error("Exception occured converting String <"	+ object.toString()	+ "> to boolean");
                    }
                }
            }
            if (object instanceof Boolean)
            {
                log.debug("is instance of Boolean...");
                value = ((Boolean)object).booleanValue();
            }
            // any integer not 0 is true...
            if (object instanceof Number)
            {
                log.debug("is instance of Number...");
                try
                {
                    integer = ((Number)object).intValue();
                    if (integer != 0)
                        value = true;
                }
                catch (Exception ex)
                {
                    log.error("Exception occured converting Number <"	+ object.toString()	+ "> to integer");
                }
            }
        }
        return value;
    }

    // ----------------------------------------------------------- toLong -
    /**
    *  Converts to a <code>Long</code> value.
    *
    *@param	object		generic object to be converted to Long
    *@return			Long value converted
    */
    public static Long toLong(Object object)
    {
        return new Long(toLng(object));
    }

    // ----------------------------------------------------------- toLng -
    /**
    *  Converts to a <code>long</code> value.
    *
    *@param	object		generic object to be converted to long
    *@return			long value converted
    */
    public static synchronized long toLng(Object object)
    {
        long value = 0L;

        if (object != null)
        {
            if (object instanceof String)
            {
                // try and make it an integer type if we have a value
                if (((String)object).length() > 0)
                {
                    try
                    {
                        value = Long.parseLong((String)object);
                    }
                    catch (NumberFormatException nfe)
                    {
                        log.error("Exception occured converting String <"	+ object.toString()	+ "> to long");
                    }
                }
            }
            if (object instanceof Number)
            {
                try
                {
                    value = ((Number)object).longValue();
                }
                catch (Exception ex)
                {
                    log.error("Exception occured converting Number <"	+ object.toString()	+ "> to long");
                }
            }
        }
        return value;
    }

    // ----------------------------------------------------------- toInteger -
    /**
    *  Converts to a <code>Integer</code> value.
    *
    *@param	object		generic object to be converted to int
    *@return			Integer value converted
    */
    public static Integer toInteger(Object object)
    {
        return new Integer(toInt(object));
    }
    // ----------------------------------------------------------- toInt -
    public static synchronized int toInt(boolean value)
    {
        if (value)
            return 1;
        else
            return 0;
    }

    // ----------------------------------------------------------- toInt -
    /**
    *  Converts to a <code>int</code> value
    *
    *@param	object		generic object to be converted to int
    *@return			int value converted
    */
    public static synchronized int toInt(Object object)
    {
        int integer = 0;

        if (object != null)
        {
            if (object instanceof String)
            {
                //removing spaces
                String strToUse =   ((String)object).trim();
                // try and make it an integer type if we have a value
                if (strToUse.length() > 0)
                {
                    try
                    {
                        // try first to check for a true / false and convert
                        if (strToUse.equalsIgnoreCase("TRUE"))
                            integer = 1;
                        else if (strToUse.equalsIgnoreCase("FALSE"))
                            integer = 0;
                        else
                            integer = Integer.parseInt(strToUse);
                    }
                    catch (NumberFormatException nfe)
                    {
                        log.error("Exception occured converting String <"	+ object.toString()	+ "> to integer");
                    }
                }
            }
            if (object instanceof Number)
            {
                try
                {
                    integer = ((Number)object).intValue();
                }
                catch (Exception ex)
                {
                    log.error("Exception occured converting Number <"	+ object.toString()	+ "> to integer");
                }
            }
        }
        return integer;
    }

    // ----------------------------------------------------------- toDouble -
    /**
    *  Converts to a <code>Double</code> value.
    *
    *@param	object		generic object to be converted to Double
    *@return			Double value converted
    */
    public static synchronized Double toDouble(Object object)
    {
        return new Double(toDbl(object));
    }

    // ----------------------------------------------------------- toDbl -
    /**
    *  Converts to a <code>double</code> value
    *
    *@param	object		generic object to be converted to double
    *@return			double value converted
    */
    public static synchronized double toDbl(Object object)
    {
        double dbl = 0.0;

        if (object != null)
        {
            if (object instanceof String)
            {
                // try and make it an integer type if we have a value
                if (((String)object).length() > 0)
                {
                    try
                    {
                        dbl = Double.parseDouble((String)object);
                    }
                    catch (NumberFormatException nfe)
                    {
                        log.error("Exception occured converting String <"	+ object.toString()	+ "> to double");
                    }
                }
            }
            if (object instanceof Number)
            {
                try
                {
                    dbl = ((Number)object).doubleValue();
                }
                catch (Exception ex)
                {
                    log.error("Exception occured converting Number <"	+ object.toString()	+ "> to double");
                }
            }
        }
        return dbl;
    }

	// ----------------------------------------------------------- toFloat -
	/**
	*  Converts to a <code>Float</code> value.
	*
	*@param	object		generic object to be converted to Float
	*@return			Float value converted
	*/
	public static synchronized Float toFloat(Object object)
	{
	    return new Float(toFlt(object));
	}

	// ----------------------------------------------------------- toFlt -
	/**
	*  Converts to a <code>float</code> value
	*
	*@param	object		generic object to be converted to float
	*@return			float value converted
	*/
	public static synchronized float toFlt(Object object)
	{
	    float value = 0;

	    if (object != null)
	    {
	        if (object instanceof String)
	        {
	            // try and make it an integer type if we have a value
	            if (((String)object).length() > 0)
	            {
	                try
	                {
	                    value = Float.parseFloat((String)object);
	                }
	                catch (NumberFormatException nfe)
	                {
	                    log.error("Exception occured converting String <"	+ object.toString()	+ "> to float");
	                }
	            }
	        }
	        if (object instanceof Number)
	        {
	            try
	            {
	                value = ((Number)object).floatValue();
	            }
	            catch (Exception ex)
	            {
	                log.error("Exception occured converting Number <"	+ object.toString()	+ "> to float");
	            }
	        }
	    }
	    return value;
	}

	// ----------------------------------------------------------- toShort -
	/**
	*  Converts to a <code>Short</code> value.
	*
	*@param	object		generic object to be converted to Short
	*@return			Short value converted
	*/
	public static synchronized Short toShort(Object object)
	{
	    return new Short(toSht(object));
	}

	// ----------------------------------------------------------- toSht -
	/**
	*  Converts to a <code>short</code> value
	*
	*@param	object		generic object to be converted to float
	*@return			float value converted
	*/
	public static synchronized short toSht(Object object)
	{
	    short value = 0;

	    if (object != null)
	    {
	        if (object instanceof String)
	        {
	            // try and make it an integer type if we have a value
	            if (((String)object).length() > 0)
	            {
	                try
	                {
	                    value = Short.parseShort((String)object);
	                }
	                catch (NumberFormatException nfe)
	                {
	                    log.error("Exception occured converting String <"	+ object.toString()	+ "> to short");
	                }
	            }
	        }
	        if (object instanceof Number)
	        {
	            try
	            {
	                value = ((Number)object).shortValue();
	            }
	            catch (Exception ex)
	            {
	                log.error("Exception occured converting Number <"	+ object.toString()	+ "> to short");
	            }
	        }
	    }
	    return value;
	}

	// ----------------------------------------------------------- toBigDecimal -
	/**
	*  Converts to a <code>BigDecimal</code> value.
	*
	*@param	object		generic object to be converted to Long
	*@return			Long value converted
	*/

	public static synchronized BigDecimal toBigDecimal(Object object) {return Convert.toBigDecimal(object, false);}
	public static synchronized BigDecimal toBigDecimal(Object object, boolean returnNulls)
	{
		if (object == null)
		{
			if (returnNulls)
				return null;
			else
				return new BigDecimal(0);
		}

		if (object instanceof Number)
		{
			if (object instanceof BigInteger)
				return new BigDecimal((BigInteger)object);
			if (object instanceof Double)
				return new BigDecimal((Double)object);
			if (object instanceof Long)
				return new BigDecimal((Long)object);
			if (object instanceof Integer)
				return new BigDecimal((Integer)object);
			if (object instanceof Float)
				return new BigDecimal((Float)object);
			if (object instanceof Short)
				return new BigDecimal((Short)object);
		}
	    return new BigDecimal(object.toString());
	}

    // -------------------------------------------------------- toString -
    /**
    *  Converts to a <code>String</code> value
    *
    * @param	object			generic object to be converted to string
    * @return         		string value converted
    */

    public static synchronized String toString(Object object)
    {
        return toString(object, false);
    }

    public static synchronized String toParameter(Date date) {return toString(date, "yyyy-MM-dd'T'HH:mm:ss.SZ");}
    public static synchronized String toString(Date date)  {return toString(date, "dd-MMM-yyyy");}
    public static synchronized String toString(Date date, String format)
    {
        String dateString = "";
        if (date != null)
        {
            SimpleDateFormat custom_format = new SimpleDateFormat(format);
            dateString = custom_format.format(date);
        }
        return dateString;
    }
    /**
     * Converts two Date objects to a string representation of the date range using the following rules:
     *   If date2 is null or date1 == date2:                 "1 Aug 2003"
     *   If date1 and date2 have the same month and year:    "1 - 5 Aug 2003"
     *   If date1 and date2 have the same year:              "1 Aug - 6 Sep 2003"
     *   If date1 and date2 are completely different:        "1 Aug 2002 - 7 Sep 2003"
     * @param date1 the start date
     * @param date2 the end date
     * @return a String representation of the date range
     */
    public static synchronized String toDateRange(Date date1, Date date2) {
        if(date1 != null) {
            Calendar c1 = new GregorianCalendar();
            c1.setTime(date1);
            c1 = new GregorianCalendar(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DATE));
            if(date2 == null) {
                return toString(c1.getTime(), "d MMM yyyy");
            }
            else {
                Calendar c2 = new GregorianCalendar();
                c2.setTime(date2);
                c2 = new GregorianCalendar(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DATE));
                if(c1.getTime().compareTo(c2.getTime()) == 0) {
                    return toString(c1.getTime(), "d MMM yyyy");
                }
                else if(c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
                    return toString(c1.getTime(), "d") + " - " + toString(c2.getTime(), "d MMM yyyy");
                }
                else {
                    if(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
                        return toString(c1.getTime(), "d MMM") + " - " + toString(c2.getTime(), "d MMM yyyy");
                    }
                    else {
                        return toString(c1.getTime(), "d MMM yyyy") + " - " + toString(c2.getTime(), "d MMM YYYY");
                    }
                }
            }
        }
        return "";
    }
    public static synchronized String toDatabaseString(String string)
    {
        // escape all sql special chars for a value in a sql statement
        string = Convert.toString(string);
        return replace(string, "'", "''", "''");
    }
    public static synchronized String toDatabaseString(Date date)
    {
        if (date == null)
            return "NULL";
        SimpleDateFormat custom_format = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        StringBuffer sql_date = new StringBuffer("{ts '");
        sql_date.append(custom_format.format(date)).append("'}");
        return sql_date.toString();
    }
    /**
     * @param object          Any object to convert to string
     * @param returnNulls     by default returns an empty string; true will return null
     *
     * @return string value   the string value converted
     */
    public static synchronized String toString(Object object, boolean returnNulls)
    {
        String string = "";

        if ((returnNulls) && (object == null))
            string = null;
        if (object != null)
        {
            if (object instanceof String)
                string = (String)object;
            else if (object instanceof java.util.Date)
            {
                Date date = (Date)object;
                SimpleDateFormat custom_format = new SimpleDateFormat("dd-MMM-yyyy");
                string = custom_format.format(date);
            }
            else
            {
                try
                {
                    string = object.toString();
                }
                catch (Exception ex)
                {
                    log.error("Exception occurred converting object to string");
                }
            }
        }
        return string;
    }
    public static synchronized String toDatabaseString(Collection list, String delimeter)
    {
        return toDatabaseString(list, delimeter, "");
    }
    public static synchronized String toDatabaseString(Object obj, String text_qualifier)
    {
        StringBuffer string = new StringBuffer();

        if (obj == null)
            string.append("NULL");
        else if (obj instanceof Date)
            string.append(Convert.toDatabaseString((Date)obj));
        else if (obj instanceof Boolean)
        {
            boolean bool = ((Boolean)obj).booleanValue();
            if (bool)
                string.append("1");
            else
                string.append("0");
        }
        else if (obj instanceof String)
        {
            // sas 01.03.12 - don't want to add quotes to hex strings for byte fields.
            // todo : later use an annotation instead of this assumption that everything starting with 0x is hex
            if (((String)obj).toUpperCase().trim().startsWith("0X"))
                string.append(Convert.toDatabaseString((String)obj));
            else
                string.append(text_qualifier).append(Convert.toDatabaseString((String)obj)).append(text_qualifier);
        }
        else if (Convert.isNumeric(Convert.toString(obj)))
            string.append(Convert.toString(obj));
        else
            string.append(text_qualifier).append(Convert.toDatabaseString(Convert.toString(obj))).append(text_qualifier);

        return string.toString();
    }
    public static synchronized String toDatabaseString(Collection list, String delimeter, String text_qualifier)
    {
        Iterator iterator;
        StringBuffer string = new StringBuffer();
        if (text_qualifier == null)
            text_qualifier = "";

        if (list != null && delimeter != null)
        {
            iterator = list.iterator();

            while (iterator.hasNext())
            {
                Object obj = iterator.next();

                if (string.length() > 0)
                    string.append(delimeter);
                string.append(toDatabaseString(obj, text_qualifier));
            }
        }
        return string.toString();
    }

    public static synchronized String toDebugString(Object _pojo)
    {
        Class dataClass = _pojo.getClass();
        StringBuffer data = new StringBuffer(100);
        data.append("\n");
        if (!dataClass.getName().equalsIgnoreCase("java.lang.Object"))
            data.append("---------- ").append(dataClass.getSimpleName()).append(" ----------\n");

        while ((dataClass != null) && (dataClass != Object.class))
        {
            Field fieldList[] = dataClass.getDeclaredFields();
            AccessibleObject.setAccessible(fieldList, true);
            StringBuffer fldstr = new StringBuffer(50);
            for (Field fld : fieldList)
            {
//                String typeclass = fld.getType().getName();
//                if (isValidType(fld.getType()))
//                {
                //data += "(" + typeclass + ")";
                fldstr.setLength(0);
                fldstr.append(fld.getName()).append(" = ");
                try
                {
                    if (fld.get(_pojo) != null && fld.get(_pojo).toString() != null && fld.get(_pojo).toString().length() > 0)
                        fldstr.append(fld.get(_pojo));
                }
                catch (IllegalAccessException iae)
                {
                    fldstr.append("????");
                }
                data.append(fldstr).append("\n");
//                }
//                else
//                    if (verbose)
//                        log.debug("(" + typeclass + ") " + fld.getName() + " skipped...");
            }
            dataClass = dataClass.getSuperclass();
        }
        data.append("-----------------------------------\n");
        return data.toString();
    }

    /**
     * @param 	list			list of items to be concated to a string
     * @return String containing a comma separated values from the collection
     */
    public static synchronized String toString(Collection list)
    {
        return toString(list, ", ", null);
    }
    /**
     * @param 	list			list of items to be concated to a string
     * @param	delimeter		string to use as the delimeter separate the list if items
     *
     * @return String containing a comma separated values from the collection
     **/
    public static synchronized String toString(Collection list, String delimeter)
    {
        return toString(list, delimeter, null);
    }
    public static synchronized String toString(Collection list, String delimeter, String text_qualifier)
    {
        Iterator iterator = null;
        String string = "";

        // todo: replace the string functions with a stringbuffer for the appending and return the toString()
        if (list != null && delimeter != null)
        {
            iterator = list.iterator();
            //iterator = list.listIterator();
            while (iterator.hasNext())
            {
                Object obj = iterator.next();
                if (string.length() > 0)
                    string += delimeter;
                if ((obj instanceof String) && (text_qualifier != null))
                    string += text_qualifier + Convert.toString(obj) + text_qualifier;
                else
                    string += Convert.toString(obj);
            }
        }
        return string;

    }
    /**
    *  parses a string to a list of strings given a delimiter.
    *
    * NOTE: will handle if 2 separators and return an empty value
    * Example: ,,2,3 will return 2 empty strings for index 1 & 2
    * then values 2 and 3 as a string for index 3 & 4.
    *
    *@param	string		the original string.
    *@param	delimeter	the delimter to separate strings on
    *@return			list of string values
    */
    // ------------------------------------------------------- toList -
    public static synchronized List<String> toList(String string, String delimeter)
    {
        // NOTE: tokenizer didn't work so doing the string manips myself
        int pos_start = 0;
        int pos_stop = 0;
        List<String> paramlist = new ArrayList<String>();

        if (string != null && delimeter != null)
        {
            while ((pos_stop = string.indexOf(delimeter, pos_start)) != -1)
            {
                if (pos_stop == 0)
                    paramlist.add("");
                else
                    paramlist.add(string.substring(pos_start, pos_stop));
                pos_start = pos_stop + delimeter.length();
            }
            if (pos_start != string.length())
                paramlist.add(string.substring(pos_start).trim());
        }
        return paramlist;

    }
    // -------------------------------------------------------- isNumeric -
    /**
     *  check if string contains only numbers
     *
     *@param	object		object to check
     *@return				if is numeric value
     */
    public static synchronized boolean isNumeric(Object object)
    {
        boolean is_digits = false;
        boolean has_decimal = false;

        String string = "";

        if (object == null)
            return false;
        // NOTE: byte, int, long, doubble, etc. all derive from number
        //		which by nature must be numeric
        if (object instanceof Number)
            return true;
        // try and convert everything else to a string and evaluate
        string = toString(object);
        if (string.length() > 0)
        {
            is_digits = true;
            char[] chars = string.toCharArray();
            for (int x = 0; x < chars.length; x++)
            {
                if (x == 0 && chars[x] == '-')
                    continue;
                if (!Character.isDigit(chars[x]))
                {
                    // we allow 1 and only 1 decimal
                    if ((Character.toString(chars[x])).equalsIgnoreCase("."))
                    {
                        if (has_decimal)
                        {
                            is_digits = false;
                            break;
                        }
                        else
                            has_decimal = true;
                    }
                    else
                    {
                        is_digits = false;
                        break;
                    }
                }
            }
        }
        return is_digits;
    }

	/**
	 * Round a float value to a specified number of decimal
	 * places.
	 *
	 * @param val the value to be rounded.
	 * @param places the number of decimal places to round to.
	 * @return val rounded to places decimal places.
	 */
	public static synchronized float round(float val, int places)
	{
		BigDecimal bd = new BigDecimal(Float.toString(val));
		bd = bd.setScale(places,BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

    /**
     * Round a double value to a specified number of decimal
     * places.
     *
     * @param val the value to be rounded.
     * @param places the number of decimal places to round to.
     * @return val rounded to places decimal places.
     */
    public static synchronized double round(double val, int places)
    {
      // see the Javadoc about why we use a String in the constructor
      // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
      BigDecimal bd = new BigDecimal(Double.toString(val));
	  return round(bd, places);
    }

	public static synchronized double round(BigDecimal bd, int places)
	{
		bd = bd.setScale(places,BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Rounds the double value to the speicified decimal points and returns that number of decimal
	 * points as a String.  For example: 4 to 2 places would return 4.00
	 *
	 * @param val : the double value to set the number of decimal digits for
	 * @param places : the number of decimal digits to set
	 * @return String containing the right number of digits for the number
	 */
    public static synchronized String toString(double val, int places)
    {


        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(places);
		nf.setMinimumFractionDigits(places);
        return nf.format(val);
    }

	/**
	 * Rounds the long value to the speicified decimal points and returns that number of decimal
	 * points as a String.  For example: 4 to 2 places would return 4.00
	 *
	 * @param val : the long value to set the number of decimal digits for
	 * @param places : the number of decimal digits to set
	 * @return String containing the right number of digits for the number
	 */
    public static synchronized String toString(long val, int places)
    {


        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		nf.setMaximumFractionDigits(places);
		nf.setMinimumFractionDigits(places);
        return nf.format(val);
    }

	public static synchronized String toHTML(String string)
	{
		string = Convert.toString(string);
		string = Convert.replace(string, "\n", "<br>");
		return string;
	}

    public static synchronized String toSHA1(String key, String value) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = key.getBytes();
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value.getBytes());

            //  Covert array of Hex bytes to a String
            return toHexString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static synchronized String toHexString( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
    // --------------------------------------------------------- replace -
     /**
      * Replaces all occurances of the specified sub-string to find with the
      * specified sub-string to replace with.
      *
      * @return The modified string.
      * @param	string			 The original string.
      * @param	replaceString The string to replace.
      * @param	substring			 The string to replace with.
      */
    public static synchronized String replace(String string, String replaceString, String substring)
    {
        return replace(string, replaceString, substring, null);
    }
    public static synchronized String replace(String string, String replaceString, String substring, String omit_string)
    {
	    int pos_start = 0;
	    int pos_stop;
        StringBuilder sb = new StringBuilder();

	    if (string == null)
	        string = "";
	    if (replaceString == null)
	        replaceString = "";
	    if (substring == null)
	        substring = "";
	    if (omit_string == null)
	        omit_string = "";
	    if (string.length() > 0 && replaceString.length() > 0)
	    {
	        while ((pos_stop = string.indexOf(replaceString, pos_start)) >= 0)
	        {
	            sb.append(string.substring(pos_start, pos_stop));
	            // we might have an omitted value
	            if (omit_string.length() > 0)
	            {
	                if (string.startsWith(omit_string, pos_stop))
	                {
	                    // omitted value: prepend the original string and move start to stop + omitstring length
	                    sb.append(omit_string);
	                    pos_start = pos_stop + omit_string.length();
	                    continue;
	                }
	            }
	            sb.append(substring);
	            pos_start = pos_stop + replaceString.length();
	        }
	        sb.append(string.substring(pos_start));
	    }
	    return sb.toString();
    }

    public static void main(String[] args)
    {
        DOMConfigurator.configure("/configs/log4j.xml");
//        System.out.println("Convert.round to 2 decimal points: " + Convert.round(20.1289,2));
//        System.out.println("Convert.round to 2 decimal points: " + Convert.round(20.0000,2));
//        System.out.println("Convert.rounding to 2 decimal points: " + Convert.round(20.1289,2));
//        System.out.println("Convert.rounding to 2 decimal points: " + Convert.round(20.0000,2));
//	    System.out.println("Rounded: " +  Convert.round((6666558899L * .001), 3));
//	    System.out.println("File Size: "+ Convert.toString((6666558899L * .001), 2) + "K");
//	    System.out.println("As Price $" + Convert.toString(55899, 2));
//        System.out.println("toInt" + Convert.toInteger("1019              ")+"good");
//        Date convertedDate = Convert.toDate("2014-02-28 00:00:00");
//        Date convertedDate = Convert.toDate("2014-02-28T00:00:00");
//        Date convertedDate = Convert.toDate("2014-02-28T00:00:00-06");
//        Date convertedDate = Convert.toDate("2014-02-28T00:00:00Z");
        Date convertedDate = Convert.toDate("2013-04-01 07:30:00");
        //Date convertedDate = Convert.toDate("20140228T000000.000-06");
        System.out.println(convertedDate);
    }

    // calls to database string with the default ' for character wrapping
    public static String toSQLString(Object object)
    {
        return toDatabaseString(object, "'");
    }

}
