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

if [ ! -z "$INPUT_POSTGRESQL_CONF_FILE" ]
then
  PWD=`pwd`
  CONF_FILE="$PWD/$INPUT_POSTGRESQL_CONF_FILE"
  echo "$(ls -l $CONF_FILE)"
  
  [ ! -d "$CONF_FILE" ] && echo "WARNING: conf file $CONF_FILE DOES NOT exist"

  docker_run="$docker_run -v '$CONF_FILE:/etc/postgresql/postgresql.conf'"
fi

docker_run="$docker_run -d -p 5432:5432 postgres:$INPUT_POSTGRESQL_VERSION"

sh -c "$docker_run"

echo "$(ls -l `pwd`)"

echo "$docker_run"