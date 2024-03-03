#!/bin/bash

# Pull new changes
git pull

# Checkout to needed git branch
git checkout $1

# Add env vars to .env config file
echo "$2" >> ./.env
echo "$3" >> ./.env
echo "$4" >> ./.env
echo "$5" >> ./.env
echo "$6" >> ./.env

# Ensure, that docker-compose stopped
docker-compose --env-file ./.env stop

# Start new deployment with provided env vars in ./.env file
docker-compose --env-file ./.env up --build -d