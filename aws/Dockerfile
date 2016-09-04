FROM java:8
ARG user=hxtpoe
ADD files /
WORKDIR /opt/docker
RUN ["chown", "-R", "daemon", "."]
USER daemon
ENV JAVA_OPTS="-XX:PermSize=3072m -XX:MaxPermSize=2048m"
ENTRYPOINT ["bin/connect"]
CMD []
EXPOSE 9000