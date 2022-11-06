#!/bin/bash
# SRCDS Base Installation Script
#
# Server Files: /mnt/server
dpkg --add-architecture i386
apt -y update
apt -y --no-install-recommends install curl ca-certificates git gnutls-bin libsdl2-2.0-0:i386 git-lfs
shopt -s dotglob nullglob


cd /tmp
curl -sSL -o steamcmd.tar.gz http://media.steampowered.com/installer/steamcmd_linux.tar.gz
mkdir -p /mnt/server/steamcmd
tar -xzvf steamcmd.tar.gz -C /mnt/server/steamcmd
cd /mnt/server/steamcmd

#
# SteamCMD fails otherwise for some reason, even running as root.
# This is changed at the end of the install process anyways.
#
chown -R root:root /mnt
export HOME=/mnt/server
./steamcmd.sh +login anonymous +force_install_dir /mnt/server +app_update 232250 +quit
mkdir -p /mnt/server/.steam/sdk32
cp -v linux32/steamclient.so ../.steam/sdk32/steamclient.so


sync
# git config pull.rebase false
# git fetch --all
# git checkout ${SERVER_BRANCH}
# git pull origin ${SERVER_BRANCH}
# k now copy and delete the folders

cp -rl /mnt/server/.addons/* /mnt/server &>/dev/null
rm -rf /mnt/server/.addons


cd /mnt/server
chmod 744 /mnt/server/start.sh
sync
