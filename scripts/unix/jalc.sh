#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"

CP="$DIR/../lib/*"
"$DIR/../runtime/bin/java" -cp "$CP" tokyo.peya.langjal.cli.Main "$@"
