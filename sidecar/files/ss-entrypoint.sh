#!/bin/bash
source /usr/local/bin/xroad/_entrypoint_common.sh
# Start services
exec /usr/bin/supervisord -n -c /etc/supervisor/supervisord.conf
