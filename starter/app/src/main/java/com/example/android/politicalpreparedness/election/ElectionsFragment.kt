package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment: Fragment() {
    private lateinit var binding: FragmentElectionBinding
    //  Declare ViewModel
    private lateinit var viewModel: ElectionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // binding values
        binding = FragmentElectionBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application

        val viewModelFactory = ElectionsViewModelFactory(application)

        viewModel = ViewModelProvider(this, viewModelFactory)[ElectionsViewModel::class.java]
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }
    private fun refreshData(){

        viewModel.getElections()
        viewModel.getSavedElections()
        viewModel.refreshElections()
        Thread.sleep(200)
        binding.swipeRefreshLayout.isRefreshing = false
    }

}