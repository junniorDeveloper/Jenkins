pipeline {
    agent any
    tools {
        tool  'Maven'
    }
    stages {
        stage('Compilar') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        stage('Pruebas Usando Jenkins') {
            steps {
                sh 'mvn test'
            }
        }
    }
}
