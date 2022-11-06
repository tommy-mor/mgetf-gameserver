cat egg.INCOMPLETE.json | jq --rawfile test install.TOBEINSERTED.sh '.scripts.installation.script = $test' > egg.json

echo "made egg.json by putting install.TOBEINSERTED.sh inside egg.INCOMPLETE.json"

