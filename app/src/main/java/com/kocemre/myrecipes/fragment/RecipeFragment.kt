package com.kocemre.myrecipes.fragment

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.kocemre.myrecipes.databinding.FragmentRecipeBinding
import com.kocemre.myrecipes.model.Recipe
import com.kocemre.myrecipes.room.RecipeDao
import com.kocemre.myrecipes.room.RoomDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class RecipeFragment : Fragment() {
    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var selectedUri: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private var id: Int? =null

    private lateinit var db: RoomDB
    private lateinit var dao: RecipeDao

    private var recipeFromMain: Recipe? = null

    private val compositeDisposable = CompositeDisposable()
    private val compositeDisposable2 = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerLauncher()

        db = Room.databaseBuilder(requireContext(),RoomDB::class.java,"Recipe").build()
        dao = db.recipeDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.let {
            if(it!=null){

                id = RecipeFragmentArgs.fromBundle(it).id
                val info = RecipeFragmentArgs.fromBundle(it).info
                if(info.equals("old")){
                    binding.saveButton.visibility = View.GONE
                    compositeDisposable2.add(
                        dao.getFromId(id!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleResponse2)
                    )
                    if(recipeFromMain!=null){
                        val byteArray = recipeFromMain!!.byteArray
                        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                        binding.imageView.setImageBitmap(bitmap)
                    }
                }
            }
        }

        binding.imageView.setOnClickListener {
            selectImage(it)
        }

        binding.saveButton.setOnClickListener {
            save(it)
        }
    }

    private fun handleResponse2(recipe: Recipe){
        binding.recipeNameText.setText(recipe.name)
        binding.recipeText.setText(recipe.recipe)
        val byteArray = recipe.byteArray
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        binding.imageView.setImageBitmap(bitmap)
        recipeFromMain = recipe
    }

    private fun handleResponse(){
        val action = RecipeFragmentDirections.actionRecipeFragmentToMainFragment()
        Navigation.findNavController(binding.root).navigate(action)
    }

    private fun selectImage(view: View) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(
                    view,
                    "Fotoğraf eklemek için medyaya erişim gereklidir!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("İZİN İÇİN DOKUNUN") {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            //permission granted

            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun save(view: View){

        val name = binding.recipeNameText.text.toString()
        val recipe = binding.recipeText.text.toString()

        if(selectedBitmap!= null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,600)

            val stream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = stream.toByteArray()

            val recipe = Recipe(name,recipe,image)
            compositeDisposable.add(
                dao.insert(recipe)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = it.data
                    if (intentFromResult != null) {
                        selectedUri = intentFromResult.data!!
                        binding.imageView.setImageURI(selectedUri)

                        try {
                            selectedBitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(
                                    requireContext().contentResolver,
                                    selectedUri
                                )
                            } else {
                                val source = ImageDecoder.createSource(
                                    requireContext().contentResolver,
                                    selectedUri!!
                                )
                                ImageDecoder.decodeBitmap(source)

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    }
                }

            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    val intentToGallery = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Medyaya erişmek için lütfen izin verin!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun makeSmallerBitmap(bitmap: Bitmap, size: Int): Bitmap {
        var height = bitmap.height.toDouble()
        var width = bitmap.width.toDouble()

        if (height >= width) {

            val ratio = size.toDouble() / height
            height = size.toDouble()
            width *= ratio


        } else if (width > height) {

            val ratio = size.toDouble() / width
            width = size.toDouble()
            height *= ratio

        }

        return Bitmap.createScaledBitmap(bitmap,width.toInt(),height.toInt(),true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        compositeDisposable2.clear()
        _binding = null
    }

}