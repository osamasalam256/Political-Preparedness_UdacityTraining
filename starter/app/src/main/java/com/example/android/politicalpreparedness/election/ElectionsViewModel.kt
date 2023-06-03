package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch
import java.util.ArrayList

//Construct ViewModel and provide election datasource
class ElectionsViewModel(dao: ElectionDao): ViewModel() {

    //live data val for upcoming elections
    private val upcomingElections = MutableLiveData<List<Election>>()
    //live data val for saved elections
    val savedElections = dao.getAllElections()
    //set API services
    private val apiService = CivicsApi.retrofitService


    //variable to navigate to saved or upcoming election voter info
    private val _navigateToElection = MutableLiveData<Election?>()
    val navigateToElection : LiveData<Election?>
        get() = _navigateToElection

    init {
        getElections()
    }

    private fun getElections() {
        viewModelScope.launch {
            try {
                val electionList = apiService.getElections()
                upcomingElections.value = electionList.elections
            } catch (e: Exception) {
                upcomingElections.value = ArrayList()
            }
        }
    }

    // Functions to navigate to saved or upcoming election voter info
    fun onElectionClicked(election: Election) {
        _navigateToElection.value = election
    }

    fun onElectionNavigated() {
        _navigateToElection.value = null
    }
}