package com.example.android.politicalpreparedness.representative

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.RepresentativeRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import timber.log.Timber

class RepresentativeViewModel: ViewModel() {

    private val representativeRepository = RepresentativeRepository()
    //live data for representatives and address
    private val _representatives = MutableLiveData<List<Representative>?>()
    val representatives: LiveData<List<Representative>?>
        get() = _representatives

    private val _representativeListState = MutableLiveData<Parcelable?>()
    val representativeListState: LiveData<Parcelable?>
        get() = _representativeListState

    private val _motionLayoutProgress = MutableLiveData<Float>()
    val motionLayoutProgress: LiveData<Float>
        get() = _motionLayoutProgress

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    init {
        _address.value = Address("", "", "", "", "")
        _representativeListState.value = null
    }
    // to fetch representatives from API from a provided address

    fun fetchRepresentatives() {
        viewModelScope.launch {
            _address.value?.let {
                try {
                    val address = _address.value!!
                    _representatives.value = representativeRepository.getRepresentatives(address)
                } catch (e: Exception) {
                    _representatives.value = null
                    Timber.e("Error fetching representatives")
                }
            }
        }
    }

    fun saveRepresentativeListState(state: Parcelable?) {
        _representativeListState.value = state
    }

    fun saveMotionLayoutProgress(progress: Float) {
        _motionLayoutProgress.value = progress
    }

    // function get address from geo location
    fun getAddressFromLocation(address: Address) {
        if (addressIsValid(address)){
            _address.value = address
        }
    }

    private fun addressIsValid(address: Address): Boolean {
        return address.line1.isNotEmpty() && address.city.isNotEmpty() && address.state.isNotEmpty() &&
                address.zip.isNotEmpty() && address.zip.length == 5
    }
}
