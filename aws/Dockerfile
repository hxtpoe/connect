FROM java:8
ARG user=hxtpoe
ADD files /
WORKDIR /opt/docker
RUN ["chown", "-R", "daemon", "."]
USER daemon
ENTRYPOINT ["bin/connect"]
CMD []
EXPOSE 9000