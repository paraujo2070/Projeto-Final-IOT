package br.com.pedro_araujo.coleta_de_dados.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import br.com.pedro_araujo.coleta_de_dados.ui.fragments.CollectionFragment
import br.com.pedro_araujo.coleta_de_dados.ui.fragments.SensorReadingsFragment
import br.com.pedro_araujo.coleta_de_dados.ui.fragments.SetupFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CollectionFragment()
            1 -> SetupFragment()
            2 -> SensorReadingsFragment()
            else -> CollectionFragment()
        }
    }
}
