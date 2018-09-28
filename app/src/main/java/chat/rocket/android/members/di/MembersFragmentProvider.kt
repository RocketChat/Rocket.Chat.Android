package chat.rocket.android.members.di

import chat.rocket.android.members.ui.MembersFragment
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.members.ui.MemberBottomSheetFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MembersFragmentProvider {

    @ContributesAndroidInjector(modules = [MembersFragmentModule::class])
    @PerFragment
    abstract fun provideMembersFragment(): MembersFragment

    @ContributesAndroidInjector()
    @PerFragment
    abstract fun provideMemberBottomSheetFragment(): MemberBottomSheetFragment

}