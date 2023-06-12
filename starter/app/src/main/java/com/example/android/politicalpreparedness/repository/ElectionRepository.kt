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
    suspend fun refreshElections(elections: List<Election>) {
        deletePastUnsavedElections()
        val newElections: List<Election>
        withContext(Dispatchers.IO) {
            if (elections.isEmpty()){

                val electionResponse = CivicsApi.retrofitService.getElections()
                newElections = electionResponse.elections
                electionDatabase.electionDao.insertAllElections(newElections)
            }else {
                val electionResponse = CivicsApi.retrofitService.getElections()
                newElections = electionResponse.elections
                newElections.forEach { election ->
                    val savedElection = electionDatabase.electionDao.getElectionById(election.id)
                    if (election.id == savedElection.id) {
                        election.isSaved = savedElection.isSaved
                    }
                }
                electionDatabase.electionDao.insertAllElections(newElections)
            }
        }
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