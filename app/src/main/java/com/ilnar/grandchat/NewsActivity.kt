package com.ilnar.grandchat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ilnar.grandchat.activities.MainActivity
import com.ilnar.grandchat.activities.SignInActivity
import com.ilnar.grandchat.databinding.ActivityNewsBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.util.HashMap

class NewsActivity : AppCompatActivity(), NewsAdapter.Listener {

    lateinit var binding: ActivityNewsBinding

    private val adapter = NewsAdapter(this)

    val db = FirebaseFirestore.getInstance()
    val newsCollection = db.collection("news")

    lateinit private var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        init()
        loadUserDetails()
        navPanelClick()
    }

    override fun onResume(){
        super.onResume()
        dataCollection()
    }
    private fun init() {

        binding.apply {
            rcViewNews.layoutManager = LinearLayoutManager(this@NewsActivity)
            rcViewNews.adapter = adapter
            newNews.setOnClickListener {
                val intent = Intent(applicationContext, EditActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivity(intent)
            }

        }
    }

    override fun onClick(news: News) {
        val currentLikes = news.likes
        updateLikes(news.dockId, currentLikes?.plus(1))
    }

    fun updateLikes(documentId: String, likes: Long?) {
        val newsRef = newsCollection.document(documentId)
        newsRef.update("likes", likes)
            .addOnSuccessListener {
                Toast.makeText(this, "Получилось", Toast.LENGTH_SHORT).show()
                adapter.likesUpdate(documentId, likes)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Не получилось", Toast.LENGTH_SHORT).show()
            }
    }


    fun dataCollection(){
        adapter.clearNews()
        newsCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val image = document.getString("image")
                    val text = document.getString("text")
                    val author = document.getString("author")
                    val docId = document.id

                    val timestamp = document.getTimestamp("date")
                    val date = timestamp?.toDate()

                    val likes = document.getLong("likes")

                    val new = News(text, author, date, image, likes, docId)
                    adapter.addNews(new)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки растений ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadUserDetails() {
        val navigationView = findViewById<NavigationView>(R.id.navViewNews)
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
            navViewNews.setNavigationItemSelectedListener {
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
                        drawerNews.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_none -> {
                        val intent = Intent(applicationContext, FavoriteActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
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
                        drawerNews.closeDrawer(GravityCompat.START)
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