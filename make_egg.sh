cat egg.json | jq --rawfile test install.sh '.scripts.installation.script = $test'

