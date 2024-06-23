package com.mbl.photosharewithfirebase.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mbl.photosharewithfirebase.R
import com.mbl.photosharewithfirebase.adaptes.PostAdapter
import com.mbl.photosharewithfirebase.databinding.FragmentFeedBinding
import com.mbl.photosharewithfirebase.databinding.FragmentLoginBinding
import com.mbl.photosharewithfirebase.models.Post


class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    val postList : ArrayList<Post> = arrayListOf()

    private var adapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener { popUpMenuInflater(it) }

        getPhotosFromFireStore()


        adapter = PostAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }
    
    private fun getPhotosFromFireStore(){
        val currentUsersEmail = Firebase.auth.currentUser?.email.toString()

        db.collection("Posts")
            //.whereEqualTo("email",currentUsersEmail)
            .orderBy("date",Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
            if (error!=null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_SHORT).show()
            } else {
                if(value!=null){
                    if(!value.isEmpty){
                        postList.clear()
                        val documents = value.documents

                        for (document in documents) {
                            val comment = document.get("comment") as String
                            val email = document.get("email") as String
                            val downloadUrl = document.get("downloadUrl") as String
                            val post = Post(email,comment,downloadUrl)

                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

    }

    fun popUpMenuInflater(view: View) {
        val myPopupMenu = PopupMenu(requireContext(), binding.floatingActionButton)
        val myPopupMenuInflater = myPopupMenu.menuInflater
        myPopupMenuInflater.inflate(R.menu.my_popup_menu, myPopupMenu.menu)
        myPopupMenu.show()
        myPopupMenu.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.itemUpload) {
            val action = FeedFragmentDirections.actionFeedFragmentToUploadFragment()
            Navigation.findNavController(requireView()).navigate(action)
        } else if (item?.itemId == R.id.itemExit) {
            auth.signOut() //signout
            val action = FeedFragmentDirections.actionFeedFragmentToLoginFragment()
            Navigation.findNavController(requireView()).navigate(action)
            Toast.makeText(requireContext(), "You have successfully logged out.", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}