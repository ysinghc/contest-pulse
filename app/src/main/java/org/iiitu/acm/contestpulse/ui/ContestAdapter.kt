package org.iiitu.acm.contestpulse.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.iiitu.acm.contestpulse.data.model.Contest
import org.iiitu.acm.contestpulse.databinding.ItemContestBinding
import java.text.SimpleDateFormat
import java.util.*

class ContestAdapter(
    private val onToggleAlarm: (String, Boolean) -> Unit,
    private val onDeleteContest: (String) -> Unit
) : ListAdapter<Contest, ContestAdapter.ContestViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val binding = ItemContestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContestViewHolder(private val binding: ItemContestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contest: Contest) {
            binding.tvTitle.text = contest.title

            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy • hh:mm a", Locale.US)
            binding.tvTime.text = dateFormat.format(Date(contest.startTimeMillis))

            val now = System.currentTimeMillis()
            val diffMs = contest.startTimeMillis - now
            if (diffMs > 0) {
                val hours = diffMs / (1000 * 3600)
                val mins = (diffMs / (1000 * 60)) % 60
                val days = hours / 24
                val remHours = hours % 24

                val relativeStr = if (days > 0) {
                    "Starts in ${days}d ${remHours}h ${mins}m"
                } else {
                    "Starts in ${hours}h ${mins}m"
                }
                binding.tvRelativeTime.text = relativeStr
            } else {
                binding.tvRelativeTime.text = "Contest in progress or starting soon"
            }

            val platform = contest.platform.uppercase()
            binding.tvPlatform.text = platform
            when (platform) {
                "LEETCODE" -> binding.tvPlatform.setBackgroundColor(Color.parseColor("#F59E0B"))
                "CODECHEF" -> binding.tvPlatform.setBackgroundColor(Color.parseColor("#8B5CF6"))
                "CUSTOM" -> binding.tvPlatform.setBackgroundColor(Color.parseColor("#06B6D4"))
                else -> binding.tvPlatform.setBackgroundColor(Color.parseColor("#EF4444"))
            }

            binding.switchAlarm.setOnCheckedChangeListener(null)
            binding.switchAlarm.isChecked = contest.isAlarmEnabled
            binding.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                onToggleAlarm(contest.id, isChecked)
            }

            if (contest.isCustom) {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setOnClickListener {
                    onDeleteContest(contest.id)
                }
            } else {
                binding.btnDelete.visibility = View.GONE
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Contest>() {
        override fun areItemsTheSame(oldItem: Contest, newItem: Contest): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contest, newItem: Contest): Boolean =
            oldItem == newItem
    }
}
