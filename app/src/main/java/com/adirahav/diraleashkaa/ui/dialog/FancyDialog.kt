package com.adirahav.diraleashkaa.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.adirahav.diraleashkaa.R
import pl.droidsonroids.gif.GifImageView


interface FancyDialogListener {
    fun onClick()
}

class FancyDialog private constructor(builder: Builder) {

    private val title: String?
    private val message: String?
    private val positiveButtonText: String?
    private val negativeButtonText: String?

    private val context: Context
    private val positiveListener: FancyDialogListener?
    private val negativeListener: FancyDialogListener?
    private val cancelListener: DialogInterface.OnCancelListener?
    private val cancel: Boolean
    var imageResource: Int
    var backgroundColor: Int

    init {
        title = builder.title
        message = builder.message
        context = builder.context
        positiveListener = builder.positiveListener
        negativeListener = builder.negativeListener
        positiveButtonText = builder.positiveButtonText
        negativeButtonText = builder.negativeButtonText
        imageResource = builder.imageResource
        backgroundColor = builder.backgroundColor
        cancel = builder.cancel
        cancelListener = builder.cancelListener
    }

    class Builder(internal val context: Context) {
        internal var title: String? = null
        internal var message: String? = null
        internal var positiveButtonText: String? = null
        internal var negativeButtonText: String? = null

        internal var positiveListener: FancyDialogListener? = null
        internal var negativeListener: FancyDialogListener? = null
        internal var cancel = false
        var imageResource = 0
        var backgroundColor = 0
        internal var cancelListener: DialogInterface.OnCancelListener? = null

        // title
        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes title: Int): Builder {
            return setTitle(
                if (title == 0)
                    ""
                else
                    context.getString(title)
            )

        }

        // message
        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            return setMessage(
                if (message == 0)
                    ""
                else
                    context.getString(message)
            )
        }

        // positive button
        internal fun setPositiveBtnText(positiveBtnText: String?): Builder {
            this.positiveButtonText = positiveBtnText
            return this
        }

        fun setPositiveBtnText(@StringRes positiveBtnText: Int): Builder {
            return setPositiveBtnText(
                if (positiveBtnText == 0)
                    ""
                else
                    context.getString(positiveBtnText)
            )
        }

        fun onPositiveClicked(positiveListener: FancyDialogListener?): Builder {
            this.positiveListener = positiveListener
            return this
        }

        // negative button
        internal fun setNegativeBtnText(negativeBtnText: String?): Builder {
            this.negativeButtonText = negativeBtnText
            return this
        }

        fun setNegativeBtnText(@StringRes negativeBtnText: Int): Builder {
            return setNegativeBtnText(
                if (negativeBtnText == 0)
                    ""
                else
                    context.getString(negativeBtnText)
            )
        }

        fun onNegativeClicked(negativeListener: FancyDialogListener?): Builder {
            this.negativeListener = negativeListener
            return this
        }

        // cancellable
        fun isCancellable(cancel: Boolean): Builder {
            this.cancel = cancel
            return this
        }

        fun setOnCancelListener(cancelListener: DialogInterface.OnCancelListener?): Builder {
            this.cancelListener = cancelListener
            return this
        }

        // gif
        fun setGifResource(imageResource: Int): Builder {
            this.imageResource = imageResource
            return this
        }

        // background color
        fun setBackgroundColor(backgroundColor: Int): Builder {
            this.backgroundColor = backgroundColor
            return this
        }

        @SuppressLint("ResourceType")
        fun build(): FancyDialog {

            // dialog
            val dialog = Dialog(context, R.style.CustomDialogTheme)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(cancel)
            dialog.setContentView(R.layout.dialog_fancy)

            // image
            val imageView: GifImageView = dialog.findViewById(R.id.imageView)
            imageView.setImageResource(imageResource)

            // container
            val container: ConstraintLayout = dialog.findViewById(R.id.container)
            container.background = ContextCompat.getDrawable(context, backgroundColor)

            // title
            val titleView: TextView = dialog.findViewById(R.id.title)
            titleView.text = title

            // message
            val messageView: TextView = dialog.findViewById(R.id.message)
            messageView.text = Html.fromHtml(message)

            // positive button
            val positiveButton: Button = dialog.findViewById(R.id.positiveButton)
            if (positiveButtonText != null) {
                positiveButton.text = HtmlCompat.fromHtml(positiveButtonText!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            if (positiveListener != null) {
                if (negativeListener == null) {
                    positiveButton.layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 65F)
                }

                positiveButton.setOnClickListener {
                    positiveListener!!.onClick()
                    dialog.dismiss()
                }
            }
            else {
                positiveButton.visibility = View.GONE
            }

            // negative button
            val negativeButton: Button = dialog.findViewById(R.id.negativeButton)
            if (negativeButtonText != null) {
                negativeButton.text = HtmlCompat.fromHtml(negativeButtonText!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            if (negativeListener != null) {
                if (positiveListener == null) {
                    negativeButton.layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 65F)
                }

                negativeButton.visibility = View.VISIBLE
                negativeButton.setOnClickListener {
                    negativeListener!!.onClick()
                    dialog.dismiss()
                }
            }
            else {
                negativeButton.visibility = View.GONE
            }

            // cancel
            if (cancelListener != null) {
                dialog.setOnCancelListener(cancelListener)
            }

            // dialog size
            val windowParams = WindowManager.LayoutParams()
            windowParams.copyFrom(dialog.window?.attributes)
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT
            windowParams.height = WindowManager.LayoutParams.MATCH_PARENT
            dialog.window?.attributes = windowParams

            // dialog background
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // show dialog
            dialog.show()

            return FancyDialog(this)
        }
    }
}
