package com.example.userregisterfirebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.userregisterfirebase.databinding.ActivityAddUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class AddUserActivity : AppCompatActivity() {

    lateinit var addUserBinding : ActivityAddUserBinding

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("MyUsers")
    lateinit var activityResultLauncher : ActivityResultLauncher<Intent> // step 2
    var imageUri : Uri? = null

    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addUserBinding = ActivityAddUserBinding.inflate(layoutInflater)
        val view = addUserBinding.root
        //enableEdgeToEdge()
        setContentView(view)

        supportActionBar?.title = "Add User"

        // register
        registerActivityForResult()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addUserBinding.buttonAddUser.setOnClickListener {
            uploadPhoto()
        }

        addUserBinding.userProfileImage.setOnClickListener {
            chooseImage()
        }
    }

    fun chooseImage(){

        // step 1

       val permission = if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU){
           Manifest.permission.READ_MEDIA_IMAGES
        }else{
           Manifest.permission.READ_EXTERNAL_STORAGE
       }

        if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(permission),1001)
        }else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            //startActivityForResult is deprecated
            activityResultLauncher.launch(intent)

        }

    }

    // step 3
    fun registerActivityForResult(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback{result ->
                val resultCode = result.resultCode
                val imageData = result.data
                if (resultCode == RESULT_OK && imageData != null){
                    imageUri = imageData.data
                    // picasso
                    imageUri?.let {
                        Picasso.get().load(it).into(addUserBinding.userProfileImage)
                    }
                }

            })

    }

    // step 4
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    fun addUserToDatabase(url : String, imageName : String){
        val name = addUserBinding.editTextName.text.toString()
        val age = addUserBinding.editTextAge.text.toString().toInt()
        val email = addUserBinding.editTextEmail.text.toString()
        val id = myReference.push().key.toString()

        val user = Users(id,name,age,email,url,imageName)
        myReference.child(id).setValue(user).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext,"The new user has been added to the database",Toast.LENGTH_SHORT).show()
                    addUserBinding.buttonAddUser.isClickable = true
                    addUserBinding.progressBarAddUser.visibility= View.INVISIBLE
                    finish()
                }else{
                    Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()

                }
        }



    }

    fun uploadPhoto(){
        addUserBinding.buttonAddUser.isClickable = false
        addUserBinding.progressBarAddUser.visibility= View.VISIBLE

        //UUID
        val imageName = UUID.randomUUID().toString()
        val imageReference = storageReference.child("images").child(imageName)
        imageUri?.let {uri ->
            imageReference.putFile(uri).addOnSuccessListener{
                Toast.makeText(applicationContext,"Image uploaded", Toast.LENGTH_SHORT).show()

                // download url
                val myUploadImageReference =  storageReference.child("images").child(imageName)
                myUploadImageReference.downloadUrl.addOnSuccessListener{ url ->
                    val imageUrl = url.toString()
                    addUserToDatabase(imageUrl,imageName)

                }

            }.addOnFailureListener{
                Toast.makeText(applicationContext,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }
}