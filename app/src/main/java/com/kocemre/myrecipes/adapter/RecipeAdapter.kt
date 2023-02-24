package com.kocemre.myrecipes.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.kocemre.myrecipes.databinding.RecyclerRowBinding
import com.kocemre.myrecipes.fragment.MainFragmentDirections
import com.kocemre.myrecipes.model.Recipe
import com.kocemre.myrecipes.room.RecipeDao
import com.kocemre.myrecipes.room.RoomDB
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class RecipeAdapter (val recipeList :MutableList<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {
    private lateinit var db: RoomDB
    private lateinit var dao: RecipeDao
    private lateinit var context: Context
    private val compositeDisposable = CompositeDisposable()
    class RecipeHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        db = Room.databaseBuilder(parent.context,RoomDB::class.java,"Recipe").build()
        dao = db.recipeDao()
        context = parent.context

        return RecipeHolder(binding)
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {
        holder.binding.recyclerViewText.text = recipeList.get(position).name
        holder.binding.recyclerViewText.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToRecipeFragment(recipeList.get(position).id,"old")
            Navigation.findNavController(it).navigate(action)
        }

        holder.binding.recyclerViewText.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                val listener = DialogInterface.OnClickListener { dialogInterface, i ->
                    if(i==DialogInterface.BUTTON_POSITIVE){

                        compositeDisposable.add(
                            dao.delete(recipeList.get(position))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                        )

                        recipeList.remove(recipeList.get(position))
                        notifyDataSetChanged()

                    }
                }
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setMessage("Tarifi silmek istediğinize emin misiniz?").setPositiveButton("Evet",listener).setNegativeButton("Hayır",listener).show()
                compositeDisposable.clear()
                return true
            }

        })





    }

    override fun getItemCount(): Int {
        return recipeList.size
    }
}