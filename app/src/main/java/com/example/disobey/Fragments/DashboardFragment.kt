package com.example.disobey.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.disobey.Backpack
import com.example.disobey.R
import com.example.disobey.Stats
import com.ismaeldivita.chipnavigation.ChipNavigationBar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    lateinit var chipNavigationBar : ChipNavigationBar;
    lateinit var DashboardView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        DashboardView= inflater.inflate(R.layout.fragment_dashboard, container, false)
        return DashboardView;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chipNavigationBar = view.findViewById(R.id.nav)
        chipNavigationBar.setItemSelected(
            R.id.bottom_nav_stats,
            true
        )
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(
                R.id.secondary_fragment_container,
                Stats()
            ).commit()
        bottomMenu()
    }
    private fun bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener { id ->
            val fragment: Fragment = when (id) {
                R.id.bottom_nav_stats -> Stats()
                R.id.bottom_nav_backpack -> Backpack()
                else -> throw IllegalArgumentException("Invalid menu item ID")
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.secondary_fragment_container, fragment)
                .commit()
        }
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment DashboardFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            DashboardFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}