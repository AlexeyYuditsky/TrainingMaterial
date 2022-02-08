package com.alexeyyuditsky.simplemvvm.view.base

import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner
import com.alexeyyuditsky.simplemvvm.ARG_SCREEN
import com.alexeyyuditsky.simplemvvm.App
import com.alexeyyuditsky.simplemvvm.MainViewModel
import java.lang.reflect.Constructor

/**
 * Use this method for getting view-models from your fragments
 */
inline fun <reified VM : ViewModel> BaseFragment.screenViewModel() = viewModels<VM> {
    val application = requireActivity().application as App
    val screen = requireArguments().getParcelable<BaseScreen>(ARG_SCREEN) as BaseScreen

    // using Providers API directly for getting MainViewModel instance
    val provider = ViewModelProvider(requireActivity(), AndroidViewModelFactory(application))
    val mainViewModel = provider[MainViewModel::class.java]

    // forming the list of available dependencies:
    // - screen ViewModel scope dependencies -> screen args
    // - activity ViewModel scope dependencies -> from MainViewModel
    // - singleton scope dependencies (repositories) -> from App class
    val dependencies = listOf(screen, mainViewModel) + application.models

    // creating factory
    ViewModelFactory(dependencies, this)
}

class ViewModelFactory(
    private val dependencies: List<Any>,
    owner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        val constructors = modelClass.constructors
        val constructor = constructors.maxByOrNull { it.typeParameters.size }!!

        // - SavedStateHandle is also a dependency from screen VM scope, but we can obtain it only here,
        //   that's why merging it with the list of other dependencies:
        val dependenciesWithSavedState = dependencies + handle

        // generating the list of arguments to be passed into the view-model's constructor
        val arguments = findDependencies(constructor, dependenciesWithSavedState)

        // creating view-model
        return constructor.newInstance(*arguments.toTypedArray()) as T
    }

    private fun findDependencies(constructor: Constructor<*>, dependencies: List<Any>): List<Any> {
        val args = mutableListOf<Any>()
        // here we iterate through view-model's constructor arguments and for each
        // argument we search dependency that can be assigned to the argument
        constructor.parameterTypes.forEach { parameterClass ->
            val dependency = dependencies.first { parameterClass.isAssignableFrom(it.javaClass) }
            args.add(dependency)
        }
        return args
    }

}