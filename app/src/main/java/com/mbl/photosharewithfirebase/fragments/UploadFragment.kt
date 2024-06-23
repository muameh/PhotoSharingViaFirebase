package com.mbl.photosharewithfirebase.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.mbl.photosharewithfirebase.databinding.FragmentUploadBinding
import java.util.UUID


class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    var selectedImage : Uri? = null
    var selectedBitmap  : Bitmap? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

        registerLauncher()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonUpload.setOnClickListener{ ClickedUploadImage(it)}
        binding.imageView2.setOnClickListener { selectImage(it) }
    }

    private fun selectImage(it: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            //read media images
            if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin yok ise
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_MEDIA_IMAGES)){
                    //izin mantığını kullanıcıya göstermemiz lazım
                    Snackbar.make(it,"Permission is required to go to the gallery.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Accept",View.OnClickListener {
                            //izin istememiz lazım
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)

                        }).show()
                } else {
                    //izin istememiz lazım
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                //iziv var
                //galeriye git kodunu yaz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            //read external storage
            if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin yok ise
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //izin mantığını kullanıcıya göstermemiz lazım
                    Snackbar.make(it,"Permission is required to go to the gallery.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Accept",View.OnClickListener {
                            //izin istememiz lazım
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                        }).show()
                } else {
                    //izin istememiz lazım
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                //izin var
                //galeriye git kodunu yaz
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun ClickedUploadImage(view: View) {

        val uuidForImageName = UUID.randomUUID()
        val imageName = "${uuidForImageName}.jpg"


        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)
        if (selectedImage != null){
            imageReference.putFile(selectedImage!!).addOnSuccessListener {  uploadTask ->

                Toast.makeText(requireContext(),"successfully uploaded", Toast.LENGTH_SHORT).show()

                imageReference.downloadUrl.addOnSuccessListener {   uri ->
                    val downloadUrl = uri.toString()

                    val postMap = hashMapOf<String,Any>()
                    postMap.put("downloadUrl",downloadUrl)
                    postMap.put("email", auth.currentUser!!.email!!)
                    postMap.put("comment",binding.editTextComment.text.toString())
                    postMap.put("date",Timestamp.now())

                    db.collection("Posts").add(postMap).addOnSuccessListener {  DocumentReference ->
                        val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                        Navigation.findNavController(view).navigate(action)

                    }.addOnFailureListener {Exception ->
                        Toast.makeText(requireContext(),Exception.localizedMessage,Toast.LENGTH_LONG).show()
                    }


                }

            }.addOnFailureListener{ Exception ->
                Toast.makeText(requireContext(),Exception.localizedMessage,Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectedImage = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    selectedImage!!
                                )
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    selectedImage
                                )
                                binding.imageView2.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){    result ->
            if (result) {
                //izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //kullanıcı izni reddetti
                Toast.makeText(requireContext(),"we need your permission",Toast.LENGTH_LONG).show()
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





}