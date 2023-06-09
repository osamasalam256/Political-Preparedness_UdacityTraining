package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper

import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.*
import java.util.Locale

@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class DetailFragment : Fragment() {

    companion object {
        // Add Constant for Location request
        private const val REQUEST_LOCATION_PERMISSION = 1
      }

    //Declare ViewModel
    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var isLocationUpdatesEnabled = false
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Establish bindings
        binding = FragmentRepresentativeBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RepresentativeViewModel::class.java]


        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.buttonSearch.setOnClickListener {
            viewModel.getAddressFromLocation(addressTextToObject())
            viewModel.fetchRepresentatives()
            hideKeyboard()
        }

        // Button listeners for field and location search
        binding.buttonLocation.setOnClickListener {
            if (checkLocationPermissions()) {
                getLocation()
            }
        }

        //Populate Representative adapter
        val representativeAdapter = RepresentativeListAdapter()
        binding.representativeList.adapter = representativeAdapter

        viewModel.representatives.observe(viewLifecycleOwner) {
            representativeAdapter.submitList(it)
        }

        // spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.state.adapter = adapter
        }

        // Observe the motionLayoutProgress
        viewModel.motionLayoutProgress.observe(viewLifecycleOwner) { progress ->
            binding.motionLayout.progress = progress
        }

        // Observe the representativeListState
        viewModel.representativeListState.observe(viewLifecycleOwner) { state ->
            binding.representativeList.layoutManager?.onRestoreInstanceState(state)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getLocation()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission has not been granted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener(requireActivity()) { location ->
                    location?.let {
                        val geoLocation = geoCodeLocation(location)
                        viewModel.getAddressFromLocation(geoLocation)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to get location: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
           startLocationUpdates()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
            .map { address ->
                Address(address?.thoroughfare.toString(),
                    address?.subThoroughfare.toString(),
                    address?.locality.toString(),
                    address?.adminArea.toString(),
                    address?.postalCode.toString())
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun addressTextToObject(): Address {
        return Address(
            binding.addressLine1.text.toString(),
            binding.addressLine2.text.toString(),
            binding.city.text.toString(),
            binding.state.selectedItem.toString(),
            binding.zip.text.toString()
        )
    }

override fun onResume() {
    super.onResume()
    setupLocationClientAndCallback()
    if (isPermissionGranted() && isLocationUpdatesEnabled) {
        startLocationUpdates()
    }
}

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        // Save the state when the fragment is paused
        val layoutManager = binding.representativeList.layoutManager
        val recycleViewState = layoutManager?.onSaveInstanceState()
        viewModel.saveRepresentativeListState(recycleViewState)

        // Save the state when the fragment is paused
        val progress = binding.motionLayout.progress
        viewModel.saveMotionLayoutProgress(progress)
    }

    private fun setupLocationClientAndCallback() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for (location in it.locations) {
                        getAddressFromLocation(location)
                    }
                }
            }
        }
    }

    private fun getAddressFromLocation(location: Location?) {
        location?.let {
            val address = geoCodeLocation(it)
            viewModel.getAddressFromLocation(address)
        } ?: Toast.makeText(requireContext(), "Location must be in the US", Toast.LENGTH_LONG).show()
    }
@SuppressLint("MissingPermission")
private fun startLocationUpdates() {
    if (isPermissionGranted()) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        isLocationUpdatesEnabled = true
    } else {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
}

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        isLocationUpdatesEnabled = false
    }
}
