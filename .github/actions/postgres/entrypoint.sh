#!/bin/sh

docker_run="docker run"
docker_run="$docker_run -e POSTGRES_DB=$INPUT_POSTGRESQL_DB"
docker_run="$docker_run -e POSTGRES_USER=$INPUT_POSTGRESQL_USER"
docker_run="$docker_run -e POSTGRES_PASSWORD=$INPUT_POSTGRESQL_PASSWORD"

if [ ! -z "$INPUT_DOCKER_NAME" ]
then
  docker_run="$docker_run --name $INPUT_DOCKER_NAME"
fi

if [ ! -z "$INPUT_POSTGRESQL_INIT_SCRIPTS" ]
then
  PWD=`pwd`
  INIT_DB_DIR="$PWD/$INPUT_POSTGRESQL_INIT_SCRIPTS"
  echo "$(ls -l INIT_DB_DIR)"
  
  [ ! -d "$INIT_DB_DIR" ] && echo "WARNING: directory $INIT_DB_DIR DOES NOT exist"

  docker_run="$docker_run -v '$INIT_DB_DIR:/docker-entrypoint-initdb.d'"
fi

docker_run="$docker_run -p 5432:5432 postgres:$INPUT_POSTGRESQL_VERSION"

if [ ! -z "$INPUT_POSTGRES_CONF" ]
then
  docker_run="$docker_run -c '$INPUT_POSTGRES_CONF'"
fi

sh -c "$docker_run"

echo "$docker_run"