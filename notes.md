See 
https://www.r-bloggers.com/benchmarking-random-forest-implementations/
https://github.com/szilard/benchm-ml
https://github.com/szilard/benchm-ml/blob/master/0-init/1-install.md

## Ubuntu 16.04:

#### R:

sudo echo "deb http://cran.rstudio.com/bin/linux/ubuntu trusty/" > /etc/apt/sources.list.d/r.list
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E084DAB9

https://www.r-bloggers.com/how-to-install-r-on-linux-ubuntu-16-04-xenial-xerus/

sudo echo "deb http://cran.rstudio.com/bin/linux/ubuntu xenial/" | sudo tee -a /etc/apt/sources.list

gpg --keyserver keyserver.ubuntu.com --recv-key E084DAB9
gpg -a --export E084DAB9 | sudo apt-key add -

sudo apt-get update
sudo apt-get install r-base r-base-dev

R --vanilla << EOF
install.packages(c("data.table","readr","randomForest","gbm","glmnet","ROCR","devtools"), repos="http://cran.rstudio.com")
q()

#### xgboost:

https://www.r-bloggers.com/installing-xgboost-on-ubuntu/

R --vanilla
install.packages('xgboost')
library(xgboost)

#### Python

already installed in Ubuntu 16.04, use python3 cmd line.

#### Voxpal Wabbit
 skipped
 
#### Java
https://www.digitalocean.com/community/tutorials/how-to-install-java-with-apt-get-on-ubuntu-16-04

Default version:

sudo apt-get update
sudo apt-get install default-jdk

Guarantee Oracle java 8:

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
Didn't run, probably not needed:
sudo apt-get install oracle-java8-set-default

#### Maven 
sudo apt-get update
sudo apt-get install maven

#### H2O

http://h2o-release.s3.amazonaws.com/h2o/rel-turing/8/index.html

Maven:
  <dependency>
   <groupId>ai.h2o</groupId>
   <artifactId>h2o-core</artifactId>
   <version>${h2oProjectVersion}</version>
  </dependency>
  <!--
  <dependency>
   <groupId>ai.h2o</groupId>
   <artifactId>h2o-algos</artifactId>
   <version>${h2oProjectVersion}</version>
  </dependency>
  <dependency>
   <groupId>ai.h2o</groupId>
   <artifactId>h2o-web</artifactId>
   <version>${h2oProjectVersion}</version>
  </dependency>
  <dependency>
   <groupId>ai.h2o</groupId>
   <artifactId>h2o-app</artifactId>
   <version>${h2oProjectVersion}</version>
  </dependency>
  -->
 R:
 
 R --vanilla
install.packages('h2o') 

#### Spark
skipped