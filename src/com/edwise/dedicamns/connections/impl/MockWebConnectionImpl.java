/**
 * 
 */
package com.edwise.dedicamns.connections.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.util.Log;

import com.edwise.dedicamns.asynctasks.AppData;
import com.edwise.dedicamns.beans.ActivityDay;
import com.edwise.dedicamns.beans.DayRecord;
import com.edwise.dedicamns.beans.MonthListBean;
import com.edwise.dedicamns.beans.MonthReportBean;
import com.edwise.dedicamns.beans.MonthReportRecord;
import com.edwise.dedicamns.beans.ProjectSubprojectBean;
import com.edwise.dedicamns.connections.ConnectionException;
import com.edwise.dedicamns.connections.WebConnection;
import com.edwise.dedicamns.db.DatabaseHelper;
import com.edwise.dedicamns.utils.DayUtils;

/**
 * @author edwise
 * 
 */
public class MockWebConnectionImpl implements WebConnection {
	private static final String LOGTAG = MockWebConnectionImpl.class.toString();

	private ProjectSubprojectBean projects = null;
	private List<String> months = null;
	private List<String> years = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.edwise.dedicamns.connections.WebConnection#isOnline(android.app.Activity )
	 */
	@Override
	public boolean isOnline(Activity activity) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.edwise.dedicamns.connections.WebConnection#connectWeb(java.lang.String , java.lang.String)
	 */
	@Override
	public Integer connectWeb(String userName, String password) {
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "connectWeb: Error en TimeUnit...", e);
			e.printStackTrace();
		}
		return 200;
	}

	@Override
	public List<String> getMonths() {
		return months;
	}

	@Override
	public List<String> getYears() {
		return years;
	}

	@Override
	public List<String> getArrayProjects() {
		return projects.getProjects();
	}

	@Override
	public List<String> getArraySubProjects(String projectId) {
		return projects.getSubProjects(projectId);
	}

	@Override
	public void populateDBProjectsAndSubprojects() {
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		this.projects = null;
		DatabaseHelper db = AppData.getDatabaseHelper();

		Log.d(LOGTAG, "(Mock) Populating DB...");

		db.insertProject("BBVA58", new String[] { "1 - Tarea mierda", "2 - Marronacos", "3 - Calentar silla" });
		db.insertProject("Educared09", new String[] { "0 - Sin subcuenta" });
		db.insertProject("BBVA68", new String[] { "0 - Sin subcuenta" });
		db.insertProject("NIELHUEVO32", new String[] { "0 - Sin subcuenta" });
		db.insertProject("ISBAN12", new String[] { "0 - Sin subcuenta" });
		db.insertProject("ANDREA99", new String[] { "1 - Media jornada", "2 - Morir" });

		Log.d(LOGTAG, "(Mock) DB Populated.");
	}

	@Override
	public void fillProyectsAndSubProyectsCached() throws ConnectionException {
		if (this.projects == null) {
			this.projects = AppData.getDatabaseHelper().getAllProjectsAndSubProjects();
		}
	}

	@Override
	public void fillMonthsAndYearsCached() {
		months = new ArrayList<String>();
		months.add("Enero");
		months.add("Febrero");
		months.add("Marzo");
		months.add("Abril");
		months.add("Mayo");
		months.add("Junio");
		months.add("Julio");
		months.add("Agosto");
		months.add("Septiembre");
		months.add("Octubre");
		months.add("Noviembre");
		months.add("Diciembre");

		years = new ArrayList<String>();
		years.add("2013");
		years.add("2014");
		years.add("2015");		
	}

	@Override
	public MonthListBean getListDaysAndActivitiesForCurrentMonth() {
		List<DayRecord> list = new ArrayList<DayRecord>();

		fillListMock(list, true);
		MonthListBean monthList = new MonthListBean("Noviembre", "2012", list);

		return monthList;
	}

	@Override
	public List<DayRecord> getListDaysAndActivitiesForMonthAndYear(int month, String year, boolean withActivities) {
		List<DayRecord> list = new ArrayList<DayRecord>();

		fillListMock(list, withActivities);

		return list;
	}

	private void fillListMock(List<DayRecord> list, boolean withActivity) {
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		for (int i = 1; i < 31; i++) {
			DayRecord dayRecord = new DayRecord();
			dayRecord.setDayNum(i);
			dayRecord.setDayName(generateDayName(i));
			dayRecord.setIsHoliday(false);
			if (DayUtils.isWeekend(dayRecord.getDayName())) {
				dayRecord.setIsWeekend(true);
			}

			if (i < 10 && !DayUtils.isWeekend(dayRecord.getDayName())) {
				dayRecord.setHours("08:30");

				if (withActivity) {
					ActivityDay activityDay = new ActivityDay();
					activityDay.setHours("08:30");
					activityDay.setProjectId("BBVA58");
					activityDay.setSubProject("3 - Calentar silla");
					activityDay.setSubProjectId("3");
					activityDay.setUpdate(true);
					activityDay.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay);
				}
			} else if (i == 15) {
				dayRecord.setHours("16:30");

				if (withActivity) {
					ActivityDay activityDay = new ActivityDay();
					activityDay.setHours("06:30");
					activityDay.setProjectId("BBVA58");
					activityDay.setSubProject("3 - Calentar silla");
					activityDay.setSubProjectId("3");
					activityDay.setUpdate(true);
					activityDay.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay);

					ActivityDay activityDay2 = new ActivityDay();
					activityDay2.setHours("05:00");
					activityDay2.setProjectId("BBVA58");
					activityDay2.setSubProject("2 - Marronacos");
					activityDay2.setSubProjectId("2");
					activityDay2.setUpdate(true);
					activityDay2.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay2);

					ActivityDay activityDay3 = new ActivityDay();
					activityDay3.setHours("04:30");
					activityDay3.setProjectId("Educared09");
					activityDay3.setSubProject("0 - Sin cuenta");
					activityDay3.setSubProjectId("0");
					activityDay3.setTask("Hacer el tonto");
					activityDay3.setUpdate(true);
					activityDay3.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay3);
				}
			} else if (i == 16) {
				dayRecord.setHours("23:30");

				if (withActivity) {
					ActivityDay activityDay = new ActivityDay();
					activityDay.setHours("01:30");
					activityDay.setProjectId("BBVA58");
					activityDay.setSubProject("3 - Calentar silla");
					activityDay.setSubProjectId("3");
					activityDay.setUpdate(true);
					activityDay.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay);

					ActivityDay activityDay2 = new ActivityDay();
					activityDay2.setHours("04:00");
					activityDay2.setProjectId("BBVA58");
					activityDay2.setSubProject("2 - Marronacos");
					activityDay2.setSubProjectId("2");
					activityDay2.setUpdate(true);
					activityDay2.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay2);

					ActivityDay activityDay3 = new ActivityDay();
					activityDay3.setHours("04:30");
					activityDay3.setProjectId("Educared09");
					activityDay3.setSubProject("0 - Sin cuenta");
					activityDay3.setSubProjectId("0");
					activityDay3.setTask("Hacer el tonto");
					activityDay3.setUpdate(true);
					activityDay3.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay3);

					ActivityDay activityDay4 = new ActivityDay();
					activityDay4.setHours("03:30");
					activityDay4.setProjectId("ISBAN12");
					activityDay4.setSubProject("0 - Sin cuenta");
					activityDay4.setSubProjectId("0");
					activityDay4.setUpdate(true);
					activityDay4.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay4);

					ActivityDay activityDay5 = new ActivityDay();
					activityDay5.setHours("02:00");
					activityDay5.setProjectId("BBVA58");
					activityDay5.setSubProject("2 - Marronacos");
					activityDay5.setSubProjectId("2");
					activityDay5.setUpdate(true);
					activityDay5.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay5);

					ActivityDay activityDay6 = new ActivityDay();
					activityDay6.setHours("01:30");
					activityDay6.setProjectId("Educared09");
					activityDay6.setSubProject("0 - Sin cuenta");
					activityDay6.setSubProjectId("0");
					activityDay6.setTask("Hacer el tonto");
					activityDay6.setUpdate(true);
					activityDay6.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay6);

					ActivityDay activityDay7 = new ActivityDay();
					activityDay7.setHours("03:30");
					activityDay7.setProjectId("BBVA58");
					activityDay7.setSubProject("1 - Tarea mierda");
					activityDay7.setSubProjectId("1");
					activityDay7.setUpdate(true);
					activityDay7.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay7);

					ActivityDay activityDay8 = new ActivityDay();
					activityDay8.setHours("01:00");
					activityDay8.setProjectId("BBVA58");
					activityDay8.setSubProject("2 - Marronacos");
					activityDay8.setSubProjectId("2");
					activityDay8.setUpdate(true);
					activityDay8.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay8);

					ActivityDay activityDay9 = new ActivityDay();
					activityDay9.setHours("01:30");
					activityDay9.setProjectId("Educared09");
					activityDay9.setSubProject("0 - Sin cuenta");
					activityDay9.setSubProjectId("0");
					activityDay9.setTask("Hacer el tonto");
					activityDay9.setUpdate(true);
					activityDay9.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay9);

					ActivityDay activityDay10 = new ActivityDay();
					activityDay10.setHours("00:30");
					activityDay10.setProjectId("Educared09");
					activityDay10.setSubProject("0 - Sin cuenta");
					activityDay10.setSubProjectId("0");
					activityDay10.setTask("Hacer el tonto más");
					activityDay10.setUpdate(true);
					activityDay10.setIdActivity(Math.random() + "");
					dayRecord.getActivities().add(activityDay10);

				}
			} else {
				dayRecord.setHours("00:00");
			}
			list.add(dayRecord);
		}
	}

	final static String[] dayNames = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };

	private String generateDayName(int i) {
		String dayName = null;

		if (i <= 7) {
			dayName = dayNames[i - 1];
		} else {
			int ind = i % 7;
			if (ind == 0) {
				dayName = dayNames[6];
			} else {
				dayName = dayNames[ind - 1];
			}
		}
		return dayName;
	}

	@Override
	public Integer saveDay(ActivityDay activityDay, String dateForm, int dayNum, boolean isBatchMontly) {
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "saveDay: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		if (!activityDay.isUpdate()) {
			activityDay.setIdActivity(Math.random() + "");
		}
		return 1;
	}

	@Override
	public Integer removeDay(ActivityDay activityDay) {
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "removeDay: Error en TimeUnit...", e);
			e.printStackTrace();
		}
		return 1;
	}

	@Override
	public Integer saveDayBatch(DayRecord dayRecord, boolean isBatchMontly) throws ConnectionException {
		for (ActivityDay activityDay : dayRecord.getActivities()) {			
			if (!activityDay.isUpdate() || !isBatchMontly) {
				activityDay.setIdActivity(String.valueOf(generateRandomNumber(1, 10000)));
			}
		}
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "saveDayBatch: Error en TimeUnit...", e);
			e.printStackTrace();
		}
		return 1;
	}

	private int generateRandomNumber(int min, int  max) {
		return min + (int)(Math.random() * ((max - min) + 1));
	}

	@Override
	public MonthReportBean getMonthReport() throws ConnectionException {
		MonthReportBean monthReport = new MonthReportBean("Febrero", "2013");

		MonthReportRecord record1 = new MonthReportRecord("Educared09", "120");
		MonthReportRecord record2 = new MonthReportRecord("BBVA58 - Movidotes", "80,5");
		MonthReportRecord record3 = new MonthReportRecord("ISBAN12", "10,5");
		MonthReportRecord record4 = new MonthReportRecord("Educared09", "120");
		MonthReportRecord record5 = new MonthReportRecord("BBVA58 - Movidotes", "80,5");
		MonthReportRecord record6 = new MonthReportRecord("ISBAN12", "10,5");
		MonthReportRecord record7 = new MonthReportRecord("Educared09", "120");
		MonthReportRecord record8 = new MonthReportRecord("BBVA58 - Movidotes", "80,5");
		MonthReportRecord record9 = new MonthReportRecord("ISBAN12", "10,5");
		MonthReportRecord record10 = new MonthReportRecord("Educared09", "120");
		MonthReportRecord record11 = new MonthReportRecord("BBVA58 - Movidotes", "80,5");
		MonthReportRecord record12 = new MonthReportRecord("ISBAN12", "10,5");
		MonthReportRecord record13 = new MonthReportRecord("Educared09", "120");
		MonthReportRecord record14 = new MonthReportRecord("BBVA58 - Movidotes", "80,5");
		MonthReportRecord record15 = new MonthReportRecord("ISBAN12", "10,5");

		monthReport.addMonthReportRecord(record1);
		monthReport.addMonthReportRecord(record2);
		monthReport.addMonthReportRecord(record3);
		monthReport.addMonthReportRecord(record4);
		monthReport.addMonthReportRecord(record5);
		monthReport.addMonthReportRecord(record6);
		monthReport.addMonthReportRecord(record7);
		monthReport.addMonthReportRecord(record8);
		monthReport.addMonthReportRecord(record9);
		monthReport.addMonthReportRecord(record10);
		monthReport.addMonthReportRecord(record11);
		monthReport.addMonthReportRecord(record12);
		monthReport.addMonthReportRecord(record13);
		monthReport.addMonthReportRecord(record14);
		monthReport.addMonthReportRecord(record15);

		monthReport.setTotal("211");

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			Log.e(MockWebConnectionImpl.class.toString(), "getMonthReport: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		return monthReport;
	}

	@Override
	public boolean recreateDBOnStart() {
		return true;
	}
}
