FROM openjdk:19-jdk-alpine3.16
WORKDIR /home/e2e
RUN apk add --no-cache wget bash git  && \
    adduser -D -h . e2e
USER e2e
RUN wget -qq https://github.com/sbt/sbt/releases/download/v1.6.2/sbt-1.6.2.tgz && \
    tar -xzf sbt-1.6.2.tgz && \
    git clone https://github.com/nationalarchives/tdr-e2e-tests.git && \
    cd tdr-e2e-tests && \
    ../sbt/bin/sbt compile
USER root
RUN apk add python3
USER e2e
CMD cd tdr-e2e-tests && \
    git checkout $BRANCH_NAME && \
    git pull && \
    ../sbt/bin/sbt test -Dconfig.file=./src/test/resources/application.conf -Dkeycloak.user.admin.secret=$USER_ADMIN_SECRET -Dkeycloak.backendchecks.secret=$BACKEND_CHECKS_SECRET -Dcucumber.features=./src/test/resources/features/Series.feature
