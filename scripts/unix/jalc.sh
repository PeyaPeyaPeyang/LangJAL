#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/../runtime/bin/java" -jar "$DIR/../cli-a.jar" "$@"
