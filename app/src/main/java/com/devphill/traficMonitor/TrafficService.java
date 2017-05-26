package com.devphill.traficMonitor;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.devphill.traficMonitor.fragments.FragmentTrafficApps;
import com.devphill.traficMonitor.fragments.MainFragmentAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TrafficService extends Service {

	final String LOG_TAG = "serviceTag";
	final int MAX_SIM = 10;
	public long mobile_trafficTXToday, mobile_trafficRXToday, mobile_trafficTXYesterday, mobile_trafficRXYesterday,allTrafficMobile;
	static public String idsim = "mySim";
	public int networkstate;

	public String list_table[] = new String[10];

	static public long rowID;

	static public boolean newDay = false;
	static public boolean newMonth = false;
	boolean firstWriteDB = true;

	private boolean isWifiEnabled = false;
	private boolean isMobilEnabled = false;

	DBHelper dbHelper;
	Timer myTimerService = new Timer(); // Создаем таймер
//	Timer myTimer2Service = new Timer(); // Создаем таймер
	public static ArrayList<ApplicationItem> appList = new ArrayList<ApplicationItem>();

	public static int CLEAN_TABLE = 1;
	public static int SET_REBOOT_ACTION = 2;
	public static int ALARM_ACTION = 3;

	boolean runTimer = false;
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

	public static final String APP_PREFERENCES = "settingsTrafficMonitor";
	public static final String APP_PREFERENCES_TRAFFIC_APPS = "TrafficApps";
	public static final String APP_PREFERENCES_TOTAL_TRAFFIC = "totalTraffic";
	public static final String APP_PREFERENCES_TRAFFIC_APPS_REBOOT= "TrafficAppsReboot";
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

	Widget widget = new Widget();

	public static boolean brRegistered = false;

	//FragmentTrafficApps fragment2 = new FragmentTrafficApps();

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

		initAppTrafficList();

		setTimer();


		initNoty();
		task();
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
			setRebootAction(1);
			resetTrafficReboot();
			Log.d(LOG_TAG, "SET_REBOOT_ACTION");
		}
		else if (task == ALARM_ACTION){
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
		Log.d(LOG_TAG, "getRebootAction = " + mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION,0));
		return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION,0);

	}
	public void updateNoty (){

		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.noty);
		NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext());
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification;

		float trafficFloat = (float)allTrafficMobile/1024;
		trafficFloat = Math.round(trafficFloat*(float)10.0)/(float)10.0;

		float trafficTxFloat = (float)mobile_trafficTXToday/1048576;
		trafficTxFloat = Math.round(trafficTxFloat*(float)10.0)/(float)10.0;  //округляем до сотых


		float trafficRxFloat = (float)mobile_trafficRXToday/1048576;
		trafficRxFloat = Math.round(trafficRxFloat*(float)10.0)/(float)10.0;  //округляем до сотых

		float procent = trafficFloat /  (float)stopLevel*100;
		procent = Math.round(procent*(float)10.0/(float)10.0);

		contentView.setImageViewResource(R.id.image, R.drawable.bittorrent);
		contentView.setProgressBar(R.id.usageData, stopLevel, (int) (allTrafficMobile / 1024), false);
		contentView.setImageViewResource(R.id.imSendData, R.drawable.arrowup);
		contentView.setImageViewResource(R.id.imDownloadData, R.drawable.arrowdown);
		if((int)trafficFloat < 100) {
			contentView.setTextViewText(R.id.title, "Использовано " + trafficFloat + " Мб");
			contentView.setTextViewText(R.id.tvDownloadData,Float.toString(trafficRxFloat));
			contentView.setTextViewText(R.id.tvSendData,Float.toString(trafficTxFloat));
		}
		else {
			contentView.setTextViewText(R.id.title, "Использовано " + (int)trafficFloat + " Мб");
			contentView.setTextViewText(R.id.tvDownloadData,"" + (int)trafficRxFloat);
			contentView.setTextViewText(R.id.tvSendData,"" + (int)trafficTxFloat);
		}

		contentView.setTextViewText(R.id.procentData,(int)procent + " %");

		Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);

		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,
				notificationIntent, 0);

		mBuilder.setContentIntent(intent);


		mBuilder.setSmallIcon(R.drawable.bittorrent);
		mBuilder.setContent(contentView);



		notification = mBuilder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		//notification.defaults |= Notification.DEFAULT_SOUND;
		//notification.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificationManager.notify(1, notification);

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
	private void refreshWidget() {

		// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query(idsim, null, null, null, null, null, null);
		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (c.moveToLast()) {
			int mobile_trafficTXYesterdayColIndex = c.getColumnIndex("mobile_trafficTXYesterday");
			int mobile_trafficRXYesterdayColIndex = c.getColumnIndex("mobile_trafficRXYesterday");
			int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
			int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");
			int reboot_deviceColIndex = c.getColumnIndex("reboot_device");

			if(getRebootAction() == 1){
				mobile_trafficTXToday = TrafficStats.getMobileTxBytes() + c.getLong(mobile_trafficTXYesterdayColIndex); //Переданные через мобильный интерфейс
				mobile_trafficRXToday = TrafficStats.getMobileRxBytes() + c.getLong(mobile_trafficRXYesterdayColIndex); //Принятые через мобильный интерфейс
			//	Log.d(LOG_TAG, "mobile_trafficTXToday = " + mobile_trafficTXToday +
				//		" mobile_trafficRXToday = " + mobile_trafficRXToday);
			}
			else{
				mobile_trafficTXToday = TrafficStats.getMobileTxBytes() - c.getLong(mobile_trafficTXYesterdayColIndex); //Переданные через мобильный интерфейс
				mobile_trafficRXToday = TrafficStats.getMobileRxBytes() - c.getLong(mobile_trafficRXYesterdayColIndex); //Принятые через мобильный интерфейс

			//	Log.d(LOG_TAG, "mobile_trafficTXToday = " + mobile_trafficTXToday +
				//		" mobile_trafficRXToday = " + mobile_trafficRXToday);
			}


		}
		allTrafficMobile = mobile_trafficTXToday + mobile_trafficRXToday;
		if(allTrafficMobile >= 0 && mobile_trafficTXToday >= 0 && mobile_trafficRXToday >= 0) {
			//Log.d(LOG_TAG, "Update Widget.");
			//Log.d(LOG_TAG, "\n allTrafficMobile " + allTrafficMobile + " \n mobile_trafficTXToday " + mobile_trafficTXToday + "\n mobile_trafficRXToday " + mobile_trafficRXToday);
			allTrafficMobile = allTrafficMobile / 1024;       //для Кб
		}
		else {
			//Log.d(LOG_TAG, "Update Widget.ошибка подсчета траффика. \n Отрицательный траффик.");
			///Log.d(LOG_TAG, "\n allTrafficMobile " + allTrafficMobile + " \n mobile_trafficTXToday " + mobile_trafficTXToday + "\n mobile_trafficRXToday " + mobile_trafficRXToday);
			mobile_trafficTXToday = 0;
			mobile_trafficRXToday = 0;
			allTrafficMobile = 0;
		}
		float trafficFloat = (float)allTrafficMobile/1024;
		trafficFloat = Math.round(trafficFloat*(float)100.0)/(float)100.0;

		float trafficTxFloat = (float)mobile_trafficTXToday/1048576;
		trafficTxFloat = Math.round(trafficTxFloat*(float)100.0)/(float)100.0;  //округляем до сотых

		float trafficRxFloat = (float)mobile_trafficRXToday/1048576;
		trafficRxFloat = Math.round(trafficRxFloat*(float)100.0)/(float)100.0;  //округляем до сотых

		Intent i = new Intent(getBaseContext(),Widget.class);
		i.setAction(Widget.FORCE_WIDGET_UPDATE);

		i.putExtra("trafficFloat",trafficFloat);
		i.putExtra("trafficTxFloat",trafficTxFloat);
		i.putExtra("trafficRxFloat",trafficRxFloat);
		i.putExtra("stopLevel",stopLevel);

		widget.onReceive(getBaseContext(),i);


	//	db.close();
		c.close();


	}
	public void task() {
		final Intent intent = new Intent(MainFragmentAdapter.BROADCAST_ACTION);
		final Handler uiHandler = new Handler();
		myTimerService.schedule(new TimerTask() { // Определяем задачу
			@Override
			public void run() {//функция для длительных задач(например работа с сетью)
					//if(runTimer) {


						if (isNewDay() || isNewMonth()) {
							Log.d(LOG_TAG, "Конец учетного периода");
							saveAppTraffic();					   //сохраним значения траффика для приложений при обнулении статистики
							saveTotalTraffic();
							resetTraffic();                        //считали значения сегоднашнего траффика, записали его в переменные вчерашнего
							cleanTable(idsim);                    //очистили таблицу
							setRebootAction(0);
							intent.putExtra(MainFragmentAdapter.UPDATE_CHART, 1);    //обновили граффик,


							try {
								sendBroadcast(intent);            //послали интент фрагменту
								Log.d(LOG_TAG, "Интент отправлен");
							} catch (Error e) {
								Log.d(LOG_TAG, "Error send br" + e.getMessage());
								e.printStackTrace();
							}
						} else
							Log.d(LOG_TAG, "Спим дальше");

						refreshWidget();

						updateNoty();
						updateData();

						//refreshService();

						dbWriteTraffic();

						updateAppTrafficList();
						saveTrafficAppsForReboot();

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

				//	}

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
	public void refreshService(){

		String netType = getNetworkType(getBaseContext());
		Timer timer = new Timer();

		TimerTask delayedThreadStartTask = new TimerTask() {
			@Override
			public void run() {

				//captureCDRProcess();
				//moved to TimerTask
				new Thread(new Runnable() {
					@Override
					public void run() {
					if (!isNetworkConnected(getBaseContext())) {
							Log.d(LOG_TAG, "Подключение к сети отсутствует!");
							stopSelf();
							stopForeground(true);
						} else {
							Log.d(LOG_TAG, "Подключение к сети присутствует!");

						}

					}
				}).start();
			}
		};

		timer.schedule(delayedThreadStartTask, 20 * 1000); //10 sec
	}
	public String getNetworkType(Context context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			return activeNetwork.getTypeName();
		}
		return null;
	}			//получим тип сети
	public boolean isNetworkConnected(Context context) {
		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}		//есть ли инет
	boolean isNewDay() {

		/*// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		Log.d(LOG_TAG, "Проверяем на новый день ");
		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query(idsim, null, null, null, null, null, null);

		if (c.moveToLast()) {

			Date d = new Date();

			int lastDayColIndex = c.getColumnIndex("lastDay");
			int idColIndex = c.getColumnIndex("id");

			int lastDay = c.getInt(lastDayColIndex);


			Log.d(LOG_TAG, "lastDay " + lastDay + ", id  " + c.getLong(idColIndex));


			if ((lastDay != d.getDate() || newDay) && period == PERIOD_DAY) {
				newDay = false;
				firstWriteDB = true;
				return true;
			}

		}
		c.close();*/

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

			Log.d(LOG_TAG, "month " + month + ", day  " + day);


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
	public void saveAppTraffic(){
		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();

		if(appList != null) {
			for (int i = 0; i < appList.size(); i++) {
				ApplicationItem item = appList.get(i);
				Log.i(LOG_TAG,  " saveAppTraffic app = " + item.getApplicationLabel(getBaseContext().getPackageManager()) + "\n " +
						item.getMobileMb());
				editor.putFloat(item.getApplicationLabel(getBaseContext().getPackageManager()),item.getMobileMb());
			}
		}
		editor.apply();


	} //ложим траффик приложения по окончанию учетного периода
	public void saveTrafficAppsForReboot(){

		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();

		if(appList != null) {
			for (int i = 0; i < appList.size(); i++) {
				ApplicationItem item = appList.get(i);
				//Log.i(LOG_TAG, " saveTrafficAppsForReboot app = " + item.getApplicationLabel(getBaseContext().getPackageManager()) + "\n " +
					//	item.getMobileUsageKb());
				editor.putFloat(item.getApplicationLabel(getBaseContext().getPackageManager()),item.getMobileUsageKb());
			}
		}

		editor.apply();


	} //ложим траффик приложения если была перезагрузка
	public void saveTotalTraffic() {

		Log.d(LOG_TAG, "saveTotalTraffic" );
		SharedPreferences mySharedPreferences = getBaseContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();

		long totalTraffic = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

		editor.putLong(APP_PREFERENCES_TOTAL_TRAFFIC, totalTraffic);

		editor.apply();
	}
	public void resetTraffic() {

		mobile_trafficTXYesterday = TrafficStats.getMobileTxBytes();
		mobile_trafficRXYesterday = TrafficStats.getMobileRxBytes();

		mobile_trafficTXToday = 0;
		mobile_trafficRXToday = 0;

	}	//перезаписываем теперешний траффик в переменные для вчерашнего
	public void resetTrafficReboot() {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		Cursor c = db.query(idsim, null, null, null, null, null, null);

		long realtime = System.currentTimeMillis();
		Date date = new Date(realtime);

		if (c.moveToLast()) {
			int mobile_trafficTXYesterdayColIndex = c.getColumnIndex("mobile_trafficTXYesterday");
			int mobile_trafficRXYesterdayColIndex = c.getColumnIndex("mobile_trafficRXYesterday");
			int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
			int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");


			mobile_trafficTXYesterday = c.getLong(mobile_trafficTXTodayColIndex); //Переданные через мобильный интерфейс
			mobile_trafficRXYesterday = c.getLong(mobile_trafficRXTodayColIndex); //Принятые через мобильный интерфейс

			}

		cv.put("mobile_trafficTXToday", mobile_trafficTXToday);
		cv.put("mobile_trafficRXToday", mobile_trafficRXToday);
		cv.put("mobile_trafficTXYesterday", mobile_trafficTXYesterday);
		cv.put("mobile_trafficRXYesterday", mobile_trafficRXYesterday);
		//cv.put("time", realtime);
		cv.put("allTrafficMobile", allTrafficMobile);
		cv.put("lastDay", date.getDate());
		cv.put("reboot_device", 1);


		// вставляем запись и получаем ее ID
		rowID = db.insert("" + idsim + "", null, cv);
		Log.d(LOG_TAG, "row inserted in resetTrafiic reboot, ID = " + rowID);

		c.close();


	}	//в переменные для вчерашнего запишем траффик до перезагрузки
	public void initSettings() {

		// создаем объект для данных
		ContentValues cv = new ContentValues();

		// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query(idsim, null, null, null, null, null, null);

		cv.put("period", TrafficService.PERIOD_DAY);
		cv.put("periodChartOffset", 29);
		cv.put("stopLevel", 98);
		cv.put("allertLevel", 80);
		cv.put("disable_internet", 0);
		cv.put("show_allert", 1);
		cv.put("day", 15);
		cv.put("month", 9);
		cv.put("year", 2017);


		// вставляем запись и получаем ее ID
		rowID = db.insert("" + "set" + idsim + "", null, cv);
		Log.d(LOG_TAG, "row inserted, ID = " + rowID);

		newDay = true;

		// закрываем подключение к БД
	//	db.close();
		c.close();
	}		//настройки старіе в бд(не используется)
	public void initAppTrafficList() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

			}
		}, 3000); //3000 sec delay


		for (ApplicationInfo app : getBaseContext().getPackageManager().getInstalledApplications(0)) {
			ApplicationItem item = ApplicationItem.create(app, getBaseContext());
			if (item != null) {
				appList.add(item);
			}
		}
		Log.d(LOG_TAG, "initAppTrafficList table " );

	}
	public void updateAppTrafficList() {
			updateNetworkState();
			for (int i = 0; i < appList.size(); i++) {

				appList.get(i).setMobilTraffic(isMobilEnabled);
				appList.get(i).update();

				/*ApplicationItem app = appList.get(i);
				app.setMobilTraffic(isMobilEnabled);
				app.update();*/
			}

	}
	private void updateNetworkState() {
		isWifiEnabled = isConnectedWifi();
		isMobilEnabled = isConnectedMobile();
	}
	public boolean isConnectedWifi(){
		ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
	}
	public boolean isConnectedMobile(){
		ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
	}
	public void dbWriteTraffic() {
		//Log.d(LOG_TAG, "--- Insert in " + idsim);
		// подготовим данные для вставки в виде пар: наименование столбца - значение

		long realtime = System.currentTimeMillis();
		Date date = new Date(realtime);
		// создаем объект для данных

		ContentValues cv = new ContentValues();
		// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// делаем запрос всех данных из таблицы mytable, получаем Cursor
		Cursor c = db.query(idsim, null, null, null, null, null, null);
		/*
		// ставим позицию курсора на первую строку выборки
		// если в выборке нет строк, вернется false
		if (c.moveToLast()) {
			int mobile_trafficTXYesterdayColIndex = c.getColumnIndex("mobile_trafficTXYesterday");
			int mobile_trafficRXYesterdayColIndex = c.getColumnIndex("mobile_trafficRXYesterday");

			Log.d(LOG_TAG, "mobile_trafficTXYesterday = " + c.getLong(mobile_trafficTXYesterdayColIndex) +
					" mobile_trafficRXYesterday = " + c.getLong(mobile_trafficRXYesterdayColIndex));

			Log.d(LOG_TAG, "TrafficStats.getMobileTxBytes() = " + TrafficStats.getMobileTxBytes() +
					" TrafficStats.getMobileRxBytes() = " + TrafficStats.getMobileRxBytes());

			mobile_trafficTXToday = TrafficStats.getMobileTxBytes() - c.getLong(mobile_trafficTXYesterdayColIndex); //Переданные через мобильный интерфейс
			mobile_trafficRXToday = TrafficStats.getMobileRxBytes() - c.getLong(mobile_trafficRXYesterdayColIndex); //Принятые через мобильный интерфейс

			mobile_trafficTXYesterday = c.getLong(mobile_trafficTXYesterdayColIndex);
			mobile_trafficRXYesterday = c.getLong(mobile_trafficRXYesterdayColIndex);

			Log.d(LOG_TAG, "mobile_trafficTXToday = " + mobile_trafficTXToday +
					" mobile_trafficRXToday = " + mobile_trafficRXToday);

		}*/
		allTrafficMobile = mobile_trafficTXToday + mobile_trafficRXToday;
		allTrafficMobile = allTrafficMobile / 1024;       //для Кб

		//Log.d(LOG_TAG, "allTrafficMobile = " + allTrafficMobile);

		if (allTrafficMobile > 0 || firstWriteDB) {

			cv.put("mobile_trafficTXToday", mobile_trafficTXToday);
			cv.put("mobile_trafficRXToday", mobile_trafficRXToday);
			cv.put("mobile_trafficTXYesterday", mobile_trafficTXYesterday);
			cv.put("mobile_trafficRXYesterday", mobile_trafficRXYesterday);
			cv.put("time", realtime);
			cv.put("allTrafficMobile", allTrafficMobile);
			cv.put("lastDay", date.getDate());
		//	cv.put("reboot_device", 1);


			// вставляем запись и получаем ее ID
			rowID = db.insert("" + idsim + "", null, cv);
			Log.d(LOG_TAG, "row inserted, ID = " + rowID);

			firstWriteDB = false;
		}
		// закрываем подключение к БД
		//db.close();
		c.close();


	}		//записть данных в бд
	public void updateData (){

		final Intent intent = new Intent(MainFragmentAdapter.BROADCAST_ACTION);

		intent.putExtra(MainFragmentAdapter.UPDATE_DATA,1);    //обновили граффик,

		Intent intent2 = new Intent(FragmentTrafficApps.UPDATE_TRAFFIC_APPS);
		intent2.putExtra("allTrafficMobile",allTrafficMobile);
		sendBroadcast(intent2);
		//fragment2.initAdapter();
	//	fragment2.updateAdapter();

	try
	{
		sendBroadcast(intent);            //послали интент фрагменту
	///	Log.d(LOG_TAG, "Интент для обновленя отправлен");
	}
	catch(Error e)
	{
		Log.d(LOG_TAG, "Error send br" + e.getMessage());
		e.printStackTrace();
	}

}			//посылем сообщения фрагментам для обновления данных
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

		Log.d(LOG_TAG, "--- Clear table " + idsim);
		// удаляем все записи
		int clearCount = db.delete(idsim, null, null);
		db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + idsim + "'");     //очищаем ID
		//Toast.makeText(this, "Таблица очищена.Удалено " + clearCount + " строк.", Toast.LENGTH_SHORT).show();
		Log.d(LOG_TAG, "deleted rows count = " + clearCount);
		//закрываем подключение к БД
		db.close();
	}	//очистка таблицы с траффиком
    public void onDestroy() {
	    super.onDestroy();//sendBroadcast(new Intent("YouWillNeverKillMe"));
		//runTimer = false;
	    Log.d(LOG_TAG, "onDestroy traffic_service");
	  }
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
}
