package chat.rocket.android.sortingandgrouping.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.sortingandgrouping.ui.SortingAndGroupingBottomSheetFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SortingAndGroupingBottomSheetFragmentProvider {

    @ContributesAndroidInjector(modules = [SortingAndGroupingBottomSheetFragmentModule::class])
    @PerFragment
    abstract fun provideSortingAndGroupingBottomSheetFragment(): SortingAndGroupingBottomSheetFragment

}