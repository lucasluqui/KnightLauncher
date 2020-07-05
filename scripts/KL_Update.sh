#!/usr/bin/env bash
# by Crowfunder
# my gh: https://github.com/Crowfunder
# KnightLauncher gh: https://github.com/lucas-allegri/KnightLauncher

GREEN="\033[0;32m"
NONE="\033[0m"  
RED="\033[0;31m"

# Checking if script was run inside Spiral Knights directory. If not, it asks for inputting game's main directory full path. Broken if somebody somehow has random "rsrc" and "scenes" folders in place where script is running. Too bad.
if [ -d "rsrc" ]; then
    if [ -d "scenes" ]; then
        skpath=$(pwd)
    else
        read -p "Please enter the Spiral Knights main folder absolute path: " skpath
    fi
else
    read -p "Please enter the Spiral Knights main folder absolute path: " skpath
fi

# Simple asking for confirmation, just in case.
echo -e "KnightLauncher will be installed/updated in this folder."
read -p "Would you like to proceed? (Y/N) > " opt

case $opt in
    [yY][eE][sS]|[yY])

        # Checking if other versions are installed, if there are - remove them retaining "KnightLauncher.properties".
        if [ -f "${skpath}/KnightLauncher.jar" ]; then
            echo -e "Detected other version installed, removing..."
            if [ -f "${skpath}/KnightLauncher.properties" ]; then  
                mv "${skpath}/KnightLauncher.properties" "${skpath}/move.properties"
                rm -v "${skpath}/"*KnightLauncher*
                mv "${skpath}/move.properties" "${skpath}/KnightLauncher.properties"
            else
                rm -v "${skpath}/"*KnightLauncher*
            fi
            echo -e "${GREEN}Success!${NONE}"
        fi

        # Downloading and installing new version.
        echo "Downloading..."
        curl -s https://api.github.com/repos/lucas-allegri/KnightLauncher/releases/latest | grep "browser_download_url" | cut -d : -f 2,3 | tr -d \" | wget --show-progress -qi -
        filename=$(curl -s https://api.github.com/repos/lucas-allegri/KnightLauncher/releases/latest | jq '.assets[0].name' | tr -d \")
        echo -e "${GREEN}Successfully downloaded ${filename}${NONE}\nExtracting..."
        mv "${filename}" "${skpath}/${filename}"
        unzip "${skpath}/${filename}" -d "${skpath}"
        rm "${skpath}/${filename}"
        echo -e "${GREEN}Success!${NONE}"
        ;;
esac
exit 0