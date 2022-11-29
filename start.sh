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

ok "downloading babashka"

if [ -f /home/container/bb ]; then
	ok "babashka already downloaded"
else
    curl -L -O https://github.com/babashka/babashka/releases/download/v1.0.166/babashka-1.0.166-linux-amd64-static.tar.gz
    tar -xvf *.tar.gz
fi

sync


if [ -d /home/container/tf/addons/sourcemod ]; then

	ok "sm already installed"
else

	ok "installing sm";
	source scripts/getmmsm.sh
fi

if [ -f /home/container/tf/maps/mge_chillypunch_final4_fix2.bsp ]; then
	ok "mge map already downloaded"
else
	ok "downloading mge map";
	cd /home/container/tf/maps/;
	curl -L -O "https://github.com/Spaceship-Servers/gameserver-repository/blob/master/tf/maps/mge_chillypunch_final4_fix2.bsp?raw=true";
	mv "mge_chillypunch_final4_fix2.bsp?raw=true" "mge_chillypunch_final4_fix2.bsp";
fi

ok "compiling sourcemod plugins"
cd /home/container/tf/addons/sourcemod/scripting

./compile.sh

cp -f /home/container/tf/addons/sourcemod/scripting/compiled/* \
      /home/container/tf/addons/sourcemod/plugins/



cd /home/container

ok "updating tf2"

steamcmd/steamcmd.sh +force_install_dir ${PWD} +login anonymous +app_update 232250 +exit

ok "./srcds_run $*"

./srcds_run $* & /home/container/bb control.clj
