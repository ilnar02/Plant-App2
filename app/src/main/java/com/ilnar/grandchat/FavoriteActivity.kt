package com.ilnar.grandchat

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.activities.MainActivity
import com.ilnar.grandchat.activities.SignInActivity
import com.ilnar.grandchat.databinding.ActivityFavoriteBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.util.HashMap
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input

class FavoriteActivity : AppCompatActivity(), FavoriteAdapter.Listener  {

    lateinit var binding: ActivityFavoriteBinding
    private val adapter = FavoriteAdapter(this)

    val db = FirebaseFirestore.getInstance()
    val collectionRef = db.collection("users")
    lateinit private var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        init()
        loadUserDetails()
        navPanelClick()
    }

    override fun onResume() {
        super.onResume()
        dataCollection()
    }

    private fun init() {
        binding.apply {
            rcViewFavorite.layoutManager = LinearLayoutManager(this@FavoriteActivity)
            rcViewFavorite.adapter = adapter

        }
    }

    override fun onClick(favorite: FavoritePlants) {
//        showToast(favorite.name.toString())
        startActivity(Intent(this, FavoriteContentActivity::class.java).apply {
            putExtra("favorite", favorite)
        })
    }

    override fun onClickConf(favorite: FavoritePlants) {
        MaterialDialog(this).show {
            title(R.string.confirmation)
            message(R.string.question)
            positiveButton(text = "Подтвердить") { dialog ->
                Del(favorite)
            }
            negativeButton(text = "Отмена") { dialog ->

            }
        }
    }

    fun Del(favorite: FavoritePlants) {
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(favorite.id)
        val recordCollection = favoriteDocs.collection("records")
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
        dataCollection()

    }


    fun dataCollection(){
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val documentRef = collectionRef.document(myDocument)
        val subcollectionRef = documentRef.collection("favorite")


        adapter.clearFavorite()
        subcollectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val image = document.getString("image")
                    val plantName = document.getString("plant_name")
                    val id = document.id
                    val input_name = document.getString("input_name")
                    val favorite = FavoritePlants(image, plantName, id, input_name)
                    adapter.addFavorite(favorite)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки растений ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    fun onClickAdd(view: View) {
        val intent = Intent(applicationContext, PlantActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    fun loadUserDetails() {
        val navigationView = findViewById<NavigationView>(R.id.navViewFavorite)
        val headerView = navigationView.getHeaderView(0)

        val nameTitle = headerView.findViewById<TextView>(R.id.atv_name_header)
        val name = preferenceManager.getString(Constants.KEY_NAME)
        nameTitle.text = name

        val imageView = headerView.findViewById<ImageView>(R.id.UserImage)
        val bytes: ByteArray = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        imageView.setImageBitmap(bitmap)
    }

    fun navPanelClick(){
        binding.apply {
            navViewFavorite.setNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.nav_directory -> {
                        val intent = Intent(applicationContext, PlantActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_chat -> {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_news -> {
                        val intent = Intent(applicationContext, NewsActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_none -> {
                        drawerFavorite.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_api -> {
                        val intent = Intent(applicationContext, ApiActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_exit -> {
                        signOut()
                    }
                    R.id.nav_help -> {
                        drawerFavorite.closeDrawer(GravityCompat.START)
                        onClickFeedback()
                    }
                }
                true
            }
        }

    }

    fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener { unused: Void? ->
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { e: Exception? -> showToast("Unable to sign out") }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("CheckResult")
    fun onClickFeedback() {
        MaterialDialog(this).show {
            message(text = "Введите текст вашего обращения/пожелания")
            input { dialog, text ->
                edit(text.toString())
            }
            positiveButton(text = "Подтвердить")
        }
    }

    fun edit(text: String){
        val myDocument = preferenceManager.getString(Constants.KEY_EMAIL)
        val db = FirebaseFirestore.getInstance()
        val feedback = db.collection("feedback")
        val feed = hashMapOf(
            "author_email" to myDocument,
            "text" to text,
        )
        feedback.add(feed)
            .addOnSuccessListener {
                showToast("Id вашего обращения ${it.id}")
            }
            .addOnFailureListener { e ->
                // Возникла ошибка при создании документа
            }
    }
}