# Launching Stacks Using the AWS CLI

This project includes a number of files in the `Templates` subdirectory. Attempts at "obviousness" have been made in order to help the user locate the correct templates for the elements they wish to deploy. In general:

* Files starting `make_artifactory-PRO_` are geared towards deploying Artifactory Pro
* Files starting `make_artifactory-EE_` are geared towards deploying Artifactory Enterprise Edition
* The first field afer the `Pro_` or `EE_` indicates what resources the template is designed to deploy:
    * _SG:_ Creates the security groups used by the networked Artifactory elements
    * _S3:_ Creates the S3 buckets used by the Artifactory EC2 hosts for storing things like daily backups and artifacts
    * _IAM:_ Creates the AWS IAM instance-roles used by the Artifactory EC2 hosts to gain permissions to relevant AWS resources
    * _EFS:_ Creates the EFS shares used by EFS-enabled EC2 hosts
    * _ELBv2:_ Creates the user-facing Elastic Load Balance that provides transit-service for "public" clients to access the EC2 resources hosted in private subnets.
    * _RDS:_ Creates the RDS-managed PostGreSQL database used to store Artifactory's configuration and artifact tracking-data
    * _EC2:_ Creates the EC2 nodes on which the Artifactory application is installed and configured.
    * _parent:_ This is a special template that acts as a parent of a group of linked-/nested-stacks. The filename provides some additional clues to the "parent" intended use-case, but a review of the parent's `Description` field will provide further details

When launching templates from the command line, it will be necessary to supply all of the parameter-values expected by the template being launched. While some parameters do not need to be defined &mdash; either because they have defaults in the template or have been explicitly defined as optional &mdash; it is still a good idea to define the parameter-values.

While it's possible to specify the parameters as a valid JSON string on the command-line, the recommended method is to use a parameters file. A parameters file will look similar to:

~~~
[
    {
        "ParameterKey": "<PARAMETER_1_NAME>",
        "ParameterValue": "<PARAMETER_1_VALUE>"
    },
    {
        "ParameterKey": "<PARAMETER_2_NAME>",
        "ParameterValue": "<PARAMETER_2_VALUE>"
    },

...

    {
        "ParameterKey": "<PARAMETER_N_NAME>",
        "ParameterValue": "<PARAMETER_N_VALUE>"
    }
]
~~~

To more clearly illustrate, see this example of the parameters-file to be used for the security-group template:

~~~
[
    {
        "ParameterValue": "subnet-cf103d24,subnet-80148835,subnet-eef61fc9",
        "ParameterKey": "EfsSubnet"
    },
    {
        "ParameterValue": "sg-0cc8628e5d275b4f3",
        "ParameterKey": "EfsSg"
    }
]
~~~

A stack may be launched from the template and parameters file using one of two methods:

* `aws cloudformation create-stack --stack-name <STACK_NAME> --template-body file:///FULLY-QUALIFIED/PATH/TO/TEMPLATE/FILE --parameters  file:///FULLY-QUALIFIED/PATH/TO/PARAMETERS/FILE`
* `aws cloudformation create-stack --stack-name <STACK_NAME> --template-url https://S3_ENDPOINT/TOOL_BUCKET/FULLY-QUALIFIED/PATH/TO/TEMPLATE/FILE --parameters  file:///FULLY-QUALIFIED/PATH/TO/PARAMETERS/FILE`

Both methods are suitable for templates that are less than about 50KiB in size. Only the latter is suitable for templates that are larger than about 50KiB in size.

Assuming that the template and the parameters files were both valid, the `aws cloudformation ...` command will return with an output similar to:

~~~
{
    "StackId": "arn:aws:cloudformation:us-east-1:XXXXXXXXXXXX:stack/EfsTst01/a3989b20-9574-11e8-b9c6-500c17b12cd2"
}
~~~
