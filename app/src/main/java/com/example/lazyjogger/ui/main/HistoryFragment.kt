package com.example.lazyjogger.ui.main


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lazyjogger.R
import kotlinx.android.synthetic.main.history_item.*


/**
 * A simple [Fragment] subclass.
 */
class HistoryFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val list = Runs.runs
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        viewManager = LinearLayoutManager(activity)
        viewAdapter = HistoryListAdapter(list)


        recycler = view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        return view
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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

