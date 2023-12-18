package com.ilnar.grandchat

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.ilnar.grandchat.databinding.ActivityNewRecordBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

@Suppress("DEPRECATION")
class NewRecordActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    lateinit var binding: ActivityNewRecordBinding
    lateinit var item: FavoritePlants
    lateinit private var preferenceManager: PreferenceManager
    val calendar = Calendar.getInstance()
    lateinit var text: String
    lateinit var timestamp: Timestamp

    var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 123
    val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edDate.isEnabled = false
        init()
    }

    private fun init() {
        item = intent.getSerializableExtra("item") as FavoritePlants
        val type = intent.getSerializableExtra("type") as Int
        preferenceManager = PreferenceManager(applicationContext)

        binding.pickDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.layoutImage.setOnClickListener { v ->
            chooseImageFromGallery(this@NewRecordActivity)
        }

        binding.bDone.setOnClickListener {
            when (type) {
                1 -> record("Полив")
                2 -> record("Удобрение")
                3 -> record("Опрыскивание")
                4 -> recordPhoto(selectedImageUri!!)
                else -> println("x не равно 1, 2 или 3")
            }
        }

        binding.btBack.setOnClickListener {
            finish()
        }

        when (type) {
            1 -> {
                text = "Полив"
                binding.imageView.setImageResource(R.drawable.ic_watering)
            }
            2 -> {
                text = "Удобрение"
                binding.imageView.setImageResource(R.drawable.ic_top_dressing)
            }
            3 -> {
                text = "Опрыскивание"
                binding.imageView.setImageResource(R.drawable.ic_characteristics)
            }
            4 -> {
                text = "Фото"
                binding.imageView.setImageResource(R.drawable.ic_photo)
                binding.photoCard.visibility = View.VISIBLE
            }
            else -> println("x не равно 1, 2 или 3")
        }

        val currentDate =
            calendar.get(Calendar.DAY_OF_MONTH).toString() + "/" + calendar.get(Calendar.MONTH)
                .toString() + "/" + calendar.get(Calendar.YEAR).toString()

        val min = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minStr = min.toString()
        var hourStr = hour.toString()
        if (min<10) {minStr = "0" + min.toString()}
        if (hour<10) {hourStr = "0" + hour.toString()}
        val timeText = hourStr + ":" + minStr
        val textRecord = text + " в " + timeText

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        val selData = calendar.time
        timestamp = Timestamp(selData)

        binding.apply {
            plantName.text = item.input_name
            typeRecord.text = text
            edDate.setText(currentDate)
            edText.setText(textRecord)
        }
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Обработка выбранной даты
        val selectedDate = "$dayOfMonth/${month + 1}/$year"

        binding.edDate.setText(selectedDate)

        calendar.set(year, month, dayOfMonth)
        val selData = calendar.time
        timestamp = Timestamp(selData)
    }

    private fun record(typeRec: String) {
        if (isValidSignUpDetails()) {
            val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
            // Получение доступа к экземпляру Firestore
            val db = FirebaseFirestore.getInstance()
            // Создание коллекции и документа
            val usersCollection = db.collection("users")
            val usersDocs = usersCollection.document(myDocument)
            // Создание подколлекции и добавление документа
            val favoriteCollection = usersDocs.collection("favorite")
            val favoriteDocs = favoriteCollection.document(item.id)
            val recordCollection = favoriteDocs.collection("records")

            // Создание данных для документа в подколлекции
            val data = hashMapOf(
                "record_type" to typeRec,
                "date" to timestamp,
                "text" to binding.edText.text.toString(),
            )

            recordCollection.add(data)
                .addOnSuccessListener {
                    showToast("Документ успешно добавлен в подколлекцию: ${it.id}")
                }
                .addOnFailureListener { e ->
                    // Возникла ошибка при создании документа
                }

            // Добавление документа в подколлекцию
//            recordCollection.add(data)
//                .addOnSuccessListener { subDocumentRef ->
//                    // Успешно добавлено
//                    showToast("Документ успешно добавлен в подколлекцию: ${subDocumentRef.id}")
//                }
//                .addOnFailureListener { e ->
//                    // Ошибка при добавлении
//                    showToast("Ошибка при добавлении документа в подколлекцию: $e")
//                }
            finish()
        }
    }

    fun getDocumentCount(collection: CollectionReference, onComplete: (Int) -> Unit) {
        var collecionSize: Int = 0
        collection
            .get()
            .addOnSuccessListener { querySnapshot ->
                collecionSize = querySnapshot.size()
                onComplete(collecionSize)
            }
            .addOnFailureListener { e ->
                // Возникла ошибка при получении документов коллекции
            }
    }

    private fun recordPhoto(imageUri: Uri) {
        var m = true
        if (selectedImageUri == null) {
            showToast("Выберите изображение")
            m = false
        }
        if (isValidSignUpDetails() && m) {
            val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
            // Получение доступа к экземпляру Firestore
            val db = FirebaseFirestore.getInstance()
            // Создание коллекции и документа
            val usersCollection = db.collection("users")
            val usersDocs = usersCollection.document(myDocument)
            // Создание подколлекции и добавление документа
            val favoriteCollection = usersDocs.collection("favorite")
            val favoriteDocs = favoriteCollection.document(item.id)
            val recordCollection = favoriteDocs.collection("records")

            val storageRef = storage.reference
            val imageRef = storageRef.child("recordImages/${imageUri.lastPathSegment}")
            // Загрузите файл в Firebase Storage
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Загрузка успешна
                    // Получите URL-адрес загруженного изображения
                    val imagePath = taskSnapshot.metadata?.path
                    val imageBucket = taskSnapshot.metadata?.bucket
                    val image = "https://firebasestorage.googleapis.com/v0/b/" + imageBucket + "/o/recordImages%2F" + imagePath?.replace("recordImages/", "") + "?alt=media&token"

                    val data = hashMapOf(
                        "record_type" to "Фото",
                        "date" to timestamp,
                        "text" to binding.edText.text.toString(),
                        "photo" to image,
                    )

                    recordCollection.add(data)
                            .addOnSuccessListener {
                                showToast("Документ успешно добавлен в подколлекцию: ${it.id}")
                            }
                            .addOnFailureListener { e ->
                                // Возникла ошибка при создании документа
                            }
                    }

                    // Делайте что-то с URL-адресом (например, сохраните его в базе данных)
                }
            // Создание данных для документа в подколлекции




            // Добавление документа в подколлекцию
//            recordCollection.add(data)
//                .addOnSuccessListener { subDocumentRef ->
//                    // Успешно добавлено
//                    showToast("Документ успешно добавлен в подколлекцию: ${subDocumentRef.id}")
//                }
//                .addOnFailureListener { e ->
//                    // Ошибка при добавлении
//                    showToast("Ошибка при добавлении документа в подколлекцию: $e")
//                }
            finish()
    }


    private fun isValidSignUpDetails(): Boolean {
        return if (binding.edDate.getText().toString().trim().isEmpty()) {
            showToast("Введите текст поста")
            false
        } else if (binding.edText.getText().toString().trim().isEmpty()) {
            showToast("Введите текст поста")
            false
        } else {
            true
        }
    }

    fun chooseImageFromGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data as Uri
            if (selectedImageUri != null) {
                binding.imageRecord.setImageURI(selectedImageUri)
                binding.textAddImage.visibility = View.GONE
            } else {
                showToast("Выбран некорректный файл")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}