package com.verve.emovision.presentation.games.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.verve.emovision.data.model.Game
import com.verve.emovision.databinding.ItemGameGridBinding

class GameAdapter(
    private val itemList: List<Game>,
    private val onItemClick: (Game) -> Unit,
) :
    RecyclerView.Adapter<GameAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ItemGameGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val binding =
            ItemGameGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
    ) {
        val item = itemList[position]
        holder.binding.tvGameName.setText(item.name)
        holder.binding.ivGameImage.setImageResource(item.img)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = itemList.size
}
