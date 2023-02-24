package com.kocemre.myrecipes.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.kocemre.myrecipes.adapter.RecipeAdapter
import com.kocemre.myrecipes.databinding.FragmentMainBinding
import com.kocemre.myrecipes.model.Recipe
import com.kocemre.myrecipes.room.RecipeDao
import com.kocemre.myrecipes.room.RoomDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class MainFragment : Fragment() {

    private var _binding : FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var id: Int? = null
    private var name: String? = null

    private lateinit var db: RoomDB
    private lateinit var dao: RecipeDao

    private lateinit var recipeMutableList : MutableList<Recipe>

    private val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),RoomDB::class.java,"Recipe").build()
        dao = db.recipeDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater,container,false)
        val view = binding.root
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recipeMutableList = mutableListOf()

        compositeDisposable.add(
            dao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )





        binding.addButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToRecipeFragment()
            Navigation.findNavController(it).navigate(action)
        }


    }





    private fun handleResponse(recipeListFromAdapter: List<Recipe>){

        recipeMutableList.clear()
        for(recipe in recipeListFromAdapter){
            recipeMutableList.add(recipe)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RecipeAdapter(recipeMutableList)
        binding.recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}