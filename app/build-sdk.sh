#!/bin/bash

CURRENT_DIR=`pwd`
# The SDK dir should be 2 directories up in the tree, so we use dirname 2 times
# to get the common parent dir of the SDK and the app
GIT=`which git`
tmp=`dirname $CURRENT_DIR`
tmp=`dirname $tmp`
SDK_DIR="$tmp/Rocket.Chat.Kotlin.SDK"

echo "CURRENT DIR: $CURRENT_DIR"
echo "SDK DIR: $SDK_DIR"

# check if there are changes not commited
function git_stat {
	local  __resultvar=$1
	cd $SDK_DIR && $GIT diff --shortstat --exit-code
	eval $__resultvar="'$?'"
}

# check for changes already on the index not commited
function git_stat_cached {
	local  __resultvar=$1
	cd $SDK_DIR && $GIT diff --cached --shortstat --exit-code
	eval $__resultvar="'$?'"
}

# get the SHA of the lastest commit
function git_sha {
	temp_sha=`cd $SDK_DIR && $GIT rev-parse --short HEAD`
	echo "$temp_sha"
}

function git_app_branch {
	temp_branch=`cd $CURRENT_DIR && $GIT rev-parse --abbrev-ref HEAD`
	echo "$temp_branch"
}

# check if the tree is dirty (has modifications not commited yet)
function check_git_dirty {
	git_stat stat
	git_stat_cached cached

	if [ $stat -eq 0 ] && [ $cached -eq 0 ]; then
		echo "not dirty"
		return 1
	else
		echo "is dirty"
		return 0
	fi
}

# check if the saved last commit is the same as the latest SHA in the tree
function check_last_commit {
	if [ ! -f $SDK_DIR/.last_commit_hash ]; then
		echo "last_commit_hash not found"
		return 0
	fi
	saved_hash=`cat $SDK_DIR/.last_commit_hash`
	last_hash=$(git_sha)
	#`cd $SDK_DIR && git rev-parse --short HEAD`
	if [ "$saved_hash" == "$last_hash" ]; then
		echo "same hash as before $saved_hash = $last_hash"
		return 1
	fi

	echo "different commits, build again"
	return 0
}

function checkout_matching_branch {
	app_branch=$(git_app_branch)
	cd $SDK_DIR && $GIT checkout $app_branch 1>/dev/null 2>/dev/null
}

checkout_matching_branch

SHA=$(git_sha)
if [ "X$SHA" == "X" ]; then
	SHA="0.1-SNAPSHOT"
fi
echo "CURRENT SHA: $SHA"

# if the tree is not dirty, there is no new commit and the .jars are still there, just skip the build
if ! check_git_dirty && ! check_last_commit && [ -f $CURRENT_DIR/libs/common-$SHA.jar ] && [ -f $CURRENT_DIR/libs/core-$SHA.jar ]; then
	echo "NO BUILD NEEDED"
	exit 0
fi

cd $SDK_DIR && ./gradlew common:assemble && cd $CURRENT_DIR
cd $SDK_DIR && ./gradlew core:assemble && cd $CURRENT_DIR

rm $CURRENT_DIR/libs/common* $CURRENT_DIR/libs/core*

mkdir -p $CURRENT_DIR/libs/
cp -v $SDK_DIR/common/build/libs/common-0.1-SNAPSHOT.jar $CURRENT_DIR/libs/common-$SHA.jar
cp -v $SDK_DIR/core/build/libs/core-0.1-SNAPSHOT.jar $CURRENT_DIR/libs/core-$SHA.jar

echo "$SHA" > $SDK_DIR/.last_commit_hash

exit 0
