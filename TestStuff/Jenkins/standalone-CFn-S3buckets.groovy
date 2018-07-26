pipeline {

    agent any

    options {
        buildDiscarder(
            logRotator(
                numToKeepStr: '5',
                daysToKeepStr: '30',
                artifactDaysToKeepStr: '30',
                artifactNumToKeepStr: '5'
            )
        )
        disableConcurrentBuilds()
        timeout(time: 5, unit: 'MINUTES')
    }

    environment {
        AWS_DEFAULT_REGION = "${AwsRegion}"
        AWS_CA_BUNDLE = '/etc/pki/tls/certs/ca-bundle.crt'
        REQUESTS_CA_BUNDLE = '/etc/pki/tls/certs/ca-bundle.crt'
    }

    parameters {
        string(name: 'AwsRegion', defaultValue: 'us-east-1', description: 'Amazon region to deploy resources into')
        string(name: 'AwsCred', description: 'Jenkins-stored AWS credential with which to execute cloud-layer commands')
        string(name: 'GitCred', description: 'Jenkins-stored Git credential with which to execute git commands')
        string(name: 'GitProjUrl', description: 'SSH URL from which to download the Artifactory git project')
        string(name: 'GitProjBranch', description: 'Project-branch to use from the Artifactory git project')
        string(name: 'CfnStackRoot', description: 'Unique token to prepend to all stack-element names')
        string(name: 'BackupBucketName', defaultValue: '', description: '')
        string(name: 'BackupBucketInventoryTracking', defaultValue: 'false', description: '')
        string(name: 'FinalExpirationDays', defaultValue: '30', description: '')
        string(name: 'BackupReportingBucket', defaultValue: '', description: '')
        string(name: 'RetainIncompleteDays', defaultValue: '3', description: '')
        string(name: 'TierToGlacierDays', defaultValue: '5', description: '')
        string(name: 'ShardBucketName', defaultValue: '', description: '')
        string(name: 'ShardBucketInventoryTracking', defaultValue: 'false', description: '')
        string(name: 'ShardReportingBucket', defaultValue: '', description: '')

    }

    stages {
        stage ('Prepare Agent Environment') {
            steps {
                deleteDir()
                git branch: "${GitProjBranch}",
                    credentialsId: "${GitCred}",
                    url: "${GitProjUrl}"
                writeFile file: 'S3Bucket.parms.json',
                    text: /
                         [
                             {
                                 "ParameterKey": "BackupBucketName",
                                 "ParameterValue": "${env.BackupBucketName}"
                             },
                             {
                                 "ParameterKey": "BackupBucketInventoryTracking",
                                 "ParameterValue": "${env.BackupBucketInventoryTracking}"
                             },
                             {
                                 "ParameterKey": "FinalExpirationDays",
                                 "ParameterValue": "${env.FinalExpirationDays}"
                             },
                             {
                                 "ParameterKey": "BackupReportingBucket",
                                 "ParameterValue": "${env.BackupReportingBucket}"
                             },
                             {
                                 "ParameterKey": "RetainIncompleteDays",
                                 "ParameterValue": "${env.RetainIncompleteDays}"
                             },
                             {
                                 "ParameterKey": "TierToGlacierDays",
                                 "ParameterValue": "${env.TierToGlacierDays}"
                             },
                             {
                                 "ParameterKey": "ShardBucketName",
                                 "ParameterValue": "${env.ShardBucketName}"
                             },
                             {
                                 "ParameterKey": "ShardBucketInventoryTracking",
                                 "ParameterValue": "${env.ShardBucketInventoryTracking}"
                             },
                             {
                                 "ParameterKey": "ShardReportingBucket",
                                 "ParameterValue": "${env.ShardReportingBucket}"
                             }
                         ]
                   /
            }
        }
        stage ('Prepare AWS Environment') {
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to delete any active ${CfnStackRoot}-S3Res stacks... "
                        aws --region "${AwsRegion}" cloudformation delete-stack --stack-name "${CfnStackRoot}-S3Res" 

                        sleep 5

                        # Pause if delete is slow
                        while [[ $(
                                    aws cloudformation describe-stacks \
                                      --stack-name ${CfnStackRoot}-S3Res \
                                      --query 'Stacks[].{Status:StackStatus}' \
                                      --out text 2> /dev/null | \
                                    grep -q DELETE_IN_PROGRESS
                                   )$? -eq 0 ]]
                        do
                           echo "Waiting for stack ${CfnStackRoot}-S3Res to delete..."
                           sleep 30
                        done
                    '''
                }
            }
        }
        stage ('Launch S3 Stack') {
            steps {
                withCredentials(
                    [
                        [$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: "${AwsCred}", secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'],
                        sshUserPrivateKey(credentialsId: "${GitCred}", keyFileVariable: 'SSH_KEY_FILE', passphraseVariable: 'SSH_KEY_PASS', usernameVariable: 'SSH_KEY_USER')
                    ]
                ) {
                    sh '''#!/bin/bash
                        echo "Attempting to create stack ${CfnStackRoot}-S3Res..."
                        aws --region "${AwsRegion}" cloudformation create-stack --stack-name "${CfnStackRoot}-S3Res" \
                          --disable-rollback --capabilities CAPABILITY_NAMED_IAM \
                          --template-body file://Templates/make_artifactory-EE_S3-buckets.tmplt.json \
                          --parameters file://S3Bucket.parms.json
 
                        sleep 15
 
                        # Pause if create is slow
                        while [[ $(
                                    aws cloudformation describe-stacks \
                                      --stack-name ${CfnStackRoot}-S3Res \
                                      --query 'Stacks[].{Status:StackStatus}' \
                                      --out text 2> /dev/null | \
                                    grep -q CREATE_IN_PROGRESS
                                   )$? -eq 0 ]]
                        do
                           echo "Waiting for stack ${CfnStackRoot}-S3Res to finish create process..."
                           sleep 30
                        done
 
                        if [[ $(
                                aws cloudformation describe-stacks \
                                  --stack-name ${CfnStackRoot}-S3Res \
                                  --query 'Stacks[].{Status:StackStatus}' \
                                  --out text 2> /dev/null | \
                                grep -q CREATE_COMPLETE
                               )$? -eq 0 ]]
                        then
                           echo "Stack-creation successful"
                        else
                           echo "Stack-creation ended with non-successful state"
                           exit 1
                        fi
                    '''
                }
            }
        }
    }
}
