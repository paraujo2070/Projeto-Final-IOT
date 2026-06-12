package br.com.pedro_araujo.coleta_de_dados.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import br.com.pedro_araujo.coleta_de_dados.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiagnosticsFragment : Fragment(R.layout.fragment_diagnostics) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Por enquanto estático, ou pode observar o serviço
    }
}
