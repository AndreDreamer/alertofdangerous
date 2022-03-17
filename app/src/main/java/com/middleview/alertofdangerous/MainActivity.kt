package com.middleview.alertofdangerous

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.middleview.alertofdangerous.databinding.ActivityMainBinding
import com.middleview.alertofdangerous.extensions.viewBinding

class MainActivity : FragmentActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val numberOfTabs = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setContentView(binding.root)
    }

    private fun setupViews() {
        with(binding) {
            val adapter = TabsPagerAdapter(supportFragmentManager, lifecycle, numberOfTabs)
            viewPager.adapter = adapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = getString(R.string.fragment_name_main)
                    1 -> tab.text = getString(R.string.fragment_name_info)
                }
            }.attach()
        }
    }
}

