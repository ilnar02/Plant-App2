package com.ilnar.grandchat

import android.app.Dialog
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ilnar.grandchat.databinding.ActivityLogBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class LogActivity : AppCompatActivity(), LogAdapter.Listener {

    lateinit var binding: ActivityLogBinding
    private val adapter = LogAdapter(this)

    lateinit var item: FavoritePlants
    lateinit private var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(applicationContext)

        init()
    }

    private fun init() {
        item = intent.getSerializableExtra("log") as FavoritePlants
        dataCollection()
        binding.apply {
            rcViewLog.layoutManager = LinearLayoutManager(this@LogActivity)
            rcViewLog.adapter = adapter

        }
    }

    fun dataCollection(){
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val recordCollection = favoriteDocs.collection("records")


        adapter.clearLog()
        recordCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val image = document.getString("photo")
                    val record_type = document.getString("record_type")
                    val timestamp = document.getTimestamp("date")
                    val date = timestamp?.toDate()
                    val id = document.id
                    val text = document.getString("text")
                    val log = Logs(record_type, date, text, id, image)
                    adapter.addLog(log)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки журнала ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    fun Del(logs: Logs) {
        val myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val usersDocs = usersCollection.document(myDocument)
        val favoriteCollection = usersDocs.collection("favorite")
        val favoriteDocs = favoriteCollection.document(item.id)
        val recordCollection = favoriteDocs.collection("records")
        val logsDocs = recordCollection.document(logs.id)
        logsDocs.delete()
            .addOnSuccessListener {
                showToast("Документ успешно удален")
            }
            .addOnFailureListener { e ->
                showToast("Обработка ошибки удаления документа")
            }
        dataCollection()

    }

    override fun onClickConf(logs: Logs) {
        MaterialDialog(this).show {
            title(R.string.confirmation)
            message(R.string.question)
            positiveButton(text = "Подтвердить") { dialog ->
                Del(logs)
            }
            negativeButton(text = "Отмена") { dialog ->

            }
        }
    }

    override fun onClickImage(logs: Logs) {
        if (logs.record_type=="Фото") {
            val imageUrl = logs.image

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_image_viewer)

            val imageView = dialog.findViewById<ImageView>(R.id.imageSave)
            Picasso.get().load(imageUrl).into(imageView)

            dialog.show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    fun onClickBack(view: View) {
        finish()
    }

}