#!/usr/bin/env bash

# This script reads the current mod version and minecraft version from the gradle.properties.
# If this tag already exists in git, it will display the current mod version and ask for the new version.
# It then
# * creates a new tag with the form: v1.0.0+1.21.1 (mod-version+minecraft-version)
# * pushes that tag

MOD_VERSION=$(grep 'mod_version=' gradle.properties | cut -d'=' -f2)
MC_VERSION=$(grep 'minecraft_version=' gradle.properties | cut -d'=' -f2)
TAG="v${MOD_VERSION}+${MC_VERSION}"

# Fetch all remote tags
git fetch --tags

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Tag $TAG already exists."
  read -p "Please enter the new mod version: " NEW_MOD_VERSION
  TAG="v${NEW_MOD_VERSION}+${MC_VERSION}"
fi

if [[ $TAG =~ ^v[0-9]+\.[0-9]+\.[0-9]+(-alpha|-beta)?\+[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "$TAG"
  git tag "$TAG"
  git push origin "$TAG"
else
  echo "$TAG: Tag is invalid"
fi

