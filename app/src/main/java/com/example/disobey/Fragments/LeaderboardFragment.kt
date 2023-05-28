package com.example.disobey.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.disobey.BuildConfig
import com.example.disobey.LeaderboardsFirestoreAdapter
import com.example.disobey.LeaderboardsUserData
import com.example.disobey.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LeaderboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeaderboardFragment : Fragment() {
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

    lateinit var LeaderboardView: View

    lateinit var fsrecyclerview: RecyclerView
    lateinit var pref: SharedPreferences
    private var disobeySteps = 0
    var userList = ArrayList<LeaderboardsUserData>()
    var fsadapter= LeaderboardsFirestoreAdapter(userList)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        LeaderboardView=inflater.inflate(R.layout.fragment_leaderboard, container, false)

        fsrecyclerview=LeaderboardView.findViewById<RecyclerView>(R.id.leaderboardsRecycler)
        fsrecyclerview.layoutManager = LinearLayoutManager(context)
        val user = FirebaseAuth.getInstance().currentUser?.displayName
        pref = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        disobeySteps= pref.getInt("disobeySteps",0)
        LeaderboardView.findViewById<TextView>(R.id.name).text= user
        LeaderboardView.findViewById<TextView>(R.id.coin).text= (disobeySteps/100).toString()
        LeaderboardView.findViewById<TextView?>(R.id.steps).text=disobeySteps.toString()
//        step.text= disobeySteps.toString()
//        coin.text= (disobeySteps/100).toString()
//        name.text= user

        val db = FirebaseFirestore.getInstance()
        var leaderBoardsList=db.collection("leaderboards").orderBy("disobeySteps", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (!queryDocumentSnapshots.isEmpty) {
                    val list = queryDocumentSnapshots.documents
                    for (d in list) {
                        var v = d.toObject(LeaderboardsUserData::class.java)
                        if (v != null) {
                            userList.add(v)
                            fsadapter.notifyDataSetChanged()
                        }

                        Log.i("TAGG",""+v?.name+" "+v?.disobeySteps+" "+d.id)
                    }
//                    courseRVAdapter.notifyDataSetChanged()
                } else {
                    // if the snapshot is empty we are displaying a toast message.
//                    Toast.makeText(
//                        requireActivity(),
//                        "No data found in Database",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }.addOnFailureListener { // if we do not get any data or any error we are displaying
                // a toast message that we do not get any data
//                Toast.makeText(requireActivity(), "Fail to get the data.", Toast.LENGTH_SHORT)
//                    .show()
            }

        fsrecyclerview.adapter=fsadapter
        Log.i("TAGG",""+leaderBoardsList)
        println(leaderBoardsList)
        return LeaderboardView;
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LeaderboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LeaderboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}