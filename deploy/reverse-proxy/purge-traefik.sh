#!/bin/bash

. <(grep traefik_ ../../src/main/resources/application.properties)

echo "  traefik_container_name=$traefik_container_name"
echo "  traefik_network_name=$traefik_network_name"

docker rm -f "$traefik_container_name"
docker network rm "$traefik_network_name"
