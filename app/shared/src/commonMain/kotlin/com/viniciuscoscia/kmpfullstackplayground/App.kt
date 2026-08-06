package com.viniciuscoscia.kmpfullstackplayground

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.viniciuscoscia.kmpfullstackplayground.substance.atlas.SubstanceAtlasShell
import com.viniciuscoscia.kmpfullstackplayground.substance.atlas.SubstanceAtlasViewModel
import com.viniciuscoscia.kmpfullstackplayground.substance.client.SubstanceAtlasConfig
import com.viniciuscoscia.kmpfullstackplayground.substance.client.SubstanceAtlasRepositories

@Composable
fun App(
    onOpenAndroidBasics: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val repositories = remember {
        SubstanceAtlasRepositories(SubstanceAtlasConfig(defaultServerBaseUrl()))
    }
    val viewModel = remember(repositories, scope) {
        SubstanceAtlasViewModel(
            catalog = repositories.catalog,
            research = repositories.research,
            reports = repositories.reports,
            scope = scope,
        )
    }
    SubstanceAtlasShell(viewModel, onOpenAndroidBasics)
}
