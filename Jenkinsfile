#!groovy
build('wb-list-manager', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def pipeJavaServiceInsideDocker
    runStage('load JavaService pipeline') {
        javaServicePipeline = load("build_utils/jenkins_lib/pipeJavaServiceInsideDocker.groovy")
    }

    def serviceName = env.REPO_NAME
    def mvnArgs = '-DjvmArgs="-Xmx256m"'
    def useJava11 = true

    pipeJavaServiceInsideDocker(serviceName, mvnArgs)
}
