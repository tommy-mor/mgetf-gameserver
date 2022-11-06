#!/bin/bash
# colors


source scripts/helpers.sh

ok "running custom startup script"

ok "pulling new server from git"
# pull from git repo (which was cloned in install)
# 
git fetch -all
git reset --hard origin/master

sync

# TODO build sourcemod plugins and stuff...

ok "installing metamod/sourcemod"

source scripts/getmmsm.sh

ok "updating tf2"

steamcmd/steamcmd.sh +force_install_dir ${PWD} +login anonymous +app_update 232250 +exit

ok "./srcds_run $*"

./srcds_run $*

