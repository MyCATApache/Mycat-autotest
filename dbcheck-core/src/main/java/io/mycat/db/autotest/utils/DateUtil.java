/**
 * 
 */
package io.mycat.db.autotest.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * 
 * @author zejian
 * @date 2014年9月20日
 */
public class DateUtil {

	/**
	 * 通用模板 yyyy-MM-dd
	 */
	public static final String YEAR_MONTH_DAY_ZH = "yyyy-MM-dd";

	/**
	 * 通用模板 yyyy-MM-dd HH:mm:ss
	 */
	public static final String YEAR_MONTH_DAY_HOUR_MIN_SEC_ZH = "yyyy-MM-dd HH:mm:ss";

	public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        DateTime localDate = new DateTime(date);
   	 	return localDate.toString(pattern);
    }

	/**
	 * 转换 字符 2 date
	 * 
	 * @param dateStr
	 * @param template
	 * @return
	 */
	public static final Date parseStrToDate(String dateStr, String template) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(template);
		return dtf.parseDateTime(dateStr).toDate();
	}

	/**
	 * 
	 * @param dateStr
	 * @return
	 */
	public static final Date parseStrToDate(String dateStr) {
		return parseStrToDate(dateStr, YEAR_MONTH_DAY_HOUR_MIN_SEC_ZH);
	}

	/**
	 * 转换 date 2 字符
	 * 
	 * @param date
	 * @return
	 */
	public static final String parseDateToStr(Date date) {
		DateTime localDate = new DateTime(date);
		return localDate.toString("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 转换 date 2 字符
	 * 
	 * @param date
	 * @param dateStr
	 * @return
	 */
	public static final String parseDateToStr(Date date, String dateStr) {
		DateTime localDate = new DateTime(date);
		return localDate.toString(dateStr);
	}



	/**
	 * 获得当天的 时间字符串 yyyy-MM-dd
	 *
	 * @return
	 */
	public static final String getStrDatebyTobay() {
		DateTime localDate = DateTime.now();
		return localDate.toString("yyyyMMdd");
	}

	/**
	 * 获得当天的 时间字符串 yyyy-MM-dd
	 *
	 * @return
	 */
	public static final String getStrDatebyTobayTime() {
		DateTime localDate = DateTime.now();
		return localDate.toString("yyyyMMddHHmmss");
	}

	/**
	 * 获得当天的 时间字符串 yyyy-MM-dd
	 * 
	 * @return
	 */
	public static final String getStrTimebyTobay() {
		DateTime localDate = DateTime.now();
		return localDate.toString("yyyy-MM-dd");
	}

	/**
	 * 获得当月的 时间字符串 yyyy-MM
	 * 
	 * @return
	 */
	public static final String getStrTimebyMon() {
		DateTime localDate = DateTime.now();
		return localDate.toString("yyyy-MM");
	}

	/**
	 * 获得下一月的同天 时间字符串 yyyy-MM-dd
	 * 
	 * @return
	 */
	public static final String getStrTimebyMonthsTobay() {
		DateTime localDate = DateTime.now();
		localDate = localDate.minusMonths(1);
		return localDate.toString("yyyy-MM-dd");
	}

	/**
	 * 获得下一月的 时间字符串 yyyy-MM
	 * 
	 * @return
	 */
	public static final String getPreviousMonth() {
		DateTime localDate = DateTime.now();
		localDate = localDate.minusMonths(1);
		return localDate.toString("yyyy-MM");
	}

	/**
	 * 获得上一月的 时间字符串 yyyy-MM
	 * 
	 * @return
	 */
	public static final String getNextMonth() {
		DateTime localDate = DateTime.now();
		localDate = localDate.plusMonths(1);
		return localDate.toString("yyyy-MM");
	}

	/**
	 * 比较时间 是否和当天 之差 几天内
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static final boolean istodayNews(String date, int day) {
		DateTime localDate = DateTime.now();

		String todaystr = localDate.toString("yyyy-MM-dd");

		DateTime localDate2 = DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
		localDate2 = localDate2.plusDays(day);
		if (todaystr.compareTo(localDate2.toString("yyyy-MM-dd")) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 获得当前的 时间字符串yyyyMMddHHmmssSS
	 * 
	 * @return
	 */
	public static final String getStrDatebyTobayFile() {
		DateTime localDate = DateTime.now();
		return localDate.toString("yyyyMMddHHmmssSS");
	}
}
