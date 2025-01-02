package com.example.userregisterfirebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.userregisterfirebase.databinding.UsersItemBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class UsersAdapter(var context: Context,
                   var userList: ArrayList<Users>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding = UsersItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserViewHolder(binding)

    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        holder.adapterBinding.textViewName.text = userList[position].userName
        holder.adapterBinding.textViewAge.text = userList[position].userAge.toString()
        holder.adapterBinding.textViewEmail.text = userList[position].userEmail

        val imageUrl = userList[position].url
        Picasso.get().load(imageUrl).into(holder.adapterBinding.imageView, object : Callback{
            override fun onSuccess() {
                holder.adapterBinding.progressBar.visibility = View.INVISIBLE
            }

            override fun onError(e: Exception?) {
                Toast.makeText(context,e?.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })

        holder.adapterBinding.linearLayout.setOnClickListener {
            val intent = Intent(context,UpdateUserActivity::class.java)
            intent.putExtra("userId",userList[position].userId)
            intent.putExtra("userName",userList[position].userName)
            intent.putExtra("userAge",userList[position].userAge)
            intent.putExtra("userEmail",userList[position].userEmail)
            intent.putExtra("imageUrl",imageUrl)
            intent.putExtra("imageName",userList[position].imageName)
            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
       return userList.size
    }


    inner class UserViewHolder(val adapterBinding: UsersItemBinding) : RecyclerView.ViewHolder(adapterBinding.root) {

    }
    fun getUSerId(position: Int) : String{
        return userList[position].userId

    }

    fun getImageName(position: Int) : String{
        return userList[position].imageName
    }
}