# Checkout branch
```shell
make checkout BRANCH=<BRANCH>
```

# Build environment
```shell
make build-env DB_USERNAME=pb_names DB_PASSWORD=enter_value DB_NAME=enter_value DB_PORT=5555 APP_PORT=8099
```

# Build application
```shell
make build-app DB_USERNAME=pb_names DB_PASSWORD=enter_value DB_NAME=enter_value DB_PORT=5555 APP_PORT=8099
```

# Deploy bot
```shell
make deploy BRANCH=master DB_USERNAME=pb_names DB_PASSWORD=enter_value DB_NAME=enter_value DB_PORT=5555 APP_PORT=8099
```

# Stop bot
```shell
make stop
```