cat egg.INCOMPLETE.json | jq --rawfile test install.sh '.scripts.installation.script = $test' > egg.json

echo "made egg.json by putting install.sh inside egg.INCOMPLETE.json"

