package com.example.bloothcontroler.ui.widget

import android.content.Context
import com.example.bloothcontroler.R
import com.example.bloothcontroler.base.BaseRcvAdapter
import com.example.bloothcontroler.databinding.FileSelectionItemsBinding
import com.example.bloothcontroler.ui.widget.entity.Document


/**
 */
class FileSelectionAdapter(mContext:Context) : BaseRcvAdapter<Document, FileSelectionItemsBinding>(mContext,
    R.layout.file_selection_items) {
    lateinit var onClickFileSelection: OnClickFileSelection
    override fun handleItem(item: Document?, binding: FileSelectionItemsBinding?, position: Int) {
        binding?.ftvContent?.setText(item?.title)
        item?.run {
            item?.fileType?.getDrawable()?.let { binding?.ivIcon?.setImageResource(it) }
        }
        binding?.llItems?.setOnClickListener {
            if(::onClickFileSelection.isInitialized)
            {
                onClickFileSelection.onClickItem(item,position)
            }
        }
    }
}

interface OnClickFileSelection{
    fun onClickItem( item: Document?, position: Int)
}