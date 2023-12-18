package com.ilnar.grandchat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.core.View
import com.ilnar.grandchat.activities.MainActivity
import com.ilnar.grandchat.activities.SignInActivity
import com.ilnar.grandchat.databinding.ActivityApiBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.TimeUnit

class ApiActivity : AppCompatActivity() {

    lateinit var binding: ActivityApiBinding
    lateinit private var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        loadUserDetails()
        navPanelClick()

        binding.btBack.setOnClickListener{
            binding.drawerApi.openDrawer(GravityCompat.START)
        }

        binding.button10.setOnClickListener {
            var textGpt = binding.editTextGpt.getText().toString().trim()
            if (!textGpt.isEmpty()){
                binding.progressBar2.visibility = VISIBLE
                binding.button10.visibility = GONE
                showToast(textGpt)
                main(textGpt)
            }else {
                showToast("Введите вопрос!")
            }
        }
    }

    fun main(text: String){
        val apiKey = "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" // Замените на ваш собственный ключ API
        val url = "https://api.openai.com/v1/chat/completions"

        val client = OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = """
        {
            "model": "gpt-3.5-turbo-16k-0613",
            "messages": [
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": "${text}"}
            ]
        }
    """.trimIndent().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.v("MyLog", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.v("MyLog", responseBody!!)
                val test = extractChatResponse(responseBody)
                runOnUiThread {
                    binding.textGpt.text = test
                    binding.textGpt.visibility = VISIBLE
                    binding.button10.visibility = VISIBLE
                    binding.progressBar2.visibility = GONE
                }

            }
        })
    }

    private fun extractChatResponse(responseBody: String?): String {
        if (responseBody.isNullOrEmpty()) {
            return "No response received"
        }

        try {
            val jsonResponse = JSONObject(responseBody)
            val choicesArray = jsonResponse.getJSONArray("choices")

            if (choicesArray.length() > 0) {
                val firstChoice = choicesArray.getJSONObject(0)
                val chatResponse = firstChoice.getJSONObject("message")
                val contentValue = chatResponse.getString("content")
                return contentValue
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "Failed to extract chat response"
    }

    fun loadUserDetails() {
        val navigationView = findViewById<NavigationView>(R.id.navView)
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
            navView.setNavigationItemSelectedListener {
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
                        val intent = Intent(applicationContext, FavoriteActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_api -> {
                        drawerApi.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_exit -> {
                        signOut()
                    }
                    R.id.nav_help -> {
                        drawerApi.closeDrawer(GravityCompat.START)
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