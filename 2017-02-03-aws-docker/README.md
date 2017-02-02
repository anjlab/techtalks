## Prerequisites

>Note: We assume that all stacks and other resources, like Customer Master Keys, live in the same AWS Region.

>AMI `ImageID`s used in parameters of sample apps taken from Frankfurt region (`eu-central-1`). Make sure to update them if you like to run this tutorial in another region.


1. Create IAM user for AWS CLI (`techtalks20170203`)
    
    Attach policies to the user:
    
    - IAMFullAccess
      > because we create IAM roles
    - AmazonEC2ContainerRegistryFullAccess 
      > allow push to registry during deploy
    - AWSCloudFormationFullAccess
      > custom policy to allow `cloudformation:*`
    - SystemAdministrator
      > create/delete different EC2 resources for our stacks

2. Install and configure AWS CLI (`~/.aws/credentials` and `~/.aws/config`)
   > Take user's API key and secret for AWS CLI

3. This tutorial uses some scripts from CodeDeploy samples repository, you'll need to manually clone them from GitHub:
    ```
    $ git clone --depth 1 https://github.com/awslabs/aws-codedeploy-samples devops/aws-codedeploy-samples
    ```

## Building infrastructure

### Creating CloudFormation stacks

1. Create key pair for SSH access (`techtalks20170203-key-pair-eu-central-1`)

2. Create Self-Signed Certificate for ELB
    ```
    $ openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 3650
      Generating a 2048 bit RSA private key
      ..........................................+++
      ........................................................+++
      writing new private key to 'key.pem'
      Enter PEM pass phrase:
      Verifying - Enter PEM pass phrase:
      -----
      You are about to be asked to enter information that will be incorporated
      into your certificate request.
      What you are about to enter is what is called a Distinguished Name or a DN.
      There are quite a few fields but you can leave some blank
      For some fields there will be a default value,
      If you enter '.', the field will be left blank.
      -----
      Country Name (2 letter code) [AU]:RU
      State or Province Name (full name) [Some-State]:Vladimir Region
      Locality Name (eg, city) []:Vladimir
      Organization Name (eg, company) [Internet Widgits Pty Ltd]:AnjLab
      Organizational Unit Name (eg, section) []:TechTalks 2017-02-03
      Common Name (e.g. server FQDN or YOUR name) []:anjlab.com
      Email Address []:dmitry@anjlab.com
      
    $ openssl rsa -in key.pem -out key-decrypted.pem
    ```

3. Import [self-signed] SSL Certificate for ELB

   http://docs.aws.amazon.com/acm/latest/userguide/import-certificate.html#import-certificate-console
   
   Use content of `cert.pem` as "Certificate body",
   `key-decrypted.pem` as "Certificate private key".

4. Copy certificate's ARN into `devops/cloudformation-stacks/my-app/web-cluster-parameters.json`

5. Now everything is ready to create our first stack:
    ```
    $ devops/bin/stack create my-app
    {
        "StackId": "arn:aws:cloudformation:eu-central-1:626307895833:stack/my-app/7bbe2a50-e929-11e6-b3ec-503f2ad2e5fe"
    }
    ```

6. Our app will use some external services, i.e. Redis for centralized session management.

   In this tutorial we'll host Redis on its own EC2 instance using same stack template as `my-app` but with different parameters, i.e. no Elastic Load Balancer (ELB).
    ```
    $ devops/bin/stack create my-services
    {
        "StackId": "arn:aws:cloudformation:eu-central-1:626307895833:stack/my-services/a9cdb650-e92d-11e6-8787-50a68ad4f262"
    }
    ```

7. Our deployments will use private Docker registry provided by Elastic Container Registry (ECR) to host our app's images, let's create it:
    ```
    $ devops/bin/stack create my-app-ecr
    {
        "StackId": "arn:aws:cloudformation:eu-central-1:626307895833:stack/my-app-ecr/d96f45b0-e8bd-11e6-8e2d-500c52a6cefe"
    }
    ```

8. Create Customer Master Key for secured properties
    ```
    $ aws kms create-key
    {
        "KeyMetadata": {
            "Origin": "AWS_KMS",
            "KeyId": "a6429aaa-2f55-4d7d-8d7a-34ff1b8d0530",
            "Description": "",
            "Enabled": true,
            "KeyUsage": "ENCRYPT_DECRYPT",
            "KeyState": "Enabled",
            "CreationDate": 1485937790.354,
            "Arn": "arn:aws:kms:eu-central-1:626307895833:key/a6429aaa-2f55-4d7d-8d7a-34ff1b8d0530",
            "AWSAccountId": "626307895833"
        }
    }
    
    $ aws kms create-alias \
        --alias-name alias/config \
        --target-key-id a6429aaa-2f55-4d7d-8d7a-34ff1b8d0530
    ```

9. Create S3 bucket for CodeDeploy:
    ```
    $ devops/bin/deploy create-codedeploy-bucket
    ```

## Deployment

### Configure properties

1. This tutorial assumes that orchestration between the stacks is done by hands.

    This means, for example, that it's our responsibility to create the `my-services` stack before we deploy anything to `my-app`.
    
    Also, we need to tell `my-app` where it can find a Redis. To do that we need to grab private IP address of the Redis host and put it to `devops/apps/my-app-v1/compose.env`:
    
    ```
    $ devops/bin/stack ec2-instances my-services PrivateDnsName
    ip-172-31-28-28.eu-central-1.compute.internal
    ```

    We also need ELB's public hostname in order to configure Tomcat's proxy settings in `devops`.
    
2. Encrypt properties manually before committing them to Git, i.e.
    ```
    $ devops/bin/encrypt alias/config "secret that you don't want in git in plaintext"
    
    AQECAHjgmLsQwxwUln3clbLeOOyA2DDROEooKdSU89Jk5l3MewAAAI0wgYoGCSqGSIb3DQEHBqB9MHsCAQAwdgYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAwzH0XfoDdcheEj8WwCARCAScwrkJhkq6lewqcS/VyCgTWRo5KVB4VzeFGiZ4FenPDBGd0HxRYBMKStP0KKcfKjPWq1hfG5KXYypxrosNe6It3wumOzIQBgJp0=
    ```

    > Note: if done right you won't have permissions to decrypt this code (AWS CLI principal does not have `kms:Decrypt` permission). But it's available to InstanceProfile.
    
    ```
    $ devops/bin/decrypt AQECAHjgmLsQwxwUln3clbLeOOyA2DDROEooKdSU89Jk5l3MewAAAI0wgYoGCSqGSIb3DQEHBqB9MHsCAQAwdgYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAwzH0XfoDdcheEj8WwCARCAScwrkJhkq6lewqcS/VyCgTWRo5KVB4VzeFGiZ4FenPDBGd0HxRYBMKStP0KKcfKjPWq1hfG5KXYypxrosNe6It3wumOzIQBgJp0=
    
    An error occurred (AccessDeniedException) when calling the Decrypt operation: The ciphertext refers to a customer master key that does not exist, does not exist in this region, or you are not allowed to access.
    ```

### Deploy!

1. Build and deploy a revision `my-services` as defined in `devops/build.gradle` to the stack `my-services` that we previously created:

    ```
    $ devops/bin/deploy push my-services my-services
    ```

2. Build and deploy a revision `my-app-v1` to the stack `my-app`:
    ```
    $ devops/bin/deploy push my-app my-app-v1
    ```