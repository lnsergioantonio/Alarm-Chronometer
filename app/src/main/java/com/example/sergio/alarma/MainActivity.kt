package com.example.sergio.alarma

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener  {
    lateinit var timepikcer : TimePicker
    lateinit var alarmManager:AlarmManager
    lateinit var txtAlarm: TextView
    lateinit var tabHost: TabHost
    lateinit var chronometer:Chronometer
    lateinit var toggleButton:ToggleButton

    lateinit var alarmIntent:PendingIntent
    var currentHour: Int = 0
    var currentMinut: Int= 0

    var isStart = false
    var timePause:Long = 0
    private var isPause= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // create and initialice tabs
        tabHost = findViewById(R.id.tabHost)
        // prepare for configuration
        tabHost.setup()
        newTab("tab1",R.id.tab1,"Alarm")
        newTab("tab2",R.id.tab2,"Chronometer")
/*        tabHost.setOnTabChangedListener { tabId: String?  ->
            Toast.makeText(this,"selected tab:"+tabId+" id tab:${tabHost.currentTab}",Toast.LENGTH_LONG).show()
        }*/
        //end tab
        // ALARM
        timepikcer = findViewById(R.id.timepickerAlarm)
        txtAlarm = findViewById(R.id.txtAlarm)
        toggleButton = findViewById(R.id.toggleButton)


        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        txtAlarm.text = "Alarma establecida: ${getHour()} : ${getMinutes()}"

        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            //swAlarm.showText=true
        timepikcer.setOnTimeChangedListener { view, hourOfDay, minute ->
            var hourDay:String ="${hourOfDay}"
            var format :String =""
            when{
                hourOfDay == 0 ->{
                    hourDay="${hourOfDay+12}"
                    format="a.m."
                }
                hourOfDay >= 12 -> {
                    hourDay="${if (hourOfDay>12) hourOfDay-12 else hourOfDay }"
                    format="p.m."
                }
                else -> format="a.m."
            }
            currentHour=getHour()
            currentMinut=getMinutes()
            txtAlarm.text = "Alarma establecida: ${hourDay}:${if (minute < 10) "0" + minute else minute} ${format}"
        }

        // CHRONOMETER
        chronometer = findViewById(R.id.chronometer)
        findViewById<Button>(R.id.btnStart).setOnClickListener(this)
        findViewById<Button>(R.id.btnPause).setOnClickListener(this)
        findViewById<Button>(R.id.btnStop).setOnClickListener(this)
        findViewById<ToggleButton>(R.id.toggleButton).setOnClickListener(this)
    }
    // functions for alarm
    fun newTab(tag:String, idContent: Int, title:String){
        var spec = tabHost.newTabSpec(tag)
        spec.setContent(idContent)
        spec.setIndicator(title)
        tabHost.addTab(spec)
    }

    // hours
    private fun setHour(hour:Int){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
            timepikcer.hour=hour
        else
            timepikcer.currentHour=hour
    }
    private fun getHour(): Int{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return timepikcer.hour
        }
        else{
            return timepikcer.currentHour
        }

    }
    // minutes
    private fun setMinutes(minute:Int){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
            timepikcer.minute=minute
        else
            timepikcer.currentMinute=minute
    }
    private fun getMinutes(): Int{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return timepikcer.minute
        }
        else{
            return timepikcer.currentMinute
        }
    }

    // functions for chronometer
    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.btnStart->{
                    if (!isStart){
                        chronometer.base=SystemClock.elapsedRealtime()-timePause
                        chronometer.start()
                        isStart=true
                        isPause=false
                        findViewById<Button>(R.id.btnStart).text="Start"
                    }
                }
                R.id.btnPause->{
                    if (isStart){
                        chronometer.stop()
                        timePause = SystemClock.elapsedRealtime() - chronometer.base
                        isStart=false
                        isPause = true
                        findViewById<Button>(R.id.btnStart).text="Resume"
                    }
                }
                R.id.btnStop->{
                    chronometer.base=SystemClock.elapsedRealtime()
                    chronometer.stop()
                }
                R.id.toggleButton ->{
                    var time: Long
                    var intent = Intent(this, AlarmReceiver::class.java)
                    if (toggleButton.isChecked){
                        Toast.makeText(this,"Alarma ON",Toast.LENGTH_LONG).show()

                        var calendar = Calendar.getInstance()
                        calendar.timeInMillis=System.currentTimeMillis()
                        calendar.set(Calendar.HOUR_OF_DAY,currentHour)
                        calendar.set(Calendar.MINUTE,currentMinut)
                        calendar.set(Calendar.SECOND,0)

                        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

                        // Repeticiones en intervalos de 20 minutos
                        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,0,alarmIntent)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
                    }else{
                        alarmIntent = PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.cancel(alarmIntent)
                        Toast.makeText(this,"Alarma Off",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
