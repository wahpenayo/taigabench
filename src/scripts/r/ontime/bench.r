# wahpenayo at gmail dot com
# since 2016-11-11
# 2017-11-17
#-------------------------------------------------------------------------------
setwd('e:/porta/projects/taigabench')
source('src/scripts/r/ontime/functions.r')
#-------------------------------------------------------------------------------
results <- NULL
for (suffix in c('0.01m','0.1m','1m','10m')) {
 gc()
 results <- rbind(results,h2o_randomForest(suffix=suffix)); 
 print(results)
 write.csv(results,file=results.file('h2o'),row.names=FALSE)
}
results <- NULL
for (suffix in c('0.01m','0.1m','1m','10m')) {
 gc()
 results <- rbind(results,xgboost_randomForest(suffix=suffix)); 
 print(results)
 write.csv(results,file=results.file('xgboost'),row.names=FALSE)
}
results <- NULL
for (suffix in c('0.01m','0.1m','1m','10m')) {
 gc()
 results <- rbind(results,single_randomForest(suffix=suffix)); 
 print(results)
 write.csv(results,file=results.file('r'),row.names=FALSE)
}
#-------------------------------------------------------------------------------
