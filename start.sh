#!/bin/bash

# Pull new changes
git pull

# Checkout to needed git branch
git checkout $1

# Add env vars to .env config file
echo "$2" >> ./build/.env
echo "$3" >> ./build/.env
echo "$4" >> ./build/.env
echo "$5" >> ./build/.env
echo "$6" >> ./build/.env

# Ensure, that docker-compose stopped
docker-compose --env-file ./build/.env stop

# Start new deployment with provided env vars in ./target/.env file
docker-compose --env-file ./build/.env up --build -d