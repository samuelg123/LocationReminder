package com.udacity.project4.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

// Source: https://stackoverflow.com/questions/36399787/how-to-count-recyclerview-items-with-espresso/39446889
class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) throw noViewFoundException
        if (view !is RecyclerView) throw Exception("RecyclerView not found!")
        view.adapter?.let {
            assertThat(it.itemCount, `is`(expectedCount))
        } ?: throw Exception("RecyclerView adapter is null!")
    }
}