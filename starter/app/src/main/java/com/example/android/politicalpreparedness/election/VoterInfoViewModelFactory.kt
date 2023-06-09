package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.models.Division

//Factory to generate VoterInfoViewModel with provided election datasource
@Suppress("UNCHECKED_CAST")
class VoterInfoViewModelFactory(
    private val application: Application,
    private val electionId: Int,
    private val division: Division
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)){
            return VoterInfoViewModel(application, electionId, division) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}