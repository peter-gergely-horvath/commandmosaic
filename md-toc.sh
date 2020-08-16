#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# set and IFS was taken from Microsoft Azure CLI templates.
# -e: immediately exit if any command has a non-zero exit status
# -o: prevents errors in a pipeline from being masked
# IFS new value is less likely to cause confusing bugs when looping arrays or arguments (e.g. $@)

die () {
    echo >&2 "$@"
    exit 1
}

usage() {
    echo -e "\nScript to generate Table Of Contents for a MarkDown file"
    echo "Usage: $0 <markdown file>" 1>&2;
    exit 1;
}

#######################################
# ENTRY POINT

if [ "$#" -ne 1 ]; then
    echo "One argument, the MarkDown file to process is expected"
    usage
fi

filename=$1
while read line; do
  if [[ $line =~ ^(#{1,2})[^#](.+)(\r|\n)*$ ]]; then

    level_indicator="${BASH_REMATCH[1]}"
    title="${BASH_REMATCH[2]}"
    title="$(echo -e "${title}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
    ref="$(echo -e "${title}" | sed -e 's/[[:space:]]/-/g' -e 's/\.//g' | tr -cd '[:alnum:]._-' |  tr '[:upper:]' '[:lower:]' )"

    indent=${level_indicator//#/  }

    echo "$indent* [$title](#$ref)"

  fi
done < "$filename"