FROM public.ecr.aws/lambda/java:11
COPY install-browsers.sh /tmp
RUN yum install -v -y unzip tar gzip bzip2 gtk3 alsa-lib-devel libdbusmenu-devel \
    && yum upgrade -y curl libcurl \
    && /tmp/install-browsers.sh
COPY target/pack/lib/* ${LAMBDA_TASK_ROOT}/lib/
COPY target/scala-2.13/test-classes ${LAMBDA_TASK_ROOT}
COPY src/test/resources/testfiles/ ${LAMBDA_TASK_ROOT}/src/test/resources/testfiles/
CMD ["runners.Lambda::handleRequest"]
