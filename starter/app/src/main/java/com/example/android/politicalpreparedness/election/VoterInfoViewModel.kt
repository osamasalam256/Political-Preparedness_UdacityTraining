package com.example.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.android.politicalpreparedness.database.ElectionDatabase

import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoterInfoViewModel(application: Application, electionId: Int, division: Division) : ViewModel() {
    private val dataSource = ElectionDatabase.getInstance(application)
    private val electionRepository = ElectionRepository(dataSource)

    //live data to hold voter info
    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _election = MutableLiveData<Election>()
    val election: LiveData<Election>
        get() = _election


    private val _votingLocationsUrl = MutableLiveData<String?>()
    val votingLocationsUrl: LiveData<String?>
        get() = _votingLocationsUrl


    private val _ballotInformationUrl = MutableLiveData<String?>()
    val ballotInformationUrl: LiveData<String?>
        get() = _ballotInformationUrl


    private val _isElectionFollowed = MutableLiveData<Boolean?>()
    val isElectionFollowed: LiveData<Boolean?>
        get() = _isElectionFollowed

    init {
        getVoteInfo(electionId, division)
    }
    //var and methods to populate voter info
    private fun getVoteInfo(electionId: Int, division: Division) {
        viewModelScope.launch {
                val election = dataSource.electionDao.getElectionById(electionId)

                _isElectionFollowed.value = election != null && election.isSaved == true
            val address = division.state + ", " + division.country
            try {
                _voterInfo.value = electionRepository.getVoterInfo(electionId, address)
            } catch (e: Exception) {
                Log.e("VoterInfoVM", "exception thrown: ${e.localizedMessage}")
            }
                _election.value = _voterInfo.value?.election
        }
    }

// var and methods to support loading URLs
    fun onVotingLocationClick() {
        _votingLocationsUrl.value =
            _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.votingLocationFinderUrl
    }

    fun onBallotInformationClick() {
        _ballotInformationUrl.value =
            _voterInfo.value?.state?.get(0)?.electionAdministrationBody?.ballotInfoUrl
    }

    fun onVotingLocationNavigated() {
        _votingLocationsUrl.value = null
    }

    fun onBallotInformationNavigated() {
        _ballotInformationUrl.value = null
    }

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

    fun followElection() {
        viewModelScope.launch {
            if (_isElectionFollowed.value == false) {
                _election.value?.let {
                    updateElectionInDatabase()
                    dataSource.electionDao.updateIsSaved(it.id, true)
                }
                _isElectionFollowed.value = true
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    _election.value?.let {
                        dataSource.electionDao.updateIsSaved(it.id, false)
                    }
                }
                _isElectionFollowed.value = false
            }
        }
    }

    private suspend fun updateElectionInDatabase() {
        withContext(Dispatchers.IO) {
            _election.value?.let { dataSource.electionDao.insertElection(it) }
        }
    }

}