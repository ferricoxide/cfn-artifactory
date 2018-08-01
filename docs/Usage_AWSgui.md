# Launching Stacks Using the AWS GUI

This project includes a number of files in the `Templates` subdirectory. Attempts at "obviousness" have been made in order to help t
he user locate the correct templates for the elements they wish to deploy. In general:

* Files starting `make_artifactory-PRO_` are geared towards deploying Artifactory Pro
* Files starting `make_artifactory-EE_` are geared towards deploying Artifactory Enterprise Edition
* The first field afer the `Pro_` or `EE_` indicates what resources the template is designed to deploy:
    * _SG:_ Creates the security groups used by the networked Artifactory elements
    * _S3:_ Creates the S3 buckets used by the Artifactory EC2 hosts for storing things like daily backups and artifacts
    * _IAM:_ Creates the AWS IAM instance-roles used by the Artifactory EC2 hosts to gain permissions to relevant AWS resources
    * _EFS:_ Creates the EFS shares used by EFS-enabled EC2 hosts
    * _ELBv2:_ Creates the user-facing Elastic Load Balance that provides transit-service for "public" clients to access the EC2 res
ources hosted in private subnets.
    * _RDS:_ Creates the RDS-managed PostGreSQL database used to store Artifactory's configuration and artifact tracking-data
    * _EC2:_ Creates the EC2 nodes on which the Artifactory application is installed and configured.
    * _parent:_ This is a special template that acts as a parent of a group of linked-/nested-stacks. The filename provides some add
itional clues to the "parent" intended use-case, but a review of the parent's `Description` field will provide further details

## Walk-through

1. Login to the AWS web console (with a user that has CloudFormation privileges)
1. Navigate to the CloudFormation console:
    ![p1](https://user-images.githubusercontent.com/7087031/43525838-d78dc8aa-9570-11e8-8842-52dd800ee5e1.png)
    Click on the `Create Stack` button.
1. At the `Select Template` page, check the `Upload a template to Amazon S3` radio-box, then click on the `Browse...` button:
    ![p2](https://user-images.githubusercontent.com/7087031/43525839-d79a1f1a-9570-11e8-8ae5-7095c5adc2da.png)
    This will pop up a file browser. Navigate to the location on you computer where you have stored the template you wish to launch. Selet the file. When the selection-window closes, the name of the selected file will now appear next to the `Browse...` button. Click on the `Next` button to go to the next page.
1. On the `Specify Details` page, fill in the boxes with appropriate values:
    ![p3](https://user-images.githubusercontent.com/7087031/43525840-d7a7c69c-9570-11e8-87ee-b0b1db2ebc6f.png)
     When done, click on the `Next` button to go to the next page.
1. Scroll down to the `Advanced` section and expand it. Then scroll down to the "Rollback on failure" section and select the `No` radio box:
    ![p4](https://user-images.githubusercontent.com/7087031/43525841-d7b3fe62-9570-11e8-8788-0511a89d44bf.png)
    Click on the `Next` button to go to the next page.
1. On the verification page, ensure that appropriate/expected values are present in the `Details` section:
    ![p5](https://user-images.githubusercontent.com/7087031/43525842-d7c36cbc-9570-11e8-9e95-12a5d3fe6c5a.png)
    When ready to launch, click on the `Create` button to kick off the routines that launch a stack from the template.
1. Once the process kicks off, you'll be returned to the main CloudFormation web console page:
    ![p6](https://user-images.githubusercontent.com/7087031/43525843-d7d3387c-9570-11e8-807a-4c91cf2458b6.png)
    From here, watch the stack deploy and ensure that no errors occur.

Once the stack achieves a `CREATE_COMPLETE` state, you can look under the stack's `Resources` tabs for information that will help you track down your newly-created objects. If you have other stacks that depend on objects created by the new stack, you may find information to populate the next-to-deploy stack's fields under the just-deployed stack's `Outputs` tab.


