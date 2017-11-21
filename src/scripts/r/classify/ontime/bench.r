# wahpenayo at gmail dot com
# since 2016-11-11
# 2017-11-20
#-----------------------------------------------------------------
setwd('e:/porta/projects/taigabench')
source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
dataset <- 'ontime'
problem <- 'classify'
testfile=test.file(dataset=dataset,problem=problem)
print(testfile)

results <- NULL
for (suffix in c('0.01m','0.1m','1m','10m')) {
 gc()
 trainfile <-train.file(
   dataset=dataset,
   problem=problem,
   suffix=suffix)
 tmp <- classify.h2o.randomForest(
   dataset=dataset,
   trainfile=trainfile,
   suffix=suffix,
   testfile=testfile,
   response='dep_delayed_15min') 
 results <- rbind(results,tmp)
 print(results)
 write.csv(results,file=results.file(
     dataset=dataset,
     problem=problem,
     prefix='h2o'),
   row.names=FALSE)
}

results <- NULL
for (suffix in c('0.01m', '0.1m','1m','10m')) {
 gc()
 trainfile <-train.file(
   dataset=dataset,
   problem=problem,
   suffix=suffix)
 results <- rbind(
   results,
   classify.xgboost.randomForest(
     dataset=dataset,
     trainfile=trainfile,
     suffix=suffix,
     testfile=testfile,
     response='dep_delayed_15min')); 
 print(results)
 write.csv(
   results,
   file=results.file(
     dataset=dataset,
     problem=problem,
     prefix='xgboost'),
   row.names=FALSE)
}

results <- NULL
# crashes in 64gb at 1m
for (suffix in c('0.01m','0.1m')) { #,'1m','10m')) {
 gc()
 trainfile <-train.file(
   dataset=dataset,
   problem=problem,
   suffix=suffix)
 results <- rbind(
   results,
   classify.randomForest(
     dataset=dataset,
     trainfile=trainfile,
     suffix=suffix,
     testfile=testfile,
     response='dep_delayed_15min')); 
 print(results)
 write.csv(
   results,
   file=results.file(
     dataset=dataset,
     problem=problem,
     prefix='randomForest'),
   row.names=FALSE)
}
#-----------------------------------------------------------------
