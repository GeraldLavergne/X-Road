#!/bin/bash
source /usr/local/bin/xroad/_entrypoint_common.sh
exec /usr/bin/supervisord -n -c /etc/supervisor/supervisord.conf
