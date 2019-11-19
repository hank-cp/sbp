#!/bin/bash
set -e

psql -c 'create database "sbp";' -U postgres
psql -c 'create database "sbp-test";' -U postgres