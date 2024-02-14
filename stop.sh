#!/bin/bash

# Ensure, that docker-compose stopped
docker-compose --env-file ./build/.env stop

# Ensure, that the old application won't be deployed again.
gradle clean