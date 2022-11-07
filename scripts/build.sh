#!/bin/bash
# script that builds the sm plugin, and also sends it to server

source .env

#DONt forget to escape any dollar signs in the password!!
export LFTP_PASSWORD="$SFTP_PASSWORD"

lftp --env-password sftp://${SFTP_USERNAME}@mge.tf:2022

export LFTP_PASSWORD=""
