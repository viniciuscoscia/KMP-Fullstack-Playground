package com.viniciuscoscia.kmpfullstackplayground.common

import androidx.activity.ComponentActivity
import com.viniciuscoscia.kmpfullstackplayground.activitiesinternals.ActivitiesInternalsActivity
import com.viniciuscoscia.kmpfullstackplayground.battery.BatteryActivity
import com.viniciuscoscia.kmpfullstackplayground.broadcastreceiver.BroadcastActivity
import com.viniciuscoscia.kmpfullstackplayground.contentproviders.ContentProvidersActivity
import com.viniciuscoscia.kmpfullstackplayground.contextdemo.ContextActivity
import com.viniciuscoscia.kmpfullstackplayground.foregroundservices.ForegroundServiceActivity
import com.viniciuscoscia.kmpfullstackplayground.intents.IntentsActivity
import com.viniciuscoscia.kmpfullstackplayground.ipc.IpcActivity
import com.viniciuscoscia.kmpfullstackplayground.launchmodes.LaunchModesActivity
import com.viniciuscoscia.kmpfullstackplayground.lifecycle.LifecycleActivity
import com.viniciuscoscia.kmpfullstackplayground.memory.MemoryActivity
import com.viniciuscoscia.kmpfullstackplayground.osarchitecture.OsArchitectureActivity
import com.viniciuscoscia.kmpfullstackplayground.resourcesdemo.ResourcesActivity
import com.viniciuscoscia.kmpfullstackplayground.security.SecurityActivity
import com.viniciuscoscia.kmpfullstackplayground.uris.UrisActivity
import com.viniciuscoscia.kmpfullstackplayground.viewmodelinternals.ViewModelInternalsActivity
import com.viniciuscoscia.kmpfullstackplayground.viewmodels.ViewModelActivity
import com.viniciuscoscia.kmpfullstackplayground.viewsystem.ViewSystemActivity
import com.viniciuscoscia.kmpfullstackplayground.workmanager.WorkManagerActivity

/**
 * One entry per video in the Philipp Lackner courses this app follows. The launcher screen
 * ([com.viniciuscoscia.kmpfullstackplayground.MainActivity]) groups this list by [Course], renders
 * a section per course and starts the matching Activity with an explicit Intent when a card is
 * tapped.
 *
 * @param number the topic number **within its own course**, which is why [course] exists: for
 *   [Course.BASICS] this is the topic/video number, for [Course.INTERNALS] this is the *section*
 *   number, so the number alone is not unique across the catalog.
 * @param status marks whether the demo is fully built or still a template to fill in.
 */
data class BasicsTopic(
    val number: Int,
    val title: String,
    val subtitle: String,
    val activityClass: Class<out ComponentActivity>,
    val course: Course = Course.BASICS,
    val status: Status = Status.DONE,
) {
    enum class Status { DONE, TEMPLATE }

    /** A PL-Coding course; declaration order is the order the sections appear in the launcher. */
    enum class Course(val label: String, val tagline: String) {
        BASICS("Android Basics 2023", "One Activity per topic"),
        INTERNALS("Android Internals", "How the OS actually runs your app"),
    }

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
            BasicsTopic(9, "WorkManager", "Deferred background work with constraints", WorkManagerActivity::class.java),
            BasicsTopic(10, "Uris", "resource:// vs file:// vs content://", UrisActivity::class.java),
            BasicsTopic(11, "Content Providers", "Resolver, URIs, Cursor & CRUD", ContentProvidersActivity::class.java),

            // Android Internals — one entry per course section.
            BasicsTopic(2, "Android OS System Architecture", "Main Thread, Looper, MessageQueue & Handlers", OsArchitectureActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(3, "Activities Under the Hood", "Template — to be implemented", ActivitiesInternalsActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(4, "ViewModels", "Template — to be implemented", ViewModelInternalsActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(5, "UI & The View System", "Template — to be implemented", ViewSystemActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(6, "Inter Process Communication (IPC)", "Template — to be implemented", IpcActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(7, "Android's Security System", "Template — to be implemented", SecurityActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(8, "Memory Management", "Template — to be implemented", MemoryActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
            BasicsTopic(9, "Battery Management", "Template — to be implemented", BatteryActivity::class.java, Course.INTERNALS, Status.TEMPLATE),
        )
    }
}
