package com.ilnar.grandchat

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Html
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ilnar.grandchat.databinding.ActivityContentBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.util.*

@Suppress("DEPRECATION")
class ContentActivity : AppCompatActivity() {
    lateinit var binding: ActivityContentBinding

    lateinit private var preferenceManager: PreferenceManager
    lateinit var item: Plant
    val calendar = Calendar.getInstance()
    lateinit var timestamp: Timestamp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getSerializableExtra("item") as Plant

        val transformation = RoundedCornersTransformation(40, 0, RoundedCornersTransformation.CornerType.ALL)
        val imageUrl = item.image
        Picasso.get()
            .load(imageUrl)
            .transform(transformation)
            .into(binding.imMain)


        binding.apply {
            tvTitle.text = item.ruName
            tvLtName.text = item.ltName
            tvDescription.text = Html.fromHtml(item.description)
            tvFact.text = Html.fromHtml(item.fact)
            tvLifeExp.text = item.lifeExp
            tvFloweringTime.text = item.floweringTime
            tvPlantHeight.text = item.plantHeight
            tvFlowerColor.text = item.flowerColor
            tvLeafColor.text = item.leafColor
            tvWatering.text = Html.fromHtml(item.watering)
            tvTopDressing.text = Html.fromHtml(item.topDressing)
            tvPruning.text = Html.fromHtml(item.pruning)
            tvSoil.text = Html.fromHtml(item.soil)
            tvLight.text = Html.fromHtml(item.light)
            tvTemperature.text = Html.fromHtml(item.temperature)
            tvPhylum.text = item.phylum
            tvClas.text = item.clas
            tvOrder.text = item.order
            tvFamily.text = item.family
            tvGenus.text = item.genus
            tvSpecies.text = item.species
            tvDisease.text = Html.fromHtml(item.disease)
        }

        preferenceManager = PreferenceManager(applicationContext)


    }

    fun onClick(view: View) {
        var myDocument = preferenceManager.getString(Constants.KEY_USER_ID)
//        Toast.makeText(getApplicationContext(),test, Toast.LENGTH_SHORT).show()\
        // Получение доступа к экземпляру Firestore
        val db = FirebaseFirestore.getInstance()

// Создание коллекции и документа
        val collectionRef = db.collection("users")
        val documentRef = collectionRef.document(myDocument)

// Создание подколлекции и добавление документа
        val subcollectionRef = documentRef.collection("favorite")

// Создание данных для документа в подколлекции
        val data = hashMapOf(
            "input_name" to item.ruName,
            "plant_name" to item.ruName,
            "image" to item.image,
            // Другие поля и значения
        )

        val min = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minStr = min.toString()
        var hourStr = hour.toString()
        if (min<10) {minStr = "0" + min.toString()}
        if (hour<10) {hourStr = "0" + hour.toString()}
        val timeText = hourStr + ":" + minStr

        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        val selData = calendar.time
        timestamp = Timestamp(selData)

        val textRecord = "Растение добавлено в " + timeText

        val dataRec = hashMapOf(
            "record_type" to "Добавлено",
            "date" to timestamp,
            "text" to textRecord,
        )

// Добавление документа в подколлекцию
        subcollectionRef.add(data)
            .addOnSuccessListener { subDocumentRef ->
                // Успешно добавлено
                showToast("Документ успешно добавлен в подколлекцию: ${subDocumentRef.id}")
                val favoriteDocs = subcollectionRef.document(subDocumentRef.id)
                val recordCollection = favoriteDocs.collection("records")

                recordCollection
                    .document(1.toString())
                    .set(dataRec)
                    .addOnSuccessListener {
                        showToast("Документ успешно добавлен в подколлекцию")
                    }
                    .addOnFailureListener { e ->
                        // Возникла ошибка при создании документа
                    }
            }
            .addOnFailureListener { e ->
                // Ошибка при добавлении
                showToast("Ошибка при добавлении документа в подколлекцию: $e")
            }



    }


    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}