/**
 * 
 */
package com.edwise.dedicamns.connections.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.maxters.android.ntlm.NTLM;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.edwise.dedicamns.beans.ActivityDay;
import com.edwise.dedicamns.beans.DayRecord;
import com.edwise.dedicamns.beans.MonthListBean;
import com.edwise.dedicamns.beans.MonthYearBean;
import com.edwise.dedicamns.beans.ProjectSubprojectBean;
import com.edwise.dedicamns.connections.ConnectionException;
import com.edwise.dedicamns.connections.WebConnection;
import com.edwise.dedicamns.utils.DayUtils;

/**
 * @author edwise
 * 
 */
public class MNSWebConnectionImpl implements WebConnection {

    private static final String LOGTAG = MNSWebConnectionImpl.class.toString();

    private static final int TIMEOUT_GETDATA = 60000;
    private static final int TIMEOUT_CONNECTION = 30000;

    private static final int FIRST_YEAR = 2004;

    private static final String DOMAIN = "medianet2k";
    private static final String COOKIE_SESSION = "ASP.NET_SessionId";
    private static final String URL_STR = "http://dedicaciones.medianet.es";
    private static final String URL_ACCOUNTS_STR = "http://dedicaciones.medianet.es/Home/Accounts";
    private static final String URL_STR_CREATE = "http://dedicaciones.medianet.es/Home/CreateActivity";
    private static final String URL_STR_MODIFY = "http://dedicaciones.medianet.es/Home/EditActivity";

    private DefaultHttpClient httpClient = null;
    private String cookie = null;
    private MonthYearBean monthYears = null;
    private ProjectSubprojectBean projects = null;

    public boolean isOnline(Activity activity) {
	boolean online = false;
	ConnectivityManager cm = (ConnectivityManager) activity
		.getSystemService(Context.CONNECTIVITY_SERVICE);
	NetworkInfo netInfo = cm.getActiveNetworkInfo();
	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	    online = true;
	}

	return online;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.edwise.dedicamns.connections.WebConnection#connectWeb()
     */
    public Integer connectWeb(String userName, String password) throws ConnectionException {
	int responseCode;
	try {
	    Log.d(LOGTAG, "connectWeb... inicio...");
	    responseCode = doLoginAndGetCookie(URL_STR, DOMAIN, userName, password);
	    Log.d(LOGTAG, "connectWeb... fin...");
	} catch (Exception e) {
	    // TODO controlar el error devolviendo un responsecode erroneo?
	    Log.e(LOGTAG, "Error en el acceso web", e);
	    throw new ConnectionException(e);
	}
	return responseCode;
    }

    private int doLoginAndGetCookie(final String urlStr, final String domain, final String userName,
	    final String password) throws ClientProtocolException, IOException, URISyntaxException {
	long beginTime = System.currentTimeMillis();

	URL url = new URL(urlStr);
	createHttpClient();
	NTLM.setNTLM(httpClient, userName, password, domain, null, -1);
	HttpGet get = new HttpGet(url.toURI());

	HttpResponse resp = httpClient.execute(get);
	List<Cookie> cookies = httpClient.getCookieStore().getCookies();
	for (Cookie c : cookies) {
	    Log.d(LOGTAG, "Cookie - Name: " + c.getName() + " Value: " + c.getValue());
	    if (COOKIE_SESSION.equals(c.getName())) {
		cookie = c.getValue();
	    }
	}

	Log.d(LOGTAG, "StatusCode: " + resp.getStatusLine().getStatusCode() + " StatusLine: "
		+ resp.getStatusLine().getReasonPhrase());

	long endTime = System.currentTimeMillis();
	Log.d(LOGTAG, "Tiempo login: " + (endTime - beginTime));

	// 200 OK. 401 error
	return resp.getStatusLine().getStatusCode();
    }

    private void createHttpClient() {
	httpClient = new DefaultHttpClient();
	HttpParams params = httpClient.getParams();
	HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
	HttpConnectionParams.setSoTimeout(params, TIMEOUT_GETDATA);
    }

    public List<String> getMonths() {
	return this.monthYears.getMonths();
    }

    public List<String> getYears() {
	return this.monthYears.getYears();
    }

    public List<String> getArrayProjects() {
	return this.projects.getProjects();
    }

    public List<String> getArraySubProjects(String projectId) {
	return this.projects.getSubProjects(projectId);
    }

    public void fillProyectsAndSubProyectsCached() throws ConnectionException {
	if (projects == null) {
	    long beginTime = System.currentTimeMillis();

	    fillProyectsAndSubProyects();

	    long endTime = System.currentTimeMillis();
	    Log.d(LOGTAG, "Tiempo carga proyectos: " + (endTime - beginTime));
	}
    }

