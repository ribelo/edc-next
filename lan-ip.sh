#!/bin/bash
echo `ip route get 8.8.8.8 | tr -s ' ' | head -n 1 | cut -d ' ' -f 7` > .lan-ip
