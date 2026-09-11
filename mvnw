#!/usr/bin/env sh
set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPS="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

distribution_url=$(sed -n 's/^distributionUrl=//p' "$PROPS" | head -n 1)
expected_sha=$(sed -n 's/^distributionSha256Sum=//p' "$PROPS" | head -n 1)
file_name=$(basename "$distribution_url")
maven_dir_name=${file_name%-bin.zip}
wrapper_dir="$BASE_DIR/.mvn/wrapper"
zip_path="$wrapper_dir/$file_name"
maven_home="$wrapper_dir/$maven_dir_name"

if [ ! -x "$maven_home/bin/mvn" ]; then
  mkdir -p "$wrapper_dir"
  if [ ! -f "$zip_path" ]; then
    echo "Downloading Maven $maven_dir_name..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "$distribution_url" -o "$zip_path"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "$distribution_url" -O "$zip_path"
    else
      echo "curl or wget is required to download Maven." >&2
      exit 1
    fi
  fi

  if [ -n "$expected_sha" ] && command -v sha256sum >/dev/null 2>&1; then
    actual_sha=$(sha256sum "$zip_path" | awk '{print $1}')
    if [ "$actual_sha" != "$expected_sha" ]; then
      rm -f "$zip_path"
      echo "Maven distribution checksum mismatch." >&2
      exit 1
    fi
  fi

  tmp_dir="$wrapper_dir/$maven_dir_name.tmp"
  rm -rf "$tmp_dir"
  mkdir -p "$tmp_dir"
  unzip -q "$zip_path" -d "$tmp_dir"
  mv "$tmp_dir/$maven_dir_name" "$wrapper_dir/"
  rm -rf "$tmp_dir"
fi

exec "$maven_home/bin/mvn" "$@"
