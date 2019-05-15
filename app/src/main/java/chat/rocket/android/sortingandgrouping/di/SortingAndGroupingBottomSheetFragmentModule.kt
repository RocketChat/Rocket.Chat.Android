package chat.rocket.android.sortingandgrouping.di

import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.sortingandgrouping.presentation.SortingAndGroupingView
import chat.rocket.android.sortingandgrouping.ui.SortingAndGroupingBottomSheetFragment
import dagger.Module
import dagger.Provides

@Module
class SortingAndGroupingBottomSheetFragmentModule {

    @Provides
    @PerFragment
    fun sortingAndGroupingView(frag: SortingAndGroupingBottomSheetFragment): SortingAndGroupingView =
        frag
}