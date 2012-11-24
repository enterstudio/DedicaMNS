package com.edwise.dedicamns.utils;

import org.apache.commons.lang3.StringUtils;

public class DayUtils {

    public static boolean isWeekend(String dayName) {
	return dayName.equalsIgnoreCase("Dom") || dayName.equalsIgnoreCase("Sáb");
    }

    public static String replaceAcutes(String name) {
	String nameWithoutAcutes = name;
	if (name.contains("acute;")) {
	    nameWithoutAcutes = nameWithoutAcutes.replace("&aacute;", "á");
	    nameWithoutAcutes = nameWithoutAcutes.replace("&eacute;", "é");
	}

	return nameWithoutAcutes;
    }

    public static String createDateString(int day, String numMonth, String year) {
	StringBuilder dateString = new StringBuilder();

	// Formato: 22/11/2012 0:00:00
	dateString.append(StringUtils.leftPad(String.valueOf(day), 2, '0'))
		.append("/").append(StringUtils.leftPad(numMonth, 2, '0')).
		append("/").append(year).
		append(" 0:00:00");
	return dateString.toString();
    }

    public static String getNumSubProject(String subProjectName) {
	String numString = subProjectName.substring(0, subProjectName.indexOf("-"));

	return numString.trim();
    }
    
}
