package com.orchardmanager.treedata.data

class Validator {

    companion object {
        //written by bard (Google)
        fun validateDate(text: String): Boolean {
            val pattern = Regex("^\\d{2}-\\d{2}-\\d{4}$")
            return pattern.matches(text)
        }

        fun validateTime(text: String): Boolean {
            val pattern = Regex("\\d{2}:\\d{2}")
            return pattern.matches(text)
        }
    }

}