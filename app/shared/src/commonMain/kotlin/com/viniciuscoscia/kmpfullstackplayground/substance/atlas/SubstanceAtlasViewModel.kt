package com.viniciuscoscia.kmpfullstackplayground.substance.atlas

import com.viniciuscoscia.kmpfullstackplayground.substance.client.CatalogRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.client.ReportRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.client.ResearchRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Dashboard
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJob
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SourceRef
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SubstanceSummary
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AtlasDestination { DASHBOARD, CATALOG, COMPARE, RESEARCH_QUEUE, SOURCES, DSA }

data class SubstanceAtlasState(
    val destination: AtlasDestination = AtlasDestination.DASHBOARD,
    val locale: String = "pt-BR",
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val selectedTagSlugs: Set<String> = emptySet(),
    val dashboard: Dashboard? = null,
    val substances: List<SubstanceSummary> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val selectedSubstance: Substance? = null,
    val compareSelection: Set<String> = emptySet(),
    val compareItems: List<Substance> = emptyList(),
    val researchJobs: List<ResearchJob> = emptyList(),
    val sources: List<SourceRef> = emptyList(),
    val analyticsWorkbook: ByteArray? = null,
)

sealed interface SubstanceAtlasAction {
    data class Navigate(val destination: AtlasDestination) : SubstanceAtlasAction
    data class ChangeQuery(val value: String) : SubstanceAtlasAction
    data class ToggleTag(val slug: String) : SubstanceAtlasAction
    data class SelectSubstance(val id: String) : SubstanceAtlasAction
    data class ToggleCompare(val id: String) : SubstanceAtlasAction
    data object Search : SubstanceAtlasAction
    data object LoadComparison : SubstanceAtlasAction
    data object Refresh : SubstanceAtlasAction
    data object ToggleLocale : SubstanceAtlasAction
    data object ExportAnalytics : SubstanceAtlasAction
    data object ClearSelection : SubstanceAtlasAction
    data object ClearError : SubstanceAtlasAction
}

class SubstanceAtlasViewModel(
    private val catalog: CatalogRepository,
    private val research: ResearchRepository,
    private val reports: ReportRepository,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(SubstanceAtlasState())
    val state: StateFlow<SubstanceAtlasState> = mutableState.asStateFlow()

    init {
        loadOverview()
    }

    fun dispatch(action: SubstanceAtlasAction) {
        when (action) {
            is SubstanceAtlasAction.Navigate -> navigate(action.destination)
            is SubstanceAtlasAction.ChangeQuery -> mutableState.update { it.copy(query = action.value) }
            is SubstanceAtlasAction.ToggleTag -> toggleTag(action.slug)
            is SubstanceAtlasAction.SelectSubstance -> loadSubstance(action.id)
            is SubstanceAtlasAction.ToggleCompare -> toggleCompare(action.id)
            SubstanceAtlasAction.Search -> loadCatalog()
            SubstanceAtlasAction.LoadComparison -> loadComparison()
            SubstanceAtlasAction.Refresh -> refreshCurrent()
            SubstanceAtlasAction.ToggleLocale -> toggleLocale()
            SubstanceAtlasAction.ExportAnalytics -> exportAnalytics()
            SubstanceAtlasAction.ClearSelection -> mutableState.update { it.copy(selectedSubstance = null) }
            SubstanceAtlasAction.ClearError -> mutableState.update { it.copy(error = null) }
        }
    }

    private fun navigate(destination: AtlasDestination) {
        mutableState.update { it.copy(destination = destination, error = null) }
        when (destination) {
            AtlasDestination.DASHBOARD -> if (state.value.dashboard == null) loadOverview()
            AtlasDestination.CATALOG -> if (state.value.substances.isEmpty()) loadCatalog()
            AtlasDestination.COMPARE -> loadComparison()
            AtlasDestination.RESEARCH_QUEUE -> loadResearchJobs()
            AtlasDestination.SOURCES -> loadSources()
            AtlasDestination.DSA -> Unit
        }
    }

    private fun loadOverview() = request {
        val locale = state.value.locale
        val dashboard = catalog.dashboard(locale)
        val substances = catalog.substances(size = 100, locale = locale).items
        val tags = catalog.tags(locale)
        mutableState.update { it.copy(dashboard = dashboard, substances = substances, tags = tags) }
    }

    private fun loadCatalog() = request {
        val current = state.value
        val page = catalog.substances(
            query = current.query,
            tags = current.selectedTagSlugs.toList(),
            size = 100,
            locale = current.locale,
        )
        mutableState.update { it.copy(substances = page.items) }
    }

    private fun loadSubstance(id: String) = request {
        val detail = catalog.substance(id, state.value.locale)
        mutableState.update { it.copy(selectedSubstance = detail) }
    }

    private fun loadComparison() {
        val ids = state.value.compareSelection.toList()
        if (ids.size < 2) {
            mutableState.update { it.copy(compareItems = emptyList()) }
            return
        }
        request {
            val locale = state.value.locale
            val details = ids.map { catalog.substance(it, locale) }
            mutableState.update { it.copy(compareItems = details) }
        }
    }

    private fun loadResearchJobs() = request {
        val jobs = research.jobs(state.value.locale)
        mutableState.update { it.copy(researchJobs = jobs) }
    }

    private fun loadSources() = request {
        val page = catalog.sources(size = 100, locale = state.value.locale)
        mutableState.update { it.copy(sources = page.items) }
    }

    private fun exportAnalytics() = request {
        val workbook = reports.analyticsWorkbook(state.value.locale)
        mutableState.update { it.copy(analyticsWorkbook = workbook) }
    }

    private fun toggleTag(slug: String) {
        mutableState.update { current ->
            val tags = current.selectedTagSlugs.toMutableSet().apply {
                if (!add(slug)) remove(slug)
            }
            current.copy(selectedTagSlugs = tags)
        }
        loadCatalog()
    }

    private fun toggleCompare(id: String) {
        mutableState.update { current ->
            val selection = current.compareSelection.toMutableSet()
            if (!selection.add(id)) {
                selection.remove(id)
            } else if (selection.size > 5) {
                selection.remove(id)
            }
            current.copy(compareSelection = selection)
        }
    }

    private fun toggleLocale() {
        mutableState.update { current ->
            current.copy(
                locale = if (current.locale == "pt-BR") "en" else "pt-BR",
                selectedSubstance = null,
                compareItems = emptyList(),
            )
        }
        loadOverview()
        when (state.value.destination) {
            AtlasDestination.RESEARCH_QUEUE -> loadResearchJobs()
            AtlasDestination.SOURCES -> loadSources()
            else -> Unit
        }
    }

    private fun refreshCurrent() {
        when (state.value.destination) {
            AtlasDestination.DASHBOARD -> loadOverview()
            AtlasDestination.CATALOG -> loadCatalog()
            AtlasDestination.COMPARE -> loadComparison()
            AtlasDestination.RESEARCH_QUEUE -> loadResearchJobs()
            AtlasDestination.SOURCES -> loadSources()
            AtlasDestination.DSA -> Unit
        }
    }

    private fun request(block: suspend () -> Unit) {
        scope.launch {
            mutableState.update { it.copy(isLoading = true, error = null) }
            runCatching { block() }
                .onFailure { error -> mutableState.update { it.copy(error = error.message ?: "Request failed") } }
            mutableState.update { it.copy(isLoading = false) }
        }
    }
}
