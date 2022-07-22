#!groovy

pipeline {
    agent any
    stages {
        stage('Build') {
            build()
        }
        stage('Test') {
            steps {
                runTest()
            }
        }
//        stage('Deploy') {
//            steps {
//                //
//            }
//        }
    }
}

def build() {
    sh "${gradle} clean build -x test"
}

def runTest() {
    sh "${gradle} test"
}

//
//appName = "regulatory-dimp-document-processor"
//
//gradle = "./gradlew --no-daemon"
//
//node("java11") {
//    withEnv(["JAVA_OPTIONS=Xmx100M -Duser.timezone=America/Sao_Paulo", "TZ=America/Sao_Paulo", "HTTP_PROXY=http://pagseguro.proxy.srv.intranet:80", "HTTPS_PROXY=http://pagseguro.proxy.srv.intranet:80"]) {
//
//        setupJava()
//
//        stage("Clean Workspace") {
//            deleteDir()
//        }
//
//        def branchName = checkout()
//        def projectRelease = getProjectRelease(branchName)
//
//        if (ignoreBuild()) {
//            this.notifyGithub(env, "success")
//            return
//        }
//
//        try {
//            stage("Build") {
//                build()
//            }
//
//            stage("Test") {
//                runTest()
//            }
//
//            stage("Sonar") {
//                runSonar(branchName)
//            }
//
//            stage("Fortify") {
//                runFortify(branchName)
//            }
//
//            if (branchName == 'develop' || branchName == 'master') {
//                def version = projectRelease.getCurrentVersion()
//                currentBuild.description = "Versao: " + version
//                currentBuild.displayName = "${version}"
//
//                if (branchName == 'develop') {
//                    stage("Release") {
//                        release(projectRelease)
//                    }
//
//                    stage("Deploy QA") {
//                        deployQA(version)
//                    }
//
//                    slack.sendMessage(slackChannel, "O Job ${env.JOB_NAME} em QA terminou com sucesso. :beers:")
//
//                } else if (branchName == 'master') {
//                    stage('Promote Docker Image') {
//                        promote(projectRelease)
//                    }
//
//                    stage("Deploy PROD") {
//                        deployPROD(version)
//                    }
//
//                    stage("Merge back") {
//                        mergeBack()
//                    }
//                }
//
//                def tagRelease = "${env.JOB_NAME}-${version}"
//
//                slack.sendMessage(slackChannel, "O Job ${env.JOB_NAME}-${tagRelease} terminou com sucesso. :beers:")
//            }
//
//            this.notifyGithub(env, "success")
//
//        } catch (error) {
//            this.notifyGithub(env, "failure")
//            throw (error)
//        }
//    }
//}
//
//def setupJava() {
//    def javaHome = tool 'java11'
//    env.PATH = "${javaHome}/bin:${env.PATH}"
//    env.JAVA_HOME = "${javaHome}"
//}
//
//def build() {
//    sh "${gradle} clean build -x test"
//    // stash name: 'source'
//}
//
//def runTest() {
//    sh "${gradle} test"
//}
//
//def runSonar(String branchName) {
//    withSonarQubeEnv("sonarServerMesos") {
//        if (branchName == "develop" || branchName == "master") {
//            sh "${gradle} sonarqube"
//        } else {
//            sh "${gradle} -Dsonar.branch.name=${branchName} sonarqube"
//        }
//    }
//
//    timeout(time: 5, unit: "MINUTES") {
//        def qg = waitForQualityGate()
//
//        if (qg.status != "OK" && !skipQualityGate) {
//            error "Pipeline abortado devido a falha no quality gate: ${qg.status}"
//        }
//    }
//}
//
//def runFortify(String branchName) {
//    build job: '/COMMONS/fortify', parameters: [
//        string(name: 'PROJECT_KEY', value: 'ps-psp'),
//        string(name: 'REPOSITORY', value: 'regulatory-dimp-document-processor'),
//        string(name: 'BRANCH', value: branchName),
//        string(name: 'LANGUAGE', value: 'java')
//    ], wait: true
//}
//
//def release(ProjectRelease projectRelease) {
//
//    def currentLongVersion = projectRelease.getCurrentVersion().split('[-]')
//    def currentShortVersion = currentLongVersion[0]
//    def newShortVersion = projectRelease.incrementVersion(currentLongVersion[0])
//    def newLongVersion = newShortVersion
//
//    // Remove o SNAPSHOT, caso tenha
//    if (currentLongVersion.size() > 1) {
//        projectRelease.updateGradleVersion(currentShortVersion)
//        newLongVersion = newShortVersion + "-" + currentLongVersion[1]
//    }
//
//    // Atualiza todos os jsons de /deploy com a nova versão da imagem
//    projectRelease.updateDockerVersion(currentShortVersion)
//
//    // Faz o build e push da imagem docker
//    withCredentials([usernamePassword(credentialsId: 'svcacc_ps_jenkins', passwordVariable: 'dpass', usernameVariable: 'duser')]) {
//        sh "docker build -t repo.intranet.pags/psp-docker/${appName}:${currentShortVersion} ."
//        sh "docker login -u ${env.duser} -p ${env.dpass} repo.intranet.pags"
//        sh "docker push repo.intranet.pags/psp-docker/${appName}:${currentShortVersion}"
//        sh "docker logout repo.intranet.pags"
//    }
//
//    // Faz um commit com a versao atual e da tag
//    projectRelease.commitVersion(currentShortVersion)
//    projectRelease.commitTag(currentShortVersion)
//
//    // Atualiza o gradle com a nova versao, com ou sem o -SNAPSHOT, dependendo se tinha antes
//    projectRelease.updateGradleVersion(newLongVersion)
//    projectRelease.commitVersion(newLongVersion)
//
//    // Envia todas os commits e a tag pro repositorio
//    projectRelease.pushRepo()
//}
//
//def deployQA(String version) {
//    retry(2) {
//        kubernetesHelper.deployTo(env: 'qa', version: version)
//        mesos.deployTo("qa-aws")
//    }
//}
//
//def deployPROD(String version) {
//    kubernetesHelper.deployTo(env: 'prod', version: version)
//    mesos.deployTo("prod-tb")
//}
//
//def mergeBack() {
//    sh "git reset --hard && git checkout develop"
//    sh "git fetch origin master && git merge origin/master"
//    sh "git push origin HEAD:develop --tags"
//}
//
//def promote(ProjectRelease projectRelease) {
//    def version = projectRelease.getCurrentVersion()
//    jfrogHelper.promote(decrementVersion(version), appName, "psp")
//}
//
//String checkout() {
//    stage("Clone") {
//        def scmVars = checkout scm
//
//        def branchName = scmVars.GIT_BRANCH.toString().replace("origin/", "")
//        echo "My branch is: ${branchName}"
//
//        return branchName
//    }
//}
//
//def ignoreBuild() {
//    String commitMessage = sh(returnStdout: true,
//            script: "git log -n 1 --pretty=format:'%s'")
//    String commitAuthor = sh(returnStdout: true,
//            script: "git log -n 1 --pretty=format:'%ae'")
//
//    if (commitMessage.contains("Automatic merge")) return true
//    if (commitMessage.startsWith("[IGNORE]")) return true
//    if (commitAuthor.contains("svcacc_ps_jenkins")) return true
//
//    return false
//}
//
//def getProjectRelease(String branchName) {
//    return new ProjectRelease(this, branchName)
//}
//
//def notifyGithub(env, status) {
//    try {
//        gitAPI.changeStatus(status, "pr_builder")
//    } catch (error) {
//        echo "Can't update Github PR, skipping (error was ${error})"
//    }
//}
//
//def decrementVersion(oldVersion) {
//    def versionParts = oldVersion.split('[.]')
//
//    int major = Integer.parseInt(versionParts[0])
//    int minor = Integer.parseInt(versionParts[1])
//    int patch = Integer.parseInt(versionParts[2])
//
//    patch = patch - 1
//
//    "${major}.${minor}.${patch}"
//}