    private void fillProyectsAndSubProyects() throws ConnectionException {
	String html = this.getHttpContent(URL_ACCOUNTS_STR);
	Document document = Jsoup.parse(html);

	Elements selectSpansAccounts = document.select("span.Account");
	if (selectSpansAccounts != null) {
	    List<String> projects = new ArrayList<String>();
	    Map<String, List<String>> projectsAndSubProjects = new HashMap<String, List<String>>();
	    projects.add(ProjectSubprojectBean.PROJECT_DEFAULT);
	    projectsAndSubProjects.put(ProjectSubprojectBean.PROJECT_DEFAULT,
		    ProjectSubprojectBean.createSubProjectsDefault());
	    Iterator<Element> it = selectSpansAccounts.iterator();
	    while (it.hasNext()) {
		Element span = it.next();
		String projectId = parseStringProjectId(span.html());
		Element liParent = span.parent();
		Element nextLiOrUl = liParent.nextElementSibling();

		List<String> subProjects = new ArrayList<String>();
		subProjects.add(ProjectSubprojectBean.SUBPROJECT_DEFAULT);
		if (nextLiOrUl != null) {
		    Elements selectSpansSubAccounts = nextLiOrUl.select("span.Subaccount");
		    if (selectSpansSubAccounts != null) {
			Iterator<Element> subIt = selectSpansSubAccounts.iterator();
			while (subIt.hasNext()) {
			    Element subSpan = subIt.next();
			    subProjects.add(DayUtils.replaceAcutes(subSpan.html()));
			}
		    }
		}

		projects.add(projectId);
		projectsAndSubProjects.put(projectId, subProjects);

	    }

	    this.projects = new ProjectSubprojectBean(projects, projectsAndSubProjects);
	}
    }

    private String parseStringProjectId(String projectName) {
	return projectName.trim().substring(0, projectName.indexOf(" -"));
    }

    public void fillMonthsAndYearsCached() {
	if (monthYears == null) {
	    fillMonthsAndYears();
	}
    }

    private void fillMonthsAndYears() {
	List<String> months = generateMonthsList();
	List<String> years = generateYearsList();

	this.monthYears = new MonthYearBean(months, years);
    }

    private List<String> generateMonthsList() {
	List<String> monthsList = new ArrayList<String>();
	monthsList.add("Enero");
	monthsList.add("Febrero");
	monthsList.add("Marzo");
	monthsList.add("Abril");
	monthsList.add("Mayo");
	monthsList.add("Junio");
	monthsList.add("Julio");
	monthsList.add("Agosto");
	monthsList.add("Septiembre");
	monthsList.add("Octubre");
	monthsList.add("Noviembre");
	monthsList.add("Diciembre");

	return monthsList;
    }

    private List<String> generateYearsList() {
	List<String> yearsList = new ArrayList<String>();

	Calendar today = Calendar.getInstance();
	int lastYear = today.get(Calendar.YEAR);
	if (today.get(Calendar.MONTH) == Calendar.DECEMBER) {
	    // Si es diciembre, le añadimos ya el año siguiente, deberia estar ya...
	    lastYear++;
	}

	for (int i = FIRST_YEAR; i <= lastYear; i++) {
	    yearsList.add(String.valueOf(i));
	}

	return yearsList;
    }

    private String getHttpContent(String url) throws ConnectionException {
	String html = null;
	try {
	    long beginTime = System.currentTimeMillis();

	    URL urlObject = new URL(url);
	    HttpGet get = new HttpGet(urlObject.toURI());
	    HttpResponse resp = httpClient.execute(get);

	    Log.d(LOGTAG, "StatusCodeGET: " + resp.getStatusLine().getStatusCode() + " StatusLineGET: "
		    + resp.getStatusLine().getReasonPhrase());

	    InputStream is = resp.getEntity().getContent();
	    StringWriter writer = new StringWriter();
	    IOUtils.copy(is, writer, "UTF-8");
	    html = writer.toString();

	    long endTime = System.currentTimeMillis();
	    Log.d(LOGTAG, "Tiempo conexión a " + url + ": " + (endTime - beginTime));
	} catch (URISyntaxException e) {
	    Log.d(LOGTAG, "Error de URI en el acceso getHttp a " + url, e);
	    throw new ConnectionException(e);
	} catch (IOException e) {
	    Log.d(LOGTAG, "Error IO en el acceso getHttp a " + url, e);
	    throw new ConnectionException(e);
	}

	return html;
    }

