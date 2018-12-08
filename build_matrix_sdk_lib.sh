#!/bin/bash

echo updir
pushd ..

echo remove sdk folder
rm -rf matrix-android-sdk

echo clone the git folder
git clone -b master https://github.com/matrix-org/matrix-android-sdk 

pushd matrix-android-sdk 

./gradlew clean assembleRelease

popd
popd

cp ../matrix-android-sdk/matrix-sdk/build/outputs/aar/matrix-sdk-release-*.aar app/libs/matrix-sdk.aar 

