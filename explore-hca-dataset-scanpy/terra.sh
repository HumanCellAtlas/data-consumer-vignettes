#!/bin/bash

usermod -aG sudo jupyter-user
echo "%sudo   ALL=(ALL:ALL) ALL" >> /etc/sudoers
echo "%sudo ALL=NOPASSWD: ALL" >> /etc/sudoers