    // TODO refactorizar bien y constantizar
    public MonthListBean getListDaysForMonth() throws ConnectionException {
	long beginTime = System.currentTimeMillis();

	List<DayRecord> listDays = new ArrayList<DayRecord>();
	String html = this.getHttpContent(URL_STR);
	Document document = Jsoup.parse(html);

	Elements optionsMonth = document.select("#month > option[selected]");
	Element optionMonth = optionsMonth.first();
	String month = optionMonth.html();
	String numMonth = optionMonth.val();

	Elements optionsYear = document.select("#year > option[selected]");
	Element optionYear = optionsYear.first();
	String year = optionYear.html();

	Elements selectUlDays = document.select("#ListOfDays");
	if (selectUlDays != null) {
	    Element ulDays = selectUlDays.first();
	    Elements liDays = ulDays.children();
	    Iterator<Element> itDays = liDays.iterator();
	    while (itDays.hasNext()) {
		Element liDay = itDays.next();
		Elements spanDayNumbers = liDay.select(".DayNumber");
		Elements spanDayNInitials = liDay.select(".DayInitials");
		Elements spanTotalHours = liDay.select(".TotalHours");

		DayRecord dayRecord = new DayRecord();
		dayRecord.setHours(spanTotalHours.first().html());
		dayRecord.setIsWeekend("WeekendDay".equals(liDay.className()));
		dayRecord.setIsHoliday("Holiday".equals(liDay.className()));
		dayRecord.setDayNum(Integer.valueOf(spanDayNumbers.first().html()));
		dayRecord.setDayName(DayUtils.replaceAcutes(spanDayNInitials.first().html()));
		dayRecord.setDateForm(DayUtils.createDateString(dayRecord.getDayNum(), numMonth, year));

		Elements selectUlActivities = liDay.select("ul.Activities");
		Element ulActivities = selectUlActivities.first();
		Elements liActivities = ulActivities.children();
		Iterator<Element> itAct = liActivities.iterator();
		while (itAct.hasNext()) {
		    Element liActivity = itAct.next();
		    ActivityDay activityDay = new ActivityDay();
		    activityDay.setIdActivity(liActivity.select("input#id").val());
		    activityDay.setHours(liActivity.select("div.ActivityHours").html());
		    activityDay.setProjectId(liActivity.select("div.ActivityAccount span").html());
		    activityDay.setSubProject("");
		    activityDay.setSubProjectId(liActivity.select("div.ActivitySubaccount span").html());
		    activityDay.setTask(liActivity.select("div.ActivityTask").html());
		    activityDay.setUpdate(true); // Para marcarla como a actualizar, si la modificamos

		    dayRecord.getActivities().add(activityDay);
		}

		listDays.add(dayRecord);
	    }
	} else {
	    throw new ConnectionException("No existen datos de días en la página recibida!");
	}

	MonthListBean monthList = new MonthListBean(month, year, listDays);

	long endTime = System.currentTimeMillis();
	Log.d(LOGTAG, "Tiempo carga lista mensual: " + (endTime - beginTime));

	return monthList;
    }

    public Integer saveDay(DayRecord dayRecord) throws ConnectionException {
	Integer result = 0;
	String html = null;
	ActivityDay activityDay = dayRecord.getActivities().get(0);
	if (activityDay.isUpdate()) {
	    html = this.doPostModify(activityDay, dayRecord.getDateForm());
	} else {
	    html = this.doPostCreate(activityDay, dayRecord.getDateForm());
	}

	Document document = Jsoup.parse(html);
	Elements errors = document.select(".input-validation-error");
	if (errors != null && errors.size() > 0) {
	    result = -3;
	} else { // Ok
	    if (!activityDay.isUpdate()) {
		// Tenemos que obtener en este caso el id
		activityDay.setIdActivity(getIdFromActivityCreated(dayRecord.getDayNum(), document,
			activityDay));
	    }

	    result = 1;
	}

	return result;
    }

    private String getIdFromActivityCreated(int dayNum, Document document, ActivityDay activityDay) {
	Elements divFrm = document.select("div#frmAC" + dayNum);
	Element divParent = divFrm.first().parent();
	Elements liActivities = divParent.select("li.Activity");
	Iterator<Element> it = liActivities.iterator();
	String id = null;
	while (it.hasNext()) {
	    Element liActivity = it.next();
	    String projectId = liActivity.select("div.ActivityAccount span").html();
	    String subProjectId = liActivity.select("div.ActivitySubaccount span").html();
	    String task = liActivity.select("div.ActivityTask").html();
	    // TODO OJO!!! SIEMPRE VIENE CON EL NBSP, AUNQUE TENGA ALGO!!
	    if (activityDay.getProjectId().equals(projectId)
		    && activityDay.getSubProjectId().equals(subProjectId)
		    && (activityDay.getTask().equals(task) || (activityDay.getTask().equals("") && ActivityDay.NBSP
			    .equals(task)))) {
		// Encontrada, nos quedamos con su id
		id = liActivity.select("input#id").val();
		break;
	    }

	}

	return id;
    }

