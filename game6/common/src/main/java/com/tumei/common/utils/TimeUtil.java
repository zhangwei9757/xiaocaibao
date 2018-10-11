package com.tumei.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Administrator on 2017/4/5 0005.
 */
public class TimeUtil {
	/**
	 * 当前时间的字符串
	 * @return
	 */
	public static String nowString() {
		return Defs.getColorString(1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
	}

	public static int getToday() {
		LocalDate ldt = LocalDate.now();
		return ldt.getYear() * 10000 + ldt.getMonthValue() * 100 + ldt.getDayOfMonth();
	}

	/**
	 * 用年和周组成一个唯一的星期时间，一个星期内都是这个标识时间
	 * @return
	 */
	public static int getWeekDay() {
		Calendar c = new GregorianCalendar(Locale.CHINA);
		return c.getWeekYear() * 100 + c.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 返回星期几; 周一是1，周日是7
	 * @return
	 */
	public static int WeekDay() {
		LocalDate ld = LocalDate.now();
		return ld.getDayOfWeek().getValue();
	}

	public static int getNextWeekDay() {
		LocalDate ld = LocalDate.now();
		int day = (8 - ld.getDayOfWeek().getValue());
		LocalDate nd = ld.plusDays(day);
		return (nd.getYear() * 1000 + nd.getDayOfYear());
	}

	/**
	 * 将一个Date转换为20171201类似的数字
	 * @param date
	 * @return
	 */
	public static int getDay(Date date) {
		LocalDateTime ldt = LocalDateTime.ofEpochSecond(date.getTime() / 1000, 0, ZoneOffset.ofHours(8));
		return ldt.getYear() * 10000 + ldt.getMonthValue() * 100 + ldt.getDayOfMonth();
	}

	/**
	 * 反转上述操作，将一个20171201的数字，转换为Date
	 * @param day
	 * @return
	 */
	public static LocalDate fromDay(int day) {
		int year = day / 10000;
		int tmp = day % 10000;
		int month = tmp / 100;
		int d = tmp % 100;

		return LocalDate.of(year, month, d);
	}


	public static int getDay(LocalDate ld) {
		return ld.getYear() * 10000 + ld.getMonthValue() * 100 + ld.getDayOfMonth();
	}


	/**
	 * 得到到进入为止过去的日期, 今天到今天是0天
	 * @param date
	 * @return
	 */
	public static int pastDays(Date date) {
		Instant instance = Instant.ofEpochMilli(date.getTime());
		LocalDate ld = LocalDateTime.ofInstant(instance, ZoneId.systemDefault()).toLocalDate();
		LocalDate today = LocalDate.now();
		if (ld.isAfter(today)) {
			return -1;
		}

		return (int)(today.toEpochDay() - ld.toEpochDay());
	}

	/**
	 * 计算二个指定日期的相差天数
	 * @param start 开始日期 格式：20181008
	 * @param end 结束日期 格式：20181008
	 * @return 相差天数
	 */
	public static int fromDuration(int start, int end) { return (int) ChronoUnit.DAYS.between(fromDay(start), fromDay(end)); }
}
