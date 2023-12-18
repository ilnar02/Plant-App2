package com.ilnar.grandchat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ilnar.grandchat.activities.MainActivity
import com.ilnar.grandchat.activities.SignInActivity
import com.ilnar.grandchat.databinding.ActivityPlantBinding
import com.ilnar.grandchat.utilities.Constants
import com.ilnar.grandchat.utilities.PreferenceManager
import java.util.*

@Suppress("DEPRECATION")
class PlantActivity : AppCompatActivity(), PlantAdapter.Listener {

    lateinit var binding: ActivityPlantBinding
    private val adapter = PlantAdapter(this)
    private var editLauncher: ActivityResultLauncher<Intent>? = null
    private val db = FirebaseFirestore.getInstance()
    private val plantCollection = db.collection("plants")


    lateinit private var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {

        preferenceManager = PreferenceManager(applicationContext)

        super.onCreate(savedInstanceState)
        binding = ActivityPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        searchViewListener()
        loadUserDetails()
        navPanelClick()
    }

    private fun init() {
        val layoutManager = GridLayoutManager(this@PlantActivity, 2)

        binding.menuOpen.setOnClickListener{
            binding.drawerDirectory.openDrawer(GravityCompat.START)
        }
        loadRecyclerView()
        binding.apply {
            rcView.layoutManager = layoutManager
            rcView.adapter = adapter
        }
    }

    override fun onClick(plant: Plant) {
        //Toast.makeText(this, "Нажали на: ${plant.ruName}", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ContentActivity::class.java).apply {
            putExtra("item", plant)
        })
    }

    private fun searchViewListener(){
        binding.svSearchPlant.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    searchFirestore(newText)
                }
                else loadRecyclerView()
                return false
            }
        })
    }

    private fun loadRecyclerView(){
        plantCollection.get()
            .addOnSuccessListener { querySnapshot ->
                dataCollection(querySnapshot)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки растений ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchFirestore(searchText: String) {
        plantCollection
            .orderBy("ruName")
            .startAt(searchText)
            .endAt(searchText + "\uf8ff")
            .get()
            .addOnSuccessListener { querySnapshot ->
                dataCollection(querySnapshot)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка загрузки растений ($exception)", Toast.LENGTH_SHORT).show()
            }
    }

    fun dataCollection(querySnapshot: QuerySnapshot) {
        adapter.clearPlant()
        for (document in querySnapshot.documents) {
            val ruName = document.getString("ruName")
            val desc = document.getString("description")
            val image = document.getString("image")
            val ltName = document.getString("ltName")
            val fact = document.getString("fact")
            val lifeExp = document.getString("lifeExp")
            val floweringTime = document.getString("floweringTime")
            val plantHeight = document.getString("plantHeight")
            val flowerColor = document.getString("flowerColor")
            val leafColor = document.getString("leafColor")
            val watering = document.getString("watering")
            val topDressing = document.getString("topDressing")
            val pruning = document.getString("pruning")
            val soil = document.getString("soil")
            val light = document.getString("light")
            val temperature = document.getString("temperature")
            val phylum = document.getString("phylum")
            val clas = document.getString("clas")
            val order = document.getString("order")
            val family = document.getString("family")
            val genus = document.getString("genus")
            val species = document.getString("species")
            val disease = document.getString("disease")

            val plant = Plant(image, ruName, desc, ltName, fact, lifeExp, floweringTime, plantHeight, flowerColor, leafColor, watering, topDressing, pruning, soil, light, temperature, phylum, clas, order, family, genus, species, disease)
            adapter.addPlant(plant)
            //Log.d("MyLog", "User name: $name")
        }
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
                        drawerDirectory.closeDrawer(GravityCompat.START)
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
                        val intent = Intent(applicationContext, ApiActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                    R.id.nav_exit -> {
                        signOut()
                    }
                    R.id.nav_help -> {
                        drawerDirectory.closeDrawer(GravityCompat.START)
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