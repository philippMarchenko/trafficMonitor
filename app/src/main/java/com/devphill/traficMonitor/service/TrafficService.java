package com.devphill.traficMonitor.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.model.Package;
import com.devphill.traficMonitor.networkStats.LoadPackageList;
import com.devphill.traficMonitor.service.helper.TrafficHelper;
import com.devphill.traficMonitor.service.helper.TrafficMHelper;
import com.devphill.traficMonitor.adapter.MainFragmentAdapter;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.reciver.AlarmReceiver;
import com.devphill.traficMonitor.ui.MainActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrafficService extends Service implements LoadPackageList.ILoadPackageListListener{

	final String LOG_TAG = "serviceTag";
	final int MAX_SIM = 10;
	public long mobile_trafficTXToday, mobile_trafficRXToday, mobile_trafficTXYesterday, mobile_trafficRXYesterday,allTrafficMobile;
	static public String idsim = "mySim";
	public int networkstate;

	public String list_table[] = new String[10];

	static public long rowID;

	static public boolean newDay = false;
	static public boolean newMonth = false;
	public static boolean firstWriteDB = true;



	DBHelper dbHelper;
	Timer myTimerService = new Timer(); // Создаем таймер
	Timer timerAppsUpdate = new Timer();

	public static int CLEAN_TABLE = 1;
	public static int SET_REBOOT_ACTION = 2;
	public static int ALARM_ACTION = 3;

	public static boolean runTimer = false;
	boolean showNotyAllert = false;
	boolean showNotyStop = false;

	public static final int PERIOD_DAY = 1;
	public static final int PERIOD_MOUNTH = 2;
	public static int period = PERIOD_DAY;
	public static int stopLevel = 100;
	public static int allertLevel = 80;
	public static int disable_internet = 1;
	public static int show_allert = 1;
	public static int day = 1;
	public static int month = 1;
	public static int mYear = 2017;

	public static List<Package> packageList = new ArrayList<>();

	public static final String APP_PREFERENCES = "settingsTrafficMonitor";
	public static final String APP_PREFERENCES_TRAFFIC_APPS = "TrafficApps";
	public static final String APP_PREFERENCES_TRAFFIC_APPS_WIFI = "TrafficAppsWiFi";
	public static final String APP_PREFERENCES_TOTAL_TRAFFIC = "totalTraffic";
	public static final String APP_PREFERENCES_TOTAL_TRAFFIC_REBOOT = "totalTrafficReboot";
	public static final String APP_PREFERENCES_TRAFFIC_APPS_REBOOT= "TrafficAppsReboot";
	public static final String APP_PREFERENCES_TRAFFIC_APPS_REBOOT_WIFI= "TrafficAppsRebootWiFi";
	public static final String APP_PREFERENCES_PERIOD = "period";
	public static final String APP_PREFERENCES_PERIOD_CHART = "period_chart";
	public static final String APP_PREFERENCES_STOPL_EVEL =  "stopLevel";
	public static final String APP_PREFERENCES_ALLERT_LEVEL = "allertLevel";
	public static final String APP_PREFERENCES_DISABLE_INTERNET = "disable_internet";
	public static final String APP_PREFERENCES_SHOW_ALLERT = "show_allert";
	public static final String APP_PREFERENCES_DAY = "day";
	public static final String APP_PREFERENCES_MONTH = "month";
	public static final String APP_PREFERENCES_YEAR = "year";
	public static final String APP_PREFERENCES_REBOOT_ACTION = "reboot_action";
	public static final String APP_PREFERENCES_HAS_VISITED = "has_visited";

	public static boolean brRegistered = false;

	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "onCreateService");
		//getSerialSim();    //получаем айди симки
		dbHelper = new DBHelper(getBaseContext());  // создаем объект для создания и управления версиями БД
		SQLiteDatabase db = dbHelper.getWritableDatabase(); // подключаемся к БД

		getlistTable(db);            //получаем список созданых таблиц

		for (int i = 0; i < MAX_SIM; i++) {
			Log.d(LOG_TAG, " Table" + i + " " + list_table[i] + "\n");    //выведем в лог список таблиц

			if (idsim.equals(list_table[i])) {                                //если есть таблица с номером нашей симки
				Log.d(LOG_TAG, "Есть совпадение таблиц!");
				break;
			}
			if (i == MAX_SIM - 1) {                                            //нет такой таблицы
				Log.d(LOG_TAG, "Не найдено такой таблицы!Создаем новую!");

				db = dbHelper.getWritableDatabase(); // подключаемся к БД

				createTableSim(db, idsim);
			}
		}

		//db = dbHelper.getWritableDatabase(); // подключаемся к БД
		//getlistTable(db);                       // еще раз выведем в лог список
	//	creatTestTable(db);
	//	showListTables();
		recoverSettings();

		setTimer();

		initNoty();

		task();

		if(Build.VERSION.SDK_INT < 23) {

		}
		else{

			startUpdateAppTraffic();

		}

	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "onStartCommandService");

		int task = 255;
		if(intent != null )
			task = intent.getIntExtra("task", 0);           //достаем номер задачи

		if (task == CLEAN_TABLE) {                       //если задача очистить таблицу
			Log.d(LOG_TAG, "CLEAN_TABLE_ACTION");
			cleanTable(idsim);

		}//очистим ее
		else if (task == SET_REBOOT_ACTION){
			if(Build.VERSION.SDK_INT < 23) {
				setRebootAction(1);
				TrafficHelper.resetTrafficReboot(getBaseContext(),idsim);
				Log.d(LOG_TAG, "SET_REBOOT_ACTION");
			}
		}
		else if (task == ALARM_ACTION){
			setTimer();
			Log.d(LOG_TAG, "ALARM_ACTION");
			newDay = true;
		}

		runTimer = true;

		return super.onStartCommand(intent, flags, startId);
		//return START_STICKY;
	}

	void creatTestTable(SQLiteDatabase db){

		Cursor c = db.query(idsim, null, null, null, null, null, null);
		// создаем объект для данных
		ContentValues cv = new ContentValues();

	try {
		db.execSQL("create table testDay" + " (" //создание таблицы с названием серии сим карты
				+ "id integer primary key autoincrement,"
				+ "allTrafficMobile integer"     //количество общего трафика через мобильный интерфейс в К
				+ ");"); //зарезервировано

		Log.d(LOG_TAG, "--- onCreating Test database ---");

		for(int i = 0; i < 5000; i++){

			cv.put("allTrafficMobile",i);

			// вставляем запись и получаем ее ID
			rowID = db.insert("" + "testDay" + "", null, cv);
			Log.d(LOG_TAG, "Insert data in  testDay, ID = " + rowID);
		}
	}
	catch (RuntimeException e){
		Log.d(LOG_TAG, "RuntimeException " + e.getMessage());
	}

		// закрываем подключение к БД
		db.close();
		c.close();
	}

	void createTableSim(SQLiteDatabase db, String id_sim) {
		db.execSQL("create table " + id_sim + " (" //создание таблицы с названием серии сим карты
				+ "id integer primary key autoincrement,"
				+ "time integer,"         //время получения данных
				+ "mobile_trafficTXToday integer,"     //количество переданного трафика через мобильный интерфейс сегодня
				+ "mobile_trafficRXToday integer,"     //количество принятого трафика через мобильный интерфейс сегодня
				+ "mobile_trafficTXYesterday integer,"     //количество переданного трафика через мобильный интерфейс вчера
				+ "mobile_trafficRXYesterday integer,"     //количество принятого трафика через мобильный интерфейс вчера
				+ "allTrafficMobile integer,"     //количество общего трафика через мобильный интерфейс в Кб
				+ "lastDay integer,"             //день последний
				+ "reserved integer" + ");"); //зарезервировано

		db.execSQL("create table traffic_app (" //создание таблицы
				+ "id integer primary key autoincrement,"
				+ "package_name text,"         //время получения данных
				+ "mobile_traffic integer,"     //количество переданного трафика через мобильный интерфейс сегодня
				+ "wifi_traffic text" + ");"); //зарезервировано



		/*db.execSQL("create table " + "set" + id_sim + " (" //создание таблицы с названием серии сим карты для настроек
				+ "id integer primary key autoincrement,"
				+ "period integer,"         //период
				+ "periodChartOffset integer,"         //период граффика
				+ "stopLevel integer,"     //уровень ограничения
				+ "allertLevel integer,"     //уровень предупреждения
				+ "disable_internet integer,"     //отклюаем ли доступ
				+ "show_allert integer,"     //показуем ли предупреждение
				+ "reboot_device integer," //было ли устройство перезагруженно (обнуление данных в устройстве)
				+ "day integer,"     //день
				+ "month integer,"             //месяц
				+ "year integer" + ");"); //год начала отсчета за месяц*/

		//initSettings();

		db.close();

		Log.d(LOG_TAG, "--- onCreating database ---" + id_sim);

	}

	void showListTables() {
		for (int i = 0; i < MAX_SIM; i++) {
			Log.d(LOG_TAG, "Table " + i + " " + list_table[i] + "\n");
		}
	}

	public void getSerialSim() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//доступ к данным о телефоне, sim и сотовой сети
		idsim = "n" + tm.getSimSerialNumber();//Получения id sim
		networkstate = tm.getDataState();//Получение значения состояния мобильного интернет подключения (0 отключено, 1 подключается, 2 подключено, 3 ожидание. 0x0000000*)
		Log.d(LOG_TAG, "idsim " + idsim);
	}

	public void getlistTable(SQLiteDatabase db) {

		Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
		int i = 0;

		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				list_table[i] = c.getString(0);
				// Toast.makeText(TrafficService.this, "Table Name=> "+c.getString(0), Toast.LENGTH_LONG).show();
				c.moveToNext();
				i++;
			}
		}

	}

	public boolean isAllertLevel(){

		if((allTrafficMobile/1024) >= allertLevel)
			return true;
		else
			return  false;
	}

	public boolean isStopLevel(){
		if(allTrafficMobile/1024 >= stopLevel)
			return true;
		else
			return false;
	}

	public void setRebootAction(int reboot){

		Log.d(LOG_TAG, "setRebootAction" );
		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();

		editor.putInt(APP_PREFERENCES_REBOOT_ACTION,reboot);

		editor.apply();
	}

	public int getRebootAction(){

		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
	//	Log.d(LOG_TAG, "getRebootAction = " + mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION,0));
		return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION,0);

	}

	public void initNoty(){
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.noty);
		NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext());
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;

		mBuilder.setSmallIcon(R.drawable.bittorrent);
		mBuilder.setContent(contentView);

		Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);

		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
				notificationIntent, 0);

		mBuilder.setContentIntent(intent);

		notification = mBuilder.build();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		//mNotificationManager.notify(1, notification);
		startForeground(1, notification);
	}

	public void sendNotyAllert(){
		String text = getResources().getString(R.string.notyAllert1) + TrafficService.allertLevel + getResources().getString(R.string.notyAllert2);

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification.Builder builder = new Notification.Builder(this)
				//.setTicker("Traffic Monitor!")
				.setContentTitle("Мониторинг траффика")
				.setContentText(text)
				.setContentIntent(pIntent)
				.setSmallIcon(R.drawable.icon_noty);
		//.addAction(R.drawable.check, "Запустить активность",
		//	pIntent).setAutoCancel(true);

		Notification notification = new Notification.BigTextStyle(builder)
				.bigText(text).build();
		Uri ringURI =
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		//notification.sound = ringURI;

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(3, notification);


		showNotyAllert = true;
		Log.d(LOG_TAG, "Показали уведомление об предупреждении");
	}

	public void sendNotyStop(){
		String text = getResources().getString(R.string.notyStop);

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification.Builder builder = new Notification.Builder(this)
				//.setTicker("Traffic Monitor!")
				.setContentTitle("Мониторинг траффика")
				.setContentText(text)
				.setContentIntent(pIntent)
				.setSmallIcon(R.drawable.icon_noty);
		//.addAction(R.drawable.check, "Запустить активность",
		//	pIntent).setAutoCancel(true);

		Notification notification = new Notification.BigTextStyle(builder)
				.bigText(text).build();
		Uri ringURI =
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		//notification.sound = ringURI;

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(2, notification);

		showNotyStop = true;
		Log.d(LOG_TAG, "Показали уведомление о лимите и отключении ");
	}

	public void task() {
		final Intent intent = new Intent(MainFragmentAdapter.BROADCAST_ACTION);
		final Handler uiHandler = new Handler();
		myTimerService.schedule(new TimerTask() { // Определяем задачу
			@Override
			public void run() {//функция для длительных задач(например работа с сетью)
					//if(runTimer) {

				if(Build.VERSION.SDK_INT < 23){

					if (isNewDay() || isNewMonth()) {
						Log.d(LOG_TAG, "Конец учетного периода");

						TrafficHelper.saveTotalTraffic(getBaseContext());
						TrafficHelper.resetTraffic();                        //считали значения сегоднашнего траффика, записали его в переменные вчерашнего
						cleanTable(idsim);                    //очистили таблицу
						setRebootAction(0);

						TrafficHelper.initAppTrafficList(getBaseContext());
						TrafficHelper.initAppTrafficList(getBaseContext());

						intent.putExtra(MainFragmentAdapter.UPDATE_CHART, 1);    //обновили граффик,


						try {
							sendBroadcast(intent);            //послали интент фрагменту
							Log.d(LOG_TAG, "Интент отправлен");
						} catch (Error e) {
							Log.d(LOG_TAG, "Error send br" + e.getMessage());
							e.printStackTrace();
						}
					} else {

						TrafficHelper.updateAppTrafficList(getBaseContext());

						if (getRebootAction() == 0) {
							TrafficHelper.saveTrafficAppsForReboot(getBaseContext());
							TrafficHelper.saveTrafficAppsForRebootWiFi(getBaseContext());
							TrafficHelper.saveTotalTrafficReboot(getBaseContext());
						}
					}

					allTrafficMobile = TrafficHelper.getAllTrafficMobile();

				}
				else{

					if (isNewDay()){
						//cleanTable("traffic_app");
						cleanTable(idsim);                    //очистили таблицу
						packageList.clear();
					}
					TrafficMHelper.refreshWidget(getBaseContext());
					TrafficMHelper.dbWriteTraffic(getBaseContext(),idsim);
					TrafficMHelper.updateNoty(getBaseContext(),getPackageName());
					//TrafficMHelper.updateData(getBaseContext());

					allTrafficMobile = TrafficMHelper.getAllTrafficMobile();

					Log.d(LOG_TAG, " Update all services on M " );

				}

				if (isAllertLevel() && !showNotyStop && !showNotyAllert)
					sendNotyAllert();
				if (isStopLevel() && !showNotyStop) {
					try {
						setMobileDataEnabled(getBaseContext(), false);
						sendNotyStop();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					}
				}

				uiHandler.post(new Runnable() { //здесь можна выводить на экран и работать с основным потоком
					@Override
					public void run() {
						if (runTimer) {


						}
					}
				});

			}

			;

		}, 0L, 3L * 1000); // интервал - 3000 миллисекунд, 0 миллисекунд до первого запуска.
	}

	boolean isNewDay() {

		if ( newDay && period == PERIOD_DAY) {
			newDay = false;
			firstWriteDB = true;
			return true;
		}

		return false;
	}			//наступил новый день?

	boolean isNewMonth() {

			Date d = new Date();

			SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);

			int month = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_MONTH,TrafficService.month);
			int day = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_DAY,TrafficService.day);

		//	Log.d(LOG_TAG, "month " + month + ", day  " + day);


			if (((day ==  d.getDate() && month == (d.getMonth() + 1)) || newMonth) && period == PERIOD_MOUNTH) {
				newMonth = false;
				firstWriteDB = true;
				return true;
			}
		return false;
	}			//наступил новый месяц?

	private void setMobileDataEnabled(Context context, boolean enabled) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
		final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField.get(conman);
		final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);

		setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}

	public void setTimer(){

		Calendar calNow = Calendar.getInstance();
		Calendar calSet = (Calendar) calNow.clone();

		calSet.set(Calendar.HOUR_OF_DAY,0);
		calSet.set(Calendar.MINUTE, 0);
		calSet.set(Calendar.SECOND, 0);
		calSet.set(Calendar.MILLISECOND, 0);

		if(calSet.compareTo(calNow) <= 0){
			//Today Set time passed, count to tomorrow
			Log.d(LOG_TAG, "Today Set time passed, count to tomorrow");
			calSet.add(Calendar.DATE, 1);
		}

		setAlarm(calSet);
	}

	public void setAlarm(Calendar calendar){

		Log.d(LOG_TAG, "\n\n***\n"
				+ "Alarm is set@ " + calendar.getTime() + "\n"
				+ "***\n");


		Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

	}

	public void startUpdateAppTraffic() {

		initAppListM();

		final Handler uiHandler = new Handler();
		timerAppsUpdate.schedule(new TimerTask() { // Определяем задачу
			@Override
			public void run() {//функция для длительных задач(например работа с сетью)
				if(TrafficService.runTimer) {

					initAppListM();

				}
				uiHandler.post(new Runnable() { //здесь можна выводить на экран и работать с основным потоком
					@Override
					public void run() {
						if (TrafficService.runTimer) {


						}
					}
				});

			}
		}, 0L, 600L * 1000); // интервал - 10 мин, 0 миллисекунд до первого запуска.
	}//каждые 3 мин обновляем список траффика по приложениям

	public void initAppListM(){

		Log.i(LOG_TAG, "initAppListM");
		packageList.clear();
		LoadPackageList loadPackageList = new LoadPackageList(getBaseContext(),this);
		loadPackageList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	} //обновляем траффик в БД по приложениям

	public void recoverSettings(){

		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);

		TrafficService.period = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_PERIOD,TrafficService.period);
		TrafficService.stopLevel = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_STOPL_EVEL,TrafficService.stopLevel);
		TrafficService.allertLevel = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_ALLERT_LEVEL,TrafficService.allertLevel);
		TrafficService.disable_internet = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_DISABLE_INTERNET,TrafficService.disable_internet);
		TrafficService.show_allert = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_SHOW_ALLERT,TrafficService.show_allert);
		TrafficService.day = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_DAY,TrafficService.day);
		TrafficService.month = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_MONTH,TrafficService.month);
		TrafficService.mYear = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_YEAR,TrafficService.mYear);



		boolean hasVisited = mySharedPreferences.getBoolean(APP_PREFERENCES_HAS_VISITED,false);

		if(!hasVisited){		//первый раз запуск приложения?
			newDay = true;		//очистим таблицу и траффик
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putBoolean(APP_PREFERENCES_HAS_VISITED,true);
			editor.apply();
		}

	}		//востановим настройки

	public void cleanTable(String table) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		Log.d(LOG_TAG, "--- Clear table " + table);
		// удаляем все записи
		int clearCount = db.delete(table, null, null);
		db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + table + "'");     //очищаем ID
		//Toast.makeText(this, "Таблица очищена.Удалено " + clearCount + " строк.", Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "deleted rows count = " + clearCount);
		//закрываем подключение к БД
		db.close();
	}	//очистка таблицы с траффиком

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent){
		Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmService.set(
				AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 1000,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);
		Log.d(LOG_TAG, "onTaskRemoved traffic_service");
	}


	@Override
	public void onGetPackage(Package p) {
		//&& !packageList.contains(p)
		if(p.isUseTraffic()){

			packageList.add(p);
			Log.i(LOG_TAG, "packageList.add ");

		}
	}
}
