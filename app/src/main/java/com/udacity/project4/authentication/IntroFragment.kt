package com.udacity.project4.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.udacity.project4.databinding.IntroFragmentBinding

class IntroFragment : Fragment() {
    private lateinit var binding: IntroFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = IntroFragmentBinding.inflate(inflater)
        binding.fragment = this
        binding.lifecycleOwner = this
        return binding.root
    }

    fun gotoLogin(view: View) = findNavController()
        .navigate(IntroFragmentDirections.actionIntroFragmentToLoginFragment())

}