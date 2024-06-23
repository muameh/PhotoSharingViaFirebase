package com.mbl.photosharewithfirebase.adaptes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mbl.photosharewithfirebase.databinding.ReyclerRowBinding
import com.mbl.photosharewithfirebase.models.Post
import com.squareup.picasso.Picasso

class PostAdapter(val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    inner class PostHolder(val binding: ReyclerRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ReyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.textViewEmail.text = postList[position].email
        holder.binding.textViewComment.text = postList[position].commment
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.imageView)
    }
}