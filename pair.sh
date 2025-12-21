#!/bin/bash

if [ $# -ne 2 ]; then
  echo "Usage: $0 <last_ip_octet> <port_number>"
  exit 1
fi

LAST_IP_OCTET=$1
PORT_NUMBER=$2

IP_ADDRESS="192.168.2.$LAST_IP_OCTET"
adb connect $IP_ADDRESS:$PORT_NUMBER
