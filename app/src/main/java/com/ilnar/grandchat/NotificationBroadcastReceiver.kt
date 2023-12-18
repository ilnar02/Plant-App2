package com.ilnar.grandchat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.Constants.CHANNEL_ID
import com.ilnar.grandchat.utilities.PreferenceManager

@Suppress("DEPRECATION")
class NotificationBroadcastReceiver  : BroadcastReceiver() {

    //lateinit var preferenceManager: PreferenceManager

    override fun onReceive(context: Context, intent: Intent) {

//        preferenceManager = PreferenceManager(context)

//        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
//        val notification = intent.getSerializableExtra(EXTRA_NOTIFICATION_ID) as? Notifications

//        val notification = intent.getStringExtra(EXTRA_NOTIFICATION_ID)!!
//        val itemId = intent.getStringExtra(ITEM_TAG)!!

        val notId = intent.getIntExtra(NOT_ID, 0)
        val notType = intent.getStringExtra(NOT_TYPE)
        val namePlant = intent.getStringExtra(NAME_PLANT)


        Log.v("MyLog", "ID документа растения: " + notId)

        when (notType) {
            "Полив" -> {
                showNotification(
                    context,
                    "Напоминание для " + namePlant,
                    "Напоминаю о том, что нужно полить растение",
                    notId
                )
            }
            "Удобрение" -> {
                showNotification(
                    context,
                    "Напоминание для " + namePlant,
                    "Напоминаю о том, что нужно удобрить растение",
                    notId
                )
            }
            "Опрыскивание" -> {
                showNotification(
                    context,
                    "Напоминание для " + namePlant,
                    "Напоминаю о том, что нужно опрыснуть растение",
                    notId
                )
            }
        }
//
//        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
//        val db = FirebaseFirestore.getInstance()
//        val usersCollection = db.collection("users")
//        val usersDocs = usersCollection.document(myDocument)
//        val favoriteCollection = usersDocs.collection("favorite")
//        val favoriteDocs = favoriteCollection.document(itemId)
//        val notificationCollection = favoriteDocs.collection("notification")
//        val notificationDocs = notificationCollection.document(notification)
//
//        notificationDocs.get()
//            .addOnSuccessListener { querySnapshot ->
//                val notifications_type = querySnapshot.getString("notifications_type")
//                val name = querySnapshot.getString("name")
//                val id = querySnapshot.getLong("id")?.toInt()!!
//                Log.v("MyLog", "ID уведомления: " + id)
//
//                when (notifications_type) {
//                    "Полив" -> {
//                        showNotification(context, "Напоминание для " + name, "Напоминаю о том, что нужно полить растение", id)
//                    }
//                    "Удобрение" -> {
//                        showNotification(context, "Уведомление 2", "Текст уведомления 2", 1)
//                    }
//                    "Опрыскивание" -> {
//                        showNotification(context, "Уведомление2 2", "Текст уведо2мления 2", 3)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.v("MyLog", "ID уведомления: " + exception)
//            }




//        Log.v("MyLog", "ID уведомления: " + notificationId)



//        when (notificationId) {
//            1 -> {
//                showNotification(context, "Напоминание для ", "Напоминаю о том, что нужно полить растение", 1)
//            }
//            2 -> {
//                showNotification(context, "Уведомление 2", "Текст уведомления 2", 2)
//            }
//            100000 -> {
//                showNotification(context, "Уведомление 2", "Текст уведомления 2", 100000)
//            }
//        }
    }

    private fun showNotification(context: Context, title: String, content: String, notificationId: Int) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bug)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_TAG, notificationId, notificationBuilder.build())
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val NOTIFICATION_TAG = "reminder_notification_tag"
        const val ITEM_TAG = "item_tag"
        const val NOT_TYPE = "not_type"
        const val NAME_PLANT = "name_plant"
        const val NOT_ID = "not_id"

    }

}