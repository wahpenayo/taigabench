# wahpenayo at gmail dot com
# 2017-11-30
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
readr.show_progress <- FALSE
#-----------------------------------------------------------------
dataset <- 'ontime'
problem <- 'l2'
response <- 'arrdelay'
dataf <- ontime.data
dtest <- dataf(test.file(dataset=dataset))
suffixes <- c('8192','65536','524288','4194304','33554432')
#-----------------------------------------------------------------
bench(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  suffixes=suffixes[1:min(3,length(suffixes))],
  trainf=l2.randomForestSRC,
  prefix='randomForestSRC')

bench(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  suffixes=suffixes,
  trainf=l2.h2o.randomForest,
  prefix='h2o')
  
bench(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  suffixes=suffixes,
  trainf=l2.xgboost.randomForest,
  prefix='xgboost')

bench(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  suffixes=suffixes,
  trainf=l2.xgboost.exact.randomForest,
  prefix='xgboost.exact')

bench(
  dataset=dataset,problem=problem,dataf=dataf,dtest=dtest,
  response=response, 
  # crashes in 64gb at 1m
  suffixes=suffixes[1:min(3,length(suffixes))],
  trainf=l2.randomForest,
  prefix='randomForest')
#-----------------------------------------------------------------
