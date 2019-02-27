#!/usr/bin/env sh

export RSAPRIVATEKEY=$(cat /run/secrets/private_key)
export RSAPUBLICKEY=$(cat /run/secrets/public_key)
java -Djava.security.egd=file:/dev/./urandom -jar /app.jar
