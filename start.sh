#!/bin/bash
# colors

GREEN=$(tput setaf 2)
ok()
{
    echo "${GREEN}[OK] ${1} ${RESET}"
}

ok "running custom startup script"

ok "pulling new server from git"

ls -al .
# pull from git repo (which was cloned in install)


ok "downloading my server config from git"

mkdir -p /home/container/.addons

cd /home/container/.addons
curl -L -O https://github.com/tommy-mor/mgetf-gameserver/archive/refs/heads/main.tar.gz
tar -xvf main.tar.gz 

cp -rfl /home/container/.addons/mgetf-gameserver-main/* /home/container/
# rm -rf /home/container/.addons

cd /home/container

sync

# TODO build sourcemod plugins and stuff...

ok "installing metamod/sourcemod"

source scripts/getmmsm.sh

cd /home/container

ok "updating tf2"

steamcmd/steamcmd.sh +force_install_dir ${PWD} +login anonymous +app_update 232250 +exit

ok "./srcds_run $*"

./srcds_run $*

