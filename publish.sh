#!/usr/bin/env bash
set -e

PROJECT_ROOT=`pwd`

DOCS_SOURCE="$PROJECT_ROOT/docs"

BUILD_ROOT="$PROJECT_ROOT/build/gh-pages"
REPOSITORY_ROOT="$BUILD_ROOT/repository"
GENERATE_ROOT="$BUILD_ROOT/generate"

echo "Clearing build directory $BUILD_ROOT"
rm -rf $BUILD_ROOT

mkdir $BUILD_ROOT

echo cloning git into build directory
git clone https://github.com/mplatvoet/kovenant-site.git $REPOSITORY_ROOT
cd $REPOSITORY_ROOT
git checkout gh-pages


cd $DOCS_SOURCE
mkdocs build
mv $DOCS_SOURCE/site $GENERATE_ROOT

diff -qr $REPOSITORY_ROOT $GENERATE_ROOT --exclude .git --exclude .DS_Store| awk -v base="$REPOSITORY_ROOT" '$1=="Only"&&$3==base":" {print base"/"$4}' | sort -r | awk '{cmd="git rm " $1; system(cmd)}'


cp -rf $GENERATE_ROOT/* $REPOSITORY_ROOT

cd $REPOSITORY_ROOT
if [ -z "$PUBLISH_KEY" ]; then
    echo "local publish"
else
    git config credential.https://github.com/mplatvoet/kovenant-site.$NAME $PUBLISH_KEY
    git config user.name "$NAME"
    git config user.email $EMAIL

fi
git add .
git status
git commit -m "auto publish"
git push