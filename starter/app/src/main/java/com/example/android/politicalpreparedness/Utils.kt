package com.example.android.politicalpreparedness

import android.text.Editable
import android.text.TextWatcher
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.google.android.material.textfield.TextInputEditText


class ValidationTextWatcher(private val editText: TextInputEditText,

) : TextWatcher {

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun afterTextChanged(editable: Editable) {
        when (editText.id) {
            R.id.zip -> isZipValid( editText)
            else -> isEditTextValid( editText)
        }
    }
}
private fun isEditTextValid(editText: TextInputEditText): Boolean {
    if (editText.text.toString().trim().isEmpty()) {
        return false
    } else {
    }
    return true
}

private fun isZipValid(editText: TextInputEditText): Boolean {
    if (editText.text.toString().trim().length != 5) {

        return false
    } else {

    }
    return true
}

fun areAllFieldsValid(binding: FragmentRepresentativeBinding): Boolean {
    return isEditTextValid(binding.addressLine1 as TextInputEditText) &&
            isEditTextValid(binding.city as TextInputEditText) &&
            isZipValid(binding.zip as TextInputEditText)
}