package com.ilnar.grandchat

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BasicGridItem
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.gridItems
import com.afollestad.materialdialogs.input.input
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.databinding.ActivityFavoriteContentBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.util.*

@Suppress("DEPRECATION")
class FavoriteContentActivity : AppCompatActivity(), NotificationAdapter.Listener {


    lateinit var binding: ActivityFavoriteContentBinding
    lateinit var item: FavoritePlants
    lateinit private var preferenceManager: PreferenceManager
    private val adapter = NotificationAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)
        item = intent.getSerializableExtra("favorite") as FavoritePlants

        val transformation = CropCircleTransformation()
        val imageUrl = item.image
        Picasso.get()
            .load(imageUrl)
            .transform(transformation)
            .into(binding.favoriteImage)

        binding.apply {
            plantName.text = item.input_name
            realPlantName.text = item.name

        }
        binding.apply {
            rvViewNotification.layoutManager = LinearLayoutManager(this@FavoriteContentActivity)
            rvViewNotification.adapter = adapter

        }

    }


    override fun onResume() {
        super.onResume()
        Log.v("MyLog", "Zashel")
        dataCollection()
    }

    fun onClickWater(view: View) {
        startActivity(Intent(this, NewRecordActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", 1)
        })
    }

    fun onClickUdob(view: View) {
        startActivity(Intent(this, NewRecordActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", 2)
        })
    }

    fun onClickOprisk(view: View) {
        startActivity(Intent(this, NewRecordActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", 3)
        })
    }

    fun onClickPhoto(view: View) {
        startActivity(Intent(this, NewRecordActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", 4)
        })
    }

    fun onClickConfirm(view: View) {
        MaterialDialog(this).show {
            title(R.string.confirmation)
            message(R.string.question)
            positiveButton(text = "Подтвердить") { dialog ->
                Dele()
            }
            negativeButton(text = "Отмена") { dialog ->

            }
        }
    }

    fun Dele() {
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val recordCollection = favoriteDocs.collection("records")
        //удаляем журнал
        recordCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                querySnapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        // Все документы успешно удалены
                        // Теперь можно удалить саму коллекцию
                        recordCollection.document().delete()
                            .addOnSuccessListener {
                                showToast("Записи успешно удалены")
                            }
                            .addOnFailureListener { e ->
                                // Обработка ошибки удаления коллекции
                            }
                    }
                    .addOnFailureListener { e ->
                        // Обработка ошибки удаления документов
                    }
            }
            .addOnFailureListener { e ->
                // Обработка ошибки получения документов в коллекции
            }
        //удаляем уведомления
        val notificationCollection = favoriteDocs.collection("notification")

        notificationCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                querySnapshot.documents.forEach { document ->

                    val notifications_type = document.getString("notifications_type")
                    val id = document.getLong("id")?.toInt()
                    val name = document.getString("name")
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
                        putExtra(NotificationBroadcastReceiver.NOT_TYPE, notifications_type)
                        putExtra(NotificationBroadcastReceiver.NAME_PLANT, name)
                        putExtra(NotificationBroadcastReceiver.NOT_ID, id)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(this, id!!, intent, 0)
                    // Отмена повторяющегося напоминания
                    alarmManager.cancel(pendingIntent)
                    Log.v("MyLog", "Удалено")

                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        // Все документы успешно удалены
                        // Теперь можно удалить саму коллекцию
                        notificationCollection.document().delete()
                            .addOnSuccessListener {
                                showToast("Записи успешно удалены")
                            }
                            .addOnFailureListener { e ->
                                // Обработка ошибки удаления коллекции
                            }
                    }
                    .addOnFailureListener { e ->
                        // Обработка ошибки удаления документов
                    }
            }
            .addOnFailureListener { e ->
                // Обработка ошибки получения документов в коллекции
            }

        favoriteDocs.delete()
            .addOnSuccessListener {
                showToast("Документ успешно удален")
            }
            .addOnFailureListener { e ->
                showToast("Обработка ошибки удаления документа")
            }
        finish()
    }

    @SuppressLint("CheckResult")
    fun onClickConfirmEdit(view: View) {
        MaterialDialog(this).show {
            message(text = "Введите новое название для растения")
            input { dialog, text ->
                edit(text.toString())
            }
            positiveButton(text = "Подтвердить")
        }
    }

    fun edit(name: String){
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val fieldUpdates = hashMapOf<String, Any>(
            "input_name" to name
        )
        favoriteDocs.update(fieldUpdates)
            .addOnSuccessListener {
                showToast("Теперь растиху зовут - " + name)
                binding.plantName.text = name
            }
            .addOnFailureListener { e ->
                showToast("Не получилось")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun onClickLog(view: View) {
        startActivity(Intent(this, LogActivity::class.java).apply {
            putExtra("log", item)
        })
    }

    fun onClickNotification(view: View) {

        val items = listOf(
            BasicGridItem(R.drawable.ic_watering, "Полив"),
            BasicGridItem(R.drawable.ic_top_dressing, "Удобрение"),
            BasicGridItem(R.drawable.ic_characteristics, "Опрыскив.")
        )

        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            cornerRadius(16f)
            title(R.string.addNot)
            gridItems(items) { _, index, item ->
                when(index) {
                    0 -> {
                        showToast("Selected item ${item.title} at index $index")
                        startNewNotification("Полив")
                    }
                    1 -> {
                        showToast("Selected item ${item.title} at index $index")
                        startNewNotification("Удобрение")
                    }
                    2 -> {
                        showToast("Selected item ${item.title} at index $index")
                        startNewNotification("Опрыскивание")
                    }
                }
            }
        }
    }

    fun startNewNotification(type: String){
        startActivity(Intent(this, NotificationAddActivity::class.java).apply {
            putExtra("item", item)
            putExtra("type", type)
        })
    }


    fun dataCollection(){
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val recordCollection = favoriteDocs.collection("notification")

        adapter.clearNoti()
        recordCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val notifications_type = document.getString("notifications_type")
                    val day_of_week = document.getLong("day_of_week")?.toInt()
                    val dockId = document.id
                    val id = document.getLong("id")?.toInt()
                    val name = document.getString("name")
                    val text = document.getString("text")
                    val time = document.getString("time")
                    val notification = Notifications(notifications_type, day_of_week, text, id, time, name, dockId)
                    adapter.addNoti(notification)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки журнала ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    fun Del(notifications: Notifications) {
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val notificationCollection = favoriteDocs.collection("notification")
        val notificationsDocs = notificationCollection.document(notifications.dockId)
        notificationsDocs.delete()
            .addOnSuccessListener {
                showToast("Документ успешно удален")
            }
            .addOnFailureListener { e ->
                showToast("Обработка ошибки удаления документа")
            }
        dataCollection()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java).apply {
            putExtra(NotificationBroadcastReceiver.NOT_TYPE, notifications.notifications_type)
            putExtra(NotificationBroadcastReceiver.NAME_PLANT, notifications.name)
            putExtra(NotificationBroadcastReceiver.NOT_ID, notifications.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, notifications.id!!, intent, 0)
        // Отмена повторяющегося напоминания
        alarmManager.cancel(pendingIntent)
        Log.v("MyLog", "Удалено")
    }

    override fun onClickConf(notifications: Notifications) {
        MaterialDialog(this).show {
            title(R.string.confirmation)
            message(R.string.question)
            positiveButton(text = "Подтвердить") { dialog ->
                Del(notifications)
            }
            negativeButton(text = "Отмена") { dialog ->

            }
        }
    }
}