package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDao

import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.models.Election

class VoterInfoFragment : Fragment() {

    private lateinit var viewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {



        // binding values
        binding = FragmentVoterInfoBinding.inflate(inflater)



        /**
        Hint: You will need to ensure proper data is provided from previous fragment.
        */


        // save button clicks
//        binding.followElectionButton.setOnClickListener {
//            viewModel.followElection()
//        }
//        viewModel.election.observe(viewLifecycleOwner) {
//            if (it != null) {
//                updateFollowButton(it)
//            }
//        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  ViewModel values and create ViewModel
        val application = requireNotNull(this.activity).application
        val arg = VoterInfoFragmentArgs.fromBundle(requireArguments())
        val electionId = arg.argElectionId
        val division = arg.argDivision

        val viewModelFactory = VoterInfoViewModelFactory(application, electionId, division)

        viewModel = ViewModelProvider(this, viewModelFactory)[VoterInfoViewModel::class.java]

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
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
                    true -> {
                        binding.followElectionButton.text = getString(R.string.follow_button)
                    }
                    false -> {
                        binding.followElectionButton.text = getString(R.string.unfollow_button)
                    }
                    else -> {}
                }
            }
        }

    }

    // method to load URL intents
    private fun setIntentToLoadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun updateFollowButton(election: Election) {
        var followElectionButtonText = getString(R.string.follow_election)
        if (election.isSaved) {
            followElectionButtonText = getString(R.string.unfollow_election)
        }
        binding.followElectionButton.text = followElectionButtonText
    }
}