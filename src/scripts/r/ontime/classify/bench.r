# wahpenayo at gmail dot com
# since 2016-11-11
# 2017-11-19
#-----------------------------------------------------------------
setwd('e:/porta/projects/taigabench')
source('src/scripts/r//functions.r')
#-----------------------------------------------------------------
#results <- NULL
#for (suffix in c('0.01m','0.1m','1m','10m')) {
# gc()
# results <- rbind(
#   results,
#   classify.h2o.randomForest(
#     dataset='ontime',
#     suffix=suffix)); 
# print(results)
# write.csv(results,file=results.file(
#     dataset='ontime',
#     problem='classify',
#     prefix='h2o'),
#   row.names=FALSE)
#}
#results <- NULL
#for (suffix in c('0.01m', '0.1m','1m','10m')) {
# gc()
# results <- rbind(
#   results,
#   classify.xgboost.randomForest(
#     dataset='ontime',
#     suffix=suffix)); 
# print(results)
# write.csv(
#   results,
#   file=results.file(
#     dataset='ontime',
#     problem='classify',
#     prefix='xgboost'),
#   row.names=FALSE)
#}
results <- NULL
# crashes in 64gb at 1m
for (suffix in c('0.01m','0.1m')) { #,'1m','10m')) {
 gc()
 results <- rbind(
   results,
   classify.randomForest(
     dataset='ontime',
     suffix=suffix)); 
 print(results)
 write.csv(
   results,
   file=results.file(
     dataset='ontime',
     problem='classify',
     prefix='randomForest'),
   row.names=FALSE)
}
#-----------------------------------------------------------------
