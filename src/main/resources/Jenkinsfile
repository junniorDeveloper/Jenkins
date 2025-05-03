pipeline {
    agent any
    tools {
        maven 'Maven3'
    }
    stages {
        stage('Compilar') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        stage('Pruebas de Integración') {
            steps {
                sh 'mvn test'
            }
        }
    }
}
