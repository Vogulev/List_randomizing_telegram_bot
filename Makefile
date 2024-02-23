BRANCH?=master

default:
	echo "command not selected"

stop:
	docker-compose --env-file ./build/.env stop

checkout:
	git checkout $(BRANCH)

build-env:
	rm -f ./build/.env
	echo DB_USERNAME=$(DB_USERNAME) >> ./build/.env
	echo DB_PASSWORD=$(DB_PASSWORD) >> ./build/.env
	echo DB_NAME=$(DB_NAME) >> ./build/.env
	echo DB_PORT=$(DB_PORT) >> ./build/.env
	echo APP_PORT=$(APP_PORT) >> ./build/.env

build-app: build-env
	docker-compose --env-file ./build/.env build

deploy: checkout build-app stop
	docker-compose --env-file ./build/.env up