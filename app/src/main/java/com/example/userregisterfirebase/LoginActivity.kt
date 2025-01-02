package com.example.userregisterfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.userregisterfirebase.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var loginBinding : ActivityLoginBinding
    val auth : FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        val view = loginBinding.root
        //enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginBinding.buttonSignin.setOnClickListener{

            val userEmail = loginBinding.editTextEmailSignin.text.toString()
            val userPassword = loginBinding.editTextPasswordSignin.text.toString()
            loginWithFirebase(userEmail,userPassword)

        }

       loginBinding.buttonSignUp.setOnClickListener{
           val intent = Intent(this@LoginActivity,SignupActivity::class.java)
           startActivity(intent)
       }

        loginBinding.buttonForgot.setOnClickListener{
            val intent = Intent(this@LoginActivity,ForgetActivity::class.java)
            startActivity(intent)
        }

        loginBinding.buttonSiginWithPhoneNumber.setOnClickListener{
            val intent = Intent(this@LoginActivity,PhoneActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun loginWithFirebase(userEmail : String, userPassword : String){

        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(applicationContext,"Login is successful",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()

                }
            }

    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null){
            val intent = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}