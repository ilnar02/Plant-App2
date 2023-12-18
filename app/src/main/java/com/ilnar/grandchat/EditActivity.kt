package com.ilnar.grandchat


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.databinding.ActivityEditBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Suppress("DEPRECATION")
class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding

    val db = FirebaseFirestore.getInstance()
    private val PICK_IMAGE_REQUEST = 123
    val storage = Firebase.storage
    val newsCollection = db.collection("news")

    lateinit var preferenceManager: PreferenceManager
    var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(applicationContext)
        setContentView(binding.root)
        initButtons()
    }

    private fun initButtons() = with(binding){

        layoutImage.setOnClickListener { v ->
            chooseImageFromGallery(this@EditActivity)
        }


        btBack.setOnClickListener{
            finish()
        }

        bDone.setOnClickListener {

            if (isValidSignUpDetails()) {
                uploadImageToFirebaseStorage(selectedImageUri!!)
            }
        }
    }

    private fun isValidSignUpDetails(): Boolean {
        return if (selectedImageUri == null) {
            showToast("Выберите изображение")
            false
        } else if (binding.edTitle.getText().toString().trim().isEmpty()) {
            showToast("Введите текст поста")
            false
        } else {
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }



    // Создайте функцию для выбора изображения из галереи
    fun chooseImageFromGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // В обработчике onActivityResult вызывается при выборе изображения из галереи
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data as Uri
            if (selectedImageUri != null) {
                binding.imageNews.setImageURI(selectedImageUri)
                binding.textAddImage.visibility = View.GONE
            } else {
                showToast("Выбран некорректный файл")
            }
        }
    }

    // Создайте функцию для загрузки изображения в Firebase Storage
    fun uploadImageToFirebaseStorage(imageUri: Uri) {
        binding.progressBar.setVisibility(View.VISIBLE)
        binding.bDone.setVisibility(View.GONE)
        val storageRef = storage.reference
        val imageRef = storageRef.child("newsImages/${imageUri.lastPathSegment}")
        // Загрузите файл в Firebase Storage
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Загрузка успешна
                // Получите URL-адрес загруженного изображения
                val imagePath = taskSnapshot.metadata?.path
                val imageBucket = taskSnapshot.metadata?.bucket
                val image = "https://firebasestorage.googleapis.com/v0/b/" + imageBucket + "/o/newsImages%2F" + imagePath?.replace("newsImages/", "") + "?alt=media&token"
                val text = binding.edTitle.text.toString()
                val author = preferenceManager.getString(Constants.KEY_NAME)
                var likes: Long = 0
                val timestamp = Timestamp.now()

                val news = hashMapOf(
                    "author" to author,
                    "text" to text,
                    "date" to timestamp,
                    "image" to image,
                    "likes" to likes,
                )

                newsCollection.add(news)
                    .addOnSuccessListener {
                        showToast("Документ успешно добавлен в подколлекцию: ${it.id}")
                    }
                    .addOnFailureListener { e ->
                            // Возникла ошибка при создании документа
                    }

                // Делайте что-то с URL-адресом (например, сохраните его в базе данных)
            }
            .addOnFailureListener { exception ->
                // Произошла ошибка загрузки
            }
        Handler().postDelayed({finish()}, 4000)
    }

    fun main() {
        val apiKey = "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" // Замените на ваш собственный ключ API
        val url = "https://api.openai.com/v1/chat/completions"

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = """
        {
            "model": "gpt-3.5-turbo",
            "messages": [
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": "Who won the world series in 2020?"}
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
                // Дополнительная обработка полученного ответа
            }
        })
    }

}