pipeline {
    agent any
    
    environment {
        // DockerHub credentials ID from Jenkins (you need to create this)
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        // Change this to YOUR DockerHub username
        DOCKER_IMAGE = 'ammarim/timesheet'
        K8S_NAMESPACE = 'chap4'
        K8S_DEPLOYMENT = 'timesheet-dep'
    }
    
    stages {
        stage('GIT') {
            steps {
                echo 'Step 1: Cloning source code from GitHub...'
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/mariemammari/Devops_Esprit_TP.git'
                    ]]
                ])
                
                // Verify we got the code
                sh 'ls -la'
                sh 'cat Jenkinsfile | head -5'
            }
        }
        
        stage('COMPILATION') {
            steps {
                echo 'Step 2: Building the project...'
                script {
                    // For workshop purposes - create a simple JAR if none exists
                    sh '''
                        if [ -f "pom.xml" ]; then
                            echo "Maven project found"
                            mvn clean compile
                        elif [ -f "build.gradle" ]; then
                            echo "Gradle project found"
                            ./gradlew clean build
                        else
                            echo "Creating test JAR for workshop..."
                            mkdir -p target
                            cd target
                            echo "public class TestApp { public static void main(String[] args) { System.out.println(\\"Timesheet App v1.1 - Workshop Chap4\\"); try { Thread.sleep(300000); } catch (Exception e) {} } }" > TestApp.java
                            javac TestApp.java
                            jar cfe app.jar TestApp TestApp.class
                            cd ..
                            ls -la target/
                        fi
                    '''
                }
            }
        }
        
        stage('INSTALLATION') {
            steps {
                echo 'Step 3: Building and pushing Docker image v1.1...'
                script {
                    // Login to DockerHub
                    sh """
                        echo "Logging into DockerHub..."
                        echo \${DOCKERHUB_CREDENTIALS_PSW} | docker login -u \${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                    """
                    
                    // Build Docker images
                    sh "docker build -t \${DOCKER_IMAGE}:1.1 ."
                    sh "docker build -t \${DOCKER_IMAGE}:latest ."
                    
                    // Push to DockerHub
                    sh "docker push \${DOCKER_IMAGE}:1.1"
                    sh "docker push \${DOCKER_IMAGE}:latest"
                    
                    // Verify
                    sh "docker images | grep timesheet"
                }
            }
        }
        
        stage('DEPLOIEMENT') {
            steps {
                echo 'Step 4: Deploying to Kubernetes...'
                script {
                    // Update Kubernetes deployment with new image
                    sh "kubectl config use-context minikube"
                    sh "kubectl set image deployment/\${K8S_DEPLOYMENT} timesheet=\${DOCKER_IMAGE}:1.1 -n \${K8S_NAMESPACE}"
                    
                    // Wait for rollout
                    sh "kubectl rollout status deployment/\${K8S_DEPLOYMENT} -n \${K8S_NAMESPACE} --timeout=120s"
                    
                    // Verify
                    sh "kubectl get pods -n \${K8S_NAMESPACE}"
                    sh "kubectl describe deployment/\${K8S_DEPLOYMENT} -n \${K8S_NAMESPACE} | grep -A5 'Image:'"
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed. Cleaning up...'
            sh 'docker logout'
        }
        success {
            echo 'SUCCESS: Pipeline executed successfully!'
            sh """
                echo "=== Application Status ==="
                kubectl get all -n \${K8S_NAMESPACE}
                echo "=== Service URL ==="
                minikube service timesheet-serv -n \${K8S_NAMESPACE} --url || echo "Service not available"
            """
        }
        failure {
            echo 'FAILURE: Pipeline failed!'
            sh """
                echo "=== Error Details ==="
                kubectl get pods -n \${K8S_NAMESPACE}
                kubectl describe deployment/\${K8S_DEPLOYMENT} -n \${K8S_NAMESPACE} | tail -20
                kubectl get events -n \${K8S_NAMESPACE} --sort-by='.lastTimestamp' | tail -10
            """
        }
    }
}
