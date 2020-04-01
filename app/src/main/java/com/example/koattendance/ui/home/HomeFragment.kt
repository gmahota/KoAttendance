package com.example.koattendance.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.koattendance.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)

        val isValidated = homeViewModel.getUserIsValidaded();

        if(isValidated){
            textView.text = "Bem vindo ao sistema Ko-Attendance, faça a sua entrada ou saida"
        }else{
            textView.text = "O seu dispositivo não se encontra registrado no sistema deverá clicar no canto superio esquerto -> Registro!"
        }
        return root
    }
}
