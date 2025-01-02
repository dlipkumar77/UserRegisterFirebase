package com.example.userregisterfirebase

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.userregisterfirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding : ActivityMainBinding

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val myReference : DatabaseReference = database.reference.child("MyUsers")

    val userList = ArrayList<Users>()
    val imageNameList = ArrayList<String>()
    lateinit var userAdapter : UsersAdapter

    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        //enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainBinding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)

        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val id = userAdapter.getUSerId(viewHolder.adapterPosition)
                myReference.child(id).removeValue()

                // delete

                val imageName = userAdapter.getImageName(viewHolder.adapterPosition)
                val imageReference = storageReference.child("images").child(imageName)
                imageReference.delete()

                Toast.makeText(applicationContext,"The user was deleted",Toast.LENGTH_SHORT).show()
            }

        }).attachToRecyclerView(mainBinding.recyclerView)

        retrieveDataFromDatabase()

    }

    fun retrieveDataFromDatabase(){

        //ChildEventListener

        myReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (eachUser in snapshot.children){
                    val user = eachUser.getValue(Users::class.java)
                    if(user != null){
                        println("UserID: ${user.userId}")
                        println("Username: ${user.userName}")
                        println("UserAge: ${user.userAge}")
                        println("UserEmail: ${user.userEmail}")
                        println("-----------------------------------")
                        userList.add(user)
                    }

                    userAdapter = UsersAdapter(this@MainActivity,userList)
                    mainBinding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    mainBinding.recyclerView.adapter = userAdapter

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_all,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.deleteAll){
            showDialogMessage()
        }else if (item.itemId == R.id.signOut){
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()

        }
        return super.onOptionsItemSelected(item)
    }

    fun showDialogMessage(){

        val dialogAlert = AlertDialog.Builder(this)
        dialogAlert.setTitle("Delete All Users")
        dialogAlert.setMessage("If click yes, all users will be deleted, " +
                "if you want to delete specific user, you can swipe the item you want to delete right or left")

        dialogAlert.setNegativeButton("Cancel", DialogInterface.OnClickListener{
            dialogInterface, i -> dialogInterface.cancel()
        })

        dialogAlert.setPositiveButton("Yes", DialogInterface.OnClickListener{ dialogInterface, i ->

            myReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (eachUser in snapshot.children){
                        val user = eachUser.getValue(Users::class.java)
                        if(user != null){
                           /*
                            println("UserID: ${user.userId}")
                            println("Username: ${user.userName}")
                            println("UserAge: ${user.userAge}")
                            println("UserEmail: ${user.userEmail}")
                            println("-----------------------------------")
                            */
                            imageNameList.add(user.imageName)
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            /*
            // these two lines not delete whole images. so above line addValueEventListener
            val imageReference = storageReference.child("images")
            imageReference.delete()

            now these two line goes to task successful block on for loop

            */

            // remove all
            myReference.removeValue().addOnCompleteListener{
                task ->
                if(task.isSuccessful){

                    for (imageName in imageNameList){
                        val imageReference = storageReference.child("images").child(imageName)
                        imageReference.delete()
                    }


                    userAdapter.notifyDataSetChanged()
                    Toast.makeText(applicationContext,"All users were deleted",Toast.LENGTH_SHORT).show()
                }
            }
        })

        dialogAlert.create().show()

        }

}