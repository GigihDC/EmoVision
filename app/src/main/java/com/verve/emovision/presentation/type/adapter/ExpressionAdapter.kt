package com.verve.emovision.presentation.type.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.verve.emovision.data.model.Expression
import com.verve.emovision.databinding.ItemExpressionGridBinding

class ExpressionAdapter(
    private val itemList: List<Expression>,
    private val onItemClick: (Expression) -> Unit,
) :
    RecyclerView.Adapter<ExpressionAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(val binding: ItemExpressionGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ItemViewHolder {
        val binding =
            ItemExpressionGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
    ) {
        val item = itemList[position]
        holder.binding.tvExpressionName.setText(item.name)
        holder.binding.ivExpressionImage.setImageResource(item.img)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = itemList.size
}
