package com.example.ai_submission.views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.example.ai_submission.R
import com.google.android.material.textfield.TextInputEditText

class EmailET: TextInputEditText {

    var isError = false
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addTextChangedListener {
            val check = android.util.Patterns.EMAIL_ADDRESS.matcher(it.toString()).matches()
            if (check) {
                isError = false
                error = null
            } else {
                isError = true
                error = resources.getString(R.string.inv_email)
            }
        }
    }
}