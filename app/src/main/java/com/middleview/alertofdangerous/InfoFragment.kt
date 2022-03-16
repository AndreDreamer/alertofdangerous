package com.middleview.alertofdangerous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.middleview.alertofdangerous.databinding.FragmentInfoBinding
import com.middleview.alertofdangerous.extensions.viewBinding

class InfoFragment : Fragment() {
    private val binding by viewBinding(FragmentInfoBinding::inflate)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
}