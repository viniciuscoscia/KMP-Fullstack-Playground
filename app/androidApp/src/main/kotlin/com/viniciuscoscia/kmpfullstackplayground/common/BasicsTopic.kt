package com.viniciuscoscia.kmpfullstackplayground.common

import androidx.activity.ComponentActivity
import com.viniciuscoscia.kmpfullstackplayground.broadcastreceiver.BroadcastActivity
import com.viniciuscoscia.kmpfullstackplayground.contentproviders.ContentProvidersActivity
import com.viniciuscoscia.kmpfullstackplayground.contextdemo.ContextActivity
import com.viniciuscoscia.kmpfullstackplayground.foregroundservices.ForegroundServiceActivity
import com.viniciuscoscia.kmpfullstackplayground.intents.IntentsActivity
import com.viniciuscoscia.kmpfullstackplayground.launchmodes.LaunchModesActivity
import com.viniciuscoscia.kmpfullstackplayground.lifecycle.LifecycleActivity
import com.viniciuscoscia.kmpfullstackplayground.resourcesdemo.ResourcesActivity
import com.viniciuscoscia.kmpfullstackplayground.uris.UrisActivity
import com.viniciuscoscia.kmpfullstackplayground.viewmodels.ViewModelActivity
import com.viniciuscoscia.kmpfullstackplayground.workmanager.WorkManagerActivity

/**
 * One entry per video in Philipp Lackner's "Android Basics 2023" series. The launcher screen
 * ([com.viniciuscoscia.kmpfullstackplayground.MainActivity]) renders this list and starts the
 * matching Activity with an explicit Intent when a card is tapped.
 *
 * @param status marks whether the demo is fully built or still a template to fill in.
 */
data class BasicsTopic(
    val number: Int,
    val title: String,
    val subtitle: String,
    val activityClass: Class<out ComponentActivity>,
    val status: Status = Status.DONE,
) {
    enum class Status { DONE, TEMPLATE }

    companion object {
        val all: List<BasicsTopic> = listOf(
            BasicsTopic(1, "Activities & Lifecycle", "Observe every lifecycle callback live", LifecycleActivity::class.java),
            BasicsTopic(2, "Tasks, Back Stack & Launch Modes", "standard vs singleTop vs singleTask", LaunchModesActivity::class.java),
            BasicsTopic(3, "ViewModels & Config Changes", "Survive rotation & process death", ViewModelActivity::class.java),
            BasicsTopic(4, "What is the Context?", "Application vs Activity context", ContextActivity::class.java),
            BasicsTopic(5, "Resources & Qualifiers", "How Android picks the right resource", ResourcesActivity::class.java),
            BasicsTopic(6, "Intents & Intent Filters", "Explicit, implicit & deep links", IntentsActivity::class.java),
            BasicsTopic(7, "Broadcasts & Receivers", "System & custom broadcasts", BroadcastActivity::class.java),
            BasicsTopic(8, "Foreground Services", "A stopwatch with a live notification", ForegroundServiceActivity::class.java),
            BasicsTopic(9, "WorkManager", "Template — to be implemented", WorkManagerActivity::class.java, Status.TEMPLATE),
            BasicsTopic(10, "Uris", "Template — to be implemented", UrisActivity::class.java, Status.TEMPLATE),
            BasicsTopic(11, "Content Providers", "Template — to be implemented", ContentProvidersActivity::class.java, Status.TEMPLATE),
        )
    }
}
