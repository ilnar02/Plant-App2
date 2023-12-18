package com.ilnar.grandchat

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.databinding.ActivityNotificationAddBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.time.DayOfWeek
import java.util.*
import kotlin.random.Random

@Suppress("DEPRECATION")
class NotificationAddActivity : AppCompatActivity() {

    lateinit var binding: ActivityNotificationAddBinding
    lateinit var preferenceManager: PreferenceManager
    lateinit var item: FavoritePlants
    lateinit var type: String
    lateinit var text: String
    var timeText = "null"
    var min = 0
    var hour = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edTime.isEnabled = false

        item = intent.getSerializableExtra("item") as FavoritePlants
        type = intent.getSerializableExtra("type") as String
        preferenceManager = PreferenceManager(applicationContext)

        when (type) {
            "Полив" -> {
                text = "Полив"
                binding.imageView.setImageResource(R.drawable.ic_watering)
            }
            "Удобрение" -> {
                text = "Удобрение"
                binding.imageView.setImageResource(R.drawable.ic_top_dressing)
            }
            "Опрыскивание" -> {
                text = "Опрыскивание"
                binding.imageView.setImageResource(R.drawable.ic_characteristics)
            }
            else -> println("x не равно 1, 2 или 3")
        }

        binding.apply {
            plantName.text = item.input_name
            typeRecord.text = text
        }

        binding.btBack.setOnClickListener {
            finish()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Channel"
            val description = "Channel for reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
            channel.description = description

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.v("MyLog", "tut")
        }
    }

    private fun setRecurringNotifications(dayOfWeek: Int, hourOfDay: Int, minute: Int, notificationId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Установка времени первого напоминания
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay) // Настройте желаемое время уведомления
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val day = dayOfWeek
        val time = timeText
        val id = notificationId
        val text = type
        val name = item.input_name

        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val recordCollection = favoriteDocs.collection("notification")
        val data = hashMapOf(
            "notifications_type" to text,
            "day_of_week" to day,
            "text" to text,
            "id" to id,
            "time" to time,
            "name" to name,
        )
        recordCollection.add(data)
            .addOnSuccessListener { subDocumentRef ->
                showToast("Документ успешно добавлен в подколлекцию: ${subDocumentRef.id}")
            }
            .addOnFailureListener { e ->
                showToast("Ошибка при добавлении документа в подколлекцию: $e")
            }

        val intent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
//                    putExtra(NotificationBroadcastReceiver.EXTRA_NOTIFICATION_ID, dockId)
//                    putExtra(NotificationBroadcastReceiver.ITEM_TAG, item.id)
            putExtra(NotificationBroadcastReceiver.NOT_TYPE, text)
            putExtra(NotificationBroadcastReceiver.NAME_PLANT, name)
            putExtra(NotificationBroadcastReceiver.NOT_ID, id)
        }

        val pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
        // Установка повторяющегося напоминания каждую неделю для первого уведомления
        Log.v("MyLog", "tam")

    }

    fun onClickAddNot(view: View) {

        if (timeText != "null" && (binding.checkBox.isChecked || binding.checkBox2.isChecked || binding.checkBox3.isChecked || binding.checkBox4.isChecked || binding.checkBox5.isChecked || binding.checkBox6.isChecked || binding.checkBox7.isChecked)) {

            if (binding.checkBox.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(2, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox2.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(3, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox3.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(4, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox4.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(5, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox5.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(6, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox6.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(7, hour, min, Random.nextInt(1, 100000))
            }
            if (binding.checkBox7.isChecked) {
                createNotificationChannel()
                setRecurringNotifications(1, hour, min, Random.nextInt(1, 100000))
            }

            finish()
        }
        else showToast("Выберите день недели и время!")
    }

    fun onClickTime(view: View) {
        MaterialDialog(this).show {
            timePicker { dialog, time ->
                min = time.get(Calendar.MINUTE)
                hour = time.get(Calendar.HOUR_OF_DAY)
                var minStr = min.toString()
                var hourStr = hour.toString()
                if (min<10) {minStr = "0" + min.toString()}
                if (hour<10) {hourStr = "0" + hour.toString()}
                timeText = hourStr + ":" + minStr
                binding.edTime.setText(timeText)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}