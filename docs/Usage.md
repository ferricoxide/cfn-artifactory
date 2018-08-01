# Using this project's contents

Single-node Artifactory Pro and Artifactory Enterprise and multi-node Artifactory Enterprise may be automatically installed through the use of the AWS CloudFormation (CFn) template files. The CFn template files all end in `.tmplt.json`. These files are located in the Templates subdirectory of this project. These files may be used directly via the AWS GUI or CLI or under other frameworks like Jenkins, Terraform, etc. While this project (currently) does not include Terraform Configuration files (`.tf`), it does include a basic set of Jenkins pipeline definitions. These pipeline definitions files are located in the Testing/Jenkins subdirectory of this project. The Jenkins pipeline definitions all end in `.groovy`.

*Note:* Use of this project assumes that the user has created some kind of "tools" bucket to host data used by the templates. It is expected that the tools bucket will be laid out:

* `Licenses`: This directory will contain the license files used to enable functionality within Artifactory Pro or Enterprise Edition. Ensure that the proper license files are uploaded to this subirectory. The Enterprise Edition license files should be named `ArtifactoryEE_1.lic`, `ArtifactoryEE_2.lic`, etc.
* `SupportFiles`: This directory will contain the install/configuration scripts used to ready the OS for Artifactory's install as well as installing and configuring the Artifactory application. Use the `s3 sync` operation to copy this project's `SupportFiles` folder to the tools bucket.
* `Templates`: This directory will contain all of the Artifactory CFn templates. This is mostly relevant only when using the `parent` templates to launch linked stacks. Use the `s3 sync` operation to copy this project's `Templates` folder to the tools bucket.

## Deploying New Installations:

* [Via AWS Console](Usage_AWSgui.md "Launching templates from the web console")
* [Via AWS CLI](Usage_AWScli.md "Launching templates from the AWS command-line interface")
* [Via Jenkins](Usage_Jenkins.md "Launching templates via Jenkins pipelines")
