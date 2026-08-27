package org.iiitu.acm.contestpulse.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import org.iiitu.acm.contestpulse.alarm.AlarmScheduler
import org.iiitu.acm.contestpulse.data.model.Contest
import org.iiitu.acm.contestpulse.data.repository.ContestRepository
import org.iiitu.acm.contestpulse.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ContestRepository
    private lateinit var adapter: ContestAdapter
    private var allContests: List<Contest> = emptyList()
    private var selectedPlatformFilter: String = "ALL"

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission required for contest alarms", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ContestRepository(this)

        setupRecyclerView()
        setupListeners()
        checkAndRequestPermissions()

        observeContests()

        // Refresh API sync on app open
        performRefresh()
    }

    private fun setupRecyclerView() {
        adapter = ContestAdapter(
            onToggleAlarm = { contestId, isEnabled ->
                lifecycleScope.launch {
                    repository.toggleAlarm(contestId, isEnabled)
                }
            },
            onDeleteContest = { contestId ->
                lifecycleScope.launch {
                    repository.deleteContest(contestId)
                    Toast.makeText(this@MainActivity, "Custom contest deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAboutAcm.setOnClickListener {
            AboutAcmDialog(this).show()
        }

        binding.swipeRefresh.setOnRefreshListener {
            performRefresh()
        }

        binding.btnRefresh.setOnClickListener {
            performRefresh()
        }

        binding.btnTestAlarm.setOnClickListener {
            Toast.makeText(this, "Triggering 15-Minute Loud Alarm Test...", Toast.LENGTH_SHORT).show()
            AlarmScheduler.triggerTestAlarm(this)
        }

        binding.fabAddCustom.setOnClickListener {
            CustomScheduleDialog(this) { newContest ->
                lifecycleScope.launch {
                    repository.addCustomContest(newContest)
                    Toast.makeText(this@MainActivity, "Weekly reminder added!", Toast.LENGTH_SHORT).show()
                }
            }.show()
        }

        binding.chipGroupPlatform.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedPlatformFilter = when (checkedIds.firstOrNull()) {
                binding.chipCodeforces.id -> "CODEFORCES"
                binding.chipLeetcode.id -> "LEETCODE"
                binding.chipCodechef.id -> "CODECHEF"
                binding.chipCustom.id -> "CUSTOM"
                else -> "ALL"
            }
            applyPlatformFilter()
        }
    }

    private fun observeContests() {
        lifecycleScope.launch {
            repository.getUpcomingContests().collectLatest { list ->
                allContests = list
                applyPlatformFilter()
            }
        }
    }

    private fun applyPlatformFilter() {
        val filtered = if (selectedPlatformFilter == "ALL") {
            allContests
        } else {
            allContests.filter { it.platform.equals(selectedPlatformFilter, ignoreCase = true) }
        }

        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            binding.containerEmpty.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.containerEmpty.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun performRefresh() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val result = repository.refreshContests()
            binding.swipeRefresh.isRefreshing = false
            if (result.isSuccess) {
                Toast.makeText(this@MainActivity, "Contests updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Loaded cached contests (Offline)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
