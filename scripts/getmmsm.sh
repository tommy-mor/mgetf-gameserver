#!/bin/bash

echo "running from git"


mm_url="https://mms.alliedmods.net/mmsdrop/1.12/mmsource-1.12.0-git1164-linux.tar.gz"
sm_url="https://sm.alliedmods.net/smdrop/1.11/sourcemod-1.11.0-git6922-linux.tar.gz"

mm_dest="mm-1.12.1164.tgz"
sm_dest="sm-1.11.6922.tgz"

export destfolder="mmsm_xtracted"


echo "rming tempfolder"

mkdir -p /home/container/.addons
rm -rf /home/container/.addons/${destfolder}
echo "mkdiring new destfolder"
mkdir /home/container/.addons/${destfolder}
echo "cding to destfolder"
cd /home/container/.addons/$destfolder

echo "Curling Metamod Source"
curl "$mm_url" --output "$mm_dest" --limit-rate 4M


echo "Curling SourceMod"
curl "$sm_url" --output "$sm_dest" --limit-rate 4M

echo "Untarring Metamod Source"
tar xf "$mm_dest" # -C "$destfolder"
echo "Untarring Metamod Source"
tar xf "$sm_dest" # -C "$destfolder"

echo "rming tgz"
rm ./*.tgz -f


echo "rming junk"
#rm ./cfg                                    -rfv
# rm ./addons/sourcemod/plugins               -rfv
# rm ./addons/sourcemod/scripting             -rfv
# rm ./addons/sourcemod/configs               -rfv
# this gets linux64 folder too lmao
#find . -name "*x64*"            -exec rm {} -rf    \;
#find . -name "*blade.so"        -exec rm {} -f     \;
#find . -name "*bms.so"          -exec rm {} -f     \;
#find . -name "*css.so"          -exec rm {} -f     \;
#find . -name "*csgo.so"         -exec rm {} -f     \;
#find . -name "*dods.so"         -exec rm {} -f     \;
#find . -name "*doi.so"          -exec rm {} -f     \;
#find . -name "*ep1.so"          -exec rm {} -f     \;
#find . -name "*ep2.so"          -exec rm {} -f     \;
#find . -name "*hl2dm.so"        -exec rm {} -f     \;
#find . -name "*insurgency.so"   -exec rm {} -f     \;
#find . -name "*l4d*.so"         -exec rm {} -f     \;
#find . -name "*nd.so"           -exec rm {} -f     \;
#find . -name "*sdk2013.so"      -exec rm {} -f     \;

echo "moving sm/mm into tf/directory"

cd ..

ls -al 

cp -rf "$destfolder/addons/" /home/container/tf/
cp -rf "$destfolder/cfg/sourcemod" /home/container/tf/cfg

cd ..

#rm -rf .addons/

echo "DONE"
