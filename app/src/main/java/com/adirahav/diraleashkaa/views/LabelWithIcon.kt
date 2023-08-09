package com.adirahav.diraleashkaa.views

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.R

class LabelWithIcon @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :

  LinearLayout(context, attrs) {

  lateinit var label: TextView
  lateinit var icon: ImageView
  lateinit var _tooltipText: String

  init {
    // attributes
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LabelWithIcon, 0, 0)
    val labelText = typedArray.getString(R.styleable.LabelWithIcon_labelText)
    val iconSrc = typedArray.getDrawable(R.styleable.LabelWithIcon_iconSrc)
    val iconExist = typedArray.getBoolean(R.styleable.LabelWithIcon_iconExist, false)
    val tooltipText = typedArray.getString(R.styleable.LabelWithIcon_iconTooltipText)
    typedArray.recycle()

    orientation = HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL

    // layout
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    inflater.inflate(R.layout.view_label_with_icon, this, true)

    // title
    label = findViewById<TextView>(R.id.label)
    label.text = labelText

    // icon
    icon = findViewById<ImageView>(R.id.icon)
    if (iconExist) {
      if (tooltipText != null) {
        _tooltipText = tooltipText
      }

      icon.visibility = VISIBLE
      icon.setImageDrawable(iconSrc)

      icon.setOnClickListener {
        openTooltipDialog(_tooltipText)
      }
    }
    else {
      icon.visibility = GONE
    }
  }

  fun setLabelText(text: String) {
    label.text = text
  }

  fun setTooltipText(text: String) {
    _tooltipText = text
  }

  // tooltip
  private fun openTooltipDialog(text: String?) {
    val dialog = Dialog(context)

    // layout
    dialog.setContentView(R.layout.dialog_tooltip)

    // close
    val dialogClose = dialog.findViewById<ImageView>(R.id.close)
    dialogClose?.setOnClickListener {
      closeTooltipDialog(dialog)
    }

    // text
    val dialogText = dialog.findViewById<TextView>(R.id.text)
    dialogText.text = HtmlCompat.fromHtml(text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

    // ok
    val dialogOk = dialog.findViewById<Button>(R.id.ok)
    dialogOk?.setOnClickListener {
      closeTooltipDialog(dialog)
    }

    dialog.show()
  }

  private fun closeTooltipDialog(dialog: Dialog) {
    dialog.hide()
  }
}