    public Integer removeDay(DayRecord dayRecord) throws ConnectionException {
	// TODO implementar!
	return 1;
    }

    private String doPostCreate(ActivityDay activityDay, String dateActivity) throws ConnectionException {
	String html = null;

	try {
	    long beginTime = System.currentTimeMillis();

	    URL urlObject = new URL(URL_STR_CREATE);
	    HttpPost post = new HttpPost(urlObject.toURI());
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    nameValuePairs.add(new BasicNameValuePair("Date", dateActivity));
	    nameValuePairs.add(new BasicNameValuePair("createActivity.Hours", activityDay.getHours()));
	    nameValuePairs.add(new BasicNameValuePair("createActivity.AccountCode", activityDay
		    .getProjectId()));
	    nameValuePairs.add(new BasicNameValuePair("createActivity.SubaccountCode", activityDay
		    .getSubProjectId()));
	    nameValuePairs.add(new BasicNameValuePair("createActivity.Task", activityDay.getTask()));
	    nameValuePairs.add(new BasicNameValuePair("ihScroll20", "0"));

	    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    HttpResponse resp = httpClient.execute(post);

	    if (resp.getStatusLine().getStatusCode() != 200) {
		throw new ConnectionException("Error en la conexión, statusCode: "
			+ resp.getStatusLine().getStatusCode());
	    }
	    
	    InputStream is = resp.getEntity().getContent();
	    StringWriter writer = new StringWriter();
	    IOUtils.copy(is, writer, "UTF-8");
	    html = writer.toString();

	    long endTime = System.currentTimeMillis();
	    Log.d(LOGTAG, "Tiempo conexión a " + URL_STR_CREATE + ": " + (endTime - beginTime));
	} catch (URISyntaxException e) {
	    Log.d(LOGTAG, "Error de URI en el acceso doPostCreate a " + URL_STR_CREATE, e);
	    throw new ConnectionException(e);
	} catch (IOException e) {
	    Log.d(LOGTAG, "Error IO en el acceso doPostCreate a " + URL_STR_CREATE, e);
	    throw new ConnectionException(e);
	}

	return html;
    }

    private String doPostModify(ActivityDay activityDay, String dateActivity) throws ConnectionException {
	String html = null;

	try {
	    long beginTime = System.currentTimeMillis();

	    URL urlObject = new URL(URL_STR_MODIFY);
	    HttpPost post = new HttpPost(urlObject.toURI());
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	    nameValuePairs.add(new BasicNameValuePair("Date", dateActivity));
	    nameValuePairs.add(new BasicNameValuePair("editActivity.id", activityDay.getIdActivity()));
	    nameValuePairs.add(new BasicNameValuePair("id", activityDay.getIdActivity()));
	    nameValuePairs.add(new BasicNameValuePair("editActivity.Hours", activityDay.getHours()));
	    nameValuePairs
		    .add(new BasicNameValuePair("editActivity.AccountCode", activityDay.getProjectId()));
	    nameValuePairs.add(new BasicNameValuePair("editActivity.SubaccountCode", activityDay
		    .getSubProjectId()));
	    nameValuePairs.add(new BasicNameValuePair("editActivity.Task", activityDay.getTask()));
	    nameValuePairs.add(new BasicNameValuePair("ihScroll" + activityDay.getIdActivity(), "0"));

	    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	    HttpResponse resp = httpClient.execute(post);

	    if (resp.getStatusLine().getStatusCode() != 200) {
		throw new ConnectionException("Error en la conexión, statusCode: "
			+ resp.getStatusLine().getStatusCode());
	    }
	    
	    InputStream is = resp.getEntity().getContent();
	    StringWriter writer = new StringWriter();
	    IOUtils.copy(is, writer, "UTF-8");
	    html = writer.toString();

	    long endTime = System.currentTimeMillis();
	    Log.d(LOGTAG, "Tiempo conexión a " + URL_STR_MODIFY + ": " + (endTime - beginTime));
	} catch (URISyntaxException e) {
	    Log.d(LOGTAG, "Error de URI en el acceso doPostModify a " + URL_STR_MODIFY, e);
	    throw new ConnectionException(e);
	} catch (IOException e) {
	    Log.d(LOGTAG, "Error IO en el acceso doPostModify a " + URL_STR_MODIFY, e);
	    throw new ConnectionException(e);
	}

	return html;
    }
}