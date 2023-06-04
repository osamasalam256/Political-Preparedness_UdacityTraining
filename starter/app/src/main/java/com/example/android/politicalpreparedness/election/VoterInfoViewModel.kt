package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class VoterInfoViewModel(private val dataSource: ElectionDao, electionId: Int, division: Division) : ViewModel() {

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
            try {
                val election = dataSource.getElectionById(electionId)
                _isElectionFollowed.value = election != null
                val address = division.state + ", " + division.country

                _voterInfo.value =
                    CivicsApi.retrofitService.getVoterInfo(address, electionId)

                _election.value = _voterInfo.value?.election

            } catch (e: Exception) {
                Timber.tag("Api_voferInfo").i("${e.message}")
            }
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
                election.value?.let { dataSource.insertElection(it) }
                _isElectionFollowed.value = true
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    election.value?.let { dataSource.delete(it) }
                }
                _isElectionFollowed.value = false
            }
        }
    }

}