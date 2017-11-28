# wahpenayo at gmail dot com
# since 2016-11-11
# 2017-11-27
#-----------------------------------------------------------------
if (file.exists('e:/porta/projects/taigabench')) {
  setwd('e:/porta/projects/taigabench')
} else {
  setwd('c:/porta/projects/taigabench')
}
source('src/scripts/r/functions.r')
#-----------------------------------------------------------------
dataset <- 'ontime'
problem <- 'classify'
response <- 'arr_delayed_15min'
testfile=test.file(dataset=dataset)
print(testfile)
dtest <- ontime.classify.data(testfile)
print(nrow(dtest))
print(nrow(dtest[,response]))
print(length(dtest[,response]))
#suffixes <- c('0.01m','0.1m','1m','10m')
suffixes <- c('8192','65536','524288','4194304','33554432')
#-----------------------------------------------------------------

results <- NULL
# crashes in 64gb at 1m
for (suffix in suffixes[1:min(3,length(suffixes))]) {
  gc()
  trainfile <-train.file(dataset=dataset,suffix=suffix)
  dtrain <- ontime.classify.data(trainfile)
  print(nrow(dtrain[,response]))
  print(length(dtrain[,response]))
  results <- rbind(
    results,
    classify.randomForest(
      dataset=dataset,
      dtrain=dtrain,
      suffix=suffix,
      dtest=dtest,
      response='arr_delayed_15min')); 
  print(results)
  write.csv(
    results,
    file=results.file(
      dataset=dataset,
      problem=problem,
      prefix='randomForest'),
    row.names=FALSE)
}

results <- NULL
for (suffix in suffixes) {
  gc()
  trainfile <-train.file(dataset=dataset,suffix=suffix)
  dtrain <- ontime.classify.data(trainfile)
  tmp <- classify.h2o.randomForest(
    dataset=dataset,
    dtrain=dtrain,
    suffix=suffix,
    dtest=dtest,
    response='arr_delayed_15min') 
  results <- rbind(results,tmp)
  print(results)
  write.csv(results,file=results.file(
      dataset=dataset,
      problem=problem,
      prefix='h2o'),
    row.names=FALSE)
}

results <- NULL
for (suffix in suffixes) {
 gc()
 trainfile <-train.file(dataset=dataset,suffix=suffix)
 dtrain <- ontime.classify.data(trainfile)
 results <- rbind(
   results,
   classify.xgboost.randomForest(
     dataset=dataset,
     dtrain=dtrain,
     suffix=suffix,
     dtest=dtest,
     response='arr_delayed_15min')); 
 print(results)
 write.csv(
   results,
   file=results.file(
     dataset=dataset,
     problem=problem,
     prefix='xgboost'),
   row.names=FALSE)
}
#-----------------------------------------------------------------
