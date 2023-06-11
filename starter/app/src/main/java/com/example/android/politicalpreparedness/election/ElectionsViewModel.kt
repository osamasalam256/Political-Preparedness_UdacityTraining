package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList

//Construct ViewModel and provide election datasource
class ElectionsViewModel(application: Application): ViewModel() {
    private val database = ElectionDatabase.getInstance(application)
    private val electionRepository = ElectionRepository(database)
    //live data val for upcoming elections
    val upcomingElections = MutableLiveData<List<Election>>()
    //live data val for saved elections
    val savedElections = MutableLiveData<List<Election>>()


    //variable to navigate to saved or upcoming election voter info
    private val _navigateToElection = MutableLiveData<Election?>()
    val navigateToElection : LiveData<Election?>
        get() = _navigateToElection

    init {
        refreshElections()
        getElections()
        getSavedElections()
    }

    private fun getElections() {
        viewModelScope.launch {
            database.electionDao.getAllElections().collect(){
                 upcomingElections.value = it
            }
        }
    }

    private fun getSavedElections() {
        viewModelScope.launch {
            database.electionDao.getSavedElections().collect(){
                savedElections.value = it
            }
        }
    }
    private fun refreshElections(){
        viewModelScope.launch {
                try {
                    electionRepository.refreshElections()
                } catch (e: Exception) {
                    //upcomingElections.value = ArrayList()
                    Timber.tag("API serveice").i(e.message)
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