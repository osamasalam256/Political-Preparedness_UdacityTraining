package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {
    private lateinit var binding: FragmentElectionBinding
    //  Declare ViewModel
    private lateinit var viewModel: ElectionsViewModel
    // Declare DatabaseDao
    private lateinit var dao: ElectionDao

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application
        dao = ElectionDatabase.getInstance(application).electionDao

        val viewModelFactory = ElectionsViewModelFactory(dao)

        viewModel = ViewModelProvider(this, viewModelFactory)[ElectionsViewModel::class.java]

        // binding values
        binding = FragmentElectionBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


         //Upcoming Elections

        val upcomingElectionsAdapter = ElectionListAdapter(ElectionListener { election ->
            election.let {
                viewModel.onElectionClicked(election)
            }
        })

        binding.upcomingElectionList.adapter = upcomingElectionsAdapter

        viewModel.upcomingElections.observe(viewLifecycleOwner) {
            it?.let {
                upcomingElectionsAdapter.submitList(it)
            }
        }


         // Saved Elections

        val savedElectionsAdapter = ElectionListAdapter(ElectionListener { election ->
            viewModel.onElectionClicked(election)
        })

        binding.savedElectionList.adapter = savedElectionsAdapter

        viewModel.savedElections.observe(viewLifecycleOwner) {
            it?.let {
                savedElectionsAdapter.submitList(it)
            }
        }

        // Navigation

        viewModel.navigateToElection.observe(viewLifecycleOwner) { election ->
            election?.let {
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        it.id, it.division
                    )
                )
                viewModel.onElectionNavigated()
            }

        }
        return binding.root
    }

}