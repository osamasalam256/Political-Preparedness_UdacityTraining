package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = ViewModelProvider(this)[RepresentativeViewModel::class.java]

        //Establish bindings
        binding = FragmentRepresentativeBinding.inflate(inflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.buttonSearch.setOnClickListener {
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

        return binding.root

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
                Address(address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode)
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

}