package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import timber.log.Timber

class RepresentativeViewModel: ViewModel() {

    //live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    private val apiService: CivicsApiService = CivicsApi.retrofitService

    init {
        _address.value = Address("", "", "", "", "")

    }
    //TODO: Create function to fetch representatives from API from a provided address

    fun fetchRepresentatives() {
        viewModelScope.launch {
            _address.value?.let {
                try {
                    val address = _address.value!!.toFormattedString()
                    // in try catch so just assert
                    val (offices, officials) = this@RepresentativeViewModel.apiService.getRepresentatives(address)
                    _representatives.value = offices.flatMap { office ->
                        office.getRepresentatives(
                            officials
                        )
                    }
                } catch (e: Exception) {
                    Timber.e("Error fetching representatives")
                }
            }
        }
    }

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official :

    val (offices, officials) = getRepresentativesDeferred.await()
    _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }

    Note: getRepresentatives in the above code represents the method used to fetch data from the API
    Note: _representatives in the above code represents the established mutable live data housing representatives

     */

    // function get address from geo location
    fun getAddressFromLocation(address: Address) {
        _address.value = address
    }
    //TODO: Create function to get address from individual fields


    private fun addressIsValid(address: Address): Boolean {
        return address.line1.isNotEmpty() && address.city.isNotEmpty() && address.state.isNotEmpty() &&
                address.zip.isNotEmpty() && address.zip.length == 5
    }
}
