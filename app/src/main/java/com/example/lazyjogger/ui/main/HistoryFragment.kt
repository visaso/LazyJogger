package com.example.lazyjogger.ui.main


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lazyjogger.R
import com.example.lazyjogger.database.UserModel


/**
 * A simple [Fragment] subclass.
 */
class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: HistoryListAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // val list = Runs.runs
        /*
        viewManager = LinearLayoutManager(activity)
        viewAdapter = HistoryListAdapter(list)

        recycler = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

         */
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView = view!!.findViewById(R.id.recyclerView)

        val ump = ViewModelProviders.of(this).get(UserModel::class.java)
        ump.getUsers().observe(this, Observer {
            viewAdapter = HistoryListAdapter(it)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = viewAdapter

        })
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        @JvmStatic
        fun newInstance(sectionNumber: Int): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


}

data class Run (val name: String, val startYear: Int, val endYear: Int, val desc: String)

internal object Runs {

    val runs = listOf(
        Run("Myyrmäkirundi", 1919, 1925, "Eka presidentti"),
        Run("Martsari", 1925, 1931, "Toka presidentti"),
        Run("Pitäjänmäki", 1931, 1937, "Kolmas presidentti"),
        Run("Olari", 1937, 1940, "Neljas presidentti"),
        Run("Varisto", 1940, 1944, "Viides presidentti"),
        Run("Lammaslampi", 1944, 1946, "Kuudes presidentti"),
        Run("Mesta", 1946, 1956, "Äkäinen ukko")
    )
}

