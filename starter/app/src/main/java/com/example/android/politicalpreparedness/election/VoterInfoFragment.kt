package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding

class VoterInfoFragment : Fragment() {

    private lateinit var viewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding
    private lateinit var dao: ElectionDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {

        //  ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application
        dao = ElectionDatabase.getInstance(application).electionDao
        val arg = VoterInfoFragmentArgs.fromBundle(requireArguments())
        val electionId = arg.argElectionId
        val division = arg.argDivision

        val viewModelFactory = VoterInfoViewModelFactory(dao, electionId, division)

        viewModel = ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]


        // binding values
        binding = FragmentVoterInfoBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */

        // Handle loading of URLs
        viewModel.ballotInformationUrl.observe(viewLifecycleOwner) {
            it?.let {
                setIntentToLoadUrl(it)
                viewModel.onBallotInformationNavigated()
            }
        }

        viewModel.votingLocationsUrl.observe(viewLifecycleOwner) {
            it?.let {
                setIntentToLoadUrl(it)
                viewModel.onVotingLocationNavigated()
            }
        }
        // Handle save button UI state
        viewModel.run {
            isElectionFollowed.observe(viewLifecycleOwner) {
                when (it) {
                    true -> binding.followElectionButton.text = getString(R.string.unfollow_button)

                    false -> binding.followElectionButton.text = getString(R.string.follow_button)
                    else -> return@observe
                }
            }
        }
        // save button clicks
        binding.followElectionButton.setOnClickListener {
            viewModel.followElection()
        }

        return binding.root
    }

    // method to load URL intents
    private fun setIntentToLoadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}