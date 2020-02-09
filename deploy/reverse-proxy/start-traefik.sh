#!/bin/bash

. <(grep traefik_ ../../src/main/resources/application.properties)

echo "  traefik_image_name=$traefik_image_name"
echo "  traefik_container_name=$traefik_container_name"
echo "  traefik_network_name=$traefik_network_name"
echo "  traefik_dashboard_host_port=$traefik_dashboard_host_port"

docker network create "$traefik_network_name" --attachable

docker run -d -p 80:80 -p "$traefik_dashboard_host_port":8080 \
  --name "$traefik_container_name" \
  --network "$traefik_network_name" \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  "$traefik_image_name" --api --docker --docker.watch --api.statistics --api.statistics.recenterrors=32
