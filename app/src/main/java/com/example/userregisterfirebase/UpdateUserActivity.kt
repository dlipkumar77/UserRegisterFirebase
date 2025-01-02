package com.example.userregisterfirebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.userregisterfirebase.databinding.ActivityUpdateUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class UpdateUserActivity : AppCompatActivity() {

    lateinit var UpdateUserBinding : ActivityUpdateUserBinding

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("MyUsers")

    lateinit var activityResultLauncher : ActivityResultLauncher<Intent> // step 2
    var imageUri : Uri? = null

    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UpdateUserBinding = ActivityUpdateUserBinding.inflate(layoutInflater)
        val view = UpdateUserBinding.root
        //enableEdgeToEdge()
        setContentView(view)

        supportActionBar?.title = "Update User"

        // register
        registerActivityForResult()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getAndSetData()

        UpdateUserBinding.buttonUpdateUser.setOnClickListener {
            uploadPhoto()
        }

        UpdateUserBinding.userUpdateProfileImage.setOnClickListener {
            chooseImage()
        }


    }

    fun getAndSetData(){
        val name = intent.getStringExtra("userName")
        val age = intent.getIntExtra("userAge",0).toString()
        val email = intent.getStringExtra("userEmail")
        val imageUrl = intent.getStringExtra("imageUrl").toString()

        UpdateUserBinding.editTextUpdateName.setText(name)
        UpdateUserBinding.editTextUpdateAge.setText(age)
        UpdateUserBinding.editTextUpdateEmail.setText(email)
        Picasso.get().load(imageUrl).into(UpdateUserBinding.userUpdateProfileImage)


    }

    fun chooseImage(){

        // no need permission for gallery


            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            //startActivityForResult is deprecated
            activityResultLauncher.launch(intent)


    }

    fun registerActivityForResult(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback{result ->
                val resultCode = result.resultCode
                val imageData = result.data
                if (resultCode == RESULT_OK && imageData != null){
                    imageUri = imageData.data
                    // picasso
                    imageUri?.let {
                        Picasso.get().load(it).into(UpdateUserBinding.userUpdateProfileImage)
                    }
                }

            })

    }


    fun updateData(imageUrl : String, imageName : String){
        val updatedName = UpdateUserBinding.editTextUpdateName.text.toString()
        val updatedAge = UpdateUserBinding.editTextUpdateAge.text.toString().toInt()
        val updatedEmail = UpdateUserBinding.editTextUpdateEmail.text.toString()
        val userId = intent.getStringExtra("userId").toString()

        val userMap = mutableMapOf<String,Any>()
        userMap["userId"] = userId
        userMap["userName"] = updatedName
        userMap["userAge"] = updatedAge
        userMap["userEmail"] = updatedEmail
        userMap["url"] = imageUrl
        userMap["imageName"] = imageName

        myReference.child(userId).updateChildren(userMap).addOnCompleteListener{
            task ->
            if(task.isSuccessful){
                Toast.makeText(applicationContext,"The user has been updated",Toast.LENGTH_SHORT).show()
                UpdateUserBinding.buttonUpdateUser.isClickable = true
                UpdateUserBinding.progressBarUpdateUser.visibility= View.INVISIBLE
                finish()
            }
        }


    }

    fun uploadPhoto(){
        UpdateUserBinding.buttonUpdateUser.isClickable = false
        UpdateUserBinding.progressBarUpdateUser.visibility= View.VISIBLE

        //UUID
        val imageName = intent.getStringExtra("imageName").toString()


        val imageReference = storageReference.child("images").child(imageName)
        imageUri?.let {uri ->
            imageReference.putFile(uri).addOnSuccessListener{
                Toast.makeText(applicationContext,"Image updated", Toast.LENGTH_SHORT).show()

                // download url
                val myUploadImageReference =  storageReference.child("images").child(imageName)
                myUploadImageReference.downloadUrl.addOnSuccessListener{ url ->
                    val imageUrl = url.toString()
                    updateData(imageUrl,imageName)

                }

            }.addOnFailureListener{
                Toast.makeText(applicationContext,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }
}