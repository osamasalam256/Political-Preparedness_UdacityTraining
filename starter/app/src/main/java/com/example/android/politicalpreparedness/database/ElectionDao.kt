package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ElectionDao {

    // insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllElections(elections: List<Election>)
    //select all election query
    @Query("select * from election_table ORDER BY electionDay")
    fun getAllElections(): Flow<List<Election>>
    //select single election query
    @Query("SELECT * FROM election_table WHERE id = :id ")
    suspend fun getElectionById(id:Int): Election

    @Query("SELECT * FROM election_table WHERE isSaved==true")
    fun getSavedElections(): Flow<List<Election>>

    @Query("update election_table SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateIsSaved(id: Int, isSaved: Boolean)
    //delete query
    @Query("DELETE FROM election_table WHERE electionDay < :today AND NOT isSaved")
    suspend fun deletePastUnsavedElections(today: Date)
}