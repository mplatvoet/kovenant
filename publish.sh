#!/usr/bin/env bash

echo cleaning build directory
rm -rf gh-pages

echo cleaning site directory
rm -rf docs/site

echo cloning git into build directory
git clone https://github.com/mplatvoet/kovenant.git gh-pages
cd gh-pages
git checkout gh-pages
cd ..

cd docs
mkdocs build
cd ..
cp -rf docs/site/ gh-pages/

cd gh-pages
git add .
git status
git commit -m "auto publish"
git push

cd ..
echo cleaning build directory
rm -rf gh-pages

echo cleaning site directory
rm -rf site
echo done