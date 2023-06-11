package com.example.android.politicalpreparedness.repository

import android.annotation.SuppressLint
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@Suppress("UNCHECKED_CAST")
class ElectionRepository(private val electionDatabase: ElectionDatabase) {


    @SuppressLint("SuspiciousIndentation")
    suspend fun refreshElections() {
        deletePastUnsavedElections()
        val elections: List<Election>
        withContext(Dispatchers.IO) {
            val electionResponse = CivicsApi.retrofitService.getElections()
            elections = electionResponse.elections
            elections.forEach { election ->
               val savedElection = electionDatabase.electionDao.getElectionById(election.id)
                    if (election.id == savedElection.id){
                         election.isSaved = savedElection.isSaved
                    }
            }
        }
            electionDatabase.electionDao.insertAllElections(elections)
    }


    suspend fun getVoterInfo(electionId: Int, address: String): VoterInfoResponse? {
        var voterInfo: VoterInfoResponse?
        withContext(Dispatchers.IO) {
            val voterInfoResponse: VoterInfoResponse =
                CivicsApi.retrofitService.getVoterInfo(electionId, address)

            voterInfo = voterInfoResponse
        }
        return voterInfo
    }
    private suspend fun deletePastUnsavedElections() {
        withContext(Dispatchers.IO) {
            electionDatabase.electionDao.deletePastUnsavedElections(getToday())
        }
    }

    private fun getToday(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }
}