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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerView = view!!.findViewById(R.id.recyclerView)

        val ump = ViewModelProviders.of(this).get(UserModel::class.java)
        ump.getUsers().observe(this, Observer {
            viewAdapter = HistoryListAdapter(context!! ,it)
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


