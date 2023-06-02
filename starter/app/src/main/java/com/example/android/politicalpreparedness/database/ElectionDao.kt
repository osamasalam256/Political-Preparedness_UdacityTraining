package com.example.android.politicalpreparedness.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    // insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(election: Election)
    //select all election query
    @Query("select * from election_table ORDER BY electionDay")
    fun getAllElections(): LiveData<List<Election>>
    //select single election query
    @Query("SELECT * FROM election_table WHERE id = :id ")
    suspend fun getElectionById(id:Int): Election?
    //delete query
    @Delete
    fun delete(election: Election)
    //clear query
    @Query("DELETE FROM election_table")
    suspend fun clear()
}