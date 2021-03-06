package com.edwise.dedicamns.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.edwise.dedicamns.beans.BatchDataBean;
import com.edwise.dedicamns.beans.DayRecord;

/**
 * Mock para obtención y parseo de los html obtenidos.
 * 
 * @author edwise
 * 
 */
@Deprecated
public class DedicaHTMLParserMock {

	private static DedicaHTMLParserMock htmlParser;

	public static DedicaHTMLParserMock getInstance() {
		if (htmlParser == null) {
			htmlParser = new DedicaHTMLParserMock();
		}
		return htmlParser;
	}

	public Integer connectWeb() {
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			Log.e(DedicaHTMLParserMock.class.toString(), "connectWeb: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		return 200;
	}

	public boolean saveDay(DayRecord dayRecord) {
		boolean saved = false;

		saved = true;

		return saved;
	}

	public boolean removeDay(DayRecord dayRecord) {
		boolean removed = false;

		removed = true;

		return removed;
	}

	public List<String> getArrayProjects() {
		List<String> arrayProjects = new ArrayList<String>();

		arrayProjects.add("Selecciona proyecto...");
		arrayProjects.add("BBVA58");
		arrayProjects.add("Educared09");
		arrayProjects.add("BBVA68");
		arrayProjects.add("NIELHUEVO32");
		arrayProjects.add("ISBAN12");

		return arrayProjects;
	}

	public List<String> getArraySubProjects(String projectId) {
		List<String> arraySubProjects = new ArrayList<String>();
		if (projectId != null && projectId.equals("BBVA58")) {
			arraySubProjects.add("1 - Tarea mierda");
			arraySubProjects.add("2 - Marronacos");
			arraySubProjects.add("3 - Calentar silla");
		} else {
			arraySubProjects.add("0 - Sin cuenta");
		}

		return arraySubProjects;
	}

	public List<String> getMonths() {
		List<String> months = new ArrayList<String>();

		months.add("Octubre 2012");
		months.add("Noviembre 2012");
		months.add("Diciembre 2012");
		months.add("Enero 2013");

		return months;
	}

	public int proccesBatch(BatchDataBean batchData) {
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			Log.e(DedicaHTMLParserMock.class.toString(), "proccesBatch: Error en TimeUnit...", e);
			e.printStackTrace();
		}

		return 1;
	}

}
