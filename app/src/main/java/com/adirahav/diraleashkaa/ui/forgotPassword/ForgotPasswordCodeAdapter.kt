package com.adirahav.diraleashkaa.ui.forgotPassword

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.adirahav.diraleashkaa.R
import com.adirahav.diraleashkaa.common.Utilities

class ForgotPasswordCodeAdapter(
    private val listener: ForgotPasswordGenerateCodeFragment
) : RecyclerView.Adapter<ForgotPasswordCodeAdapter.ViewHolder>() {

    var recyclerView: RecyclerView? = null

    private var codeList: CharArray? = null

    inner class ViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        // code
        @JvmField
        @BindView(R.id.code)
        var code: EditText? = null

        init {
            // bind views
            ButterKnife.bind(this, view!!)
        }
    }

    init {
        codeList = CharArray(itemCount)
    }

    fun setItems(codeList: CharArray) {
        this.codeList = codeList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forgot_password_code, parent, false)

        val height: Int = parent.measuredHeight
        val width: Int = parent.measuredWidth / itemCount
        view.layoutParams = RecyclerView.LayoutParams(width, height)

        recyclerView = parent as RecyclerView

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.code!!.requestFocus()
        }

        holder.code?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (holder.code?.text.toString().length == 0) {
                    if (holder.absoluteAdapterPosition > 0) {
                        val prevCodeHolder = recyclerView!!.getChildAt(holder.absoluteAdapterPosition.minus(1))
                        prevCodeHolder.requestFocus()
                    }
                }
            }
            return@setOnKeyListener false
        }

        holder.code?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 1) {
                    if (holder.absoluteAdapterPosition < itemCount.minus(1)) {
                        val nextCodeHolder = recyclerView!!.getChildAt(holder.absoluteAdapterPosition.plus(1))
                        nextCodeHolder.requestFocus()
                    }
                    else {
                        listener._activity?.submitNext(null)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int {
        return codeList?.size ?: 0
    }
}