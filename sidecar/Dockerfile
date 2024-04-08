ARG VERSION=7.4.2
ARG TAG=niis/xroad-security-server-sidecar
FROM $TAG:$VERSION-slim
RUN mv /usr/share/xroad/jlib/addon/proxy/messagelog.conf.disabled /usr/share/xroad/jlib/addon/proxy/messagelog.conf  \
  && mv /usr/share/xroad/jlib/addon/proxy/opmonitoring.conf.disabled /usr/share/xroad/jlib/addon/proxy/opmonitoring.conf \
  && mv /usr/share/xroad/jlib/addon/proxy/proxymonitor-service.conf.disabled /usr/share/xroad/jlib/addon/proxy/proxymonitor-service.conf

RUN chgrp -R 0 /root && \
    chmod -R g=u /root

COPY files/ss-entrypoint.sh /root/entrypoint.sh
COPY --chown=root:root files/ss-xroad.conf /etc/supervisor/conf.d/xroad.conf
CMD ["/root/entrypoint.sh"]
