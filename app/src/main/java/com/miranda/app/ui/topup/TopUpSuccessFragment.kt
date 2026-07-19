package com.miranda.app.ui.topup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.miranda.app.R
import com.miranda.app.ui.dashboard.DashboardFragment

class TopUpSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_top_up_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка "Вернуться"
        view.findViewById<Button>(R.id.btn_back).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Кнопка "Продолжить"
        view.findViewById<Button>(R.id.btn_continue).setOnClickListener {
            // Возвращаемся на главный экран
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, DashboardFragment())  // ✅ Используем android.R.id.content
                .commit()
        }
    }
}