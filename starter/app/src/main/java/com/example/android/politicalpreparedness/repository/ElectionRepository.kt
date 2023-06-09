package com.example.android.politicalpreparedness.repository

import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionRepository(private val electionDatabase: ElectionDatabase) {

    suspend fun refreshElections() {
        val elections: List<Election>?
        withContext(Dispatchers.IO) {
            val electionResponse = CivicsApi.retrofitService.getElections()
            elections = electionResponse.elections
            electionDatabase.electionDao.insertAllElections(elections)
        }
    }

    suspend fun getVoterInfo(electionId: Int, address: String): VoterInfoResponse? {
        var voterInfo: VoterInfoResponse?
        withContext(Dispatchers.IO) {
            val voterInfoResponse: VoterInfoResponse = CivicsApi.retrofitService.getVoterInfo(address, electionId)

            voterInfo = voterInfoResponse
        }
        return voterInfo
    }
    suspend fun updateIsSaved(election: Election){
    withContext(Dispatchers.IO){

            electionDatabase.electionDao.updateIsSaved(election.id, true )
        }
    }

    suspend fun updateIsNotSave(election: Election) {
        withContext(Dispatchers.IO) {
            electionDatabase.electionDao.updateIsSaved(election.id, false)
        }
    }
}